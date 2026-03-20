package net.jrz.d9;

import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

// 拒绝策略 函数式接口
@FunctionalInterface
interface RejectPolicy<T> { // 拒绝策略
    void reject(BlockingQueue<T> queue, T task); // BiFunction<T, K, V>
}

// 阻塞队列 两个等候室 利用ReentrantLock来实现
@Slf4j(topic = "c.BlockingQueue")
class BlockingQueue<T> {
    // 1. task Queue
    private Queue<T> queue = new LinkedList<>();
    // 2. lock
    private ReentrantLock lock = new ReentrantLock();
    // 3. 生产者等候室
    private Condition fullWaitSet = lock.newCondition(); // 队列满了之后就要等候，为生产者分配一个休息室
    // 4. 消费者等候室
    private Condition emptyWaitSet = lock.newCondition(); // 队列空了之后要等候，为消费者分配一个休息室
    // 5. 容量
    private int capacity;

    public BlockingQueue(int capacity) {
        this.capacity = capacity;
    }

    // 超时阻塞获取
    public T poll(long timeout, TimeUnit unit) {
        lock.lock();
        try {
            // 将timeout统一转换为 纳秒
            long nanos = unit.toNanos(timeout);
            while (queue.isEmpty()) {
                try {
                    if (nanos <= 0)
                        return null;
                    nanos = emptyWaitSet.awaitNanos(nanos); // 可能存在虚假唤醒，此时需要获取到底还要等待多少时间
                } catch (InterruptedException e) {
                    e.printStackTrace(System.out);
                }
            }
            T t = queue.poll();
            fullWaitSet.signal();
            return t;
        } finally {
            lock.unlock();
        }
    }

    // 阻塞获取
    public T poll() {
        lock.lock();
        try {
            while (queue.isEmpty()) {
                try {
                    emptyWaitSet.await();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.out);
                }
            }
            T t = queue.poll();
            fullWaitSet.signal();
            return t;
        } finally {
            lock.unlock();
        }
    }

    // 阻塞添加
    public void put(T task) {
        lock.lock();
        try {
            while (queue.size() == capacity) {
                try {
                    log.debug("等待加入任务队列, {} ... ", task);
                    fullWaitSet.await();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.out);
                }
            }
            log.debug("加入该任务队列 {}", task);
            queue.offer(task);
            emptyWaitSet.signal();
        } finally {
            lock.unlock();
        }
    }

    // 带超时时间的阻塞添加
    public boolean offer(T task, long timeout, TimeUnit unit) {
        lock.lock();
        try {
            long nanos = unit.toNanos(timeout);
            while (queue.size() == capacity) {
                try {
                    if (nanos <= 0) {
                        return false;
                    }
                    log.debug("等待加入任务队列, {}...", task);
                    nanos = fullWaitSet.awaitNanos(nanos);
                } catch (InterruptedException e) {
                    e.printStackTrace(System.out);
                }
            }
            log.debug("加入任务队列 {}", task);
            queue.offer(task);
            emptyWaitSet.signal();
            return true;
        } finally {
            lock.unlock();
        }
    }

    // 获取任务队列的长度
    public int size() {
        lock.lock();
        try {
            return queue.size();
        } finally {
            lock.unlock();
        }
    }

    // 尝试将任务放入队列
    public void tryPut(RejectPolicy<T> rejectPolicy, T task) {
        lock.lock();
        try {
            // 判断队列是否满
            if (queue.size() == capacity) {
                rejectPolicy.reject(this, task);
            } else {
                // 有空闲
                log.debug("加入任务队列成功 {}", task);
                queue.offer(task);
                emptyWaitSet.signal();
            }
        } finally {
            lock.unlock();
        }
    }
}

@Slf4j(topic = "c.ThreadPool")
class ThreadPool {
    // 1. 阻塞队列
    private BlockingQueue<Runnable> queue; // 直接存储Runnable对象，然后就可以直接拿取运行
    // 2. workers -> 线程池
    private final Set<Thread> workers = new HashSet<>();
    // 3. poolSize -> 线程池的容量 多少个线程
    private int poolSize;
    // 4. 超时等待时间
    private long timeWait;
    private TimeUnit timeUnit;
    private RejectPolicy<Runnable> rejectPolicy;

    class Worker extends Thread {
        private Runnable task;

        public Worker(Runnable task) {
            this.task = task;
        }

        @Override
        public void run() {
            // task不为空直接运行; task为空去队列中取
            while (task != null || (task = queue.poll(timeWait, timeUnit)) != null) {
                try {
                    log.debug("正在执行任务 {}", task);
                    task.run();
                } catch (Exception e) {
                    e.printStackTrace(System.out);
                } finally {
                    // 执行完任务就进行删除
                    task = null;
                }
            }
            // 此时这个线程可以释放掉了，不需要工作了
            synchronized (ThreadPool.this.workers) {
                log.debug("今日无事可做，从线程池中释放 {}", this);
                workers.remove(this);
            }
        }
    }

    public ThreadPool(int poolSize, long timeWait, TimeUnit timeUnit, RejectPolicy<Runnable> rejectPolicy) {
        this.poolSize = poolSize;
        this.timeWait = timeWait;
        this.timeUnit = timeUnit;
        this.rejectPolicy = rejectPolicy;
        this.queue = new BlockingQueue<>(poolSize);
    }

    public void execute(Runnable task) {
        synchronized (this.workers) {
            if (workers.size() < poolSize) {// 线程池中线程数量充足，直接创建一个线程专门干这个事
                Worker worker = new Worker(task);
                log.debug("新增worker {}, executing task is {}", worker.getName(), task);
                workers.add(worker);
                worker.start();
            } else { // 线程数紧张，需要将任务推进阻塞队列里面等待
                // 此时需要等待线程来完成，放入队列中来等待线程池中的线程完成自身任务之后来取了运行
                queue.tryPut(rejectPolicy, task);
            }
        }

    }
}

@Slf4j(topic = "c.TestQueueThreadPool")
public class TestQueueThreadPool {
    public static void main(String[] args) {
        ThreadPool threadPool = new ThreadPool(2, 1000, TimeUnit.MILLISECONDS, (queue, task) -> {
            // 1. 死等
//            queue.put(task);
            // 2. 带超时等待
            log.debug("task({}) execute {}", task, queue.offer(task, 10, TimeUnit.MILLISECONDS));
            // 3. 调用者放弃任务执行
//            log.debug("放弃执行 ...");
            // 4. 调用者抛出异常
//            throw new RuntimeException("任务执行失败" + task);
            // 5. 调用者自己执行 这里为了更好打印显示效果，重新开一个线程
//            new Thread(task, UUID.randomUUID().toString().replace("-", "")).start();
        });

        for (int i = 0; i < 10; ++i) {
            int j = i;
            threadPool.execute(() -> {
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace(System.out);
                }
                log.debug("执行任务完毕 {}", j);
            });
        }
    }
}
