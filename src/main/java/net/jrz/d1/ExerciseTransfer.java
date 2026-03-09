package net.jrz.d1;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

@Slf4j(topic = "c.ExerciseTransfer")
public class ExerciseTransfer {
    static Random random = new Random();
    public static int getRandomNumber(){
        return random.nextInt(100) + 1;
    }
    public static void main(String[] args) throws InterruptedException {
        Account a = new Account(1000);
        Account b = new Account(1000);

        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 1000; ++i)
                a.transfer(b, getRandomNumber());
        }, "t1");

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 1000; ++i)
                b.transfer(a, getRandomNumber());
        }, "t2");

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        // 打印结果
        log.debug("The sum amount is {}", a.getMoney() + b.getMoney());
    }
}

@Data
@AllArgsConstructor
class Account{
    private int money;

    public void transfer(Account target, int amount){
        // 这里其实要保护的变量是this和target，这个函数共享了这两个对象，于是我们对它们共享的class进行加锁
        // 但是这个性能非常低，转账直接变为了串行化，只能一笔一笔转，锁粒度太大
        synchronized (Account.class){
            if (this.money >= amount){
                this.setMoney(this.getMoney() - amount);
                target.setMoney(target.getMoney() + amount);
            }
        }
    }
}