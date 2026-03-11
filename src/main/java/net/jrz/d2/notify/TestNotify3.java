package net.jrz.d2.notify;

import lombok.extern.slf4j.Slf4j;
import net.jrz.util.Sleeper;

// 使用notify时此时只会随机唤醒一个wait的线程
@Slf4j(topic = "c.TestNotify3")
public class TestNotify3 {
    private static Object room = new Object();
    private static boolean hasCigarette = false;
    private static boolean hasTakeout = false;

    public static void main(String[] args) {
        new Thread(() -> {
            synchronized (room) {
                log.debug("有烟没？[{}]", hasCigarette);
                if (!hasCigarette) {
                    log.debug("没烟，先歇会！");
                    try {
                        room.wait(2000); // 超时自动唤醒
                    } catch (InterruptedException e) {
                        e.printStackTrace(System.out);
                    }
                }
                log.debug("有烟没？[{}]", hasCigarette);
                if (hasCigarette) {
                    log.debug("可以开始干活了");
                } else {
                    log.debug("没干成活");
                }
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
            hasTakeout = true;
            log.debug("外卖到了，开始唤醒Thread");
            room.notify(); // 只会随机唤醒一个线程
        }
    }
}
