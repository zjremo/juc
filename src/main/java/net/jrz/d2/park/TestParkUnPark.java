package net.jrz.d2.park;

import lombok.extern.slf4j.Slf4j;
import net.jrz.util.Sleeper;

import java.util.concurrent.locks.LockSupport;

@Slf4j(topic = "c.TestParkUnpark")
public class TestParkUnPark {
    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            log.debug("start...");
            Sleeper.sleep(1);
            log.debug("park...");
            LockSupport.park();
            log.debug("resume...");
        }, "t1");
        t1.start();

        Sleeper.sleep(2);
        log.debug("unpark...");
        LockSupport.unpark(t1); // unpark可以在park之前执行，此时park会失效；在park之后执行可以起到interrupt的作用
    }
}
