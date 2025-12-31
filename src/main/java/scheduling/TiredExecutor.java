package scheduling;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TiredExecutor {

    private final TiredThread[] workers;
    private final PriorityBlockingQueue<TiredThread> idleMinHeap = new PriorityBlockingQueue<>();
    private final AtomicInteger inFlight = new AtomicInteger(0);//how many tasks are currently being executed

    public TiredExecutor(int numThreads) {
        if (numThreads <= 0) {
            throw new IllegalArgumentException("Number of threads must be positive");
        }
        this.workers = new TiredThread[numThreads];
        for (int i = 0; i < numThreads; i++) {
           double fatigue = 0.5 + java.util.concurrent.ThreadLocalRandom.current().nextDouble(1.0);
           TiredThread w = new TiredThread(i, fatigue);
           workers[i] = w;
           w.start();
           idleMinHeap.add(w);
        }

      
    }

    // Submit a single task to be executed by an idle worker. If no idle workers are available,this method blocks until one becomes available.
    public void submit(Runnable task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        while (true) {
            final TiredThread w;
            try {
                 w = idleMinHeap.take();
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            inFlight.incrementAndGet();
            Runnable wrapped = () -> { 
                try {task.run();}
                finally {
                    idleMinHeap.offer(w);
                    inFlight.decrementAndGet();
                    synchronized (TiredExecutor.this) { 
                        TiredExecutor.this.notifyAll();
                    }
                }
            };
            try {
                w.newTask(wrapped);//assign the task to the worker
                return;
                }
            catch (IllegalStateException ex) {
                idleMinHeap.offer(w);
                inFlight.decrementAndGet();
            }
        }
    }
            
    public void submitAll(Iterable<Runnable> tasks) {
        if (tasks == null) {
            throw new IllegalArgumentException("tasks cannot be null");
        }
        for (Runnable task : tasks) {
            submit(task);
        }
        synchronized (this) {
            while (inFlight.get() != 0) {
                try {
                    this.wait();// puts the calling thread (the Main thread) into a "Waiting" state. It releases the lock and stays idle, consuming no CPU power, while the workers do the math.
                } 
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for tasks to finish", e);
                }
            }
        }
    }

    public void shutdown() throws InterruptedException {
       synchronized (this) {
            while (inFlight.get() != 0) {//wait for all tasks to finish
              this.wait();
            }
        }
        for (TiredThread w : workers) {
            w.shutdown();//send poison pill, killing them
        }
        for (TiredThread w : workers) {
            w.join();//preventing zombie threads
        }
    }

    public synchronized String getWorkerReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== TiredExecutor Worker Report ===\n");
        sb.append("inFlight = ").append(inFlight.get()).append("\n\n");
        
        for (TiredThread w : workers) {
            sb.append("Worker #").append(w.getWorkerId()).append(" | busy=").append(w.isBusy())
            .append(" | fatigue=").append(String.format("%.3f", w.getFatigue())).append(" | timeUsed=").append(w.getTimeUsed())
            .append(" | timeIdle=").append(w.getTimeIdle()).append("\n");
        }
        return sb.toString();
    }
}
