package net.jrz.d3;

import lombok.extern.slf4j.Slf4j;
import net.jrz.util.Sleeper;

@Slf4j(topic = "c.TestMultiLocks2")
public class TestMultiLocks2 {
    public static void main(String[] args) {
        Room2 room = new Room2();

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

@Slf4j(topic = "c.TestMultiLocks.Room2")
class Room2{
    private final Object studyRoom = new Object();
    private final Object sleepRoom = new Object();

    public void sleep(){
        synchronized (this.studyRoom){
            log.debug("sleeping two hours");
            Sleeper.sleep(2);
        }
    }

    public void study(){
        synchronized (this.sleepRoom){
            log.debug("study one hour");
            Sleeper.sleep(1);
        }
    }
}