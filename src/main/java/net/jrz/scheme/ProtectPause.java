package net.jrz.scheme;

import lombok.extern.slf4j.Slf4j;
import net.jrz.util.Sleeper;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

// 保护性暂停
@Slf4j(topic = "c.ProtectPause")
public class ProtectPause {
    private static AtomicInteger threadId = new AtomicInteger(0);

    public static void test() {
        GuardedObject guardedObject = new GuardedObject();
        Thread t1 = new Thread(guardedObject::get, threadId.getAndIncrement() + "");
        t1.start();
        Sleeper.sleep(2);
        guardedObject.complete(new User("jrz", 22));
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace(System.out);
            throw new RuntimeException(e);
        }
        log.debug("all is over");
    }

    public static void test2() {
        GuardedObject guardedObject = new GuardedObject();
        Thread t1 = new Thread(() -> {
            guardedObject.getWait(3L, TimeUnit.SECONDS);
        }, threadId.getAndIncrement() + "");
        t1.start();
        Sleeper.sleep(2);
        guardedObject.complete(new User("jrz", 22));
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace(System.out);
            throw new RuntimeException(e);
        }
        log.debug("all is over");
    }

    public static void main(String[] args) {
        test2();
    }
}

@Slf4j(topic = "c.GuardedObject")
class GuardedObject {
    private Object response;
    private final Object lock = new Object();

    public Object get() {
        synchronized (lock) {
            while (response == null) {
                try {
                    log.debug("begin wait ...");
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.out);
                    throw new RuntimeException(e);
                }
            }
            return response;
        }
    }

    public void complete(Object response) {
        synchronized (lock) {
            // 条件满足，通知等待线程
            this.response = response;
            log.debug("add response : {}", response);
            lock.notifyAll();
        }
    }

    // 超时等待版本
    public Object getWait(long timeOut, TimeUnit unit) {
        synchronized (lock) {
            long timeToMillis = unit.toMillis(timeOut);
            long begin = System.currentTimeMillis();
            long timePassed = 0;
            log.debug("开始计时...");
            while (response == null) {
                long waitTime = timeToMillis - timePassed;
                if (waitTime <= 0) {
                    log.debug("超时等待时间到...");
                    break;
                }
                try {
                    lock.wait(waitTime); // 要防止虚假唤醒，虚假唤醒是指response的要求没有达到，结果唤醒了线程
                } catch (InterruptedException e) {
                    e.printStackTrace(System.out);
                    throw new RuntimeException(e);
                }
                timePassed = System.currentTimeMillis() - begin;
                log.debug("timePassed: {}, object is null {}", timePassed, response == null);
            }
            return response;
        }
    }
}
