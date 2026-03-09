package net.jrz.d0;

import lombok.extern.slf4j.Slf4j;
import net.jrz.util.Sleeper;

@Slf4j(topic = "c.TestMakeTea")
public class TestMakeTea {
    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            log.debug("洗水壶");
            Sleeper.sleep(1);
            log.debug("烧开水");
            Sleeper.sleep(15);
        }, "小明");

        Thread t2 = new Thread(() -> {
            log.debug("洗茶壶");
            Sleeper.sleep(1);
            log.debug("洗茶杯");
            Sleeper.sleep(2);
            log.debug("拿茶叶");
            Sleeper.sleep(1);
            // 泡茶需要等线程1完成烧水之后再开始
            try {
                t1.join();
                // 开始烧水
                log.debug("泡茶");
            } catch (InterruptedException e) {
                e.printStackTrace(System.out);
            }
        }, "小红");
        t1.start();
        t2.start();
    }
}
