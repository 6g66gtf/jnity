package com.tool;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class Main {
    public static int w = 1200;
    public static int h = 900;
    private static int delay = 100;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Draw draw = new Draw();
            JFrame frame = new JFrame("by 6g66gtf");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            frame.setSize(w, h);
            frame.setResizable(false);
            frame.add(draw);

            //程序循环
            Timer timer = new Timer(delay, new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    draw.repaint();
                }
            });
            //启动循环
            timer.start();
        });
    }
}
