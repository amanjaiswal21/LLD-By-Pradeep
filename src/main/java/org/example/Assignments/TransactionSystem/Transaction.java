package org.example.Assignments.TransactionSystem;

import java.time.LocalDateTime;

public class Transaction {
    private final String transactionId;
    private final String customerId;
    private final double amount;
    private final String currency;
    private final String notes;
    private final LocalDateTime timestamp;

    private Transaction(Builder builder) {
        this.transactionId = builder.transactionId;
        this.customerId = builder.customerId;
        this.amount = builder.amount;
        this.currency = builder.currency;
        this.notes = builder.notes;
        this.timestamp = builder.timestamp;
    }

    public double getAmount(){
        return this.amount;
    }
    public String getTransactionId(){
        return this.transactionId;
    }

    public static class Builder {

        //mandatory field
        private final String transactionId;
        private final String customerId;
        private final double amount;

        // optional field
        private String currency;
        private String notes;
        private LocalDateTime timestamp;

        public Builder(String transactionId, String customerId, double amount) {
            this.transactionId = transactionId;
            this.customerId = customerId;
            this.amount = amount;
        }

        public Builder setCurrency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder setNotes(String notes) {
            this.notes = notes;
            return this;
        }

        public Builder setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Transaction build() {
            return new Transaction(this);
        }
    }
}
