package net.jrz.d10;

import lombok.extern.slf4j.Slf4j;
import net.jrz.util.Sleeper;

import java.util.concurrent.locks.StampedLock;

@Slf4j(topic = "c.TestStampedLock")
public class TestStampedLock {
    private int data;
    private final StampedLock lock = new StampedLock();

    public TestStampedLock(int data) {
        this.data = data;
    }

    public int read(int readTime){
        long stamp = lock.tryOptimisticRead();
        log.debug("optimistic read locking...{}", stamp);
        Sleeper.sleep(readTime);
        if (lock.validate(stamp)){ // 校验戳
            log.debug("read finish...{}, data:{}", stamp, data);
            return data;
        }
        // 锁升级 - 读锁
        log.debug("updating to read lock... {}", stamp);
        try {
            stamp = lock.readLock();
            log.debug("read lock {}", stamp);
            Sleeper.sleep(readTime);
            log.debug("read finish...{}, data: {}", stamp, data);
            return data;
        } finally {
            log.debug("read unlock {}", stamp);
            lock.unlockRead(stamp);
        }
    }

    public void write(int newData){
        long stamp = lock.writeLock();
        log.debug("write lock {}", stamp);
        try {
            Sleeper.sleep(2);
            this.data = newData;
        } finally {
            log.debug("write unlock {}", stamp);
            lock.unlockWrite(stamp);
        }
    }

    public static void testRead(){
        TestStampedLock sLock = new TestStampedLock(1);
        new Thread(() -> {
            sLock.read(1);
        }, "t1").start();
        Sleeper.sleep(0.5);
        new Thread(() -> {
            sLock.read(0);
        }, "t2").start();
    }

    public static void testWrite(){
        TestStampedLock sLock = new TestStampedLock(1);
        new Thread(() -> {
            sLock.read(1);
        }, "t1").start();
        Sleeper.sleep(0.5);
        new Thread(() -> {
            sLock.write(100);
        }, "t2").start();
    }

    public static void main(String[] args) {
        testWrite();
    }
}
