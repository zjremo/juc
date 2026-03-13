package net.jrz.d3;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.jrz.util.Sleeper;

@Slf4j(topic = "c.TestPhilosopher")
public class TestPhilosopher {
    public static void main(String[] args) {
        Chopstick c1 = new Chopstick("1");
        Chopstick c2 = new Chopstick("2");
        Chopstick c3 = new Chopstick("3");
        Chopstick c4 = new Chopstick("4");
        Chopstick c5 = new Chopstick("5");
        new Philosopher("苏格拉底", c1, c2).start();
        new Philosopher("柏拉图", c2, c3).start();
        new Philosopher("亚里士多德", c3, c4).start();
        new Philosopher("赫拉克里特", c4, c5).start();
//        new Philosopher("阿基米德", c5, c1).start();
        // 此时顺序加锁可以避免死锁，但是会导致此线程非常饥饿.
        new Philosopher("阿基米德", c1, c5).start();
    }
}

@Data
@AllArgsConstructor
class Chopstick{
    String name;
}

@Slf4j(topic = "c.Philosopher")
class Philosopher extends Thread{
    final Chopstick left;
    final Chopstick right;

    public Philosopher(String name, Chopstick left, Chopstick right) {
        super(name);
        this.left = left;
        this.right = right;
    }

    @Override
    public void run() {
        while (true){
            // get left chopstick
            synchronized (this.left){
                // get right chopstick
                synchronized (this.right){
                    // 吃饭
                    eat();
                }
                // free right chopstick
            }
            // free left chopstick
        }
    }

    public void eat() {
        log.debug("eating...");
        Sleeper.sleep(1);
    }
}