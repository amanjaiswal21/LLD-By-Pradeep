package org.example.Assignments.MultiThreading;

/*
Implement a counter that is safe when 100 threads increament it concurrently
a) using synchronised
b) reentrant lock
c) Atomic Integer
d)
 */

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// using synchronised
class SynchronisedCounter{
    int counter=1;

    synchronized public void increament(){
        counter++;
    }

    synchronized public int getCounter(){
        return counter;
    }
}

class ReentrantLockCounter{
    int counter=1;
    ReentrantLock lock=new ReentrantLock();

     public void increament(){
         lock.lock();
        counter++;
        lock.unlock();
    }

    public int getCounter(){
         lock.lock();
        int val= counter;
        lock.unlock();
        return val;
    }
}

class ReadWriteLockCounter{
    int counter=1;
    ReadWriteLock lock=new ReentrantReadWriteLock();

    public void increament(){
        lock.writeLock().lock();
        counter++;
        lock.writeLock().unlock();
    }

    public int getCounter(){
        lock.readLock().lock();
        int val=counter;
        lock.readLock().unlock();
        return val;
    }
}




public class MultiThreadingDemo {
    public static void main(String[] args) throws InterruptedException {
      System.out.println(" using synchronised ");

      SynchronisedCounter counter=new SynchronisedCounter();
      for(int i=1;i<=100;i++){
          new Thread(()->{
              counter.increament();
          }).start();
      }
        Thread.sleep(5000); // beacuse main there is no waiting to complete the thread its task another method we can do store in
        // arraylist and then we can iterte and start it and after that we can add join also so main thread will wait for the threads

        System.out.println("value after incrementing :"+ counter.getCounter());



        System.out.println(" using Reentrant ");

        ReentrantLockCounter counter2=new ReentrantLockCounter();
        for(int i=1;i<=100;i++){
            new Thread(()->{
                counter2.increament();
            }).start();
        }
        Thread.sleep(5000);
        System.out.println("value after incrementing :"+ counter2.getCounter());

        System.out.println(" using read write lock ");
        ReadWriteLockCounter counter3=new ReadWriteLockCounter();
        for(int i=1;i<=100;i++){
            new Thread(()->{
                counter3.increament();
            }).start();
        }
        Thread.sleep(5000);
        System.out.println("value after incrementing :"+ counter3.getCounter());
    }

}
