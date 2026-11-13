//package org.example.designPattern.Creational.Factory;
//
//import org.example.designPattern.Structural.Decorator.WhatsappNotification;
//
//class Application{
//    NotificationFactory factory;
//
//    void sendNotification(String message, String type){
//     factory.getNotification(type).send();
//    }
//}
//
//interface Notification{
//    void send();
//}
//
//class Whatsapp implements Notification{
//
//    @Override
//    public void send() {
//
//    }
//}
//class Email implements Notification{
//
//    @Override
//    public void send() {
//
//    }
//}
//
//class NotificationFactory{
//    Notification getNotification(String type){
//        if(type.equals("Whatsapp")){
//            return new Whatsapp();
//        }
//        else if(type.equals("Email")){
//            return new Email();
//        }
//        else{
//            throw new IllegalArgumentException("type is not valid");
//        }
//    }
//}
//
//public class SimpleFactoryDemo {
//    public static void main(String[] args) {
//
//    }
//}
