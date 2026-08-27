package org.example.designPattern.Structural.Decorator;

//Decorator is a structural design pattern used to add additional behavior or responsibilities to an existing object dynamically,
// without modifying its original class or breaking the client code.

public class DecoratorDemo {
    public static void main(String[] args) {
        Notification notification=new Logger(new Authenticator(new RateLimiting(new SMSNotification())));
        notification.sendNotification("good");
    }
}
