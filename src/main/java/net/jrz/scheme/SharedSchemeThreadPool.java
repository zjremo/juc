package net.jrz.scheme;

import cn.hutool.core.lang.UUID;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j(topic = "c.SharedSchemeThreadPool")
public class SharedSchemeThreadPool {
    public static void main(String[] args) {
        ThreadPool threadPool = new ThreadPool(2, 1000, TimeUnit.MILLISECONDS, (queue, task) -> {
            // 1. 死等
//            queue.offer(task);
            // 2. 带超时等待
//            log.debug("{} execute {}", task, queue.offer(task, TimeUnit.MILLISECONDS, 1000));
            // 3. 调用者自动放弃
//            log.debug("放弃执行...");
            // 4. 抛出异常
//            throw new RuntimeException("任务执行失败 " + task);
            // 5. 调用者自己新开一个线程执行
            new Thread(task, UUID.randomUUID().toString(true)).start();
        });

        for (int i = 0; i < 10; ++i){
            threadPool.execute(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace(System.out);
                }
                log.debug("{}执行任务完毕 ", Thread.currentThread().getName());
            });
        }
    }
}

@FunctionalInterface
interface RejectPolicy<T> {
    void reject(BlockingQueue<T> queue, T task);
}

// 阻塞队列
@Slf4j
class BlockingQueue<T> {
    // 任务队列
    private final Queue<T> queue;
    // 锁
    private final ReentrantLock rLock;
    // 容量
    private final int capacity;
    // 条件变量
    private final Condition fullCondition;
    private final Condition emptyCondition;

    public BlockingQueue(int capacity) {
        this.capacity = capacity;
        this.queue = new LinkedList<>();
        this.rLock = new ReentrantLock();
        this.fullCondition = rLock.newCondition();
        this.emptyCondition = rLock.newCondition();
    }

    // 阻塞获取弹出任务
    public T poll() {
        rLock.lock();
        try {
            while (queue.isEmpty()) {
                try {
                    emptyCondition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.out);
                }
            }
            T t = queue.poll();
            log.debug("get obj {}", t);
            fullCondition.signal();
            return t;
        } finally {
            rLock.unlock();
        }
    }

    // 带超时时间的获取弹出任务
    public T poll(TimeUnit unit, long timeout) {
        rLock.lock();
        try {
            long timeNanos = unit.toNanos(timeout);
            while (queue.isEmpty()) {
                if (timeNanos <= 0) {
                    return null; // 此时已经超时，直接返回null
                }

                try {
                    timeNanos = emptyCondition.awaitNanos(timeNanos);
                } catch (InterruptedException e) {
                    e.printStackTrace(System.out);
                    throw new RuntimeException(e);
                }
            }
            T t = queue.poll();
            log.debug("get obj {}", t);
            fullCondition.signalAll();
            return t;
        } finally {
            rLock.unlock();
        }
    }

    // 阻塞放入任务
    public void offer(T t) {
        rLock.lock();
        try {
            while (queue.size() == capacity) {
                try {
                    fullCondition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.out);
                    throw new RuntimeException(e);
                }
            }
            queue.offer(t);
            log.debug("put obj {}", t);
            emptyCondition.signalAll();
        } finally {
            rLock.unlock();
        }
    }

    // 带超时时间的放入任务
    public boolean offer(T t, TimeUnit unit, long timeout) {
        rLock.lock();
        try {
            long timeNanos = unit.toNanos(timeout);
            while (queue.size() == capacity) {
                if (timeNanos <= 0) {
                    return false;
                }
                try {
                    timeNanos = fullCondition.awaitNanos(timeNanos);
                } catch (InterruptedException e) {
                    e.printStackTrace(System.out);
                    throw new RuntimeException(e);
                }
            }
            queue.offer(t);
            log.debug("put obj {}", t);
            emptyCondition.signalAll();
            return true;
        } finally {
            rLock.unlock();
        }
    }

    // 获取任务队列的长度
    public int size() {
        rLock.lock();
        try {
            return queue.size();
        } finally {
            rLock.unlock();
        }
    }

    // 尝试将任务放入队列
    public void tryPut(RejectPolicy<T> rejectPolicy, T task) {
        rLock.lock();
        try {
            if (queue.size() == capacity) {
                rejectPolicy.reject(this, task);
            } else {
               // 用空闲
                log.debug("tryPut obj {} successfully", task);
                queue.offer(task);
                emptyCondition.signalAll();
            }
        } finally {
            rLock.unlock();
        }
    }
}

// 线程池
@Slf4j
class ThreadPool {
    private BlockingQueue<Runnable> bq;
    private Set<Worker> workers;

    private int capacity;

    private long timeWait;
    private TimeUnit timeUnit;
    private RejectPolicy<Runnable> rejectPolicy;

    private ReentrantLock lock;

    class Worker extends Thread {
        private Runnable task;

        public Worker(Runnable task) {
            this.task = task;
        }

        @Override
        public void run() {
            while (task != null || (task = bq.poll(timeUnit, timeWait)) != null){
                try {
                    log.debug("正在执行任务 {}", task);
                    task.run();
                } catch (Exception e){
                    e.printStackTrace(System.out);
                } finally {
                    // 执行完当前的任务就将其清理
                    task = null;
                }
            }
            // 此时这个线程可以释放掉了，不需要工作
            lock.lock();
            try {
                workers.remove(this);
            } finally {
                lock.unlock();
            }
        }
    }

    public ThreadPool(int capacity, long timeWait, TimeUnit timeUnit, RejectPolicy<Runnable> rejectPolicy) {
        this.capacity = capacity;
        this.timeWait = timeWait;
        this.timeUnit = timeUnit;
        this.rejectPolicy = rejectPolicy;

        this.bq = new BlockingQueue<>(capacity);
        this.workers = new HashSet<>();
        this.lock = new ReentrantLock();
    }

    public void execute(Runnable task){
        lock.lock();
        try {
            if (workers.size() < capacity){
                // 创建线程直接分配
                Worker worker = new Worker(task);
                log.debug("新增 worker{}, add task is {}", worker, task);
                workers.add(worker);
                worker.start();
            } else {
                // 放入阻塞队列
                bq.tryPut(rejectPolicy, task);
            }
        } finally {
            lock.unlock();
        }
    }
}


