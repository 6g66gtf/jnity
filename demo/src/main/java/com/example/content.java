package com.example;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.json.JSONArray;
import org.json.JSONObject;
import com.example.Execute.ActionType;

class Block {

    public int x, y, w, h, id;
    public boolean isCollide, isDraw;
    private Image image;

    public Block(int x, int y, int w, int h, boolean isCollide, boolean isDraw, String icon, int id) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.isCollide = isCollide;
        this.isDraw = isDraw;
        this.id = id;
        readIcon(icon);
    }

    public void draw(int px, int py, Graphics2D g) {
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
        g.drawImage(image, sx, sy, this.w * draw.TILE_SIZE, this.h * draw.TILE_SIZE, null);
    }

    public void readIcon(String icon) {
        try {
            this.image = ImageIO.read(new File(icon));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    public abstract Object myAction(int x, int y, String face, Object o);

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

class Message extends Entity {

    private String[] msg;
    private String name;

    public Message(int x, int y, String type, String[] msg, String name) {
        super(x, y, type);
        this.msg = msg;
        this.name = name;
    }

    @Override
    public Object myAction(int x, int y, String face, Object o) {
        if (isCollide(x, y, face)) {
            Map<String ,Object> map = new HashMap<>();
            map.put("msg", msg);
            map.put("name", name);
            Events.get().queue("DrawMessages", register.content, map);
            return null;
        }
        return null;
    }
}

class Button extends Entity {

    private List<Execute> actions = new ArrayList<>();

    public Button(int x, int y, String type, JSONArray jsoa) {
        super(x, y, type);
        for (int i = 0;i<jsoa.length();i++) {
            JSONObject json = jsoa.getJSONObject(i);
            Execute a = new Execute();
            switch (json.getString("type")) {
                case "queueEvent":
                    a.type = ActionType.queueEvent;
                    JSONObject event = json.getJSONObject("event");
                    switch (event.getString("event")) {
                        case "draw_msg":
                            a.msg = event.getString("msg");
                            a.name = event.getString("name");
                            a.event = Execute.Event.draw_msg;
                            break;
                        case "playSound":
                            a.soundKey = event.getString("key");
                            a.event = Execute.Event.playSound;
                            break;
                        case "playLoopSound":
                            a.soundKey = event.getString("key");
                            a.event = Execute.Event.playLoopSound;
                            break;
                        case "playBGM":
                            a.soundKey = event.getString("key");
                            a.event = Execute.Event.playBGM;
                            break;
                    
                        default:
                            a.key = event.getString("event");
                            a.event = Execute.Event.Default;
                            break;
                    }
                    break;
                case "setBlock":
                    a.type = ActionType.setBlock;
                    a.x = json.getInt("x");
                    a.y = json.getInt("y");
                    a.isCollide = json.getBoolean("isCollide");
                    a.isDraw = json.getBoolean("isDraw");
                    break;
                case "LuaScript":
                    a.type = ActionType.LuaScript;
                    a.script = json.getString("script");
                    break;
            
                default:
                    break;
            }
            actions.add(a);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object myAction(int x, int y, String face, Object o) {
        if (isCollide(x, y, face)) {
            for (Execute a : actions) {
                switch (a.type) {
                    case queueEvent:
                        Map<String ,Object> map = new HashMap<>();
                        switch (a.event) {
                            case draw_msg:
                                map.put("msg", a.msg);
                                map.put("name", a.name);

                                Events.get().queue("DrawMessage", register.content, map);//发送绘制对话框事件
                                break;
                            case playSound:
                                map.put("key",a.soundKey);
                                Events.get().queue("playSound", register.content, map);
                                break;
                            case playLoopSound:
                                map.put("key",a.soundKey);
                                Events.get().queue("playLoopSound", register.content, map);
                                break;
                            case playBGM:
                                map.put("key",a.soundKey);
                                Events.get().queue("playBGM", register.content, map);
                                break;
                            case Default:
                                map.put("key",a.key);
                                Events.get().queue("test", register.content, map);
                                break;
                            default:
                                break;
                        }
                        break;
                    case setBlock:
                        Content cot = (Content) o;
                        List<Block> list_block = cot.blocks;
                        for (Block b : list_block) {
                            if (b.x == a.x && b.y == a.y) {
                                b.isCollide = a.isCollide;
                                b.isDraw = a.isDraw;
                            }
                        }
                        break;
                    case LuaScript:
                        LuaEngine.get().eval(a.script);
                        break;
                
                    default:
                        break;
                }
            }
        }
        return o;
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

    public Trigger(int x, int y, String type, JSONObject json) {
        super(x, y, type);
        isRepeat = json.getBoolean("repeat");
        analysisExecute(json.getJSONObject("action").getJSONArray("execute"));
        analysisCondition(json.getJSONObject("action").getJSONArray("condition"));
    }
    
    private void analysisExecute(JSONArray jsoa) {
        for (int i = 0;i<jsoa.length();i++) {
            JSONObject json = jsoa.getJSONObject(i);
            Execute execute = new Execute();
            execute.type = Execute.ActionType.valueOf(json.getString("type"));
            switch (execute.type) {
                case queueEvent:
                    JSONObject event = json.getJSONObject("event");
                    execute.event = Execute.Event.valueOf(event.getString("event"));
                    switch (execute.event) {
                        case draw_msg:
                            execute.name = event.getString("name");
                            execute.msg = event.getString("msg");
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
                    
                        default:
                            break;
                    }
                    break;
                case setBlock:
                    JSONObject setblock = json.getJSONObject("set");
                    execute.x = setblock.getInt("x");
                    execute.y = setblock.getInt("y");
                    execute.isCollide = setblock.getBoolean("isCollide");
                    execute.isDraw = setblock.getBoolean("isDraw");
                    break;
                case LuaScript:
                    execute.script = json.getString("script");
                    break;
            
                default:
                    break;
            }
            actions.add(execute);
        }
    }

    private void analysisCondition(JSONArray jsoa) {
        for (int i = 0;i<jsoa.length();i++) {
            JSONObject json = jsoa.getJSONObject(i);
            Condition condition = new Condition();
            condition.conditionType = Condition.ConditionType.valueOf(json.getString("type"));
            condition.logic = Condition.Logic.valueOf(json.getString("logic"));
            switch (condition.conditionType) {
                case collide:
                    
                    break;
                case mainHand:
                    condition.mainHand_name = json.getString("name");
                    condition.mainHand_label = json.getString("label");
                    condition.mainHand_value = json.getInt("value");
                    break;
                case event:
                    condition.event = json.getString("event");
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
                        }else{isExecute = false;}
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
    //执行动作
    private void ExecuteAction() {
        Events event = Events.get();//获取唯一的事件系统

        for (Execute e : actions) {
            switch (e.type) {
                case queueEvent:
                    Map<String,Object> map = new HashMap<>();
                    switch (e.event) {
                        case draw_msg:
                            map.put("msg", e.msg);
                            map.put("name", e.name);
                            event.queue("DrawMessage", register.content, map);
                            break;
                        case draw_msgs:
                            //这个功能暂时不实现
                            break;
                        case playSound:
                            map.put("key", e.soundKey);
                            event.queue("playSound", register.content, map);
                            break;
                        case playLoopSound:
                            map.put("key", e.soundKey);
                            event.queue("playLoopSound", register.content, map);
                            break;
                        case playBGM:
                            map.put("key", e.soundKey);
                            event.queue("playBGM", register.content, map);
                            break;
                    
                        default:
                            break;
                    }
                    break;
                case setBlock:
                    List<Block> blocks = register.content.blocks;
                    for (Block b : blocks) {
                        if (b.x == e.x && b.y == e.y) {
                            b.isCollide = e.isCollide;
                            b.isDraw = e.isDraw;
                        }
                    }
                    break;
                case LuaScript:
                    LuaEngine.get().eval(e.script);
                    break;
            
                default:
                    break;
            }
        }
    }

    @Override
    public Object myAction(int x, int y, String face, Object o) {
        judge();
        if (isExecute && en) {
            if (!isExecuteing) {
                ExecuteAction();
                if (!isRepeat) en = false;
                isExecuteing = true;
            }
        }else{
            isExecuteing = false;
        }
        return null;
    }

    //特有的碰撞检测
    public boolean isCollide(int x, int y) {
        return (super.getX() == x && super.getY() == y);
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

class Item {
    private String name, label;
    private int value, x, y;
    private boolean isGround;
    private Image image;

    public Item(String name, int value, String label, int x, int y, String icon) {
        this.name = name;
        this.value = value;
        this.label = label;
        this.x = x;
        this.y = y;
        this.isGround = true;
        readIcon(icon);
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
        if (this.x > px - 3 && this.x < px + 3 && this.y > py - 3 && this.y < py + 3 && this.isGround) {
            g.setColor(Color.black);
            g.drawImage(this.image, (this.x - px + 3) * 60 - 20, (this.y - py + 2) * 60, 60, 60, null);
        }
    }

    public boolean isCollide(int x, int y) {
        return (x == this.x && y == this.y && this.isGround);
    }
}

class Player {
    private int x, y;
    private String face;
    private Image image;
    private Item mainHand;

    public Player(int x, int y, String icon) {
        this.x = x;
        this.y = y;
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
        g.drawImage(this.image, 2 * 60 + 40, 2 * 60, 60, 60, null);
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

        if (en) {
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
        if (face != null) {
            for (Item i : items) {
                if (i.isCollide(this.x, this.y)) {
                    this.mainHand = i;
                    i.setIsGround(false);

                    Map<String,Object> map = new HashMap<>();
                    map.put("name", "system");
                    map.put("msg","你捡起了" + i.getName());
                    Events.get().queue("DrawMessage", register.content, map);
                }
            }
        }
    }
}

class point {
    public point(int a,int b) {
        this.X = a;
        this.Y = b;
    }

    public int X;
    public int Y;
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
    public enum ActionType {queueEvent, setBlock, LuaScript}
    public enum Event {draw_msg, draw_msgs, playSound, playLoopSound, playBGM, Default}
    public Event event;
    public ActionType type;
    public String name;
    public String msg;
    public String[] msgs;
    public int x;
    public int y;
    public boolean isCollide;
    public boolean isDraw;
    public String soundKey;
    public String key;
    public String script;
}
