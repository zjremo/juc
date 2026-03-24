package net.jrz.d10;

import lombok.extern.slf4j.Slf4j;
import net.jrz.util.Sleeper;

import java.util.concurrent.Semaphore;

@Slf4j(topic = "c.TestSemaphore")
public class TestSemaphore {
    public static void main(String[] args) {
        // 1. 创建semaphore 对象
        Semaphore semaphore = new Semaphore(3);

        // 2. 10个线程同时运行
        for (int i = 0; i < 10; ++i){
            new Thread(() -> {
                // 3. 获取许可
                try {
                    semaphore.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.out);
                }

                try {
                    log.debug("running ... ");
                    Sleeper.sleep(1);
                    log.debug("end ...");
                } finally {
                    // 4. 释放许可
                    semaphore.release();
                }
            }).start();
        }
    }
}
