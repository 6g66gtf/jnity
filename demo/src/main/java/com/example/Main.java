package com.example;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static int w = 400, h = 340;
    public static register r = new register();

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

            KeyBoard.AddKey(a, "W", "w", () -> {
                draw.SignManager("moveFace", "up", true);
                draw.SignManager("ismove", false, true);
                draw.SignManager("draw", null, true);
            });
            KeyBoard.AddKey(a, "A", "a", () -> {
                draw.SignManager("moveFace", "left", true);
                draw.SignManager("ismove", false, true);
                draw.SignManager("draw", null, true);
            });
            KeyBoard.AddKey(a, "S", "s", () -> {
                draw.SignManager("moveFace", "down", true);
                draw.SignManager("ismove", false, true);
                draw.SignManager("draw", null, true);
            });
            KeyBoard.AddKey(a, "D", "d", () -> {
                draw.SignManager("moveFace", "right", true);
                draw.SignManager("ismove", false, true);
                draw.SignManager("draw", null, true);
            });
            KeyBoard.AddKey(a, "F", "f", () -> {
                draw.SignManager("draw", null, true);
                draw.SignManager("en", true, true);
                draw.SignManager("en1", true, true);
                draw.SignManager("ismove", true, true);
                Map<String, Object> map = new HashMap<>();
                map.put("key", "f");
                Events.get().queue("test", register.content, map);
            });
            KeyBoard.AddKey(a, "Z", "z", () -> {
                draw.SignManager("isPack", !(boolean) draw.SignManager("isPack", null, false), true);
                draw.SignManager("draw", null, true);
            });
            KeyBoard.AddKey(a, "R", "r", () -> {
                register.content = null;
                draw.SignManager("isRun", false, true);
                register.content = null;
                r.readContent();//重置关卡
            });
            KeyBoard.AddKey(a, "B", "b", () -> {
                System.out.println("------调试------");
                System.out.println("X:" + register.content.player.getX() + "          Y:" + register.content.player.getX());
            });
        });
    }
}