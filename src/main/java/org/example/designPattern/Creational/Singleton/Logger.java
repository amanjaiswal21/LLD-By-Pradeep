package org.example.designPattern.Creational.Singleton;

public class Logger {
    private String path;
    private static Logger instance = null;

    private Logger(String path) {
        System.out.println("object created");
        this.path = path;
    }

    public static synchronized Logger getInstance() {
        if (instance == null) {
            instance = new Logger("loggerInstance");
        }
        return instance;
    }
}

