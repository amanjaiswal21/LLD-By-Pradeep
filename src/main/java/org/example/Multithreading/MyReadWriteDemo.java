package org.example.Multithreading;

import java.util.HashMap;

class MyReadWriteLock {
    int totalReadLock = 0;
    boolean isWriteLockActive = false;

    synchronized void accquireReadLock() throws InterruptedException {
        while (isWriteLockActive) {
            wait();
        }
        totalReadLock++;
    }

    synchronized void releaseReadLock() {
        if (totalReadLock <= 0) {
            throw new IllegalStateException(
                    "No reader currently holds the lock"
            );
        }
        totalReadLock--;
        if (totalReadLock == 0) notifyAll();
    }

   synchronized void accquireWriteLock() throws InterruptedException {
     while (totalReadLock>0 || isWriteLockActive){
         wait();
     }
     isWriteLockActive=true;
    }

    synchronized void releaseWriteLock() {
      if(isWriteLockActive==false)
          throw new IllegalStateException(
                  "No writer currently holds the lock"
          );
      isWriteLockActive=false;
      notifyAll();
    }
}

class Cache{
    HashMap<Integer,Integer> cache=new HashMap<>();
    MyReadWriteLock lock=new MyReadWriteLock();

    int getVal(int key) throws InterruptedException {
       // lock.accquireReadLock();
        int val=-1;
        if(cache.containsKey(key))
            val=cache.get(key);
      //  lock.releaseReadLock();
        return val;
    }

    void addVal(int key,int val) throws InterruptedException {
      //  lock.accquireWriteLock();
        cache.put(key,val);
       // lock.releaseWriteLock();
    }
}

public class MyReadWriteDemo {
    public static void main(String[] args) {
        Cache cache=new Cache();
     Thread t1=new Thread(()->{
         try {
             cache.addVal(3,4);
         } catch (InterruptedException e) {
             throw new RuntimeException(e);
         }
     });
        Thread t2=new Thread(()->{
            try {
                cache.addVal(3,6);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        Thread t3=new Thread(()->{
            try {
                cache.addVal(3,8);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        Thread t4=new Thread(()->{
            try {
                System.out.println(cache.getVal(3));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        t2.start();
        t3.start();
        t1.start();
        t4.start();

    }
}
