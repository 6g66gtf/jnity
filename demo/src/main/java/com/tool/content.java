package com.tool;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.json.JSONArray;
import org.json.JSONObject;

class Block {

    private int x, y, w, h;
    private boolean isCollide, isDraw;
    @SuppressWarnings("unused")
    private Image image;

    public Block(int x, int y, int w, int h, boolean isCollide, boolean isDraw, String icon) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.isCollide = isCollide;
        this.isDraw = isDraw;
        readIcon(icon);
    }

    public void readIcon(String icon) {
        try {
            this.image = ImageIO.read(new File(icon));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public boolean getDraw() {
        return this.isDraw;
    }

    public boolean getCollide() {
        return this.isCollide;
    }

    public void setDraw(boolean e) {
        this.isDraw = e;
    }

    public void setCollide(boolean en) {
        this.isCollide = en;
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
}

class Message extends Entity {

    @SuppressWarnings("unused")
    private String[] msg;
    @SuppressWarnings("unused")
    private String name;

    public Message(int x, int y, String type, String[] msg, String name) {
        super(x, y, type);
        this.msg = msg;
        this.name = name;
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
                    a.type = com.tool.Execute.ActionType.queueEvent;
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
                    a.type = com.tool.Execute.ActionType.setBlock;
                    a.x = json.getInt("x");
                    a.y = json.getInt("y");
                    a.isCollide = json.getBoolean("isCollide");
                    a.isDraw = json.getBoolean("isDraw");
                    break;
            
                default:
                    break;
            }
            actions.add(a);
        }
    }
}

class Trigger extends Entity {
    //触发器类,无法交互
    private List<Execute> actions = new ArrayList<>();
    private List<Condition> conditions = new ArrayList<>();

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
}

class Player {
    private int x, y;
    private String face;
    @SuppressWarnings("unused")
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
    public enum ActionType {queueEvent, setBlock}
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
}
