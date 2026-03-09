package net.jrz.d0.method;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class SleepInterrupted {
    public static void main(String[] args) {
        Thread t1 = new Thread("t1") {
            @Override
            public void run() {
                log.debug("enter sleep...");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    log.debug("wake up ... {}", Thread.currentThread().getName());
                    e.printStackTrace(System.out);
                }
            }
        };

        t1.start();
        try {
//            Thread.sleep(1000);
            TimeUnit.SECONDS.sleep(1); // TimeUnit类具有更好的可读性，推荐使用这个，内部其实调用了Thread.sleep()
        } catch (InterruptedException e) {
            e.printStackTrace(System.out);
        }
        log.debug("interrupt ... ");
        t1.interrupt(); // 打断t1线程的睡眠
    }

}
