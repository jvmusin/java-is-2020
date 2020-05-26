package impl.matrix;

import java.util.ArrayDeque;
import java.util.Queue;

public class ThreadPool {
    private final Thread[] threads;
    private final ConcurrentQueue<Runnable> tasks;

    public ThreadPool(int threadCount) {
        threads = new Thread[threadCount];
        tasks = new ConcurrentQueue<>();
    }

    public void addTask(Runnable task) {
        tasks.push(task);
    }

    public void run() {
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Worker();
            threads[i].start();
        }
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException ignored) {
            }
        }
    }

    private class Worker extends Thread {
        @Override
        public void run() {
            while (true) {
                Runnable task = tasks.poll();
                if (task == null) break;
                task.run();
            }
        }
    }

    private static class ConcurrentQueue<T> {
        Queue<T> queue = new ArrayDeque<>();

        synchronized void push(T item) {
            queue.add(item);
        }

        synchronized T poll() {
            return queue.poll();
        }
    }
}
