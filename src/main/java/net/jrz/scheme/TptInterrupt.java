package net.jrz.scheme;

import lombok.extern.slf4j.Slf4j;
import net.jrz.util.Sleeper;

@Slf4j(topic = "c.TptInterrupt")
public class TptInterrupt {
    public static void main(String[] args) {
        TptInterrupt tpt = new TptInterrupt();
        tpt.start();
        Sleeper.sleep(2);
        tpt.stop();
    }

    private Thread t;

    public void start(){
        t = new Thread(() -> {
            Thread cur = Thread.currentThread();
            while (true){
                // sleep 2 s
                if (cur.isInterrupted()){
                    log.debug("料理后事...");
                    break;
                } else{
                    try {
                        log.debug("begin sleep ...");
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        cur.interrupt(); // 恢复打断标记
                    }
                }
            }
        }, "t1");
        t.start();
    }

    public void stop(){
        t.interrupt();
    }
}
