package org.example.designPattern.Structural.Decorator;

public abstract class NotificationDecorator implements Notification {
    protected  Notification wrapper;

    public NotificationDecorator(Notification wrapper){
        this.wrapper=wrapper;
    }
}
