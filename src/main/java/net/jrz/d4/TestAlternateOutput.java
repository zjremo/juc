package net.jrz.d4;

// 交替输出 abcabcabc 三个线程
// 使用wait notify实现 自己实现版本
public class TestAlternateOutput {
    private static int global_flag = 0;
    private static final Object lock = new Object();

    // 由于需要引入新的变量，所以这里直接继承Thread
    static class MyThread extends Thread {
        private final int flag;
        private final int loopNumber;

        public MyThread(String name, int flag, int loopNumber) {
            super(name);
            this.flag = flag;
            this.loopNumber = loopNumber;
        }

        @Override
        public void run() {
            synchronized (lock) {
                for (int i = 0; i < loopNumber; ++i) {
                    while (this.flag != global_flag){
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace(System.out);
                        }
                    }
                    // 轮到自己
                    System.out.print("" + (char)('a' + this.flag));
                    global_flag = (global_flag + 1) % loopNumber;
                    lock.notifyAll();
                }
            }
        }
    }

    public static void main(String[] args) {
        new MyThread("t1", 0, 3).start();
        new MyThread("t2", 1, 3).start();
        new MyThread("t3", 2, 3).start();
    }
}
