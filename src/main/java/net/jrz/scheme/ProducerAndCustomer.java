package net.jrz.scheme;

import lombok.extern.slf4j.Slf4j;
import net.jrz.util.Sleeper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j(topic = "c.ProducerAndCustomer")
public class ProducerAndCustomer {
    private static final AtomicInteger mgId = new AtomicInteger(0);

    public static void main(String[] args) {
        MessageQueue mq = new MessageQueue(2);

        // 4个生产者，一个消费者
        List<Thread> producers = new ArrayList<>(4);
        for (int i = 0; i < 4; ++i) {
            int j = i;
            producers.add(
                    new Thread(() -> {
                        mq.put(new Message(mgId.getAndIncrement(), String.format("Message %d", j)));
                    }, "producer " + i)
            );
        }

        producers.forEach(Thread::start);
        new Thread(mq::take, "customer").start();

        Sleeper.sleep(3);
        log.debug("Test is over");
    }

}

record Message(int id, Object message) {

}

@Slf4j(topic = "c.MessageQueue")
class MessageQueue {
    private final Queue<Message> queue;
    private final ReentrantLock lock;
    private final Condition fullCondition;
    private final Condition emptyCondition;
    private final int capacity;

    public MessageQueue(int capacity) {
        this.queue = new LinkedList<>();
        this.lock = new ReentrantLock();
        this.fullCondition = lock.newCondition();
        this.emptyCondition = lock.newCondition();
        this.capacity = capacity;
    }

    public void put(Message message) {
        lock.lock();
        try {
            while (queue.size() == capacity) {
                try {
                    fullCondition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.out);
                    throw new RuntimeException(e);
                }
            }
            // put message
            log.debug("put message {}", message);
            queue.offer(message);
            emptyCondition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void take() {
        lock.lock();
        try {
            while (queue.isEmpty()) {
                try {
                    emptyCondition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.out);
                    throw new RuntimeException(e);
                }
            }
            Message message = queue.poll();
            log.debug("take message {}", message);
            fullCondition.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
