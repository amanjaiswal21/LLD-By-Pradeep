package org.example.designPattern.Structural.Adapter;

/*
Initially, we have an IPaymentProcessor contract that is already shared with our clients, and our application has its own legacy implementation
of this interface.

Now we want to integrate a third-party payment provider like Stripe. However, Stripe exposes a different interface and method signature from
our existing IPaymentProcessor.

We cannot change the existing contract because thousands of clients are already using it. If we change the interface, their existing code may
break.

To solve this, we use the Adapter Design Pattern.
 */

interface IPaymentProcessor {
    void pay();
}

class Application implements IPaymentProcessor {
    @Override
    public void pay() {
        System.out.println("Legacy code");
    }
}

class StripePayment {
    void makePayment() {
        System.out.println("Stripe is making payment");
    }
}

class StripeAdapter implements IPaymentProcessor {
    StripePayment stripePayment;

    @Override
    public void pay() {
        stripePayment.makePayment();
    }
}

/*
In the future, if we want to support another payment gateway such as Paytm, we can simply create a PaytmPaymentAdapter that also implements
 IPaymentProcessor.

This way, the client-facing contract remains unchanged, while our system can support multiple payment gateways without breaking existing
clients. */

class PaytmPayment {
    void makePayment() {
        System.out.println("Paytm is making payment");
    }
}

class PaytmAdapter implements IPaymentProcessor {
    PaytmPayment paytmPayment;

    @Override
    public void pay() {
        paytmPayment.makePayment();
    }
}

public class AdapterDesignPatternDemo {
}
