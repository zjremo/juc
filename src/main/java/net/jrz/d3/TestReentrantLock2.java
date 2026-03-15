package net.jrz.d3;

import lombok.extern.slf4j.Slf4j;
import net.jrz.util.Sleeper;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j(topic = "c.TestReentrantLock2")
public class TestReentrantLock2 {
    static ReentrantLock lock = new ReentrantLock();
    static Condition waitCigaretteQueue = lock.newCondition();
    static Condition waitBreakfastQueue = lock.newCondition();
    static volatile boolean hasCigarette = false;
    static volatile boolean hasBreakfast = false;

    public static void main(String[] args) {
        new Thread(() -> {
            lock.lock();
            try {
                while (!hasCigarette){
                    // 去等烟休息室
                    try {
                        waitCigaretteQueue.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace(System.out);
                    }
                }
                log.debug("等到了它的烟");
            } finally {
                lock.unlock();
            }
        }, "t1").start();

        new Thread(() -> {
            lock.lock();
            try {
                while (!hasBreakfast){
                    try {
                        waitBreakfastQueue.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace(System.out);
                    }
                }
            } finally {
                lock.unlock();
            }
            log.debug("等到了早餐");
        } , "t2").start();

        Sleeper.sleep(1);
        sendCigarette();
        Sleeper.sleep(1);
        sendBreakfast();
    }

    private static void sendCigarette(){
        lock.lock();
        try {
            log.debug("成功给烟");
            hasCigarette = true;
            waitCigaretteQueue.signal();
        } finally {
            lock.unlock();
        }
    }

    private static void sendBreakfast(){
        lock.lock();
        try {
            log.debug("成功给早餐");
            hasBreakfast = true;
            waitBreakfastQueue.signal();
        } finally {
            lock.unlock();
        }
    }
}


