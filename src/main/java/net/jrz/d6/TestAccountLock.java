package net.jrz.d6;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "c.TestAccountLock")
public class TestAccountLock {
    public static void main(String[] args) {
        Account account = new AccountSafe(10000);
        Account.demo(account);
    }
}

class AccountSafe implements Account {
    private Integer balance;

    public AccountSafe(Integer balance) {
        this.balance = balance;
    }

    @Override
    public Integer getBalance() {
        synchronized (this){
            return this.balance;
        }
    }

    @Override
    public void withdraw(Integer amount) {
        synchronized (this){
            this.balance -= amount;
        }
    }
}