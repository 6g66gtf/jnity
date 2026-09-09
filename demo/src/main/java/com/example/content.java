package com.example;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import org.json.JSONArray;
import org.json.JSONObject;
import com.example.Execute.ActionType;


class Box {
    public int x, y, w, h, id;
    public boolean isCollide;
    public int rotate, layer;
    public boolean isDraw, isRepeat;
    public Image image;

    public boolean isCollide(int x, int y, String face) {
        int newX = x, newY = y;
        switch (face) {
            case "up":
                newY--;
                break;
            case "left":
                newX--;
                break;
            case "down":
                newY++;
                break;
            case "right":
                newX++;
                break;
        }
        return (newX >= this.x && newX < this.x + this.w &&
                newY >= this.y && newY < this.y + this.h && this.isCollide);
    }

    public void draw(int px, int py, Graphics2D g) {
        // 保存当前原始坐标系
        AffineTransform oldTransform = g.getTransform();

        if (!isDraw) return;
        // 视口世界坐标范围（整数格）
        int left   = px - draw.VIEWPORT_COLS / 2;
        int right  = px + draw.VIEWPORT_COLS / 2;
        int top    = py - draw.VIEWPORT_ROWS / 2;
        int bottom = py + draw.VIEWPORT_ROWS / 2;

        // 矩形相交判断（方块占据 [x, x+w-1] × [y, y+h-1]）
        if (this.x > right || this.x + this.w - 1 < left ||
            this.y > bottom || this.y + this.h - 1 < top) {
            return;
        }

        // 计算绘制位置（基于世界坐标）
        int sx = draw.VIEWPORT_LEFT + (this.x - px + draw.VIEWPORT_COLS / 2) * draw.TILE_SIZE;
        int sy = draw.VIEWPORT_TOP  + (this.y - py + draw.VIEWPORT_ROWS / 2) * draw.TILE_SIZE;

        // 绘制整个方块（尺寸为 w*TILE_SIZE, h*TILE_SIZE）
        if (!isRepeat) {
            //设置旋转角
            g.rotate((double) rotate * Math.PI / 180, sx + (w * 60) / 2, sy + (h * 60) / 2);
            g.drawImage(image, sx, sy, this.w * draw.TILE_SIZE, this.h * draw.TILE_SIZE, null);
            // 恢复坐标系，避免影响后续绘制
            g.setTransform(oldTransform);
        } else {
            for (int nw = 0;nw < this.w;nw++) {
                for (int nh = 0;nh < this.h;nh++) {
                    //设置旋转角
                    int cx = sx + nw * draw.TILE_SIZE + draw.TILE_SIZE / 2;
                    int cy = sy + nh * draw.TILE_SIZE + draw.TILE_SIZE / 2;
                    g.rotate((double) rotate * Math.PI / 180, cx, cy);
                    g.drawImage(image, sx + nw * draw.TILE_SIZE, sy + nh * draw.TILE_SIZE, draw.TILE_SIZE, draw.TILE_SIZE, null);
                    // 恢复坐标系，避免影响后续绘制
                    g.setTransform(oldTransform);
                }
            }
        }
    }
}

class Block extends Box {
    public Block(int x, int y, int w, int h, boolean isCollide, boolean isDraw, boolean isRepeat, String icon, int rotate, int layer, int id) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.isCollide = isCollide;
        this.isDraw = isDraw;
        this.isRepeat = isRepeat;
        this.id = id;
        this.layer = layer;
        this.rotate = rotate;
        readIcon(icon);
    }

    public void readIcon(String icon) {
        try {
            this.image = ImageIO.read(new File(icon));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Trap extends Box { 
    public int damage;
    private boolean en;

    public Trap(int x, int y, int w, int h, int id, int damage, int rotate, boolean isRepeat, String image) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.rotate = rotate;
        this.id = id;
        this.isCollide = false;
        this.isDraw = true;
        this.isRepeat = isRepeat;
        this.damage = damage;
        this.en = true;
        readIcon(image);
    }

    public void readIcon(String icon) {
        try {
            this.image = ImageIO.read(new File(icon));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void myAction(int px, int py) {
        if (px >= x && px <= x + w - 1 && py >= y && py <= y + h - 1) {
            if (damage == -1) {
                register.content.player.health = 0;
            } else {
                if (register.content.player.health > 0) register.content.player.health = register.content.player.health - damage;
                else {
                    if (en) {
                        Map<String,Object> map = new HashMap<>();
                        map.put("name", "system");
                        map.put("msg", "你寄了，按R键重开");
                        Events.get().queue("draw_msg", register.content, map);
                        Events.get().queue("stopAll", register.content, map);
                        map.clear();
                        map.put("key", "dead");
                        Events.get().queue("playSound", register.content, map);
                        en = false;
                    }
                }
            }
        }
    }
}

abstract class Entity {
    private int x, y;
    private String type;

    public Entity(int x, int y, String type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getType() {
        return this.type;
    }

    public abstract void myAction(int x, int y, String face, Object o);

    public boolean isCollide(int x, int y, String face) {
        int newX = x;
        int newY = y;
        if (face != null) {
            switch (face) {
                case "up":
                    newY--;
                    break;
                case "left":
                    newX--;
                    break;
                case "down":
                    newY++;
                    break;
                case "right":
                    newX++;
                    break;

                default:
                    break;
            }
        }
        return newX == getX() && newY == getY();
    }
}

class Button extends Entity {
    //按钮类，交互后按顺序执行动作
    private List<Execute> actions = new ArrayList<>();

    public Button(int x, int y, String type, JSONArray jsoa) {
        super(x, y, type);
        actions = Tool.analysisExecute(jsoa);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void myAction(int x, int y, String face, Object o) {
        if (isCollide(x, y, face)) {
            Tool.Execute(actions);
        }
    }
}

class Trigger extends Entity {
    //触发器类,无法交互
    private List<Execute> actions = new ArrayList<>();
    private List<Condition> conditions = new ArrayList<>();

    private boolean isExecute = false;
    private boolean isExecuteing = false;
    private boolean en = true;
    public boolean isRepeat;

    public int w,h;

    public Trigger(int x, int y, String type, JSONObject json) {
        super(x, y, type);
        w = 1;
        h = 1;
        if (!json.isNull("w") && !json.isNull("h")) {
            w = json.getInt("w");
            h = json.getInt("h");
        }
        isRepeat = json.getBoolean("repeat");
        actions = Tool.analysisExecute(json.getJSONObject("action").getJSONArray("execute"));
        analysisCondition(json.getJSONObject("action").getJSONArray("condition"));
    }

    private void analysisCondition(JSONArray jsoa) {
        for (int i = 0;i<jsoa.length();i++) {
            System.out.println("[触发器]注册触发条件");
            JSONObject json = jsoa.getJSONObject(i);
            Condition condition = new Condition();
            condition.conditionType = Condition.ConditionType.valueOf(json.getString("type"));
            condition.logic = Condition.Logic.valueOf(json.getString("logic"));
            System.out.println("[触发器]注册了一个触发条件 [" + condition.conditionType.toString() + "]");
            switch (condition.conditionType) {
                case collide:
                    System.out.println("[触发器] collide: x[" + super.getX() + "] y[" + super.getY() + "] w[" + w + "] h[" + h + "]");
                    break;
                case mainHand:
                    condition.mainHand_name = json.getString("name");
                    condition.mainHand_label = json.getString("label");
                    condition.mainHand_value = json.getInt("value");
                    System.out.println("[触发器] mainHand: name[" + condition.mainHand_name + "] label[" + condition.mainHand_label + "] value[" + condition.mainHand_value + "]");
                    break;
                case event:
                    condition.event = json.getString("event");
                    System.out.println("[触发器] event[" + condition.event + "]");
                    break;
            
                default:
                    break;
            }
            conditions.add(condition);
        }
    }
    //检查条件
    private void judge() {
        boolean en = true;
        for (Condition c : conditions) {
            if (en) {
                switch (c.conditionType) {
                    case collide:
                        isExecute = isCollide(register.content.player.getX(), register.content.player.getY());
                        break;
                    case mainHand:
                        Item i = register.content.player.getMainHand();
                        if (i != null) {
                            isExecute = isItem(c.mainHand_name, c.mainHand_label, c.mainHand_value, i);
                        }else{isExecute = false;}
                        break;
                    case event:
                        isExecute = Events.get().getLOG(c.event);
                        break;
                
                    default:
                        break;
                }
                en = false;
            } else {
                switch (c.conditionType) {
                    case collide:
                        switch (c.logic) {
                            case and:
                                isExecute = isExecute & isCollide(register.content.player.getX(), register.content.player.getY());
                                break;
                            case or:
                                isExecute = isExecute | isCollide(register.content.player.getX(), register.content.player.getY());
                                break;
                            case not:
                                isExecute = isExecute & !isCollide(register.content.player.getX(), register.content.player.getY());
                                break;
                        
                            default:
                                break;
                        }
                        break;
                    case mainHand:
                        Item i = register.content.player.getMainHand();
                        if (i != null) {
                            switch (c.logic) {
                                case and:
                                    isExecute = isExecute & isItem(c.mainHand_name, c.mainHand_label, c.mainHand_value, i);
                                    break;
                                case or:
                                    isExecute = isExecute | isItem(c.mainHand_name, c.mainHand_label, c.mainHand_value, i);
                                    break;
                                case not:
                                    isExecute = isExecute & !isItem(c.mainHand_name, c.mainHand_label, c.mainHand_value, i);
                                    break;
                            
                                default:
                                    break;
                            }
                        }else{
                            if (c.logic.toString() == "not") {isExecute = true;} else {isExecute = false;}
                        }
                        break;
                    case event:
                        switch (c.logic) {
                            case and:
                                isExecute = isExecute & Events.get().getLOG(c.event);
                                break;
                            case or:
                                isExecute = isExecute | Events.get().getLOG(c.event);
                                break;
                            case not:
                                isExecute = isExecute & !Events.get().getLOG(c.event);
                                break;
                        
                            default:
                                break;
                        }
                        break;
                
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public void myAction(int x, int y, String face, Object o) {
        judge();
        if (isExecute && en) {
            if (!isExecuteing) {
                Tool.Execute(actions);
                if (!isRepeat) en = false;
                isExecuteing = true;
            }
        }else{
            isExecuteing = false;
        }
    }

    //特有的碰撞检测
    public boolean isCollide(int x, int y) {
        int tx = super.getX();
        int ty = super.getY();
        return (x >= tx && y >= ty && x <= tx + w -1 && y <= ty + h -1);
    }

    //检查主手物品信息
    public boolean isItem(String name, String label, int value, Item i) {
        boolean en;

        if (name.equals("NAN")) {
            en = true;
        } else {
            en = (i.getName().equals(name));
        }
        if (label.equals("NAN")) {
            en = en & true;
        } else {
            en = en & (i.getLabel().equals(label));
        }
        if (value == -1) {
            en = en & true;
        } else {
            en = en & (i.getValue() == value);
        }
        return en;
    }
}

class Effect extends Entity {
    private class Particle implements Runnable {
        private int vx, vy, w, h, health;
        private Image image;
        private Color color;

        public Particle(int vx, int vy, int w, int h, int health, Image image, Color color) {
            this.vx = vx;
            this.vy = vy;
            this.health = health;
            this.image = image;
            this.color = color;
        }

        @Override
        public void run() {
            SwingUtilities.invokeLater(() -> {
                for (int i = 0;i < health;i++) {
                    
                }
            });
        }
    }

    public Effect(int x, int y, String type) {
        super(x, y, type);
    }

    @Override
    public void myAction(int x, int y, String face, Object o) {
    }
}

class Item {
    private String name, label;
    private int value, x, y;
    private boolean isGround;
    private Image image;
    private List<Execute> executes;

    public Item(String name, int value, String label, int x, int y, String icon, JSONArray jsoa) {
        this.name = name;
        this.value = value;
        this.label = label;
        this.x = x;
        this.y = y;
        this.isGround = true;
        executes = Tool.analysisExecute(jsoa);
        readIcon(icon);
    }

    public void Use() {
        Tool.Execute(executes);
    }

    public void readIcon(String icon) {
        try {
            this.image = ImageIO.read(new File(icon));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getLabel() {
        return this.label;
    }

    public String getName() {
        return this.name;
    }

    public int getValue() {
        return this.value;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public List<Execute> getExecutes() {
        return  this.executes;
    }

    public void setIsGround(boolean en) {
        this.isGround = en;
    }

    public boolean getIsGround() {
        return this.isGround;
    }

    public Image getIcon() {
        return this.image;
    }

    public void draw(int px, int py, Graphics2D g) {
        // 视口世界坐标范围（整数格）
        int left   = px - draw.VIEWPORT_COLS / 2;
        int right  = px + draw.VIEWPORT_COLS / 2;
        int top    = py - draw.VIEWPORT_ROWS / 2;
        int bottom = py + draw.VIEWPORT_ROWS / 2;
        if (this.x > left && this.x < right && this.y > top && this.y < bottom && this.isGround) {
            g.setColor(Color.black);
            g.drawImage(this.image, (this.x - px + 6) * 60 - 20, (this.y - py + 5) * 60, 60, 60, null);
        }
    }

    public boolean isCollide(int x, int y) {
        return (x == this.x && y == this.y && this.isGround);
    }
}

class Player {
    public int x, y;
    private String face;
    private Image image;
    private Item mainHand;
    public int health;

    public Player(int x, int y, int health, String icon) {
        this.x = x;
        this.y = y;
        this.health = health;
        readIcon(icon);
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public String getFace() {
        return this.face;
    }

    public Item getMainHand() {
        return this.mainHand;
    }

    public void readIcon(String icon) {
        try {
            this.image = ImageIO.read(new File(icon));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void draw(Graphics2D g) {
        if (isDead()) g.drawImage(this.image, 5 * 60 + 40, 5 * 60, 60, 60, null);
    }

    public void useItem() {
        if (mainHand != null) {
            this.mainHand.Use();
            mainHand = null;
        } else {
            Map<String,Object> map = new HashMap<>();
            map.put("name", "system");
            map.put("msg", "你的手上没东西可用");

            Events.get().queue("draw_msg", register.content, map);
            Events.get().delayQueue(10, "stopAll_msg", map);
        }
    }

    public void move(String face, List<Block> blocks, List<Item> items) {
        this.face = face;
        Iterator<Block> i = blocks.iterator();
        boolean en = true;
        while (i.hasNext()) {
            Block b = i.next();
            if (b.isCollide(this.x, this.y, face)) {
                en = false;
            }
        }

        if (en && isDead()) {
            switch (face) {
                case "up":
                    this.y--;
                    break;
                case "down":
                    this.y++;
                    break;
                case "left":
                    this.x--;
                    break;
                case "right":
                    this.x++;
                    break;

                default:
                    break;
            }
        }
    }

    public void pickup(String face, List<Item> items) {
        if (face != null && isDead()) {
            for (Item i : items) {
                if (i.isCollide(this.x, this.y)) {
                    this.mainHand = i;
                    i.setIsGround(false);

                    Map<String,Object> map = new HashMap<>();
                    map.put("name", "system");
                    map.put("msg","你捡起了" + i.getName());
                    Events.get().queue("draw_msg", register.content, map);
                }
            }
        }
    }

    public boolean isDead() {
        if (this.health > 0) {
            return true;
        } else {
            return false;
        }
    }
}

class Condition {
    public enum Logic {and, or, not}
    public enum ConditionType {collide, mainHand, event}
    public Logic logic;
    public ConditionType conditionType;
    public String event;
    public String mainHand_name,mainHand_label;
    public int mainHand_value;

}

class Execute {
    public enum ActionType {queueEvent ,queueDelayEvent}
    public enum Event {draw_msg, draw_image, playSound, playLoopSound, playBGM, stopAll, stopAll_msg, setBlock, Default, setPlayerHealth, LuaScript, setSave}
    public Event event;
    public ActionType type;
    public String name, msg, soundKey, key, script, image;
    public int x, y , w, h, id, health, ticks;
    public boolean isCollide, isDraw;
}

class Tool {
    public static void Execute(List<Execute> actions) {
        for (Execute execute : actions) {
            switch (execute.type) {
                case queueEvent:
                    Map<String ,Object> map = new HashMap<>();
                    switch (execute.event) {
                        case draw_msg:
                            map.put("msg", execute.msg);
                            map.put("name", execute.name);
                            Events.get().queue("draw_msg", register.content, map);//发送绘制对话框事件
                            break;
                        case draw_image:
                            map.put("image", execute.image);
                            Events.get().queue("draw_image", register.content, map);//发送绘制大对话框事件
                            break;
                        case playSound:
                            map.put("key",execute.soundKey);
                            Events.get().queue("playSound", register.content, map);
                            break;
                        case playLoopSound:
                            map.put("key",execute.soundKey);
                            Events.get().queue("playLoopSound", register.content, map);
                            break;
                        case playBGM:
                            map.put("key",execute.soundKey);
                            Events.get().queue("playBGM", register.content, map);
                            break;
                        case Default:
                            map.put("key",execute.key);
                            Events.get().queue("test", register.content, map);
                            break;
                        case setSave:
                            map.put("x", execute.x);
                            map.put("y", execute.y);
                            Events.get().queue("setSave", register.content, map);
                            break;
                        case setPlayerHealth:
                            map.put("health", execute.health);
                            Events.get().queue("setPlayerHealth", register.content, map);
                            break;
                        case stopAll:
                            Events.get().queue("stopAll", register.content, map);
                            break;
                        case stopAll_msg:
                            Events.get().queue("stopAll_msg", register.content, map);
                            break;
                        case setBlock:
                            map.put("x", execute.x);
                            map.put("y", execute.y);
                            map.put("w", execute.w);
                            map.put("h", execute.h);
                            map.put("isCollide", execute.isCollide);
                            map.put("isDraw", execute.isDraw);
                            map.put("id", execute.id);
                            Events.get().queue( "setBlock", register.content, map);
                            break;
                        case LuaScript:
                            System.out.println("[组件]正在运行脚本 [" + execute.script + "]");
                            LuaEngine.get().eval(execute.script);
                            break;
                        default:
                            break;
                    }
                    break;
                case queueDelayEvent:
                    Map<String ,Object> map1 = new HashMap<>();
                    switch (execute.event) {
                        case draw_msg:
                            map1.put("msg", execute.msg);
                            map1.put("name", execute.name);
                            Events.get().delayQueue(execute.ticks, "draw_msg", map1);//发送绘制对话框事件
                            break;
                        case draw_image:
                            map1.put("image", execute.image);
                            Events.get().delayQueue(execute.ticks, "draw_image", map1);//发送绘制大对话框事件
                            break;
                        case playSound:
                            map1.put("key",execute.soundKey);
                            Events.get().delayQueue(execute.ticks, "playSound", map1);
                            break;
                        case playLoopSound:
                            map1.put("key",execute.soundKey);
                            Events.get().delayQueue(execute.ticks, "playLoopSound", map1);
                            break;
                        case playBGM:
                            map1.put("key",execute.soundKey);
                            Events.get().delayQueue(execute.ticks, "playBGM", map1);
                            break;
                        case Default:
                            map1.put("key",execute.key);
                            Events.get().delayQueue(execute.ticks, "test", map1);
                            break;
                        case setSave:
                            map1.put("x", execute.x);
                            map1.put("y", execute.y);
                            Events.get().delayQueue(execute.ticks, "setSave", map1);
                            break;
                        case setPlayerHealth:
                            map1.put("health", execute.health);
                            Events.get().delayQueue(execute.ticks, "setPlayerHealth", map1);
                            break;
                        case stopAll:
                            Events.get().delayQueue(execute.ticks, "stopAll", map1);
                            break;
                        case stopAll_msg:
                            Events.get().delayQueue(execute.ticks, "stopAll_msg", map1);
                            break;
                        case setBlock:
                            map1.put("x", execute.x);
                            map1.put("y", execute.y);
                            map1.put("w", execute.w);
                            map1.put("h", execute.h);
                            map1.put("isCollide", execute.isCollide);
                            map1.put("isDraw", execute.isDraw);
                            map1.put("id", execute.id);
                            Events.get().delayQueue(execute.ticks, "setBlock", map1);
                            break;
                        default:
                            break;
                    }
                    break;
            
                default:
                    break;
            }
        }
    }

    public static List<Execute> analysisExecute(JSONArray jsoa) {
        List<Execute> actionList = new ArrayList<>();

        for (int i = 0;i<jsoa.length();i++) {
            JSONObject json = jsoa.getJSONObject(i);
            Execute execute = new Execute();
            execute.type = Execute.ActionType.valueOf(json.getString("type"));
            switch (execute.type) {
                case queueEvent:
                    System.out.println("[组件]注册一个事件推送动作");
                    JSONObject event = json.getJSONObject("event");
                    execute.event = Execute.Event.valueOf(event.getString("event"));
                    System.out.println("[组件]注册了一个事件 [" + execute.event.toString() + "]");
                    switch (execute.event) {
                        case draw_msg:
                            execute.name = event.getString("name");
                            execute.msg = event.getString("msg");
                            break;
                        case draw_image:
                            execute.image = event.getString("image");
                            break;
                        case playSound:
                            execute.soundKey = event.getString("key");
                            break;
                        case playLoopSound:
                            execute.soundKey = event.getString("key");
                            break;
                        case playBGM:
                            execute.soundKey = event.getString("key");
                            break;
                        case setSave:
                            execute.x = event.getInt("x");
                            execute.y = event.getInt("y");
                            break;
                        case setPlayerHealth:
                            execute.health = event.getInt("health");
                            break;
                        case setBlock:
                            JSONObject setblock = json.getJSONObject("event");
                            execute.event = Execute.Event.setBlock;
                            if (!setblock.isNull("x")) {
                                execute.x = setblock.getInt("x");
                            }
                            if (!setblock.isNull("y")) {
                                execute.y = setblock.getInt("y");
                            }
                            if (!setblock.isNull("w")) {
                                execute.w = setblock.getInt("w");
                            }
                            if (!setblock.isNull("h")) {
                                execute.h = setblock.getInt("h");
                            }
                            if (!setblock.isNull("id")) {
                                execute.id = setblock.getInt("id");
                            }
                            if (!setblock.isNull("isCollide")) {
                                execute.isCollide = setblock.getBoolean("isCollide");
                            }
                            if (!setblock.isNull("isDraw")) {
                                execute.isDraw = setblock.getBoolean("isDraw");
                            }
                            break;
                        case LuaScript:
                            String scriptName = event.getString("script");
                            execute.script = scriptName;
                            System.out.println("[组件]注册一个Lua脚本 [" + scriptName + "]");
                            break;
                    
                        default:
                            execute.key = event.getString("event");
                            break;
                    }
                    break;
                case queueDelayEvent:
                    System.out.println("[组件]注册一个延时事件推送动作");
                    execute.type = ActionType.queueDelayEvent;
                    JSONObject event1 = json.getJSONObject("event");
                    execute.event = Execute.Event.valueOf(event1.getString("event"));
                    execute.ticks = event1.getInt("ticks");
                    System.out.println("[组件]注册了一个延时事件 [" + execute.event.toString() + "]");
                    switch (execute.event) {
                        case draw_msg:
                            execute.msg = event1.getString("msg");
                            execute.name = event1.getString("name");
                            break;
                        case draw_image:
                            execute.image = event1.getString("image");
                            break;
                        case playSound:
                            execute.soundKey = event1.getString("key");
                            break;
                        case playLoopSound:
                            execute.soundKey = event1.getString("key");
                            break;
                        case playBGM:
                            execute.soundKey = event1.getString("key");
                            break;
                        case setSave:
                            execute.x = event1.getInt("x");
                            execute.y = event1.getInt("y");
                            break;
                        case setPlayerHealth:
                            execute.health = event1.getInt("health");
                            break;
                        case setBlock:
                            JSONObject setblock = json.getJSONObject("event");
                            execute.event = Execute.Event.setBlock;
                            if (!setblock.isNull("x")) {
                                execute.x = setblock.getInt("x");
                            }
                            if (!setblock.isNull("y")) {
                                execute.y = setblock.getInt("y");
                            }
                            if (!setblock.isNull("w")) {
                                execute.w = setblock.getInt("w");
                            }
                            if (!setblock.isNull("h")) {
                                execute.h = setblock.getInt("h");
                            }
                            if (!setblock.isNull("id")) {
                                execute.id = setblock.getInt("id");
                            }
                            if (!setblock.isNull("isCollide")) {
                                execute.isCollide = setblock.getBoolean("isCollide");
                            }
                            if (!setblock.isNull("isDraw")) {
                                execute.isDraw = setblock.getBoolean("isDraw");
                            }
                            break;
                        case LuaScript:
                            String scriptName = event1.getString("script");
                            execute.script = scriptName;
                            System.out.println("[组件]注册一个Lua脚本 [" + scriptName + "]");
                            break;
                    
                        default:
                            execute.key = event1.getString("event");
                            break;
                    }
                    break;
            
                default:
                    break;
            }
            actionList.add(execute);
        }
        return actionList;
    }
}