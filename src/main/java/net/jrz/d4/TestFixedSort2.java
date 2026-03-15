package net.jrz.d4;

import lombok.extern.slf4j.Slf4j;
import net.jrz.util.Sleeper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.locks.LockSupport;

// 固定顺序，先2后1
// park和unpark来进行解决
@Slf4j(topic = "c.TestFixedSort2")
public class TestFixedSort2 {
    static Object response = null;

    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            LockSupport.park();
            log.debug("t1 executing...");
            assert response != null;
            log.debug("t1 response is {}", response);
        }, "t1");

        t1.start();

        Sleeper.sleep(2);

        new Thread(() -> {
            log.debug("t2 executing...");
            response = new ArrayList<>(Arrays.asList(1, 2, 3));
            LockSupport.unpark(t1);
        }, "t2").start();
    }

}
