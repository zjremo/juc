package net.jrz.d2;

import lombok.extern.slf4j.Slf4j;
import net.jrz.util.Sleeper;

@Slf4j(topic = "c.TestWait")
public class TestWait {
    private static Object obj = null;
    public static void main(String[] args) {
        obj = new Object();

        new Thread(() -> {
            synchronized (obj){
                try {
                    obj.wait();
                    log.debug("begin wait first");
                } catch (InterruptedException e) {
                    e.printStackTrace(System.out);
                }
            }
        }, "t1").start();

        new Thread(() -> {
            synchronized (obj){
                try {
                    obj.wait();
                    log.debug("begin wait second");
                } catch (InterruptedException e) {
                    e.printStackTrace(System.out);
                }
            }
        }, "t2").start();

        Sleeper.sleep(2); // 主线程休息两秒
        log.debug("begin notify the last thread");
        synchronized (obj){
//            obj.notify(); // 唤醒上一个线程
            obj.notifyAll(); // 唤醒所有正在等待的线程
        }
    }
}
