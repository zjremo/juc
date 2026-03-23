package net.jrz.d9;

import lombok.extern.slf4j.Slf4j;
import net.jrz.util.Sleeper;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j(topic = "c.TestTimer")
public class TestTimer {
    // timer是单线程串行执行
    private static void method1(){
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                log.debug("task 1");
                Sleeper.sleep(2);
            }
        };

        TimerTask task2 = new TimerTask() {
            @Override
            public void run() {
                log.debug("task 2");
            }
        };

        log.debug("start ... ");
        timer.schedule(task, 1000); // 计划在1s后执行task
        timer.schedule(task2, 1000);
    }

    // schedule方法，并发执行，互不影响 对齐时间轴
    private static void method2(){
        // ScheduledThreadPool 运用的还是线程池，并且是并发执行，一个任务遇到异常不会影响另外一个任务的执行
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
        log.debug("start ... ");
        // 添加
        executor.schedule(() -> {
            log.debug("task 1 begin");
            Sleeper.sleep(2);
            log.debug("task 1 end");
        }, 1000, TimeUnit.MILLISECONDS);
        executor.schedule(() -> {
            log.debug("task 2");
        }, 1000, TimeUnit.MILLISECONDS);
    }

    // ScheduleAtFixedRate方法，按照固定时间间隔执行这个任务 串行延迟链
    public static void method3(){
        ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);
        log.debug("start...");
        pool.scheduleAtFixedRate(() -> {
            log.debug("running...");
            Sleeper.sleep(1);
        }, 1, 1, TimeUnit.SECONDS);
        /*
        *   2026-03-23 16:20:03.016 [main] DEBUG c.TestTimer- start...
            2026-03-23 16:20:04.021 [pool-1-thread-1] DEBUG c.TestTimer- running...
            2026-03-23 16:20:05.020 [pool-1-thread-1] DEBUG c.TestTimer- running...
            2026-03-23 16:20:06.021 [pool-1-thread-1] DEBUG c.TestTimer- running...
        * */
    }

    // scheduleWithFixedDelay 上一个任务结束 -> 延时 -> 下一个任务开始 下面的例子间隔都是3s
    public static void method4(){
        ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);
        log.debug("start...");
        pool.scheduleWithFixedDelay(() -> {
            log.debug("running ... ");
            Sleeper.sleep(2);
        }, 1, 1, TimeUnit.SECONDS);
        /*
        *   2026-03-23 16:23:41.378 [pool-1-thread-1] DEBUG c.TestTimer- running ...
            2026-03-23 16:23:44.380 [pool-1-thread-1] DEBUG c.TestTimer- running ...
            2026-03-23 16:23:47.381 [pool-1-thread-1] DEBUG c.TestTimer- running ...
        *
        * */
    }

    public static void main(String[] args) {
        method3();
    }
}
