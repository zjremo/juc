package net.jrz.d5;

public final class Singleton5 {
    private Singleton5(){}

    public static class LazyHolder{
        static final Singleton5 INSTANCE = new Singleton5();
    }
    public static Singleton5 getInstance(){
        return LazyHolder.INSTANCE;
    }
}
