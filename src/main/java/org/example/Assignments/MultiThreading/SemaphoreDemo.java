package org.example.Assignments.MultiThreading;

/*
build a mutex lock using semaphore lock
 */

class MySemaphore{
    int activeThread=0;
    int totalThread;
    public MySemaphore(int size){
        this.totalThread=size;
    }
   synchronized void accquireLock() throws InterruptedException {
        while (activeThread>=totalThread){
            wait();
        }
        activeThread++;
    }

    synchronized void releaseLock(){
      if(activeThread<=0)
          throw new IllegalStateException(
                  "No active thread is present"
          );
      activeThread--;
      if(activeThread<totalThread)
          notifyAll();
    }

}

class SharedResource{
    MySemaphore lock=new MySemaphore(1);
    int counter=1;

    public void increament() throws InterruptedException {
        lock.accquireLock();
        counter++;
        lock.releaseLock();
    }

    public int getCounter() throws InterruptedException {
        lock.accquireLock();
        int val=counter;
        lock.releaseLock();
        return val;
    }
}

public class SemaphoreDemo {
    public static void main(String[] args) throws InterruptedException {
        SharedResource sharedResource=new SharedResource();
        for(int i=1;i<=100;i++){
            new Thread(()->{
                try {
                    sharedResource.increament();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }

        Thread.sleep(2000);
        System.out.println("value after incrementing :"+ sharedResource.getCounter());
    }
}
