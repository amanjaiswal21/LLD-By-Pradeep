package org.example.Assignments.TransactionSystem.PaymentProcessor;

public class NetBankingProcessor implements PaymentProcessor {
    @Override
    public void processPayment(double amount) {
        System.out.println("Processing net banking payment of " + amount);
    }
}
