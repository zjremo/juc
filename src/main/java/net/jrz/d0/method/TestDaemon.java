package net.jrz.d0.method;

import lombok.extern.slf4j.Slf4j;
import net.jrz.util.Sleeper;

@Slf4j(topic = "c.TestDaemon")
public class TestDaemon {
    public static void main(String[] args) {
        log.debug("开始运行...");
        Thread t1 = new Thread(() -> {
            log.debug("开始运行...");
            Sleeper.sleep(0.5);
            log.debug("运行结束...");
        }, "daemon");

        t1.setDaemon(true);
        t1.start();

        Sleeper.sleep(1);
        log.debug("运行结束...");
    }
}
