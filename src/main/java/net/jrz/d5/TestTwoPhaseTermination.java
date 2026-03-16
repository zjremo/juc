package net.jrz.d5;

import lombok.extern.slf4j.Slf4j;
import net.jrz.util.Sleeper;

@Slf4j(topic = "c.TestTwoPhaseTermination")
public class TestTwoPhaseTermination {
    public static void main(String[] args) {
//        TptInterrupt tpt = new TptInterrupt();
//        tpt.start();
//        Sleeper.sleep(3);
//        tpt.stop();

        TptVolatile tpt = new TptVolatile();
        tpt.start();
        Sleeper.sleep(3);
        tpt.stop();
    }
}

// 两阶段终止模式 利用打断标记来实现
@Slf4j(topic = "c.TestTPTInterrupt")
class TptInterrupt {
    private Thread thread;

    public void start() {
        thread = new Thread(() -> {
            while (true) {
                Thread current = Thread.currentThread();
                if (current.isInterrupted()) {
                    // 此时表示已经被打断
                    log.debug("料理后事");
                    break;
                }

                try {
                    Thread.sleep(1000);
                    log.debug("将结果保存");
                } catch (InterruptedException e) {
                    // 此时打断标记被清空了，并且还没料理后事
                    current.interrupt();
                }
            }
        }, "monitor thread");
        thread.start();
    }

    public void stop() {
        thread.interrupt();
    }
}

@Slf4j
class TptVolatile {
   private Thread thread;
   private volatile boolean isStop = false;

   public void start(){
       thread = new Thread(() -> {
           while (true){
               if (isStop){
                   log.debug("料理后事");
                   break;
               }

               try {
                   Thread.sleep(1000);
                   log.debug("将结果保存");
               } catch (InterruptedException e) {
               }
           }
       }, "monitor thread");

       thread.start();
   }

   public void stop(){
       isStop = true;
       thread.interrupt(); // 唤醒
   }
}