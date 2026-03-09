package net.jrz.d0.method;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SleepTest {
    public static void main(String[] args) {
        Thread t1 = new Thread("t1") {
            @Override
            public void run() {
                try {
                    // 在哪个线程中被调用，就让哪个线程休眠
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace(System.out);
                }
            }
        };
        t1.start();
        log.debug("t1 state: {}", t1.getState());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace(System.out);
        }

        log.debug("t1 state: {}", t1.getState());
    }
}
