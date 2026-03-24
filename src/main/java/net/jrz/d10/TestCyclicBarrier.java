package net.jrz.d10;

import lombok.extern.slf4j.Slf4j;
import net.jrz.util.Sleeper;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

@Slf4j(topic = "c.TestCyclicBarrier") // 栅栏，人满放行
public class TestCyclicBarrier {
    public static void main(String[] args) {
        test();
    }

    public static void test(){
        CyclicBarrier cb = new CyclicBarrier(2); // 个数为2时才会继续执行

        new Thread(() -> {
            log.debug("线程1开始...");

            try {
                cb.await(); // 当个数不足时，等待; 等待的线程数达到指定个数才会放行
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace(System.out);
                throw new RuntimeException(e);
            }
            log.debug("线程1继续向下运行...");
        }).start();

        new Thread(() -> {
            log.debug("线程2开始...");
            Sleeper.sleep(2); // 睡眠两秒
            try {
                cb.await(); // 此时等待的个数达到了两个，开始放行
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace(System.out);
                throw new RuntimeException(e);
            }
            log.debug("线程2继续向下运行...");
        }).start();

    }
}

