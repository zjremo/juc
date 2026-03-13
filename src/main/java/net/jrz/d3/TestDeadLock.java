package net.jrz.d3;

import lombok.extern.slf4j.Slf4j;
import net.jrz.util.Sleeper;

// 检测死锁 jps可以定位进程id，然后用jstack <pId>定位死锁。
//$ jps
//17012 Main
//20380 Jps
//20300 Launcher
//20302 TestDeadLock
//$ jstack 20302
@Slf4j(topic = "c.TestDeadLock")
public class TestDeadLock {
    public static void main(String[] args) {
        Object a = new Object(), b = new Object();
        Thread t1 = new Thread(() -> {
            synchronized (a){
                log.debug("lock a");
                Sleeper.sleep(1);
                synchronized (b){
                    log.debug("lock b");
                    log.debug("操作...");
                }
            }
        });

        Thread t2 = new Thread(() -> {
            synchronized (b){
                log.debug("lock b");
                Sleeper.sleep(0.5);
                synchronized (a){
                    log.debug("lock a");
                    log.debug("操作...");
                }
            }
        });

        t1.start();
        t2.start();
    }
}
