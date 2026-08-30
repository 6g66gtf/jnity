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
    private static Player player;

    public static Content content;
    
    public void readContent() {
        JSONObject json = Reader.readJson(path);
        JSONArray content = json.getJSONArray("content");
        JSONArray events = json.getJSONArray("events");
        runEvent(events);

        int id = 0; //方块唯一ID

        for (int i = 0; i < content.length(); i++) {
            JSONObject object = content.getJSONObject(i);
            String type = object.getString("type");
            switch (type) {
                case "block":
                    int x = object.getInt("x");
                    int y = object.getInt("y");
                    int w = object.getInt("w");
                    int h = object.getInt("h");
                    boolean collide = object.getBoolean("isCollide");
                    boolean draw = object.getBoolean("isDraw");
                    String icon = object.getString("image");
                    blocks.add(
                            new Block(x, y, w, h, collide, draw, icon, id));
                            id++;
                    break;

                case "entity":
                    createEntity(object);
                    break;
                case "player":
                    player = new Player(object.getInt("x"), object.getInt("y"), object.getInt("default-health"), object.getString("image"));
                    break;
                case "item":
                    items.add(new Item(object.getString("name"), object.getInt("value"), object.getString("label"),
                            object.getInt("x"), object.getInt("y"), object.getString("image")));
                    break;

                default:
                    break;
            }
        }
        register.content = new Content(blocks, entitys, items, triggers, player);
        draw.SignManager("isRun", true, true);
    }

    public void createEntity(JSONObject json) {
        switch (json.getString("type1")) {
            case "message":
                JSONArray jsoa = json.getJSONArray("msg");
                String[] msg = new String[jsoa.length()];
                for (int i = 0; i < jsoa.length(); i++) {
                    msg[i] = jsoa.getString(i);
                }
                Message m = new Message(json.getInt("x"), json.getInt("y"), "Message", msg, json.getString("name"));
                entitys.add(m);
                break;
            case "button":
                Button b = new Button(json.getInt("x"), json.getInt("y"), "Button", json.getJSONArray("actions"));
                entitys.add(b);
                break;
            case "trigger":
                Trigger t = new Trigger(json.getInt("x"), json.getInt("y"), "Trigger", json);
                triggers.add(t);
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
                    Event.queue("DrawMessage", content, map);
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