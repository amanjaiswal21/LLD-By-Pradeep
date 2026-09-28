package org.example.Multithreading;


import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

class CustomThreadPool{
    BlockingQueue<Runnable>tasks;
    ArrayList<Thread>workers;
    volatile boolean isShutdown=false;

   public CustomThreadPool(int poolSize){
        tasks=new LinkedBlockingQueue<>();
        workers=new ArrayList<>();

        Runnable workerTask=()->{
            while (!isShutdown){
                Runnable task=null;
                try {
                    task=tasks.take();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                task.run();
            }

        };

        for(int i=0;i<poolSize;i++){
            workers.add(new Thread(workerTask, "Thread no"+i));
        }
       for (Thread worker : workers) {
           worker.start();
       }
    }


    public void submit(Runnable task){
        tasks.offer(task);
    }

    public void shutDown(){
       isShutdown=true;
    }
}

public class ThreadPool {
    public static void main(String[] args) throws InterruptedException {
        CustomThreadPool threadPool=new CustomThreadPool(8);
        for(int i=0;i<5000;i++){
            int a = i;
            Runnable task=()->{
                System.out.println("my name is printing "+ a);
            };
            threadPool.submit(task);
        }

    }
}
