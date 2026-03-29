package net.jrz.scheme;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j(topic = "c.ControlSortPrint")
public class ControlSortPrint {
    public static void main(String[] args) {
        controlSortReentrantLock(5);
    }
    // Scheme 1: wait notify
    public static void controlSortWN(int loopNumber){
        SyncWN syncWN = new SyncWN(5, 0);

        new Thread(() -> {
            syncWN.print('a', 0, 1);
        }, "t1").start();

        new Thread(() -> {
            syncWN.print('b', 1, 2);
        }, "t2").start();

        new Thread(() -> {
            syncWN.print('c', 2, 0);
        }, "t3").start();
    }

    // Scheme 2: park unpark
    public static void controlSortParkUnpark(int loopNumber){
        SyncPark syncPark = new SyncPark(5);
        List<Thread> ts = new ArrayList<>();

        Thread t1 = new Thread(() -> {
            syncPark.print('a', ts.get(1));
        }, "t1");

        Thread t2 = new Thread(() -> {
            syncPark.print('b', ts.get(2));
        }, "t2");

        Thread t3 = new Thread(() -> {
            syncPark.print('c', ts.getFirst());
        }, "t3");

        ts.add(t1);
        ts.add(t2);
        ts.add(t3);

        ts.forEach(Thread::start);

        LockSupport.unpark(t1);
    }

    // Scheme 3: ReentrantLock
    public static void controlSortReentrantLock(int loopNumber){
        SyncReentrantLock syncReentrantLock = new SyncReentrantLock(loopNumber);
        ArrayList<Condition> conditions= IntStream.range(0, 3).mapToObj(i -> syncReentrantLock.newCondition()).collect(Collectors.toCollection(ArrayList::new));

        new Thread(() -> {
            syncReentrantLock.print('a', conditions.get(0), conditions.get(1));
        }, "t1").start();

        new Thread(() -> {
            syncReentrantLock.print('b', conditions.get(1), conditions.get(2));
        }, "t2").start();

        new Thread(() -> {
            syncReentrantLock.print('c', conditions.get(2), conditions.get(0));
        }, "t3").start();

        syncReentrantLock.start(conditions.get(0));
    }
}

class SyncWN{
    private final int loopNumber;
    private volatile int flag;

    public SyncWN(int loopNumber, int flag) {
        this.loopNumber = loopNumber;
        this.flag = flag;
    }

    public void print(char c, int needFlag, int nextFlag){
        for (int i = 0; i < loopNumber; ++i){
            synchronized (this){
                while (flag != needFlag){
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace(System.out);
                        throw new RuntimeException(e);
                    }
                }
                System.out.print(c + "");
                this.flag = nextFlag;
                this.notifyAll();
            }
        }
    }
}

class SyncPark{
    private final int loopNumber;

    public SyncPark(int loopNumber) {
        this.loopNumber = loopNumber;
    }

    public void print(char c, Thread next){
        for (int i = 0; i < loopNumber; ++i){
            LockSupport.park();
            System.out.print(c + "");
            LockSupport.unpark(next);
        }
    }
}

@Slf4j(topic = "c.SyncReentrantLock")
class SyncReentrantLock extends ReentrantLock {
    private final int loopNumber;

    public SyncReentrantLock(int loopNumber) {
        this.loopNumber = loopNumber;
    }

    public void print(char c, Condition curCondition, Condition nextCondition){
        for (int i = 0; i < loopNumber; ++i){
            this.lock();
            try {
                curCondition.await();
                System.out.print(c + "");
                nextCondition.signalAll();
            } catch (InterruptedException e) {
                e.printStackTrace(System.out);
                throw new RuntimeException(e);
            } finally {
                this.unlock();
            }
        }
    }

    public void start(Condition condition){
        this.lock();
        try {
            log.debug("start...");
            condition.signalAll();
        } finally {
            this.unlock();
        }
    }
}
