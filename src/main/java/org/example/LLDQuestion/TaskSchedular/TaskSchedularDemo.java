package org.example.LLDQuestion.TaskSchedular;

/*
Functional Requirement
Schedule a task
execute a task at given time
support different priorities of task
support different types of task -> recurring, non-recurring

Non-Function Requirement
code follow solid principle
code should be extensible (we can add multiple task by extending task interface)
thread safety


 */



import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.PriorityBlockingQueue;

enum TaskPriority {
    HIGH(3), MEDIUM(2), LOW(1);
    final int val;
    TaskPriority(int val) { this.val = val; }
}

enum TaskType { RECURRING, ONE_TIME }

interface Task { void execute(); }

class ScheduledTask implements Comparable<ScheduledTask> {
    String taskId;
    TaskPriority taskPriority;
    TaskType taskType;
    Task task;
    long scheduledAt;
    long interval;

    ScheduledTask(TaskPriority taskPriority, TaskType taskType, Task task, long scheduledAt, long interval) {
        if (taskType == TaskType.RECURRING && interval <= 0) throw new IllegalArgumentException("Interval must be positive");
        this.taskId = UUID.randomUUID().toString();
        this.taskPriority = taskPriority;
        this.taskType = taskType;
        this.task = task;
        this.scheduledAt = scheduledAt;
        this.interval = interval;
    }

    @Override
    public int compareTo(ScheduledTask other) {
        if (this.scheduledAt < other.scheduledAt) return -1;
        if (this.scheduledAt > other.scheduledAt) return 1;
        if (this.taskPriority.val > other.taskPriority.val) return -1;
        if (this.taskPriority.val < other.taskPriority.val) return 1;
        return 0;
    }
}

class TaskSchedular {
    PriorityBlockingQueue<ScheduledTask> tasks = new PriorityBlockingQueue<>();
    private boolean running = true;

    synchronized void addTask(ScheduledTask task) {
        if (!running) return;
        tasks.add(task);
        System.out.println("[SCHEDULER] Added: " + task.taskId.substring(0, 8) + " | Priority: " + task.taskPriority + " | Type: " + task.taskType + " | Queue size: " + tasks.size());
        notifyAll();
    }

    synchronized boolean isRunning() { return running; }

    synchronized void shutdown() {
        running = false;
        notifyAll();
    }
}

class DispatcherWorker implements Runnable {
    final TaskSchedular taskSchedular;

    DispatcherWorker(TaskSchedular taskSchedular) { this.taskSchedular = taskSchedular; }

    @Override
    public void run() {
        String workerName = Thread.currentThread().getName();
        System.out.println("[" + workerName + "] Started");

        while (true) {
            ScheduledTask scheduledTask;

            synchronized (taskSchedular) {
                while (taskSchedular.tasks.isEmpty() && taskSchedular.isRunning()) {
                    try {
                        System.out.println("[" + workerName + "] Queue empty. Waiting...");
                        taskSchedular.wait();
                        System.out.println("[" + workerName + "] Woke up! Checking queue...");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }

                if (!taskSchedular.isRunning()) return;

                scheduledTask = taskSchedular.tasks.peek();
                long currentTime = System.currentTimeMillis();
                long waitTime = scheduledTask.scheduledAt - currentTime;

                if (waitTime > 0) {
                    try {
                        System.out.println("[" + workerName + "] Task " + scheduledTask.taskId.substring(0, 8) + " waiting " + waitTime + " ms");
                        taskSchedular.wait(waitTime);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    continue;
                }

                scheduledTask = taskSchedular.tasks.poll();
                System.out.println("[" + workerName + "] Picked Task " + scheduledTask.taskId.substring(0, 8) + " | Priority: " + scheduledTask.taskPriority);
            }

            try {
                System.out.println("[" + workerName + "] Executing Task " + scheduledTask.taskId.substring(0, 8));
                scheduledTask.task.execute();
                System.out.println("[" + workerName + "] Completed Task " + scheduledTask.taskId.substring(0, 8));
            } catch (Exception e) {
                System.out.println("[" + workerName + "] Task failed: " + e.getMessage());
            }

            if (scheduledTask.taskType == TaskType.RECURRING && taskSchedular.isRunning()) {
                scheduledTask.scheduledAt = System.currentTimeMillis() + scheduledTask.interval;
                System.out.println("[" + workerName + "] Rescheduling Task " + scheduledTask.taskId.substring(0, 8) + " after " + scheduledTask.interval + " ms");
                taskSchedular.addTask(scheduledTask);
            }
        }
    }
}

class DispatcherWorkerThreadPool {
    List<Thread> workers;
    final TaskSchedular taskSchedular;

    DispatcherWorkerThreadPool(int n, TaskSchedular taskSchedular) {
        this.taskSchedular = taskSchedular;
        workers = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            DispatcherWorker task = new DispatcherWorker(taskSchedular);
            Thread thread = new Thread(task, "Th-" + i);
            workers.add(thread);
            thread.start();
        }
    }

    void shutdown() throws InterruptedException {
        taskSchedular.shutdown();
        for (Thread thread : workers) thread.interrupt();
        for (Thread thread : workers) thread.join();
        System.out.println("[THREAD POOL] All workers stopped");
    }
}

public class TaskSchedularDemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("===== TASK SCHEDULER STARTED =====");

        TaskSchedular scheduler = new TaskSchedular();
        DispatcherWorkerThreadPool threadPool = new DispatcherWorkerThreadPool(5, scheduler);
        long executionTime = System.currentTimeMillis() + 3000;

        for (int i = 1; i <= 6; i++) {
            final int taskNumber = i;
            TaskPriority priority = i <= 2 ? TaskPriority.HIGH : i <= 4 ? TaskPriority.MEDIUM : TaskPriority.LOW;

            Task task = () -> {
                System.out.println(">>> Task " + taskNumber + " RUNNING on " + Thread.currentThread().getName());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                System.out.println(">>> Task " + taskNumber + " FINISHED");
            };

            scheduler.addTask(new ScheduledTask(priority, TaskType.ONE_TIME, task, executionTime, 0));
        }

        scheduler.addTask(new ScheduledTask(TaskPriority.MEDIUM, TaskType.RECURRING, () -> System.out.println(">>> RECURRING TASK running on " + Thread.currentThread().getName()), System.currentTimeMillis() + 5000, 3000));

        Thread.sleep(1000);
        System.out.println("===== ADDING AN IMMEDIATE TASK =====");

        scheduler.addTask(new ScheduledTask(TaskPriority.HIGH, TaskType.ONE_TIME,   () -> System.out.println(">>> IMMEDIATE TASK EXECUTED by " + Thread.currentThread().getName()), System.currentTimeMillis(), 0));

        Thread.sleep(12000);
        System.out.println("===== SHUTTING DOWN =====");
        threadPool.shutdown();
        System.out.println("===== DEMO FINISHED =====");
    }
}