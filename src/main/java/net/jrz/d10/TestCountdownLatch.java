package net.jrz.d10;

import lombok.extern.slf4j.Slf4j;
import net.jrz.util.Sleeper;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

// 主要与线程池配合使用，让线程池执行完所有任务再执行主线程
// 如果要获取每个线程的执行结果的话，还是使用future更好
@Slf4j(topic = "c.TestCountdownLatch")
public class TestCountdownLatch {
    public static void main(String[] args) {
        test3();
    }

    public static void test() {
        CountDownLatch latch = new CountDownLatch(3);

        new Thread(() -> {
            log.debug("begin ... ");
            Sleeper.sleep(1);
            latch.countDown();
            log.debug("end ... {}", latch.getCount());
        }).start();

        new Thread(() -> {
            log.debug("begin ... ");
            Sleeper.sleep(2);
            latch.countDown();
            log.debug("end ... {}", latch.getCount());
        }).start();

        new Thread(() -> {
            log.debug("begin...");
            Sleeper.sleep(1.5);
            latch.countDown();
            log.debug("end... {}", latch.getCount());
        }).start();

        log.debug("waiting ... ");
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace(System.out);
            throw new RuntimeException(e);
        }
        log.debug("wait end ... ");
    }

    public static void test2() {
        CountDownLatch latch = new CountDownLatch(3);
        ExecutorService service = Executors.newFixedThreadPool(4);
        service.submit(() -> {
            log.debug("begin ... ");
            Sleeper.sleep(1);
            latch.countDown();
            log.debug("end ...{}", latch.getCount());
        });
        service.submit(() -> {
            log.debug("begin ... ");
            Sleeper.sleep(1.5);
            latch.countDown();
            log.debug("end ... {}", latch.getCount());
        });
        service.submit(() -> {
            log.debug("begin ... ");
            Sleeper.sleep(2);
            latch.countDown();
            log.debug("end ... {}", latch.getCount());
        });
        service.submit(() -> {
            try {
                log.debug("waiting...");
                latch.await();
                log.debug("wait end...");
            } catch (InterruptedException e) {
                e.printStackTrace(System.out);
            }
        });

        service.shutdown();
        try {
            service.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace(System.out);
            throw new RuntimeException(e);
        }
    }

    // 应用之同步等待多线程准备完毕
    public static void test3(){
        AtomicInteger id = new AtomicInteger(0);

        ExecutorService service = Executors.newFixedThreadPool(10, (r) -> new Thread(r, "t" + id.getAndIncrement()));
        CountDownLatch latch = new CountDownLatch(4);
        String[] all = new String[10];
        Random r = new Random(47);

        for (int i = 0; i < 10; ++i){
            int x = i;
            service.submit(() -> {
                for (int j = 0; j <= 100; ++j){
                    try {
                        Thread.sleep(r.nextInt(100));
                    } catch (InterruptedException e) {
                        e.printStackTrace(System.out);
                    }
                    // 重新设置进度
                    all[x] = Thread.currentThread().getName() + "(" + (j + "%") + ")";
                    System.out.println("\r" + Arrays.toString(all));
                }
                latch.countDown();
            });
        }
        try {
            latch.await(); // 等待线程池中的线程都把任务执行完后才能继续运行
            System.out.println("\n 游戏开始...");
        } catch (InterruptedException e) {
            e.printStackTrace(System.out);
        }

        service.shutdown();
        try {
            service.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace(System.out);
        }
    }
}
