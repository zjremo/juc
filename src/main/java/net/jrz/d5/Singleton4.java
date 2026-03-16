package net.jrz.d5;

// 懒汉单例
public final class Singleton4 {
    private Singleton4(){}

    private static Singleton4 INSTANCE = null;
    public static synchronized Singleton4 getInstance(){
        if (INSTANCE != null){
            return INSTANCE;
        }

        INSTANCE = new Singleton4();
        return INSTANCE;
    }
}
