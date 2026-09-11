# Exercise Set: Creational Design Patterns

## Transaction Processing System

This project demonstrates the use of three **Creational Design Patterns** in Java:

- Factory Pattern
- Builder Pattern
- Singleton Pattern

The goal is to design a simple **Transaction Processing System** for a fintech platform that supports multiple payment methods, configurable transaction objects, and shared resources.

---

## Problem Context

The system allows users to:

- Make transactions using different payment methods
- Configure transaction details dynamically
- Efficiently manage shared resources such as transaction logging

The following design patterns are used:

| Pattern | Purpose |
|---|---|
| Factory | Create different payment processors |
| Builder | Construct configurable and immutable transaction objects |
| Singleton | Maintain one shared transaction logger |

---

# Part 1: Factory Pattern

## Problem

The system must support multiple payment methods:

- Credit Card
- UPI
- Net Banking

All payment processors implement a common interface.

### PaymentProcessor Interface

```java
public interface PaymentProcessor {
    void processPayment(double amount);
}