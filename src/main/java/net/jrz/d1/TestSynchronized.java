package net.jrz.d1;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "c.TestSynchronized")
public class TestSynchronized {
    private static int count = 0;
    private static final Object lock = new Object();

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 5000; ++i)
                synchronized (lock){
                    ++count;
                }
        }, "t1");

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 5000; ++i){
                synchronized (lock){
                    --count;
                }
            }
        }, "t2");

        t1.start();
        t2.start();
        t1.join();
        t2.join();
        log.debug("{}", count);
    }
}
