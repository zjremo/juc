package net.jrz.d0.method;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

@Slf4j(topic = "c.TwoPhaseTermination")
class TwoPhaseTermination{
    private Thread monitor;

    // 开启线程
    public void start(){
        monitor = new Thread(() -> {
            while (true){
                Thread current = Thread.currentThread();
                if (current.isInterrupted()){
                    log.debug("料理后事");
                    break;
                }

                try {
                    TimeUnit.SECONDS.sleep(1);
                    log.debug("执行监控记录");
                } catch (InterruptedException e) {
                    e.printStackTrace(System.out);
                    // 如果是sleep的时候被打断，那么会抛异常，并且将打断标记置为false
                    // 这样的话就无法退出循环，所以我们要重新打断来设置打断标记为true
//                    current.interrupt();
                }
            }
        }, "monitor");
        monitor.start();
    }

    // 停止监控线程
    public void stop(){
        monitor.interrupt();
    }
}

@Slf4j(topic = "c.Interrupt")
public class TestInterrupt {
    public static void main(String[] args) throws InterruptedException {
        test3();
//        TwoPhaseTermination tpt = new TwoPhaseTermination();
//        tpt.start();
//        TimeUnit.SECONDS.sleep(4);
//        tpt.stop();
    }

    private static void test3() throws InterruptedException {
        Thread t2 = new Thread(() -> {
            log.debug("park...");
            LockSupport.park();
            log.debug("unpark...");
            // 调用实例方法isInterrupted，此时会让获取打断标记并不会清除打断标记
            // 调用静态方法Thread.interrupted()此时会让打断标记清除
            log.debug("打断状态: {}", Thread.currentThread().isInterrupted());
            // 如果打断标记已经是true，则park会失效
            LockSupport.park(); // park其实和wait很像，它相当于主动检查自己现在的'干粮'是否支持接下来的运行
            log.debug("unpark...");
        }, "t2");
        t2.start();
        TimeUnit.SECONDS.sleep(2);
        t2.interrupt();
    }

    private static void test2() throws InterruptedException {
        Thread t2 = new Thread(() -> {
            while (true){
                Thread current = Thread.currentThread();
                boolean interrupted = current.isInterrupted();
                if (interrupted){
                    log.debug(" 打断状态: {}", interrupted);
                    break;
                }
            }
        }, "t2");
        t2.start();

        TimeUnit.SECONDS.sleep(1);
        t2.interrupt();
    }

    private static void test1() throws InterruptedException {
        Thread t1 = new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace(System.out);
            }
        });
        t1.start();
        TimeUnit.SECONDS.sleep(1);
        t1.interrupt();
        log.debug(" 打断状态: {}", t1.isInterrupted());
    }
}
