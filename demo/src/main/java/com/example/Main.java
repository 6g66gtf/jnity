package com.example;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static int w = 760, h = 700;
    public static register r = new register();

    //停止移动
    private static void stopMove() {
        draw.UP = false;
        draw.LEFT = false;
        draw.DOWN = false;
        draw.RIGHT = false;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            draw a = new draw();
            r.readContent();

            JFrame frame = new JFrame("by 6g66gtf");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            frame.setSize(w, h);
            frame.setResizable(false);
            frame.add(a);

            KeyBoard.AddKey(a, "W", "w", () -> {//W按下
                draw.UP = true;
            });
            KeyBoard.AddKeyRelease(a, "W", "wr", () -> {//W松开
                draw.UP = false;
            });
            KeyBoard.AddKey(a, "A", "a", () -> {//A按下
                draw.LEFT = true;
            });
            KeyBoard.AddKeyRelease(a, "A", "ar", () -> {//A松开
                draw.LEFT = false;
            });
            KeyBoard.AddKey(a, "S", "s", () -> {//S按下
                draw.DOWN = true;
            });
            KeyBoard.AddKeyRelease(a, "S", "sr", () -> {//S松开
                draw.DOWN = false;
            });
            KeyBoard.AddKey(a, "D", "d", () -> {//D按下
                draw.RIGHT = true;
            });
            KeyBoard.AddKeyRelease(a, "D", "dr", () -> {//D松开
                draw.RIGHT = false;
            });

            KeyBoard.AddKey(a, "F", "f", () -> {
                draw.interaction = true;
                Map<String, Object> map = new HashMap<>();
                map.put("key", "f");
                Events.get().queue("test", register.content, map);
            });
            KeyBoard.AddKey(a, "E", "e", () -> {
                draw.isPack = !(boolean) draw.isPack;
                if (!draw.isPack) {
                    Map<String,Object> map = new HashMap<>();
                    Events.get().queue("stopAll_msg", register.content, map);
                }
                draw.en1 = true;
                a.repaint();
            });
            KeyBoard.AddKey(a, "R", "r", () -> {
                Events.get().queue("stopAll_msg", register.content, null);
                LuaEngine.get().cancelAll(); //中断所有运行中的脚本
                register.content = null;
                draw.isRun = false;
                Events.get();
                Events.tick = 0;
                stopMove();
                r.readContent();//重置关卡
            });
        });
    }
}