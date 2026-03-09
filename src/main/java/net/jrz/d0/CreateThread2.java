package net.jrz.d0;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

class MyCallable implements Callable<String>{
    @Override
    public String call() throws Exception {
        return Thread.currentThread().getName() + "->" + "hello world";
    }
}

public class CreateThread2 {
    public static void main(String[] args) {
        Callable<String> call = new MyCallable();
        FutureTask<String> task = new FutureTask<>(call);

        // future task -> thread
        Thread t1 = new Thread(task, "t1");
        t1.start();

        try {
            String s = task.get();
            System.out.println(s);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace(System.out);
        }
    }

}
