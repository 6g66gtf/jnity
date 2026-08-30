package com.tool;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

public class Draw extends JPanel {
    public Draw() {
        this.setLayout(null);
        JMenuBar jmb = new JMenuBar();
        jmb.setBounds(0, 0, Main.w, 50);
        JMenu jm = new JMenu();
        jm.setText("物体");
        jmb.add(jm);
        JMenu entity = new JMenu("实体们");
        jm.add(entity);
        JMenuItem block = new JMenuItem("街区");
        block.addActionListener(MeunItemAction.block);
        jm.add(block);
        JMenuItem item = new JMenuItem("项目");
        item.addActionListener(MeunItemAction.item);
        jm.add(item);
        JMenuItem button = new JMenuItem("扣子");
        button.addActionListener(MeunItemAction.button);
        entity.add(button);
        JMenuItem message = new JMenuItem("消息");
        message.addActionListener(MeunItemAction.message);
        entity.add(message);
        JMenuItem trigger = new JMenuItem("扳机");
        trigger.addActionListener(MeunItemAction.trigger);
        entity.add(trigger);

        this.add(jmb);
    }

    //画面渲染
    @Override
    protected void paintComponent(Graphics g1) {
        super.paintComponent(g1);
        Graphics2D g = (Graphics2D) g1;
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, Main.w, Main.h);
        g.setColor(Color.BLACK);
        for (int i = 0;i<15;i++) {
            g.fillRect(50+50*i, 0, 1, Main.h);
        }
        for (int j = 0;j<15;j++) {
            g.fillRect(0, 50+50*j, Main.w, 1);
        }
        g.setColor(Color.WHITE);
        g.fillRect(0, 751, Main.w, 150);
        g.fillRect(751, 50, 450, Main.h);
    }
}

class MeunItemAction {
    public static ActionListener block = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
        }
        
    };
    public static ActionListener item = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
        }
        
    };
    public static ActionListener button = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
        }
        
    };
    public static ActionListener message = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
        }
        
    };
    public static ActionListener trigger = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
        }
        
    };
}