package org.example.LLDQuestion.SplitWise;

/*
Functional Requirement
-> user can create a group
-> user can add/remove member in the group
-> user will be able to create a expense
-> system should support multiple splitting strategy
-> user should be able to see how much money user has to  give/take

Non-Functional Requirement
-> code should be extensible like in future we can add more split stratgy
-> code should follow solid principle
-> thread-safety


 */

import java.util.*;

class User {
    String userId;
}

class Group {
    String groupId;
    List<User> users;

    public Group(List<User> users) {
        this.groupId = UUID.randomUUID().toString();
        this.users = users;
    }
}

interface SplitStrategy {
    List<Split> getSplits(List<User> users, double amount, Map<String, Double> splitValues);
}

class EqualSplitStrategy implements SplitStrategy {
    @Override
    public List<Split> getSplits(List<User> users, double amount, Map<String, Double> splitValues) {
        int totalMember = splitValues.size();
        List<Split> result = new ArrayList<>();
        double val = amount / totalMember;
        System.out.println("Strategy: EQUAL SPLIT");
        System.out.println("Each member share: " + val);

        for (Map.Entry<String, Double> entry : splitValues.entrySet()) {
            String userId = entry.getKey();
            result.add(new Split(userId, val));
        }
        return result;
    }
}

class PercentageSplitStrategy implements SplitStrategy {
    @Override
    public List<Split> getSplits(List<User> users, double amount, Map<String, Double> splitValues) {
        List<Split> result = new ArrayList<>();
        Double totalPercentage = 0.0;
        System.out.println("Strategy: PERCENTAGE SPLIT");

        for (Map.Entry<String, Double> entry : splitValues.entrySet()) {
            String userId = entry.getKey();
            Double percentagePerUser = entry.getValue();
            totalPercentage += percentagePerUser;
            double val = (percentagePerUser * amount) / 100;
            result.add(new Split(userId, val));
            System.out.println(userId + " -> " + percentagePerUser + "% = Rs." + val);
        }

        if (totalPercentage != 100.0) {
            throw new RuntimeException("sum of all percentage should be equal to 100");
        }
        return result;
    }
}

class Expense {
    String expenseId;
    String groupId;
    Double amount;
    User paidBy;
    List<Split> splits;

    public Expense(String groupId, Double amount, User paidBy) {
        this.groupId = groupId;
        this.amount = amount;
        this.paidBy = paidBy;
        this.expenseId = UUID.randomUUID().toString();
    }
}

class Split {
    String userId;
    double amount;

    public Split(String userId, double amount) {
        this.amount = amount;
        this.userId = userId;
    }
}

class Repositories {
    Map<String, Group> groupMap = new HashMap<>();
    Map<String, List<Expense>> expensesHistory = new HashMap<>();
    Map<String, Map<String, Double>> balances = new HashMap<>();
}

class SplitWise {
    Repositories repository;
    SplitStrategy splitStrategy;

    SplitWise(SplitStrategy strategy) {
        this.repository = new Repositories();
        this.splitStrategy = strategy;
    }

    public synchronized void addExpense(Expense expense, SplitStrategy strategy, String groupId, Map<String, Double> splitValues) {
        System.out.println("\n==================================");
        System.out.println("NEW EXPENSE");
        System.out.println("Expense ID: " + expense.expenseId);
        System.out.println("Paid By: " + expense.paidBy.userId);
        System.out.println("Total Amount: Rs." + expense.amount);
        System.out.println("==================================");

        Group group = repository.groupMap.get(groupId);
        List<Split> splits = strategy.getSplits(group.users, expense.amount, splitValues);
        expense.splits = splits;
        repository.expensesHistory.computeIfAbsent(groupId, key -> new ArrayList<>()).add(expense);

        System.out.println("\nUpdating balances:");

        for (Split split : splits) {
            String userId = split.userId;
            String paidBy = expense.paidBy.userId;
            if (userId.equals(paidBy)) continue;

            double value = split.amount;
            double userBalance = repository.balances.get(userId).getOrDefault(paidBy, 0.0);
            repository.balances.get(userId).put(paidBy, userBalance - value);
            double payerBalance = repository.balances.get(paidBy).getOrDefault(userId, 0.0);
            repository.balances.get(paidBy).put(userId, payerBalance + value);

            System.out.println(userId + " balance with " + paidBy + ": " + userBalance + " -> " + (userBalance - value));
            System.out.println(paidBy + " balance with " + userId + ": " + payerBalance + " -> " + (payerBalance + value));
        }

        System.out.println("\nExpense added successfully!");
        System.out.println("Total expenses in group: " + repository.expensesHistory.get(groupId).size());
    }
}

public class SplitWiseDemo {
    public static void main(String[] args) {
        User A = new User();
        A.userId = "A";
        User B = new User();
        B.userId = "B";
        User C = new User();
        C.userId = "C";

        Group group = new Group(Arrays.asList(A, B, C));
        SplitWise splitWise = new SplitWise(new EqualSplitStrategy());
        splitWise.repository.groupMap.put(group.groupId, group);

        splitWise.repository.balances.put("A", new HashMap<>());
        splitWise.repository.balances.put("B", new HashMap<>());
        splitWise.repository.balances.put("C", new HashMap<>());

        System.out.println("GROUP CREATED");
        System.out.println("Members: A, B, C");

        Map<String, Double> equalSplit = new HashMap<>();
        equalSplit.put("A", 0.0);
        equalSplit.put("B", 0.0);
        equalSplit.put("C", 0.0);

        Expense expense1 = new Expense(group.groupId, 900.0, A);
        splitWise.addExpense(expense1, new EqualSplitStrategy(), group.groupId, equalSplit);

        System.out.println("\nBalances after Expense 1:");
        System.out.println(splitWise.repository.balances);

        Expense expense2 = new Expense(group.groupId, 600.0, B);
        splitWise.addExpense(expense2, new EqualSplitStrategy(), group.groupId, equalSplit);

        System.out.println("\nBalances after Expense 2:");
        System.out.println(splitWise.repository.balances);

        Map<String, Double> percentageSplit = new HashMap<>();
        percentageSplit.put("A", 25.0);
        percentageSplit.put("B", 25.0);
        percentageSplit.put("C", 50.0);

        Expense expense3 = new Expense(group.groupId, 400.0, C);
        splitWise.addExpense(expense3, new PercentageSplitStrategy(), group.groupId, percentageSplit);

        System.out.println("\n========== FINAL BALANCES ==========");

        for (Map.Entry<String, Map<String, Double>> userEntry : splitWise.repository.balances.entrySet()) {
            String userId = userEntry.getKey();

            for (Map.Entry<String, Double> balanceEntry : userEntry.getValue().entrySet()) {
                String otherUser = balanceEntry.getKey();
                double balance = balanceEntry.getValue();

                if (balance < 0) {
                    System.out.println(userId + " needs to GIVE Rs." + (-balance) + " to " + otherUser);
                } else if (balance > 0) {
                    System.out.println(userId + " needs to RECEIVE Rs." + balance + " from " + otherUser);
                }
            }
        }

        System.out.println("====================================");
    }
}
