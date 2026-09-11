package org.example.Assignments.TransactionSystem.PaymentProcessor;

public class UpiProcessor implements PaymentProcessor {
    @Override
    public void processPayment(double amount) {
        System.out.println("Processing upi payment of " + amount);
    }
}
