package org.example.designPattern.Structural.Decorator;

public class RateLimiting extends NotificationDecorator{
    public RateLimiting(Notification wrapper) {
        super(wrapper);
    }

    @Override
    public void sendNotification(String message) {
        System.out.println("code for rate limitter");
        wrapper.sendNotification(message);
    }
}
