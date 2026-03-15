package net.jrz.d4;

// 交替输出 abcabcabc 三个线程
// 使用wait notify实现 黑马实现
public class TestAlternateOutput_v1 {
    public static void main(String[] args) {
        SyncWaitNotify syncWaitNotify = new SyncWaitNotify(0, 3);
        new Thread(() -> {
            syncWaitNotify.print(0, 1, "a");
        }, "t1").start();

        new Thread(() -> {
            syncWaitNotify.print(1, 2, "b");
        }, "t2").start();

        new Thread(() -> {
            syncWaitNotify.print(2, 0, "c");
        }, "t3").start();
    }
}

class SyncWaitNotify{
    private int flag;
    private int loopNumber;

    public SyncWaitNotify(int flag, int loopNumber) {
        this.flag = flag;
        this.loopNumber = loopNumber;
    }

    public void print(int waitFlag, int nextFlag, String str){
        synchronized (this){
            for (int i = 0; i < loopNumber; ++i){
                while (this.flag != waitFlag){
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace(System.out);
                    }
                }
                // 此时拿到锁
                System.out.print(str);
                flag = nextFlag;
                this.notifyAll();
            }
        }
    }
}
