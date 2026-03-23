package net.jrz.d10;

import lombok.extern.slf4j.Slf4j;
import net.jrz.util.Sleeper;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

@Slf4j(topic = "c.TestAqs")
public class TestAqs {
    public static void main(String[] args) {
        MyLock lock = new MyLock();

        new Thread(() -> {
            lock.lock();
            try {
                log.debug("locking...");
//                lock.lock();
                Sleeper.sleep(1);
            } finally {
                log.debug("unlocking ... ");
                lock.unlock();
            }
        }, "t1").start();

//        new Thread(() -> {
//            lock.lock();
//            try {
//                log.debug("locking...");
//            } finally {
//                log.debug("unlocking...");
//                lock.unlock();
//            }
//        }, "t2").start();
    }
}

final class MySync extends AbstractQueuedSynchronizer {
    @Override
    protected boolean tryAcquire(int acquires) {
        if (acquires == 1) {
            if (compareAndSetState(0, 1)) { // cas操作修改state为加锁状态
                // 当前线程设置为锁的持有者
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean tryRelease(int acquires) {
        if (acquires == 1) {
            if (getState() == 0) {
                throw new IllegalMonitorStateException();
            }
            // 设置为没有线程占有锁
            setExclusiveOwnerThread(null); // 这里放在volatile字段的前面享受写屏障，对其他线程可见
            // 设置状态为无锁
            setState(0);
            return true;
        }
        return false;
    }

    @Override // 是否持有独占锁
    protected boolean isHeldExclusively() {
        return getState() == 1;
    }

    protected Condition newCondition() {
        return new ConditionObject();
    }
}

class MyLock implements Lock {
    static MySync sync = new MySync();

    @Override // 条件变量
    public Condition newCondition() {
        return sync.newCondition();
    }

    @Override // 可打断加锁
    public void lockInterruptibly() throws InterruptedException {
        sync.acquireInterruptibly(1);
    }

    @Override // 加锁，不成功进入等待队列
    public void lock() {
        sync.acquire(1);
    }

    @Override // 尝试加锁 带超时
    public boolean tryLock(long time, TimeUnit timeUnit) throws InterruptedException {
        return sync.tryAcquireNanos(1, timeUnit.toNanos(time));
    }

    @Override // 解锁
    public void unlock() {
        sync.release(1);
    }

    @Override // 尝试加锁 一次
    public boolean tryLock() {
        return sync.tryAcquire(1);
    }
}
