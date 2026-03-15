package net.jrz.d3;

import lombok.extern.slf4j.Slf4j;
import net.jrz.util.Sleeper;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j(topic = "c.TestReentrantLock")
// 可重入 可打断 锁超时 公平锁(降低并发度，一般不用)
public class TestReentrantLock {
    private static final ReentrantLock lock = new ReentrantLock();

    public static void main(String[] args) {
        new TestReentrantLock().test3();
    }

    public void method1() {
        lock.lock();
        try {
            log.debug("execute method1");
            method2();
        } finally {
            lock.unlock();
        }
    }

    public void method2() {
        lock.lock();
        try {
            log.debug("execute method2");
            method3();
        } finally {
            lock.unlock();
        }
    }

    public void method3() {
        lock.lock();
        try {
            log.debug("execute method3");
        } finally {
            lock.unlock();
        }
    }

    // 可重入测试
    public void test1() {
        method1();
    }

    // 可打断测试
    public void test2(){
        Thread t1 = new Thread(() -> {
            log.debug("启动 ... ");
            try {
                lock.lockInterruptibly(); // 如果没有这么设置，便是不可打断模式
            } catch (InterruptedException e) {
                e.printStackTrace(System.out);
                log.debug("等待过程中被打断");
                return;
            }
            try {
                log.debug("获得了锁");
            } finally {
                lock.unlock();
            }
        }, "t1");

        lock.lock();
        log.debug("获得了锁");
        t1.start();
        try {
            Sleeper.sleep(1);
            t1.interrupt();
            log.debug("执行打断");
        } finally {
            lock.unlock();
        }
    }

    // 锁超时
    public void test3(){
        Thread t1  = new Thread(() -> {
            log.debug("启动...");
//            if (!lock.tryLock()){
            try {
                if (!lock.tryLock(1, TimeUnit.SECONDS)){
                    log.debug("获取立刻失败，返回");
                    return;
                }
            } catch (InterruptedException e) {
                e.printStackTrace(System.out);
            }
            try {
                log.debug("获得了锁");
            } finally {
                lock.unlock();
            }
        }, "t1");

        lock.lock();
        t1.start();
        try {
            Sleeper.sleep(2);
        } finally {
            lock.unlock();
        }
    }
}


