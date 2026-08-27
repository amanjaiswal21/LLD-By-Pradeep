package org.example.designPattern.Structural.Decorator;

public class Authenticator extends NotificationDecorator{
    public Authenticator(Notification wrapper) {
        super(wrapper);
    }

    @Override
    public void sendNotification(String message) {
        System.out.println("code for authentication");
        wrapper.sendNotification(message);
    }
}
