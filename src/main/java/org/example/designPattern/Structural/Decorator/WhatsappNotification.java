package org.example.designPattern.Structural.Decorator;

public class WhatsappNotification implements Notification{
    @Override
    public void sendNotification(String message) {
        System.out.println("message is sent via sms");
    }
}
