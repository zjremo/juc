package net.jrz.d5;

// 练习: 保证doInit方法只执行一次
public class TestVolatile2 {
    volatile boolean initialized = false;

    void init(){
        /*
            if (initialized){ // volatile无法保证原子性
                return;
            }
            doInit();
            initialized = true;
        **/

        if (!initialized){ // 使用双检锁来保证安全性
            synchronized (this){
                if (!initialized){
                    doInit();
                    initialized = true;
                }
            }
        }
    }

    private void doInit(){

    }
}
