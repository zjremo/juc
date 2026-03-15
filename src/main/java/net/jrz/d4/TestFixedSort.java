package net.jrz.d4;

import lombok.extern.slf4j.Slf4j;
import net.jrz.util.Sleeper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

// 固定顺序，先2后1
// 保护性暂停模式
@Slf4j(topic = "c.TestFixedSort")
public class TestFixedSort {
    static Object lock = new Object(); // 对象锁
    static  Object response = null; // 标记
    public static void main(String[] args) {
        new Thread(() -> {
            synchronized (lock){
                // t2线程没有执行，response中始终为null
                while (Objects.isNull(response)){
                    try {
                        // 一直等待
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace(System.out);
                    }
                }
                log.debug("t1 executing ... ");
                log.debug("t1 object is {}", response);
            }
        }, "t1").start();

        Sleeper.sleep(2);

        new Thread(() -> {
            synchronized (lock){
                log.debug("t2 executing ... ");
                response = new ArrayList<>(Arrays.asList(1, 2, 3));
                lock.notifyAll();
            }
        }, "t2").start();
    }

}
