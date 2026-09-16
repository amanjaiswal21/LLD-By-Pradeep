package org.example.designPattern.Behavioural.StateDesignPattern;

/*
Atm machine
state hasCard,NoCard,WithdrawingCash
methods-> insertCard, ejectCard, checkBalance, withdraw
 */



class AtmMachine {
    State currentState;

    void insertCard(){

    }

    void ejectCard(){

    }

    void checkBalance(){

    }

    void withdraw(){

    }


}

interface State {
    void insertCard();

    void ejectCard();

    void checkBalance();

    void withdrawCash();
}

class NoCardState implements State {

    @Override
    public void insertCard() {

    }

    @Override
    public void ejectCard() {

    }

    @Override
    public void checkBalance() {

    }

    @Override
    public void withdrawCash() {

    }
}

class HasCardState implements State {

    @Override
    public void insertCard() {

    }

    @Override
    public void ejectCard() {

    }

    @Override
    public void checkBalance() {

    }

    @Override
    public void withdrawCash() {

    }
}

class CashDispensedState implements State {

    @Override
    public void insertCard() {

    }

    @Override
    public void ejectCard() {

    }

    @Override
    public void checkBalance() {

    }

    @Override
    public void withdrawCash() {

    }
}


public class StateDesignPatternDemo {
    public static void main(String[] args) {

    }
}
