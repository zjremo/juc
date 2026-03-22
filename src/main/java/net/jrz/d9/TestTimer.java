package net.jrz.d9;

import lombok.extern.slf4j.Slf4j;
import net.jrz.util.Sleeper;

import java.util.Date;
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

    private static void method2(){
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

    public static void main(String[] args) {
        method2();
    }
}
