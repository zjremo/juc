package net.jrz.scheme;

import lombok.extern.slf4j.Slf4j;

// 犹豫模式：用于一个线程发现另一个线程或者本线程做了相同的事，没必要重新再做
@Slf4j(topic = "c.Balking")
public class Balking {
    // volatile保证每次拿到最新的
    private volatile boolean starting;

    public void start(){
        log.info("尝试启动监控线程");
        synchronized (this){
            if (starting){
                return ;
            }
            starting = true;
        }
        // 真正启动监控线程

    }
}
