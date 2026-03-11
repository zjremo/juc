package net.jrz.d2.notify;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.jrz.util.Sleeper;

import java.util.*;

@Slf4j(topic = "c.TestGuardedObject3")
public class TestGuardedObject3 {
    public static void main(String[] args) {
        for (int i = 0; i < 3; ++i){
            new People().start();
        }
        Sleeper.sleep(1);
        Set<String> mailIds = Mailboxes.getIds();
        for (String id: mailIds){
            new Postman(id, "内容" + id).start();
        }
    }
}

@Slf4j(topic = "c.People")
class People extends Thread{
    @Override
    public void run() {
        // 收信
        GuardedObject3 guardedObject3 = Mailboxes.createGuardedObject();
        log.debug("收信 id:{}", guardedObject3.getId());
        Object mail = guardedObject3.get(5000);
        log.debug("收到信 id:{}, 内容为:{}", guardedObject3.getId(), mail);
    }
}

@Slf4j(topic = "c.Postman")
class Postman extends Thread{
    private String id;
    private String mail;

    public Postman(String id, String mail){
        this.id = id;
        this.mail = mail;
    }

    @Override
    public void run() {
        // 通过id拿到邮箱
        GuardedObject3 guardedObject = Mailboxes.getGuardedObjectById(id);
        log.debug("Postman 送信id: {}, 内容为: {}", this.id, this.mail);
        // 传递mail
        guardedObject.complete(mail);
    }
}

// 用于解耦的邮箱类
class Mailboxes{
   private static final Map<String, GuardedObject3> boxes = new HashMap<>();

   private static String generateId(){
       return UUID.randomUUID().toString().replace("-", "");
   }

   public static GuardedObject3 getGuardedObjectById(String id){
       return boxes.remove(id);
   }
   public static GuardedObject3 createGuardedObject(){
       GuardedObject3 go = new GuardedObject3(generateId());
       boxes.put(go.getId(), go);
       return go;
   }

   public static Set<String> getIds(){
       return new HashSet<>(boxes.keySet()); // 这里一定要注意keySet拿到的其实是个引用，不拷贝的话后面我们一边迭代一边postman删key会有问题
   }
}

// 保护性暂停模式 改进: wait不要无限等待，增加超时时间 用id进行标识，进一步在线程之中进行解耦
@Data
@NoArgsConstructor
class GuardedObject3{
    // 结果
    private Object response;
    // 共享标识ID
    private String id;

    public GuardedObject3(String id) {
        this.id = id;
    }

    // 获取结果
    public Object get(long timeout){
        synchronized (this){
            // 没有结果
            // 记录开始时间
            long begin = System.currentTimeMillis();
            long passedTime = 0;
            while (response == null){ // response不为空代表其他线程完成了任务，可以执行了
                // 经历时间超过最大等待时间时，退出循环
                long waitTime = timeout- passedTime;
                if (waitTime <= 0)
                    break;
                try {
                    this.wait(waitTime); // 可以保证最小的等待时间
                } catch (InterruptedException e) {
                    e.printStackTrace(System.out);
                    throw new RuntimeException(e);
                }

                passedTime = System.currentTimeMillis() - begin;
            }
            return response;
        }
    }

    // 产生结果
    public void complete(Object response){
        synchronized (this){
            // 给结果成员变量赋值
            this.response = response; // 完成任务之后，其他线程传递信号(结果)
            this.notifyAll();
        }
    }
}
