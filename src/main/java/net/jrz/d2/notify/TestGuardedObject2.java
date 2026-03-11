package net.jrz.d2.notify;

import lombok.extern.slf4j.Slf4j;
import net.jrz.util.Sleeper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unchecked")
@Slf4j(topic = "c.TestGuardedObject2")
public class TestGuardedObject2 {
    public static void main(String[] args) {
        GuardedObject2 guardedObject = new GuardedObject2();
        new Thread(() -> {
            // 等待结果
            log.debug("等待结果");
            // 一直循环等待取得结果
            List<Integer> obj = (List<Integer>) guardedObject.get(1000);
            if (Objects.isNull(obj)){
                log.debug("未取得结果...");
            } else {
                log.debug("结果大小: {}", obj.size());
            }
        }, "t1").start();

        new Thread(() -> {
            log.debug("模拟做任务");
            Sleeper.sleep(2);
            List<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3));
            guardedObject.complete(list);
        }).start();
    }
}

// 保护性暂停模式 改进: wait不要无限等待，增加超时时间
class GuardedObject2{
    // 结果
    private Object response;

    // 获取结果
    public Object get(long timeout){
        synchronized (this){
            // 没有结果
            // 记录开始时间
            long begin = System.currentTimeMillis();
            long passedTime = 0;
            while (response == null){ // response不为空代表其他线程完成了任务，可以执行了
                // 经历时间超过最大等待时间时，退出循环
                long waitTime = timeout- passedTime;
                if (waitTime <= 0)
                    break;
                try {
                    this.wait(waitTime); // 可以保证最小的等待时间
                } catch (InterruptedException e) {
                    e.printStackTrace(System.out);
                    throw new RuntimeException(e);
                }

                passedTime = System.currentTimeMillis() - begin;
            }
            return response;
        }
    }

    // 产生结果
    public void complete(Object response){
        synchronized (this){
            // 给结果成员变量赋值
            this.response = response; // 完成任务之后，其他线程传递信号(结果)
            this.notifyAll();
        }
    }
}
