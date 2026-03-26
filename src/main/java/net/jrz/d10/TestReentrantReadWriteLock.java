package net.jrz.d10;

import lombok.extern.slf4j.Slf4j;
import net.jrz.util.Sleeper;

import java.util.concurrent.locks.ReentrantReadWriteLock;

// 读写锁
@Slf4j(topic = "c.TestReentrantReadWriteLock")
public class TestReentrantReadWriteLock {
    private Object data;
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();

    public Object read(){
        log.debug("get readLock...");
        readLock.lock();
        try {
            log.debug("read...");
            Sleeper.sleep(1);
            return data;
        } finally {
            log.debug("free readLock...");
            readLock.unlock();
        }
    }

    public void write(){
        log.debug("get writeLock...");
        writeLock.lock();
        try {
            log.debug("write...");
            Sleeper.sleep(1);
        } finally {
            log.debug("free writeLock...");
            writeLock.unlock();
        }
    }

    public static void main(String[] args) {
        TestReentrantReadWriteLock rwLock = new TestReentrantReadWriteLock();
        new Thread(rwLock::read, "t1").start();
        new Thread(rwLock::write, "t2").start();
    }

}


