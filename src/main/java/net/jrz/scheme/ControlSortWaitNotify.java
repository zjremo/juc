package net.jrz.scheme;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "c.ControlSortWaitNotify")
public class ControlSortWaitNotify {
    static final Object obj = new Object();
    static boolean t2runed = false;

    // 先t2线程打印2,然后t1线程打印1
    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            synchronized (obj){
                while (!t2runed){ //
                    try {
                        obj.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace(System.out);
                        throw new RuntimeException(e);
                    }
                }
            }
            log.debug("1");
        }, "t1");

        Thread t2 = new Thread(() -> {
            log.debug("2");
            synchronized (obj){
                t2runed = true;
                obj.notifyAll();
            }
        }, "t2");

        t1.start();
        t2.start();
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace(System.out);
            throw new RuntimeException(e);
        }
    }
}
