package org.example.Assignments.TransactionSystem;

import org.example.Assignments.TransactionSystem.PaymentProcessor.CreditCardProcessor;
import org.example.Assignments.TransactionSystem.PaymentProcessor.NetBankingProcessor;
import org.example.Assignments.TransactionSystem.PaymentProcessor.PaymentProcessor;
import org.example.Assignments.TransactionSystem.PaymentProcessor.UpiProcessor;

public class PaymentProcessorFactory {
    public static PaymentProcessor getPayementProcessor(PayementMethod payementMethod) {
        return switch (payementMethod) {
            case payementMethod.CREDIT_CARD -> new CreditCardProcessor();
            case payementMethod.UPI -> new UpiProcessor();
            case payementMethod.NET_BANKING -> new NetBankingProcessor();

            default -> throw new IllegalArgumentException(
                    "Unsupported payment type: " + payementMethod
            );
        };
    }
}
