package me.papa.video.services;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;

import me.papa.utils.MyLog;
import android.text.TextUtils;
import android.util.Log;

public class HttpServer {

    private String TAG = "HttpServer";

    public Response serve(String uri, String method, Properties header, Properties parms,
            Properties files) {
        MyLog.e(TAG, "serve=" + method + " '" + uri + "' ");
        Enumeration e = header.propertyNames();
        while (e.hasMoreElements()) {
            String value = (String) e.nextElement();
        }
        e = parms.propertyNames();
        while (e.hasMoreElements()) {
            String value = (String) e.nextElement();
        }
        e = files.propertyNames();
        while (e.hasMoreElements()) {
            String value = (String) e.nextElement();
        }

        // Map uri to acture file in ContentTree

        String itemId = uri.replaceFirst("/", "");
        itemId = URLDecoder.decode(itemId);
        MyLog.e(TAG, "url=" + itemId);
        if (itemId.equalsIgnoreCase("index_test")) {

            return getIndex();
        }

        if (filterForbidden(uri)) {
            return new Response(HTTP_FORBIDDEN, MIME_PLAINTEXT,
                    "FORBIDDEN:You have no right to access");
        } else {
            return serveFile(uri, header, myRootDir, false);
        }

    }

    private Response getIndex() {
        String content = "<!DOCTYPE html PUBLIC \"-//WAPFORUM//DTD XHTML Mobile 1.0//EN\" \"http://www.wapforum.org/DTD/xhtml-mobile10.dtd \"><html xmlns=\"http://www.w3.org/1999/xhtml\"><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" /></head><body>"
                + "内容" + getImages() + "</body></html>";
        Response r = new Response(HTTP_OK, MIME_HTML, content);
        return r;
    }

    private String getImages() {
        // ContentNode rootNode = ContentTree.getRootNode( );

        StringBuilder sb = new StringBuilder();

        return sb.toString();
    }

    /**
     * 过滤 增加权限
     * 
     * @param url
     * @return
     */
    private boolean filterForbidden(String url) {

        return false;
    }

    /**
     * HTTP response. Return one of these from serve().
     */
    public class Response {

        /**
         * Default constructor: response = HTTP_OK, data = mime = 'null'
         */
        public Response() {
            this.status = HTTP_OK;
        }

        /**
         * Basic constructor.
         */
        public Response(String status, String mimeType, InputStream data) {
            this.status = status;
            this.mimeType = mimeType;
            this.data = data;
        }

        /**
         * Convenience method that makes an InputStream out of given text.
         */
        public Response(String status, String mimeType, String txt) {
            this.status = status;
            this.mimeType = mimeType;
            try {
                this.data = new ByteArrayInputStream(txt.getBytes("UTF-8"));
            } catch (java.io.UnsupportedEncodingException uee) {
                uee.printStackTrace();
            }
        }

        /**
         * Adds given line to the header.
         */
        public void addHeader(String name, String value) {
            header.put(name, value);
        }

        /**
         * HTTP status code after processing, e.g. "200 OK", HTTP_OK
         */
        public String status;

        /**
         * MIME type of content, e.g. "text/html"
         */
        public String mimeType;

        /**
         * Data of the response, may be null.
         */
        public InputStream data;

        /**
         * Headers for the HTTP response. Use addHeader() to add lines.
         */
        public Properties header = new Properties();
    }

    /**
     * Some HTTP response status codes
     */
    public static final String HTTP_OK = "200 OK", HTTP_PARTIALCONTENT = "206 Partial Content",
            HTTP_RANGE_NOT_SATISFIABLE = "416 Requested Range Not Satisfiable",
            HTTP_REDIRECT = "301 Moved Permanently", HTTP_FORBIDDEN = "403 Forbidden",
            HTTP_NOTFOUND = "404 Not Found", HTTP_BADREQUEST = "400 Bad Request",
            HTTP_INTERNALERROR = "500 Internal Server Error",
            HTTP_NOTIMPLEMENTED = "501 Not Implemented";

    /**
     * Common mime types for dynamic content
     */
    public static final String MIME_PLAINTEXT = "text/plain", MIME_HTML = "text/html",
            MIME_DEFAULT_BINARY = "application/octet-stream", MIME_XML = "text/xml";

    // ==================================================
    // Socket & server code
    // ==================================================

    /**
     * Starts a HTTP server to given port.
     * <p>
     * Throws an IOException if the socket is already in use
     */
    public HttpServer(int port) throws IOException {
        myTcpPort = port;
        this.myRootDir = new File("/");
        myServerSocket = new ServerSocket(myTcpPort);
        myThread = new Thread(new Runnable() {

            public void run() {
                try {
                    while (true)
                        new HTTPSession(myServerSocket.accept());
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        });
        myThread.setDaemon(true);
        myThread.start();
        Log.i(TAG, "HttpServer   staring===============");

    }

    BufferedInputStream serviceInputStream = null;

    BufferedOutputStream serviceBOutputStream = null;

    public BufferedInputStream getServiceInputStream() {
        return serviceInputStream;

    }

    public BufferedOutputStream getOutputStream() {
        return serviceBOutputStream;
    }

    private boolean isConnected = false;

    public boolean isConnected() {
        return mConntectServiceSocket != null && mConntectServiceSocket.isConnected();
    }

    private Socket mConntectServiceSocket;

    public void startConnectToService() {
        if (mConntectServiceSocket != null && mConntectServiceSocket.isConnected()) {
            return;
        }
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub

                try {
                    mConntectServiceSocket = new Socket("10.10.5.20", 9090);
                    InputStream is = mConntectServiceSocket.getInputStream();
                    OutputStream os = mConntectServiceSocket.getOutputStream();

                    serviceInputStream = new BufferedInputStream(is);
                    serviceBOutputStream = new BufferedOutputStream(os);

                    serviceBOutputStream.write("8000000000".getBytes());
                    serviceBOutputStream.flush();
                    if (!mConntectServiceSocket.isConnected()) {
                        Log.e(TAG, "service close");
                        isConnected = false;
                    } else {
                        isConnected = true;
                        Log.e(TAG, "service connect");
                    }

                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Stops the server.
     */
    public void stop() {
        try {
            myServerSocket.close();
            myThread.join();
        } catch (IOException ioe) {} catch (InterruptedException e) {}
    }

    /**
     * Handles one session, i.e. parses the HTTP request and returns the
     * response.
     */
    private class HTTPSession implements Runnable {

        private Socket mySocket;

        public HTTPSession(Socket s) {
            mySocket = s;
            Thread t = new Thread(this);
            t.setDaemon(true);
            t.start();
            MyLog.i(TAG, "HTTPSession   " + s);
        }

        public byte[] int2byte(int res) {
            byte[] targets = new byte[4];

            targets[0] = (byte) (res & 0xff);// 最低位 
            targets[1] = (byte) ((res >> 8) & 0xff);// 次低位 
            targets[2] = (byte) ((res >> 16) & 0xff);// 次高位 
            targets[3] = (byte) (res >>> 24);// 最高位,无符号右移。 
            return targets;
        }

        public void run() {
            try {

                InputStream is = mySocket.getInputStream();
                if (is == null) {
                    return;
                }
                Log.i(TAG, "HTTPSessionHTTPSessionHTTPSession  running");
                // Read the first 8192 bytes.
                // The full header should fit in here.
                // Apache's default header limit is 8KB.
                int bufsize = 8192;
                byte[] buf = new byte[bufsize];
                int rlen = is.read(buf, 0, bufsize);
                Log.e(TAG, "rlen=" + rlen);
                if (rlen <= 0) {
                    return;
                }

                // Create a BufferedReader for parsing the header.
                ByteArrayInputStream hbis = new ByteArrayInputStream(buf, 0, rlen);
                BufferedReader hin = new BufferedReader(new InputStreamReader(hbis));
                Properties pre = new Properties();
                Properties parms = new Properties();
                Properties header = new Properties();
                Properties files = new Properties();
                // Decode the header into parms and header java properties
                decodeHeader(hin, pre, parms, header);
                String method = pre.getProperty("method");
                String uri = pre.getProperty("uri");
                Log.e(TAG, "uri==========================" + uri);
                bufsize = 4096;
                //接受相机传过来的数据   并传给服务器
                if (TextUtils.isEmpty(uri)) {
                    int length = -1;
                    while ((length = is.read(buf)) != -1) {
                        if (isConnected()) {
                            try {
                                byte bufferLg[] = int2byte(length);
                                byte[] newbyte = new byte[length + 6];
                                newbyte[0] = 0;
                                newbyte[1] = 0;
                                newbyte[2] = bufferLg[0];
                                newbyte[3] = bufferLg[1];
                                newbyte[4] = bufferLg[2];
                                newbyte[5] = bufferLg[3];
                                System.arraycopy(buf, 0, newbyte, 6, length);
                                serviceBOutputStream.write(newbyte, 0, newbyte.length);
                                serviceBOutputStream.flush();
                            } catch (Exception ex) {
                                serviceBOutputStream.close();
                                ex.printStackTrace();
                            }
                        }
                    }
                } else {
                    //接受服务器的数据
                    int lg = -1;
                    Log.i(TAG, "lg=" + lg + "1111111111111isConnected=" + isConnected());
                    OutputStream out = mySocket.getOutputStream();
                    if (isConnected() == false) {
                        return;
                    }
                    if (serviceInputStream != null) {
                        while ((lg = serviceInputStream.read(buf)) != -1) {
                            if (lg > 2) {
                                out.write(buf, 6, lg - 6);
                            } else {
                                //小于2服务器 控制流
                            }
                            Log.i(TAG, "lg==================" + lg);
                        }
                    }
                }

                long size = 0x7FFFFFFFFFFFFFFFl;
                String contentLength = header.getProperty("content-length");
                if (contentLength != null) {
                    try {
                        size = Integer.parseInt(contentLength);
                    } catch (NumberFormatException ex) {}
                }

                // We are looking for the byte separating header from body.
                // It must be the last byte of the first two sequential new lines.
                int splitbyte = 0;
                boolean sbfound = false;
                while (splitbyte < rlen) {
                    if (buf[splitbyte] == '\r' && buf[++splitbyte] == '\n'
                            && buf[++splitbyte] == '\r' && buf[++splitbyte] == '\n') {
                        sbfound = true;
                        break;
                    }
                    splitbyte++;
                }
                splitbyte++;

                // Write the part of body already read to ByteArrayOutputStream f
                ByteArrayOutputStream f = new ByteArrayOutputStream();
                if (splitbyte < rlen) f.write(buf, splitbyte, rlen - splitbyte);

                // While Firefox sends on the first read all the data fitting
                // our buffer, Chrome and Opera sends only the headers even if
                // there is data for the body. So we do some magic here to find
                // out whether we have already consumed part of body, if we
                // have reached the end of the data to be sent or we should
                // expect the first byte of the body at the next read.
                if (splitbyte < rlen) size -= rlen - splitbyte + 1;
                else if (!sbfound || size == 0x7FFFFFFFFFFFFFFFl) size = 0;

                // Now read all the body and write it to f
                buf = new byte[512];
                while (rlen >= 0 && size > 0) {
                    rlen = is.read(buf, 0, 512);
                    size -= rlen;
                    if (rlen > 0) f.write(buf, 0, rlen);
                }

                // Get the raw body as a byte []
                byte[] fbuf = f.toByteArray();

                // Create a BufferedReader for easily reading it as string.
                ByteArrayInputStream bin = new ByteArrayInputStream(fbuf);
                BufferedReader in = new BufferedReader(new InputStreamReader(bin));

                // If the method is POST, there may be parameters
                // in data section, too, read it:
                if (method.equalsIgnoreCase("POST")) {
                    String contentType = "";
                    String contentTypeHeader = header.getProperty("content-type");
                    StringTokenizer st = new StringTokenizer(contentTypeHeader, "; ");
                    if (st.hasMoreTokens()) {
                        contentType = st.nextToken();
                    }
                    if (contentType.equalsIgnoreCase("multipart/form-data")) {
                        // Handle multipart/form-data
                        if (!st.hasMoreTokens()) sendError(
                                HTTP_BADREQUEST,
                                "BAD REQUEST: Content type is multipart/form-data but boundary missing. Usage: GET /example/file.html");
                        String boundaryExp = st.nextToken();
                        st = new StringTokenizer(boundaryExp, "=");
                        if (st.countTokens() != 2) sendError(
                                HTTP_BADREQUEST,
                                "BAD REQUEST: Content type is multipart/form-data but boundary syntax error. Usage: GET /example/file.html");
                        st.nextToken();
                        String boundary = st.nextToken();

                        decodeMultipartData(boundary, fbuf, in, parms, files);
                    } else {
                        // Handle application/x-www-form-urlencoded
                        String postLine = "";
                        char pbuf[] = new char[512];
                        int read = in.read(pbuf);
                        while (read >= 0 && !postLine.endsWith("\r\n")) {
                            postLine += String.valueOf(pbuf, 0, read);
                            read = in.read(pbuf);
                        }
                        postLine = postLine.trim();
                        decodeParms(postLine, parms);
                    }
                }

                // Ok, now do the serve()
                Response r = null;
                Log.e(TAG, "uri=======" + uri);
                r = serve(uri, method, header, parms, files);
                if (r == null) {
                    sendError(HTTP_INTERNALERROR,
                            "SERVER INTERNAL ERROR: Serve() returned a null response.");
                } else {
                    sendResponse(r.status, r.mimeType, r.header, r.data);
                }
                // }
                in.close();
                is.close();
            } catch (IOException ioe) {
                try {
                    sendError(HTTP_INTERNALERROR,
                            "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
                } catch (Throwable t) {}
            } catch (InterruptedException ie) {

            }
        }

        /**
         * Decodes the sent headers and loads the data into java
         * Properties' key - value pairs
         **/
        private void decodeHeader(BufferedReader in, Properties pre, Properties parms,
                Properties header) throws InterruptedException {
            try {
                // Read the request line
                String inLine = in.readLine();
                Log.e(TAG, "inLine=" + inLine);
                if (inLine == null) {
                    return;
                }
                StringTokenizer st = new StringTokenizer(inLine);
                if (!st.hasMoreTokens()) {
                    Log.e(TAG, "hhhhh");
                    return;
                    //                    sendError(HTTP_BADREQUEST,
                    //                            "BAD REQUEST: Syntax error. Usage: GET /example/file.html");
                }

                String method = st.nextToken();
                pre.put("method", method);
                if (!st.hasMoreTokens()) {
                    Log.e(TAG, "hhhhh23333333");
                    return;
                    //                    sendError(HTTP_BADREQUEST,
                    //                            "BAD REQUEST: Missing URI. Usage: GET /example/file.html");
                }

                String uri = st.nextToken();

                // Decode parameters from the URI
                int qmi = uri.indexOf('?');
                if (qmi >= 0) {
                    decodeParms(uri.substring(qmi + 1), parms);
                    uri = decodePercent(uri.substring(0, qmi));
                } else {
                    uri = decodePercent(uri);
                }
                Log.e(TAG, "uri=" + uri);
                // If there's another token, it's protocol version,
                // followed by HTTP headers. Ignore version but parse headers.
                // NOTE: this now forces header names lowercase since they are
                // case insensitive and vary by client.
                if (st.hasMoreTokens()) {
                    String line = in.readLine();
                    while (line != null && line.trim().length() > 0) {
                        int p = line.indexOf(':');
                        Log.e(TAG, "line=" + line);
                        if (p >= 0) {
                            header.put(line.substring(0, p).trim().toLowerCase(),
                                    line.substring(p + 1).trim());
                        }
                        line = in.readLine();

                    }
                }

                pre.put("uri", uri);
            } catch (IOException ioe) {
                ioe.printStackTrace();
                //                sendError(HTTP_INTERNALERROR,
                //                        "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
            }
        }

        /**
         * Decodes the Multipart Body data and put it into java Properties'
         * key - value pairs.
         **/
        private void decodeMultipartData(String boundary, byte[] fbuf, BufferedReader in,
                Properties parms, Properties files) throws InterruptedException {
            try {
                int[] bpositions = getBoundaryPositions(fbuf, boundary.getBytes());
                int boundarycount = 1;
                String mpline = in.readLine();
                while (mpline != null) {
                    if (mpline.indexOf(boundary) == -1) sendError(
                            HTTP_BADREQUEST,
                            "BAD REQUEST: Content type is multipart/form-data but next chunk does not start with boundary. Usage: GET /example/file.html");
                    boundarycount++;
                    Properties item = new Properties();
                    mpline = in.readLine();
                    while (mpline != null && mpline.trim().length() > 0) {
                        int p = mpline.indexOf(':');
                        if (p != -1) item.put(mpline.substring(0, p).trim().toLowerCase(), mpline
                                .substring(p + 1).trim());
                        mpline = in.readLine();
                    }
                    if (mpline != null) {
                        String contentDisposition = item.getProperty("content-disposition");
                        if (contentDisposition == null) {
                            sendError(
                                    HTTP_BADREQUEST,
                                    "BAD REQUEST: Content type is multipart/form-data but no content-disposition info found. Usage: GET /example/file.html");
                        }
                        StringTokenizer st = new StringTokenizer(contentDisposition, "; ");
                        Properties disposition = new Properties();
                        while (st.hasMoreTokens()) {
                            String token = st.nextToken();
                            int p = token.indexOf('=');
                            if (p != -1) disposition.put(
                                    token.substring(0, p).trim().toLowerCase(),
                                    token.substring(p + 1).trim());
                        }
                        String pname = disposition.getProperty("name");
                        pname = pname.substring(1, pname.length() - 1);

                        String value = "";
                        if (item.getProperty("content-type") == null) {
                            while (mpline != null && mpline.indexOf(boundary) == -1) {
                                mpline = in.readLine();
                                if (mpline != null) {
                                    int d = mpline.indexOf(boundary);
                                    if (d == -1) value += mpline;
                                    else value += mpline.substring(0, d - 2);
                                }
                            }
                        } else {
                            if (boundarycount > bpositions.length) sendError(HTTP_INTERNALERROR,
                                    "Error processing request");
                            int offset = stripMultipartHeaders(fbuf, bpositions[boundarycount - 2]);
                            String path = saveTmpFile(fbuf, offset, bpositions[boundarycount - 1]
                                    - offset - 4);
                            files.put(pname, path);
                            value = disposition.getProperty("filename");
                            value = value.substring(1, value.length() - 1);
                            do {
                                mpline = in.readLine();
                            } while (mpline != null && mpline.indexOf(boundary) == -1);
                        }
                        parms.put(pname, value);
                    }
                }
            } catch (IOException ioe) {
                sendError(HTTP_INTERNALERROR,
                        "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
            }
        }

        /**
         * Find the byte positions where multipart boundaries start.
         **/
        public int[] getBoundaryPositions(byte[] b, byte[] boundary) {
            int matchcount = 0;
            int matchbyte = -1;
            Vector matchbytes = new Vector();
            for (int i = 0; i < b.length; i++) {
                if (b[i] == boundary[matchcount]) {
                    if (matchcount == 0) matchbyte = i;
                    matchcount++;
                    if (matchcount == boundary.length) {
                        matchbytes.addElement(new Integer(matchbyte));
                        matchcount = 0;
                        matchbyte = -1;
                    }
                } else {
                    i -= matchcount;
                    matchcount = 0;
                    matchbyte = -1;
                }
            }
            int[] ret = new int[matchbytes.size()];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = ((Integer) matchbytes.elementAt(i)).intValue();
            }
            return ret;
        }

        /**
         * Retrieves the content of a sent file and saves it to a temporary
         * file. The full path to the saved file is returned.
         **/
        private String saveTmpFile(byte[] b, int offset, int len) {
            String path = "";
            if (len > 0) {
                String tmpdir = System.getProperty("java.io.tmpdir");
                try {
                    File temp = File.createTempFile("NanoHTTPD", "", new File(tmpdir));
                    OutputStream fstream = new FileOutputStream(temp);
                    fstream.write(b, offset, len);
                    fstream.close();
                    path = temp.getAbsolutePath();
                } catch (Exception e) { // Catch exception if any
                    System.err.println("Error: " + e.getMessage());
                }
            }
            return path;
        }

        /**
         * It returns the offset separating multipart file headers from the
         * file's data.
         **/
        private int stripMultipartHeaders(byte[] b, int offset) {
            int i = 0;
            for (i = offset; i < b.length; i++) {
                if (b[i] == '\r' && b[++i] == '\n' && b[++i] == '\r' && b[++i] == '\n') break;
            }
            return i + 1;
        }

        /**
         * Decodes the percent encoding scheme. <br/>
         * For example: "an+example%20string" -> "an example string"
         */
        private String decodePercent(String str) throws InterruptedException {
            try {
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < str.length(); i++) {
                    char c = str.charAt(i);
                    switch (c) {
                        case '+':
                            sb.append(' ');
                            break;
                        case '%':
                            sb.append((char) Integer.parseInt(str.substring(i + 1, i + 3), 16));
                            i += 2;
                            break;
                        default:
                            sb.append(c);
                            break;
                    }
                }
                return sb.toString();
            } catch (Exception e) {
                sendError(HTTP_BADREQUEST, "BAD REQUEST: Bad percent-encoding.");
                return null;
            }
        }

        /**
         * Decodes parameters in percent-encoded URI-format ( e.g.
         * "name=Jack%20Daniels&pass=Single%20Malt" ) and adds them to
         * given Properties. NOTE: this doesn't support multiple
         * identical keys due to the simplicity of Properties -- if you
         * need multiples, you might want to replace the Properties with a
         * Hashtable of Vectors or such.
         */
        private void decodeParms(String parms, Properties p) throws InterruptedException {
            if (parms == null) return;

            StringTokenizer st = new StringTokenizer(parms, "&");
            while (st.hasMoreTokens()) {
                String e = st.nextToken();
                int sep = e.indexOf('=');
                if (sep >= 0) p.put(decodePercent(e.substring(0, sep)).trim(),
                        decodePercent(e.substring(sep + 1)));
            }
        }

        /**
         * Returns an error message as a HTTP response and throws
         * InterruptedException to stop further request processing.
         */
        private void sendError(String status, String msg) throws InterruptedException {
            sendResponse(status, MIME_PLAINTEXT, null, new ByteArrayInputStream(msg.getBytes()));
            throw new InterruptedException();
        }

        /**
         * Sends given response to the socket.
         */
        private void sendResponse(String status, String mime, Properties header, InputStream data) {
            try {
                if (status == null) throw new Error("sendResponse(): Status can't be null.");

                OutputStream out = mySocket.getOutputStream();
                PrintWriter pw = new PrintWriter(out);
                pw.print("HTTP/1.0 " + status + " \r\n");

                if (mime != null) pw.print("Content-Type: " + mime + "\r\n");

                if (header == null || header.getProperty("Date") == null) pw.print("Date: "
                        + gmtFrmt.format(new Date()) + "\r\n");

                if (header != null) {
                    Enumeration e = header.keys();
                    while (e.hasMoreElements()) {
                        String key = (String) e.nextElement();
                        String value = header.getProperty(key);
                        pw.print(key + ": " + value + "\r\n");
                    }
                }

                pw.print("\r\n");
                pw.flush();

                if (data != null) {
                    int pending = data.available(); // This is to support partial sends, see serveFile()
                    byte[] buff = new byte[2048];
                    while (pending > 0) {
                        int read = data.read(buff, 0, ((pending > 2048) ? 2048 : pending));
                        if (read <= 0) break;
                        out.write(buff, 0, read);
                        pending -= read;
                    }
                }
                out.flush();
                out.close();
                if (data != null) data.close();
            } catch (IOException ioe) {
                // Couldn't write? No can do.
                try {
                    mySocket.close();
                } catch (Throwable t) {}
            }
        }

    }

    /**
     * URL-encodes everything between "/"-characters. Encodes spaces as
     * '%20' instead of '+'.
     */
    private String encodeUri(String uri) {
        String newUri = "";
        StringTokenizer st = new StringTokenizer(uri, "/ ", true);
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            if (tok.equals("/")) newUri += "/";
            else if (tok.equals(" ")) newUri += "%20";
            else {
                newUri += URLEncoder.encode(tok);
                // For Java 1.4 you'll want to use this instead:
                // try { newUri += URLEncoder.encode( tok, "UTF-8" ); } catch ( java.io.UnsupportedEncodingException uee ) {}
            }
        }
        return newUri;
    }

    private int myTcpPort;

    private final ServerSocket myServerSocket;

    private Thread myThread;

    private File myRootDir;

    // ==================================================
    // File server code
    // ==================================================

    /**
     * Serves file from homeDir and its' subdirectories (only). Uses only
     * URI, ignores all headers and HTTP parameters.
     */
    public Response serveFile(String uri, Properties header, File homeDir,
            boolean allowDirectoryListing) {
        Response res = null;

        // Make sure we won't die of an exception later
        if (!homeDir.isDirectory()) res = new Response(HTTP_INTERNALERROR, MIME_PLAINTEXT,
                "INTERNAL ERRROR: serveFile(): given homeDir is not a directory.");

        if (res == null) {
            // Remove URL arguments
            uri = uri.trim().replace(File.separatorChar, '/');
            if (uri.indexOf('?') >= 0) uri = uri.substring(0, uri.indexOf('?'));

            // Prohibit getting out of current directory
            if (uri.startsWith("..") || uri.endsWith("..") || uri.indexOf("../") >= 0) {
                res = new Response(HTTP_FORBIDDEN, MIME_PLAINTEXT,
                        "FORBIDDEN: Won't serve ../ for security reasons.");
                MyLog.d("HttpServer", "FORBIDDEN: Won't serve ../ for security reasons.");
            }
        }
        File f = new File(homeDir, uri);
        if (res == null && !f.exists()) {
            MyLog.d("HttpServer", f.getPath() + "Error 404, file not found.");
            res = new Response(HTTP_NOTFOUND, MIME_PLAINTEXT, "Error 404, file not found.");
        }

        // List the directory, if necessary
        if (res == null && f.isDirectory()) {
            // Browsers get confused without '/' after the
            // directory, send a redirect.
            if (!uri.endsWith("/")) {
                uri += "/";
                res = new Response(HTTP_REDIRECT, MIME_HTML, "<html><body>Redirected: <a href=\""
                        + uri + "\">" + uri + "</a></body></html>");
                res.addHeader("Location", uri);
            }

            if (res == null) {
                // First try index.html and index.htm
                if (new File(f, "index.html").exists()) {
                    f = new File(homeDir, uri + "/index.html");
                } else if (new File(f, "index.htm").exists()) {
                    f = new File(homeDir, uri

                    + "/index.htm");
                }
                // No index file, list the directory if it is readable
                else if (allowDirectoryListing && f.canRead()) {
                    String[] files = f.list();
                    String msg = "<html><body><h1>Directory " + uri + "</h1><br/>";

                    if (uri.length() > 1) {
                        String u = uri.substring(0, uri.length() - 1);
                        int slash = u.lastIndexOf('/');
                        if (slash >= 0 && slash < u.length()) msg += "<b><a href=\""
                                + uri.substring(0, slash + 1) + "\">..</a></b><br/>";
                    }

                    if (files != null) {
                        for (int i = 0; i < files.length; ++i) {
                            File curFile = new File(f, files[i]);
                            boolean dir = curFile.isDirectory();
                            if (dir) {
                                msg += "<b>";
                                files[i] += "/";
                            }

                            msg += "<a href=\"" + encodeUri(uri + files[i]) + "\">" + files[i]
                                    + "</a>";

                            // Show file size
                            if (curFile.isFile()) {
                                long len = curFile.length();
                                msg += " &nbsp;<font size=2>(";
                                if (len < 1024) msg += len + " bytes";
                                else if (len < 1024 * 1024) msg += len / 1024 + "."
                                        + (len % 1024 / 10 % 100) + " KB";
                                else msg += len / (1024 * 1024) + "." + len % (1024 * 1024) / 10
                                        % 100 + " MB";

                                msg += ")</font>";
                            }
                            msg += "<br/>";
                            if (dir) msg += "</b>";
                        }
                    }
                    msg += "</body></html>";
                    res = new Response(HTTP_OK, MIME_HTML, msg);
                } else {
                    res = new Response(HTTP_FORBIDDEN, MIME_PLAINTEXT,
                            "FORBIDDEN: No directory listing.");
                }
            }
        }

        try {
            if (res == null) {
                // Get MIME type from file name extension, if possible
                String mime = null;
                int dot = f.getCanonicalPath().lastIndexOf('.');
                if (dot >= 0) mime = (String) theMimeTypes.get(f.getCanonicalPath()
                        .substring(dot + 1).toLowerCase());
                if (mime == null) mime = MIME_DEFAULT_BINARY;

                // Support (simple) skipping:
                long startFrom = 0;
                long endAt = -1;
                String range = header.getProperty("range");
                if (range != null) {
                    if (range.startsWith("bytes=")) {
                        range = range.substring("bytes=".length());
                        int minus = range.indexOf('-');
                        try {
                            if (minus > 0) {
                                startFrom = Long.parseLong(range.substring(0, minus));
                                endAt = Long.parseLong(range.substring(minus + 1));
                            }
                        } catch (NumberFormatException nfe) {}
                    }
                }

                // Change return code and add Content-Range header when skipping is requested
                long fileLen = f.length();
                if (range != null && startFrom >= 0) {
                    if (startFrom >= fileLen) {
                        res = new Response(HTTP_RANGE_NOT_SATISFIABLE, MIME_PLAINTEXT, "");
                        res.addHeader("Content-Range", "bytes 0-0/" + fileLen);
                    } else {
                        if (endAt < 0) endAt = fileLen - 1;
                        long newLen = endAt - startFrom + 1;
                        if (newLen < 0) newLen = 0;

                        final long dataLen = newLen;
                        FileInputStream fis = new FileInputStream(f) {

                            public int available() throws IOException {
                                return (int) dataLen;
                            }
                        };
                        fis.skip(startFrom);

                        res = new Response(HTTP_PARTIALCONTENT, mime, fis);
                        res.addHeader("Content-Length", "" + dataLen);
                        res.addHeader("Content-Range", "bytes " + startFrom + "-" + endAt + "/"
                                + fileLen);
                    }
                } else {
                    res = new Response(HTTP_OK, mime, new FileInputStream(f));
                    res.addHeader("Content-Length", "" + fileLen);
                }
            }
        } catch (IOException ioe) {
            res = new Response(HTTP_FORBIDDEN, MIME_PLAINTEXT, "FORBIDDEN: Reading file failed.");
        }

        res.addHeader("Accept-Ranges", "bytes"); // Announce that the file server accepts partial content requestes
        return res;
    }

    /**
     * Hashtable mapping (String)FILENAME_EXTENSION -> (String)MIME_TYPE
     */
    private static Hashtable theMimeTypes = new Hashtable();
    static {
        StringTokenizer st = new StringTokenizer("css		text/css " + "js			text/javascript "
                + "htm		text/html " + "html		text/html " + "txt		text/plain " + "asc		text/plain "
                + "gif		image/gif " + "jpg		image/jpeg " + "jpeg		image/jpeg " + "png		image/png "
                + "mp3		audio/mpeg " + "m3u		audio/mpeg-url " + "pdf		application/pdf "
                + "doc		application/msword " + "ogg		application/x-ogg "
                + "zip		application/octet-stream " + "exe		application/octet-stream "
                + "class		application/octet-stream ");
        while (st.hasMoreTokens())
            theMimeTypes.put(st.nextToken(), st.nextToken());
    }

    /**
     * GMT date formatter
     */
    private static java.text.SimpleDateFormat gmtFrmt;
    static {
        gmtFrmt = new java.text.SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

}
