package net.jrz.d8;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;

@Slf4j(topic = "c.TestSdf")
public class TestSdf {// 这个会有线程安全性问题
    public static void main(String[] args) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    log.debug("{}", sdf.parse("1951-04-21"));
                } catch (ParseException e) {
                    log.error("{}", e);
                }
            }).start();
        }
    }
}
