package net.jrz.d2;

import lombok.extern.slf4j.Slf4j;
import org.openjdk.jol.info.ClassLayout;

import java.util.concurrent.TimeUnit;

// Java21中偏向锁已经完全被移除掉了
@Slf4j(topic = "c.TesetBiasedLock")
public class TestBiasedLock {
    public static void main(String[] args) throws InterruptedException {
        Dog d = new Dog();
        log.debug(ClassLayout.parseInstance(d).toPrintable());
        synchronized (d){// 偏向锁已经被移除，此时直接使用最高效的轻量级锁
            log.debug(ClassLayout.parseInstance(d).toPrintable());
        }

        TimeUnit.MILLISECONDS.sleep(4000);
        log.debug(ClassLayout.parseInstance(new Dog()).toPrintable());
    }
}

class Dog{
}