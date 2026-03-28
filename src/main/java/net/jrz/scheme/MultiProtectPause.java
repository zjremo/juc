package net.jrz.scheme;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.jrz.util.Sleeper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j(topic = "c.MultiProtectPause")
public class MultiProtectPause {
    public static void main(String[] args) {
        for (int i = 0; i < 3; ++i){
            new Customer(String.format("Customer(%d)", i)).start();
        }

        Sleeper.sleep(1);
        for (int id: MailBox.getKeys()){
            new Postman(id, String.format("Content{%d}", id)).start();
        }
    }
}

@Slf4j(topic = "c.Customer")
class Customer extends Thread{
    public Customer(String name) {
        super(name);
    }

    @Override
    public void run() {
        // 收信
        MultiGuardedObject guardedObject = MailBox.createGuardedObjById();
        log.debug("开始收信 Id = {}", guardedObject.getId());
        Object obj = guardedObject.getWait(3, TimeUnit.SECONDS);
//        Object obj = guardedObject.get();
        log.debug("收到信 Id = {}, 内容为 {}", guardedObject.getId(), obj);
    }
}

@Slf4j(topic = "c.Postman")
class Postman extends Thread{
    private int id;
    private String mail;

    public Postman(int id, String mail) {
        this.id = id;
        this.mail = mail;
    }

    @Override
    public void run() {
        MultiGuardedObject guardedObject = MailBox.getGuardedObjById(id);
        log.debug("送信 id: {}, 内容: {}", id, mail);
        guardedObject.complete(mail);
    }
}

class MailBox{
    private static final AtomicInteger curId = new AtomicInteger(0);
    private static final Map<Integer, MultiGuardedObject> map = new HashMap<>();

    public static MultiGuardedObject getGuardedObjById(int id){
        return map.get(id);
    }

    public static MultiGuardedObject createGuardedObjById(){
        MultiGuardedObject guardedObject = new MultiGuardedObject(curId.getAndIncrement());
        map.put(guardedObject.getId(), guardedObject);
        return guardedObject;
    }

    public static Set<Integer> getKeys(){
        return new HashSet<>(map.keySet());
    }
}

@Slf4j(topic = "c.MultiGuardedObject")
@Getter
class MultiGuardedObject {
    private int id;

    public MultiGuardedObject(int id) {
        this.id = id;
    }

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