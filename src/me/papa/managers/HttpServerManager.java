package me.papa.managers;

import java.io.IOException;

import me.papa.video.services.HttpServer;

public class HttpServerManager {

    public static final int port = 9090;

    private static HttpServer mHttpServer;

    public static HttpServer getInstance() {
        return mHttpServer;
    }

    public static void init() {
        if (mHttpServer == null) {
            try {
                mHttpServer = new HttpServer(port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
