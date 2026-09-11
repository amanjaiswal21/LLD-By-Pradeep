package org.example.Assignments.TransactionSystem;

public class TransactionLogger {
    public static TransactionLogger transactionLogger;

    private TransactionLogger(){
    }

    public static synchronized TransactionLogger getTransactionLoggerInstance(){
        if(transactionLogger==null){
            return new TransactionLogger();
        }
        return transactionLogger;
    }

    public void log(String message) {
        System.out.println("[TRANSACTION LOG] " + message);
    }
}
