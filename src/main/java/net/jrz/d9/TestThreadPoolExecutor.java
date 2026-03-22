package net.jrz.d9;

import lombok.extern.slf4j.Slf4j;
import net.jrz.util.Sleeper;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j(topic = "c.TestThreadPoolExecutor")
public class TestThreadPoolExecutor {
    public static void main(String[] args) {
        try {
            test2();
        } catch (InterruptedException e) {
            e.printStackTrace(System.out);
            throw new RuntimeException(e);
        }
    }

    public static void test(){
        ExecutorService threadPool = Executors.newFixedThreadPool(2, new ThreadFactory() {
            private AtomicInteger t = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "myPool_" + t.getAndIncrement());
            }
        });

        threadPool.execute(() -> {
            log.debug("1");
        });

        threadPool.execute(() -> {
            log.debug("2");
        });

        threadPool.execute(() -> {
            log.debug("3");
        });

        threadPool.shutdown();
    }

    public static void test2() throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(3);

        List<Future<String>> futures = pool.invokeAll(Arrays.asList(
                () -> {
                    log.debug("begin");
                    Sleeper.sleep(1);
                    return "1";
                },
                () -> {
                    log.debug("begin");
                    Sleeper.sleep(0.5);
                    return "2";
                },
                () -> {
                    log.debug("begin");
                    Sleeper.sleep(2);
                    return "3";
                }
        ));

        System.out.print("res is: ");
        futures.forEach(v -> {
            try {
                System.out.print(v.get() + " ");
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace(System.out);
                throw new RuntimeException(e);
            }
        });

        pool.shutdown();
    }
}
