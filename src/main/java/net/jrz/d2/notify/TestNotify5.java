package net.jrz.d2.notify;

import lombok.extern.slf4j.Slf4j;
import net.jrz.util.Sleeper;

// 完整版实现，此时不再有线程阻塞情况，任何情况都可以执行
@Slf4j(topic = "c.TestNotify5")
public class TestNotify5 {
    private static Object room = new Object();
    private static boolean hasCigarette = false;
    private static boolean hasTakeout = false;

    public static void main(String[] args) {
        new Thread(() -> {
            synchronized (room){
                // 用while来取代if else， 避免只判断一次
                while (!hasCigarette) {
                    log.debug("没烟，先歇会！");
                    try {
                        room.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace(System.out);
                    }
                }

                // 此时必定获得烟了
                log.debug("此时已经获取了烟，{}开始干活", Thread.currentThread().getName());
            }
        }, "小南").start();

        for (int i = 0; i < 5; ++i) {
            new Thread(() -> {
                synchronized (room) {
                    log.debug("外卖送到没？[{}]", hasTakeout);
                    if (!hasTakeout) {
                        log.debug("没外卖，先歇会！");
                        try {
                            room.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace(System.out);
                        }
                    }
                    log.debug("外卖送到没？[{}]", hasTakeout);
                    if (hasTakeout) {
                        log.debug("可以开始干活了");
                    } else {
                        log.debug("没干成活...");
                    }
                }
            }).start();
        }

        Sleeper.sleep(2);
        synchronized (room) {
            hasCigarette = true;
            hasTakeout = true;
            log.debug("begin notify all Threads");
            room.notifyAll();
        }
    }
}
