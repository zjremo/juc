package net.jrz.d6;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

// CAS: compare and switch 比较成功则交换修改
// 乐观锁: 其他线程对变量可以修改，如果修改我重试即可
// 悲观锁: 不允许其他线程对变量修改，自己修改完后才允许别人改
@Slf4j(topic = "c.TestAccountCas")
public class TestAccountCas {
    public static void main(String[] args) {
        Account account = new AccountCas(10000);
        Account.demo(account);
    }
}

class AccountCas implements Account {
    private AtomicInteger balance;

    public AccountCas(int balance){
        this.balance = new AtomicInteger(balance);
    }

    @Override
    public Integer getBalance() {
        return balance.get();
    }

    @Override
    public void withdraw(Integer amount) {
        /*
        * CAS 实现原理
        * */
//        while (true){
//            int prev = balance.get();
//            int cur = prev - amount;
//            if (balance.compareAndSet(prev, cur)){
//                break;
//            }
//        }
        balance.getAndAdd(-1 * amount);
    }

}
