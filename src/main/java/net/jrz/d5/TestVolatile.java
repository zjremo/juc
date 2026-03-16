package net.jrz.d5;

import lombok.extern.slf4j.Slf4j;
import net.jrz.util.Sleeper;

@Slf4j(topic = "c.TestVolatile")
public class TestVolatile {
    static boolean run = true;
    //    volatile static boolean run = true; // volatile还可以禁用**指令重排序**

    final static Object lock = new Object();

    public static void main(String[] args) {
        test3();
    }

    // Test Version1: 此时退不出循环
    public static void test() {
        Thread t = new Thread(() -> {
            while (run) {
                // ...
            }
        }, "t1");
        t.start();
        Sleeper.sleep(1);
        run = false; // 线程t不会如预想地停下来, 此时run每次都是从t线程的缓存中读取的
        try {
            t.join();
            log.debug("execute over...");
        } catch (InterruptedException e) {
            e.printStackTrace(System.out);
            throw new RuntimeException(e);
        }

    }

    // Test version2: 此时利用同步块加锁 加锁会保证代码块的原子性，还会保证代码块内变量的可见性
    public static void test2(){
        Thread t1 = new Thread(() -> {
            while (true){
                // 此时加锁读取
                synchronized (lock){
                    if (!run){
                        break;
                    }
                }
            }
        }, "t1");
        t1.start();

        Sleeper.sleep(1);
        log.debug("停止 t");
        synchronized (lock){
            run = false;
        }
    }

    // Test version3: 利用println中的锁来被迫更新内存，获取最新的主存结果
    public static void test3(){
        Thread t1 = new Thread(() -> {
            while (run){
                System.out.println();
            }
        }, "t1");
        t1.start();

        Sleeper.sleep(1);
        log.debug("停止 t");
        run = false;
    }

}
