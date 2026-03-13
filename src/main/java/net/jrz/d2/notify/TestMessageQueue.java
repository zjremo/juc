package net.jrz.d2.notify;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.jrz.util.Sleeper;

import java.util.Deque;
import java.util.LinkedList;

@Slf4j(topic = "c.TestMessageQueue")
public class TestMessageQueue {
    public static void main(String[] args) {
        String produce_prefix = "producer:", consumer_prefix = "consumer:";
        MessageQueue messageQueue = new MessageQueue(2);
        for (int i = 0; i < 3; ++i){
            int id = i;
            new Thread(() -> {
                messageQueue.put(new Message(id, "值" + id));
            }, produce_prefix + i).start();
        }

        new Thread(()->{
            while (true){
                Sleeper.sleep(1);
                Message message = messageQueue.take();
            }
        }, consumer_prefix + 1).start();
    }
}

// 消息队列
@Slf4j(topic = "c.MessageQueue")
class MessageQueue {
    // 消息队列集合
    private final Deque<Message> queue = new LinkedList<>();
    // 队列容量
    private int capacity;

    // 消息队列构造
    public MessageQueue(int capacity) {
        this.capacity = capacity;
    }

    // 获取消息
    public Message take() {
        // 检查队列是否为空
        synchronized (queue) {
            while (queue.isEmpty()) {
                try {
                    log.debug("队列为空，消费者线程等待");
                    queue.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.out);
                }
            }
            // 从队列头部获取消息返回, 此时需要唤醒等待线程，通知他们可以继续生产了
            Message message = queue.poll();
            log.debug("已经消费消息, {}", message);
            queue.notifyAll();
            return message;
        }

    }

    // 存入消息
    public void put(Message message){
        synchronized (queue){
            while (capacity == queue.size()){
                try {
                    log.debug("队列已满，生产者线程等待");
                    queue.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.out);
                }
            }
            // 加入到队列尾部, 并且唤醒等待的消费者线程，可以进行消费了
            queue.offer(message);
            log.debug("已经生产消息, {}", message);
            queue.notifyAll();
        }
    }
}

// 消息类
@Getter
final class Message {
    private final int id;
    private final Object value;

    public Message(int id, Object value) {
        this.id = id;
        this.value = value;
    }
}
