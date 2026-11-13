package org.example.designPattern.Creational.Factory;

class App{
    void pay(int amt, factory factory){
        factory.getPayment().makePayment(amt);
    }
    void refund(factory factory){
        factory.getRefund().getRefund();
    }
    void invoice(factory factory){
        factory.getInvoice().getInvoice();
    }
}

interface Pay{
    void makePayment(int amt);
}
interface Refund{
    void getRefund();
}
interface Invoice{
    void getInvoice();
}

class PaytmPayment implements Pay{

    @Override
    public void makePayment(int amt) {

    }
}

class PaytmRefund implements Refund{

    @Override
    public void getRefund() {

    }
}

class PaytmInvoice implements Invoice{

    @Override
    public void getInvoice() {

    }
}

class StripePayment implements Pay{

    @Override
    public void makePayment(int amt) {

    }
}

class StripeRefund implements Refund{

    @Override
    public void getRefund() {

    }
}

class StripeInvoice implements Invoice{

    @Override
    public void getInvoice() {

    }
}

interface factory{
    Pay getPayment();
    Refund getRefund();
    Invoice getInvoice();
}

class PaytmFactory implements factory{

    @Override
    public Pay getPayment() {
        return new PaytmPayment();
    }

    @Override
    public Refund getRefund() {
        return new PaytmRefund();
    }

    @Override
    public Invoice getInvoice() {
        return new PaytmInvoice();
    }
}

class StripeFactory implements factory{

    @Override
    public Pay getPayment() {
        return new StripePayment();
    }

    @Override
    public Refund getRefund() {
        return new StripeRefund();
    }

    @Override
    public Invoice getInvoice() {
        return new StripeInvoice();
    }
}

public class AbstarctFactoryDesignPattern {
    public static void main(String[] args) {

    }
}
