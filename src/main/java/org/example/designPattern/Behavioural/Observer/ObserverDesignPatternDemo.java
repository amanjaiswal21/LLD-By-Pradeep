package org.example.designPattern.Behavioural.Observer;


import java.util.ArrayList;
import java.util.List;

class Stock{
    int price;
    List<Observer> observers=new ArrayList<>();

    void addObserver(Observer observer){
        observers.add(observer);
    }
    void removerObserver(Observer observer){
        observers.remove(observer);
    }

    private void notifyAllObserver(){
        for(Observer observer: observers){
            observer.notify(price);
        }
    }

    void setPrice(int state){
        if(price!=state){
            this.price=state;
            notifyAllObserver();
        }
    }
}

interface Observer{
    void notify(int state);
}

class Phone implements Observer{

    @Override
    public void notify(int state) {
        System.out.println("Phone got notification,Stock price got updated " + state);
    }
}

class TV implements Observer{

    @Override
    public void notify(int state) {
        System.out.println("TV got notification,Stock price got updated " + state);
    }
}


public class ObserverDesignPatternDemo {
    public static void main(String[] args) {
      Stock stock =new Stock();
      stock.addObserver(new TV());
      stock.addObserver(new Phone());

      stock.setPrice(2000);
    }
}
