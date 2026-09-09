package com.example;

import java.util.Random;

public class Lottery {
    public static int random(int i) {
        Random r = new Random();
        int num = r.nextInt(i);
        System.out.println("[随机数生成器] 输出[" + num + "]");
        return num;
    }

    public static String weightRandom(Target[] args) {
        int and = 0;
        for (Target t : args) {
            and += t.weight();
        }
        int i = random(and);
        and = 0;
        for (Target t : args) {
            if (and + t.weight() > i) {
                return t.arg();
            }
            and += t.weight();
        }
        return "";
    }
}

record Target(String arg, int weight) {}
