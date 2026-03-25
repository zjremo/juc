package net.jrz.d10;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j(topic = "c.TestReentrantReadWriteLock")
public class TestReentrantReadWriteLock {
    private final Map<String, String> cache = new HashMap<>();
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();

    public String get(String key){
        readLock.lock();
        try {
            String val = cache.get(key);
            log.debug("read data {}", val);
            return val;
        } finally {
            readLock.unlock();
        }
    }

    public void put(String key, String value){
        writeLock.lock();
        try {
            log.debug("write data ({}, {})", key, value);
            cache.put(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    public void demo(){
        TestReentrantReadWriteLock demo = new TestReentrantReadWriteLock();

        // 2个写线程，5个读线程
        for (int i = 0; i < 2; ++i){
            final int idx = i;

            new Thread(() -> {
                demo.put("k" + idx, "v" + idx);
            }, "Writer-" + i).start();
        }

        for (int i = 0; i < 5; ++i){
            new Thread(() -> {
                demo.get("k1");
            }, "Reader-" + i).start();
        }
    }

    public static void main(String[] args) {
        new TestReentrantReadWriteLock().demo();
    }
}


