package net.jrz.d2.notify;

import lombok.extern.slf4j.Slf4j;
import net.jrz.util.Sleeper;

@Slf4j(topic = "c.TestNotify2")
public class TestNotify2 {
    private static Object room = new Object();
    private static boolean hasCigarette = false;
    private static boolean hasTakeOut = false;

    // 使用wait和notify机制，解决了其他干活的线程阻塞的问题，但是如果其他干活的线程也具有运行条件的时候又会有问题
    public static void main(String[] args) {
        new Thread(() -> {
            synchronized (room) {
                log.debug("有烟没？[{}]", hasCigarette);
                if (!hasCigarette) {
                    log.debug("没烟，先歇会！");
                    try {
                        room.wait(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace(System.out);
                    }
                }
                log.debug("有烟没？[{}]", hasCigarette);
                if (hasCigarette) {
                    log.debug("可以开始干活了");
                }
            }
        }, "小南").start();
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                synchronized (room) {
                    log.debug("可以开始干活了");
                }
            }, "其它人").start();
        }
        Sleeper.sleep(1);
        new Thread(() -> {
            synchronized (room){
                hasCigarette = true;
                room.notify(); // 唤醒等待线程
                log.debug("begin notify the wait Thread");
            }
        }, "送烟的").start();
    }
}
