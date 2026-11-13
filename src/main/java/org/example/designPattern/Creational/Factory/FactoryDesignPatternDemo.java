package org.example.designPattern.Creational.Factory;


class Application{

    void sendNotification(String message, NotificationFactory factory){
        factory.createNotification().send();
    }
}

interface Notification{
    void send();
}

class Whatsapp implements Notification{

    @Override
    public void send() {

    }
}
class Email implements Notification{

    @Override
    public void send() {

    }
}

interface NotificationFactory{
    Notification createNotification();
}
class WhatsappFactory implements NotificationFactory{

    @Override
    public Notification createNotification() {
        return new Whatsapp();
    }
}

class EmailFactory implements NotificationFactory{

    @Override
    public Notification createNotification() {
        return new Email();
    }
}

public class FactoryDesignPatternDemo {
    public static void main(String[] args) {

    }
}
