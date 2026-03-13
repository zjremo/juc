package net.jrz.d3;

import lombok.extern.slf4j.Slf4j;
import net.jrz.util.Sleeper;

@Slf4j(topic = "c.TestMultiLocks")
public class TestMultiLocks {
    public static void main(String[] args) {
        Room room = new Room();

        new Thread(() -> {
            log.debug("{} begin sleep", Thread.currentThread().getName());
            room.sleep();
        }, "t1").start();

        new Thread(() -> {
            log.debug("{} begin study", Thread.currentThread().getName());
            room.study();
        }, "t2").start();
    }
}

// 睡觉和学习是两个不同的功能，明明是互不影响的，但是由于对了所在的这个房间加了重量锁，导致互相阻塞影响
@Slf4j(topic = "c.TestMultiLocks.Room")
class Room{

    public void sleep(){
        synchronized (this){
            log.debug("sleeping two hours");
            Sleeper.sleep(2);
        }
    }

    public void study(){
        synchronized (this){
            log.debug("study one hour");
            Sleeper.sleep(1);
        }
    }
}