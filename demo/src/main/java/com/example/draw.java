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
    public static boolean interaction = false; //玩家交互
    public static boolean frame = false; //玩家交互
    public static boolean en1 = true; //打开背包上升沿
    private static String draw = null; // 绘制对话框
    private static String icon = null; // 被展示的图片
    public static boolean isPack = false; // 打开背包
    public static boolean isRun = true; // 游戏运行
    public static boolean UP = false; // 向上移动
    public static boolean LEFT = false; // 向左移动
    public static boolean DOWN = false; // 向下移动
    public static boolean RIGHT = false; // 向右移动

    public static String msg = null;
    public static String name = null;

    // 游戏刻定时器
    private Timer gameTimer;
    private final int TICK_INTERVAL_MS = 100;

    //坐标常量
    public static int TILE_SIZE = 60;
    public static int VIEWPORT_COLS = 11;   // 视口格子列数
    public static int VIEWPORT_ROWS = 11;   // 视口格子行数
    public static int VIEWPORT_LEFT = 40;  // 视口在面板中的X偏移
    public static int VIEWPORT_TOP  = 0;   // 视口在面板中的Y偏移
    public static int VIEWPORT_W = 660; //视口宽
    public static int VIEWPORT_H = 660; //视口高
    public static int POINT_X = 6; //坐标变换的x，y偏移
    public static int POINT_Y = 6;

    public draw() {
        //事件系统
        Events eMer = Events.get();
        //绘制对话
        eMer.register("draw_msg", (cxt,obj) -> {
            Map<String, Object> data = (Map<String, Object>) obj;
            name = (String) data.get("name");
            msg = (String) data.get("msg");
            draw = "draw_msg";
            repaint();
            System.out.println("[事件系统]事件发出了一个对话框请求 标题[" + name + "] 内容[" + msg + "]");
        });
        //大号对话框(展示图片)
        eMer.register("draw_image", (cxt,obj) -> {
            Map<String, Object> data = (Map<String, Object>) obj;
            if (
                data.containsKey("image")) {icon = (String) data.get("image");
                System.out.println("[事件系统]事件展示了一张图片 [" + icon + "]");
                frame = true;
            }
            else {
                if (icon == null) {
                    System.out.println("[事件系统]图片展示缓存区中没有图片");
                    return ;
                }
                frame = true;
            }
        });
        //关闭对话框
        eMer.register("stopAll_msg", (cxt,obj) -> {
            draw = null;
            frame = false;
            repaint();
            System.out.println("[事件系统]事件停止了所有对话框");
        });
        //播放音频
        eMer.register("playSound", (cxt,obj) -> {
            Map<String, Object> data = (Map<String, Object>) obj;
            try {
                //播放音效
                Sound.getSoundSystem().PlaySound((String) data.get("key"));
                System.out.println("[事件系统]事件播放了音频 [" + (String) data.get("key") + "]");
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
                System.out.println("[事件系统]事件播放了循环音频 [" + (String) data.get("key") + "]");
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
            System.out.println("[事件系统]事件将BGM修改为 [" + (String) data.get("key") + "]");
        });
        //切换BGM
        eMer.register("stopAll", (cxt,obj) -> {
            Sound sound = Sound.getSoundSystem();
            sound.stopAll();
            System.out.println("[事件系统]事件停止了所有音频");
        });
        //设置玩家状态
        eMer.register("setPlayer", (cxt,obj) -> {
            Map<String, Object> data = (Map<String, Object>) obj;
            if (data.containsKey("health")) {
                register.content.player.health = Integer.parseInt(data.get("health").toString());
                System.out.println("[事件系统]事件将玩家血量修改为 [" + register.content.player.health + "]");
            }
            if (data.containsKey("x")) {
                register.content.player.x = Integer.parseInt(data.get("x").toString());
                System.out.println("[事件系统]事件将玩家X坐标修改为 [" + register.content.player.x + "]");
            }
            if (data.containsKey("y")) {
                register.content.player.y = Integer.parseInt(data.get("y").toString());
                System.out.println("[事件系统]事件将玩家Y坐标修改为 [" + register.content.player.y + "]");
            }
        });
        //设置方块属性
        eMer.register("setBlock", (cxt,obj) -> {
            Map<String, Object> data = (Map<String, Object>) obj;
            // 必须包含 id
            if (!data.containsKey("id")) {
                System.err.println("setBlock 事件缺少 blockID");
                return;
            }
            int id;
            id = Integer.parseInt(data.get("id").toString());
            for (Block b : register.content.blocks) {
                if (b.id == id) {
                    System.out.println("[事件系统]事件修改了方块 [" + (id) + "]");
                    // 仅当字段存在时更新
                    if (data.containsKey("x")) b.x = Integer.parseInt(data.get("x").toString());
                    if (data.containsKey("y")) b.y = Integer.parseInt(data.get("y").toString());
                    if (data.containsKey("w")) b.w = Integer.parseInt(data.get("w").toString());
                    if (data.containsKey("h")) b.h = Integer.parseInt(data.get("h").toString());
                    if (data.containsKey("rotate")) b.rotate = Integer.parseInt(data.get("rotate").toString());
                    if (data.containsKey("isDraw")) b.isDraw = Boolean.parseBoolean(data.get("isDraw").toString());
                    if (data.containsKey("isCollide")) b.isCollide = Boolean.parseBoolean(data.get("isCollide").toString());
                    if (data.containsKey("isRepeat")) b.isRepeat = Boolean.parseBoolean(data.get("isRepeat").toString());
                    break;
                }
            }
            repaint();
        });
        //存档
        eMer.register("setSave", (cxt,obj) -> {
            Map<String, Object> data = (Map<String, Object>) obj;
            int x = register.content.player.getX();
            int y = register.content.player.getY();
            if (data.containsKey("x")) x = Integer.parseInt(data.get("x").toString());
            if (data.containsKey("y")) y = Integer.parseInt(data.get("y").toString());
            Main.r.Save(x, y);
            System.out.println("[事件系统]事件将玩家存档点设为 x[" + x + "] y[" + y + "]");
        });

        // 启动游戏循环
        gameTimer = new Timer(TICK_INTERVAL_MS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tick();
            }
        });
        gameTimer.start();
        myTimer.startTimer();
    }

    //游戏周期
    private void tick() {
        if (register.content == null) return;
        if (isRun) { 
            //玩家移动
            if (UP) {
                move("up");
            }
            if (LEFT) {
                move("left");
            }
            if (DOWN) {
                move("down");
            }
            if (RIGHT) {
                move("right");
            }
            //玩家交互
            if (interaction) {
                if (!isPack) {
                    Iterator<Entity> list_entity = register.content.entitys.iterator();
                    while (list_entity.hasNext()) {
                        Entity e = list_entity.next();
                        switch (e.getType()) {
                            case "Button":
                                e.myAction(register.content.player.getX(), register.content.player.getY(), register.content.player.getFace(), register.content);
                                break;
                            default:
                                break;
                        }
                    }
                    register.content.player.pickup(register.content.player.getFace(), register.content.items);
                } else {
                    register.content.player.useItem();
                }
            }
            //检查触发器条件
            for (Trigger t : register.content.triggers) {
                t.myAction(0, 0, null, null);
            }
            //检查陷阱碰撞
            for (Trap t : register.content.traps) {
                t.myAction(register.content.player.getX(), register.content.player.getY());
            }

            repaint();
            interaction = false;
        }
    }

    //画面渲染
    @Override
    protected void paintComponent(Graphics g1) {
        super.paintComponent(g1);
        Graphics2D g = (Graphics2D) g1;

        // 绘制背景
        g.setColor(Color.white);
        g.fillRect(40, 0, VIEWPORT_W, VIEWPORT_H);

        for (int i = 0; i < 12; i++) {
            g.setColor(Color.GRAY);
            g.drawLine(i * 60 + 40, 0, i * 60 + 40, VIEWPORT_H);
            for (int j = 0; j < 12; j++) {
                g.drawLine(40, j * 60, VIEWPORT_W + 40, j * 60);
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
            //绘制陷阱
            for (Trap t : register.content.traps) {
                t.draw(register.content.player.getX(), register.content.player.getY(), g);
            }

            // 绘制玩家
            register.content.player.draw(g);
        }else{
            g.setColor(Color.gray);
            g.fillRect(40, 0, 660, 660);

            Item i = register.content.player.getMainHand();
            if (i == null) {
                if (en1) {
                    Map<String ,Object> map = new HashMap<>();
                    map.put("msg", "你的手上没有东西");
                    map.put("name", "system");

                    Events.get().queue("draw_msg", register.content, map);
                    en1 = false;
                }
            }else{
                g.drawImage(i.getIcon(),40,0,100,100,null);
                g.setColor(Color.white);

                if (en1) {
                    Map<String ,Object> map = new HashMap<>();
                    map.put("msg", "名称:" + i.getName() + " 描述:" + i.getLabel() + " 值:" + i.getValue());
                    map.put("name", "system");

                    Events.get().queue("draw_msg", register.content, map);
                    en1 = false;
                }
            }
            g.setColor(Color.white);
            g.setFont(new Font("Dialog", Font.PLAIN, 25));
            g.drawString("玩家属性", 40 + VIEWPORT_W - 100, 20);
            g.setFont(new Font("Dialog", Font.PLAIN, 20));
            g.drawString("x:" + register.content.player.x, 40 + VIEWPORT_W - 100, 50);
            g.drawString("y:" + register.content.player.y, 40 + VIEWPORT_W - 100, 80);
            g.drawString("health:" + register.content.player.health, 40 + VIEWPORT_W - 100, 110);
        }
        
        // 绘制左右两边黑框
        g.setColor(Color.black);
        g.fillRect(0, 0, 40, Main.h);
        g.fillRect(Main.w - 60, 0, 45, Main.h);

        //图片展示
        if (frame) {
            g.drawImage(Reader.readImage(icon), 40, 0, VIEWPORT_W, VIEWPORT_H, null);
        }

        // 绘制实体对话框
        if (draw != null) {
            g.setColor(Color.black);
            g.fillRect(0, Main.h - 150, Main.w, 5);
            g.setColor(Color.white);
            g.fillRect(0, Main.h - 145, Main.w, 145);
            switch (draw) {
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