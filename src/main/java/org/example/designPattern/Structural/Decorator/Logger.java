package org.example.designPattern.Structural.Decorator;

public class Logger extends NotificationDecorator{
    public Logger(Notification wrapper) {
        super(wrapper);
    }

    @Override
    public void sendNotification(String message) {
        System.out.println("log all the data");
        wrapper.sendNotification(message);
    }
}
