package org.example.Assignments.TransactionSystem;

import org.example.Assignments.TransactionSystem.PaymentProcessor.PaymentProcessor;

public class TransactionSystemDemo {
    public static void main(String[] args) {
        PaymentProcessor processor = PaymentProcessorFactory.getPayementProcessor(PayementMethod.CREDIT_CARD);

        Transaction txn = new Transaction.Builder("txn1", "cust1", 500)
                .setCurrency("INR")
                .build();

        processor.processPayment(txn.getAmount());
        TransactionLogger logger=TransactionLogger.getTransactionLoggerInstance();
        logger.log(
                "Transaction is completed, transactionId is "
                        + txn.getTransactionId()
                        + " and transaction amount is "
                        + txn.getAmount());

    }
}
