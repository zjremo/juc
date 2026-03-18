package net.jrz.d6;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public interface DecimalAccount {
    // 获取余额
    BigDecimal getBalance();

    // 取款
    void withdraw(BigDecimal amount);

    /*
     * 方法内会启动 1000 个线程，每个线程做 -10 元 的操作
     * 如果初始余额为 10000 那么正确的结果应当是 0
     * */
    static void demo(DecimalAccount decimalAccount){
        List<Thread> ts = new ArrayList<>();
        for (int i = 0; i < 1000; ++i){
            ts.add(new Thread(() -> {
                decimalAccount.withdraw(BigDecimal.TEN);
            }));
        }

        // 统计时间
        long start = System.currentTimeMillis();
        ts.forEach(Thread::start);
        ts.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace(System.out);
            }
        });
        long end = System.currentTimeMillis();
        System.out.println(decimalAccount.getBalance() + " cost: " + (end - start) + " ms");
    }
}



