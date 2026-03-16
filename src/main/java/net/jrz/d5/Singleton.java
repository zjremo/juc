package net.jrz.d5;

// 利用volatile的有序性来保证双检查锁的安全性问题   DCL懒汉单例
public final class Singleton { // 加final防止有子类
    private Singleton() {
    }

    // 利用volatile来禁止指令重排序，以此保证双检查锁的安全性
    private static volatile Singleton INSTANCE = null;
    public static Singleton getInstance(){
        if (INSTANCE == null){
            // 首次访问会同步，之后的使用没有 synchronized
            synchronized (Singleton.class){
                if (INSTANCE == null){
                    INSTANCE = new Singleton();
                }
            }
        }
        return INSTANCE;
    }
}
/*
         0: getstatic     #7                  // Field INSTANCE:Lnet/jrz/d5/Singleton;
         3: ifnonnull     37
         6: ldc           #8                  // class net/jrz/d5/Singleton
         8: dup
         9: astore_0
        10: monitorenter
        11: getstatic     #7                  // Field INSTANCE:Lnet/jrz/d5/Singleton;
        14: ifnonnull     27
        17: new           #8                  // class net/jrz/d5/Singleton
        20: dup
        21: invokespecial #13                 // Method "<init>":()V
        24: putstatic     #7                  // Field INSTANCE:Lnet/jrz/d5/Singleton;
        27: aload_0
        28: monitorexit
        29: goto          37
        32: astore_1
        33: aload_0
        34: monitorexit
        35: aload_1
        36: athrow
        37: getstatic     #7                  // Field INSTANCE:Lnet/jrz/d5/Singleton;
        40: areturn
 */
