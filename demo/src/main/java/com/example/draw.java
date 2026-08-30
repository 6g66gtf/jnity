package com.example;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JPanel;

public class draw extends JPanel {
    private static boolean en = false; //玩家交互
    private static boolean en1 = false; //索引叠加
    private static String draw = null; // 绘制对话框
    private static boolean ismove = false; //是否正在移动（避免重复移动）
    private static boolean isPack = false;
    private static boolean isRun = true;

    private static String msg = null;
    private static String[] msgs = null;
    private static String name = null;
    // 移动方向请求（由键盘设置）
    private static String moveFace = null;
    
    private static int index = -1;
    private static int maxIndex;

    // 游戏刻定时器
    private Timer gameTimer;
    private final int TICK_INTERVAL_MS = 100; // 20 TPS（每秒20刻）

    //坐标常量
    public static int TILE_SIZE = 60;
    public static int VIEWPORT_COLS = 5;   // 视口格子列数
    public static int VIEWPORT_ROWS = 5;   // 视口格子行数
    public static int VIEWPORT_LEFT = 40;  // 视口在面板中的X偏移
    public static int VIEWPORT_TOP  = 0;   // 视口在面板中的Y偏移
    //标志管理
    public static Object SignManager(String key, Object value, boolean setter) {
        switch (key) {
            case "en":
                if (setter) {
                    en = (boolean) value;
                } else {
                    return en;
                }
                return null;
            case "en1":
                if (setter) {
                en1 = (boolean) value;
                } else {
                    return en1;
                }
                return null;
            case "draw":
                if (setter) {
                draw = (String) value;
                } else {
                    return draw;
                }
                return null;
            case "ismove":
                if (setter) {
                ismove = (boolean) value;
                } else {
                    return ismove;
                }
                return null;
            case "isPack":
                if (setter) {
                isPack = (boolean) value;
                } else {
                    return isPack;
                }
                return null;
            case "msg":
                if (setter) {
                msg = (String) value;
                } else {
                    return msg;
                }
                return null;
            case "msgs":
                if (setter) {
                msg = (String) value;
                } else {
                    return msgs;
                }
                return null;
            case "name":
                if (setter) {
                msgs = (String[]) value;
                } else {
                    return name;
                }
                return null;
            case "moveFace":
                if (setter) {
                moveFace = (String) value;
                } else {
                    return moveFace;
                }
                return null;
            case "index":
                if (setter) {
                index = (int) value;
                } else {
                    return index;
                }
                return null;
        
            default:
                return null;
        }
    }

    public draw() {
        //事件系统
        Events eMer = Events.get();
        //绘制对话(多)
        eMer.register("DrawMessages", (cxt,obj) -> {
            Map<String, Object> data = (Map<String, Object>) obj;
            name = (String) data.get("name");
            msgs = (String[]) data.get("msg");
            maxIndex = msgs.length;
            draw = "draw_msgs";
            repaint();
            if (en1) {
                index++;
                en1 = false;
            }
            if (index > msgs.length - 1) {
                index = 0;
            }
        });
        //绘制对话
        eMer.register("DrawMessage", (cxt,obj) -> {
            Map<String, Object> data = (Map<String, Object>) obj;
            name = (String) data.get("name");
            msg = (String) data.get("msg");
            draw = "draw_msg";
            repaint();
        });
        //播放音频
        eMer.register("playSound", (cxt,obj) -> {
            Map<String, Object> data = (Map<String, Object>) obj;
            try {
                //播放音效
                Sound.getSoundSystem().PlaySound((String) data.get("key"));
            } catch (UnsupportedAudioFileException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            };
        });
        //循环播放音频
        eMer.register("playLoopSound", (cxt,obj) -> {
            Map<String, Object> data = (Map<String, Object>) obj;
            try {
                //播放音效
                Sound.getSoundSystem().PlaySoundLoop((String) data.get("key"));
            } catch (UnsupportedAudioFileException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            };
        });
        //切换BGM
        eMer.register("playBGM", (cxt,obj) -> {
            Map<String, Object> data = (Map<String, Object>) obj;
            Sound sound = Sound.getSoundSystem();
            sound.stopAll();
            sound.playBGM((String) data.get("key"));
        });
        //切换BGM
        eMer.register("stopAll", (cxt,obj) -> {
            Sound sound = Sound.getSoundSystem();
            sound.stopAll();
        });
        //设置玩家血量
        eMer.register("setPlayerHealth", (cxt,obj) -> {
            Map<String, Object> data = (Map<String, Object>) obj;
            register.content.player.health = (int) data.get("health");
        });

        // 启动游戏循环
        gameTimer = new Timer(TICK_INTERVAL_MS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tick();
            }
        });
        gameTimer.start();
    }

    //游戏周期
    private void tick() {
    if (isRun) { 
            //玩家移动
            String face = moveFace;
            if (!ismove & moveFace != null) {
                move(face);
                ismove = true;
                en = false;
            }
            //玩家交互
            if (en) {
                Iterator<Entity> list_entity = register.content.entitys.iterator();
                while (list_entity.hasNext()) {
                    Entity e = list_entity.next();
                    switch (e.getType()) {
                        case "Message":
                            e.myAction(register.content.player.getX(), register.content.player.getY(), register.content.player.getFace(), null);
                            break;
                        case "Button":
                            e.myAction(register.content.player.getX(), register.content.player.getY(), register.content.player.getFace(), register.content);
                            break;

                        default:
                            break;
                    }
                }
                register.content.player.pickup(register.content.player.getFace(), register.content.items);
            }
            //检查触发器条件
            for (Trigger t : register.content.triggers) {
                t.myAction(0, 0, null, null);
            }

            repaint();
            en = false;
        }
    }

    //画面渲染
    @Override
    protected void paintComponent(Graphics g1) {
        super.paintComponent(g1);
        Graphics2D g = (Graphics2D) g1;

        // 绘制背景
        g.setColor(Color.white);
        g.fillRect(40, 0, 300, 400);

        for (int i = 0; i < 6; i++) {
            g.setColor(Color.GRAY);
            g.drawLine(i * 60 + 40, 0, i * 60 + 40, 350);
            for (int j = 0; j < 6; j++) {
                g.drawLine(40, j * 60, 340, j * 60);
            }
        }

        //绘制画面
        if (!isPack) {
            // 绘制方块
            Iterator<Block> ibs = register.content.blocks.iterator();
            while (ibs.hasNext()) {
                Block b = ibs.next();
                b.draw(register.content.player.getX(), register.content.player.getY(), g);
            }
            //绘制物品
            Iterator<Item> list_item = register.content.items.iterator();
            while (list_item.hasNext()) {
                Item item = list_item.next();
                item.draw(register.content.player.getX(), register.content.player.getY(), g);
            }

            // 绘制玩家
            register.content.player.draw(g);
        }else{
            g.setColor(Color.gray);
            g.fillRect(40, 0, 300, 400);

            Item i = register.content.player.getMainHand();
            if (i == null) {
                Map<String ,Object> map = new HashMap<>();
                map.put("msg", "你的手上没有东西");
                map.put("name", "system");

                Events.get().queue("DrawMessage", register.content, map);
            }else{
                g.drawImage(i.getIcon(),40,0,100,100,null);
                g.setColor(Color.white);

                Map<String ,Object> map = new HashMap<>();
                map.put("msg", "名称:" + i.getName() + " 描述:" + i.getLabel() + " 值:" + i.getValue());
                map.put("name", "system");

                Events.get().queue("DrawMessage", register.content, map);
            }
        }
        
        // 绘制左右两边黑框
        g.setColor(Color.black);
        g.fillRect(0, 0, 40, 305);
        g.fillRect(340, 0, 45, 305);


        // 绘制实体对话框
        if (draw != null) {
            g.setColor(Color.black);
            g.fillRect(0, Main.h - 150, Main.w, 5);
            g.setColor(Color.white);
            g.fillRect(0, Main.h - 145, Main.w, 145);
            switch (draw) {
                case "draw_msgs":
                    g.setFont(new Font("Dialog", Font.PLAIN, 25));
                    g.setColor(Color.BLACK);
                    g.drawString(name, 10, Main.h - 125);
                    g.setFont(new Font("Dialog", Font.PLAIN, 20));
                    g.drawString("页数" + (index + 1) + "/" + maxIndex + " 按F继续", 10, Main.h - 45);
                    g.drawString(msgs[index], 10, Main.h - 105);
                    break;
                case "draw_msg":
                    g.setFont(new Font("Dialog", Font.PLAIN, 25));
                    g.setColor(Color.BLACK);
                    g.drawString(name, 10, Main.h - 125);
                    g.setFont(new Font("Dialog", Font.PLAIN, 20));
                    g.drawString(msg, 10, Main.h - 105);
                    break;
            
                default:
                    break;
            }
        }
    }
    //玩家移动
    public void move(String face) {
        register.content.player.move(face, register.content.blocks, register.content.items);
    }
}