package net.jrz.d1;

import lombok.extern.slf4j.Slf4j;
import net.jrz.util.Sleeper;

@Slf4j(topic = "c.Test8Locks")
public class Test8Locks {
    public static void main(String[] args) {
        Number n1 = new Number();
        Number n2 = new Number();
        new Thread(() -> {
            log.debug("{} begin", Thread.currentThread().getName());
            n1.a();
        }, "t1").start();

        new Thread(() -> {
            log.debug("{} begin", Thread.currentThread().getName());
            n1.b();
        }, "t2").start();

        new Thread(() -> {
            log.debug("{} begin", Thread.currentThread().getName());
            n1.c();
        }, "t3").start();
    }
}

// Case 1: 3 1s 12
// Case 2: 32 1s 1
// Case 3: 23 1s 1
@Slf4j(topic = "c.Number")
class Number{
    public synchronized void a(){
        // sleep是不会释放锁的
        Sleeper.sleep(1);
        log.debug("1");
    }

    // 锁住的是Number对象
    public synchronized void b(){
        log.debug("2");
    }

    public void c(){
        log.debug("3");
    }

    // 锁住的是Number 类本身
    public static synchronized void d(){
        Sleeper.sleep(1);
        log.debug("4");
    }

    public static synchronized void e(){
        log.debug("5");
    }
}