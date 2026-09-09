package com.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class register {

    public String setting = "setting.txt";
    public String path = Reader.readJson(setting).getString("map");

    private static List<Block> blocks = new ArrayList<>();
    private static List<Entity> entitys = new ArrayList<>();
    private static List<Item> items = new ArrayList<>();
    private static List<Trigger> triggers = new ArrayList<>();
    private static List<Trap> traps = new ArrayList<>();
    private static Player player;

    //存档点
    private boolean isSave = false;
    private int sX, sY;

    public static Content content;

    //存档
    public void Save(int x, int y) {
        isSave = true;
        sX = x;
        sY = y;
    }
    
    public void readContent() {
        System.out.println("[注册器]正在初始化游戏资源");
        JSONObject json = Reader.readJson(path);
        JSONArray content = json.getJSONArray("content");
        JSONArray events = json.getJSONArray("events");
        runEvent(events);

        //清除所有资源
        blocks.clear();
        entitys.clear();
        items.clear();
        triggers.clear();
        Events.get().delayClear();
        System.out.println("[注册器]清除所有旧的游戏资源");

        int id = 0; //方块唯一ID

        for (int i = 0; i < content.length(); i++) {
            JSONObject object = content.getJSONObject(i);
            String type = object.getString("type");
            int x = object.getInt("x");
            int y = object.getInt("y");
            switch (type) {
                case "box":
                    createBox(object, id);
                    id++;
                    break;

                case "entity":
                    createEntity(object);
                    break;
                case "player":
                    int newX, newY;
                    if (isSave) {
                        newX = sX;
                        newY = sY;
                    } else {
                        newX = x;
                        newY = y;
                    }
                    player = new Player(newX, newY, object.getInt("default-health"), object.getString("image"));
                    System.out.println("[注册器]注册了一个玩家 x[" + x + "] y[" + y + "]");
                    break;
                case "item":
                    String name = object.getString("name");
                    String label = object.getString("label");
                    int value = object.getInt("value");
                    items.add(new Item(name, value, label, x, y, object.getString("image"), object.getJSONArray("actions")));
                    System.out.println("[注册器]注册了一个物品 名称[" + name + "] 标签[" + label + "] 值[" + value + "] x[" + x + "] y[" + y + "]");
                    break;

                default:
                    break;
            }
        }
        register.content = new Content(Sorter.sorter(blocks), entitys, items, triggers, traps, player);
        draw.isRun = true;
    }

    public void createEntity(JSONObject json) {
        int x = json.getInt("x");
        int y = json.getInt("y");
        switch (json.getString("type1")) {
            case "button":
                Button b = new Button(x, y, "Button", json.getJSONArray("actions"));
                entitys.add(b);
                System.out.println("[注册器]注册了一个按钮 x[" + x + "] y[" + y + "]");
                break;
            case "trigger":
                Trigger t = new Trigger(x, y, "Trigger", json);
                triggers.add(t);
                System.out.println("[注册器]注册了一个触发器 x[" + x + "] y[" + y + "]");
                break;

            default:
                break;
        }
    }

    public void createBox(JSONObject json, int id) {
        String type = json.getString("type1");
        int x = json.getInt("x");
        int y = json.getInt("y");
        int w = json.getInt("w");
        int h = json.getInt("h");
        int rotate = json.getInt("rotate");
        boolean repeat = json.getBoolean("isRepeat");
        String icon = json.getString("image");
        switch (type) {
            case "block":
                int layer = json.getInt("layer");
                boolean collide = json.getBoolean("isCollide");
                boolean draw = json.getBoolean("isDraw");
                blocks.add(new Block(x, y, w, h, collide, draw, repeat, icon, rotate, layer, id));
                System.out.println("[注册器]注册了一个方块 x[" + x + "] y[" + y + "] w[" + w + "] h[" + h + "] isCollide[" + collide + "] isDraw[" + draw + "] isRepeat[" + repeat + "] 旋转角[" + rotate + "] layer[" + layer + "] id[" + id + "] icon[" + icon + "]");
                break;
            case "trap":
                w = json.getInt("w");
                h = json.getInt("h");
                int damage = json.getInt("damage");
                icon = json.getString("image");
                System.out.println("[注册器]注册了一个陷阱 x[" + x + "] y[" + y + "] w[" + w + "] h[" + h + "] isRepeat[" + repeat + "] 旋转角[" + rotate + "] id[" + id + "] icon[" + icon + "]");
                traps.add(new Trap(x, y, w, h, id, damage, rotate, repeat, icon));
                break;
        
            default:
                break;
        }
    }

    public void runEvent(JSONArray events) {
        for (int i = 0;i < events.length(); i++) {
            JSONObject event = events.getJSONObject(i);
            Events Event = Events.get();
            Map<String, Object> map = new HashMap<>();

            switch (Execute.Event.valueOf(event.getString("event"))) {
                case draw_msg:
                    map.put("name", event.getString("name"));
                    map.put("msg", event.getString("msg"));
                    Event.queue("draw_msg", content, map);
                    break;
                case playSound:
                    map.put("key",event.getString("key"));
                    Event.queue("playSound", content, map);
                    break;
                case playLoopSound:
                    map.put("key",event.getString("key"));
                    Event.queue("playLoopSound", content, map);
                    break;
                case playBGM:
                    map.put("key",event.getString("key"));
                    Event.queue("playBGM", content, map);
                    break;
                case stopAll:
                    Event.queue("stopAll", content, map);
                    break;
                case setPlayerHealth:
                    map.put("health", event.getInt("health"));
                    Event.queue("setPlayerHealth", content, map);
                    break;
                case Default:
                    map.put("key",event.getString("key"));
                    Event.queue("test", content, map);
                    break;
            
                default:
                    break;
            }
        }
    }
}