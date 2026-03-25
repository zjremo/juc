package net.jrz.d10;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "c.ThreadLocalDemo")
public class ThreadLocalDemo {
    private static final ThreadLocal<String> userHolder = new ThreadLocal<>();

    public static void main(String[] args) {
        Runnable task = () -> {
            try {
                String name = Thread.currentThread().getName();
                // 设置线程私有变量
                userHolder.set("user-" + name);
                // 模拟业务
                log.debug("name get user: {}" , userHolder.get());
            } finally {
                // 必须清理（防止线程池内存泄露）
                userHolder.remove();
            }
        };

        for (int i = 0; i < 3; ++i){
            new Thread(task, "T" + i).start();
        }
    }
}
