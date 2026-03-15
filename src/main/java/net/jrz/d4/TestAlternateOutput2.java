package net.jrz.d4;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

// 交替输出 abcabcabc 三个线程
// 使用ReentrantLock方法来实现
public class TestAlternateOutput2 {
    public static void main(String[] args) {
        AwaitSignal as = new AwaitSignal(5);
        Condition aWaitSet = as.newCondition();
        Condition bWaitSet = as.newCondition();
        Condition cWaitSet = as.newCondition();

        new Thread(() -> {
            as.print("a", aWaitSet, bWaitSet);
        }, "t1").start();
        new Thread(() -> {
            as.print("b", bWaitSet, cWaitSet);
        }, "t1").start();
        new Thread(() -> {
            as.print("c", cWaitSet, aWaitSet);
        }, "t1").start();

        // 开始启动
        as.start(aWaitSet);
    }
}

@Slf4j(topic = "c.AwaitSignal")
class AwaitSignal extends ReentrantLock{
    private final int loopNumber;

    public AwaitSignal(int loopNumber) {
        this.loopNumber = loopNumber;
    }

    public void start(Condition first){
        this.lock();
        try {
            log.debug("start");
            first.signal();
        } finally {
            this.unlock();
        }
    }

    public void print(String str, Condition cur, Condition next){
        for (int i = 0; i < loopNumber; ++i){
            this.lock();
            try {
                cur.await();
                System.out.print(str);
                next.signal();
            } catch (InterruptedException e) {
                e.printStackTrace(System.out);
            } finally {
                this.unlock();
            }
        }
    }
}
