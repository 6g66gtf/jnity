 package com.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class Events {
    private Map<String, Action> listeners = new HashMap<>();
    private List<delayAction> delayListener = new CopyOnWriteArrayList<>();
    private List<String> LOG = new ArrayList<>();

    public static int tick = 0;

    private static final Events inc = new Events();

    private Events() {}

    public static Events get() {return inc;}

    public boolean getLOG(String key) {
        List<String> newlog = new ArrayList<>();
        boolean en = false;
        for (String k : LOG) {
            if (k != null) {
                if (!k.equals(key)) {
                    newlog.add(k);
                }else en = true;
            }
        }
        LOG = newlog;
        return en;
    }
    //注册事件
    public void register(String key, Action value) {
        listeners.put(key ,value);
    }
    //将事件插入队列
    public void queue(String key, Content cot, Map<String, Object> map) {
        if (!key.equals("test")) {
            listeners.get(key).action(cot,map);
            LOG.add(key);
        }else{
            LOG.add((String) map.get("key"));
        }
    }
    //将事件插入延时队列
    public void delayQueue(int ticks, String key, Map<String, Object> map) {
        delayListener.add(new delayAction(tick + ticks, key, map));
    }
    //延时函数
    public void tick() {
        if (!delayListener.isEmpty()) {
            for (delayAction dl : delayListener) {
                if (tick >= dl.ticks) {
                    queue(dl.key, register.content, dl.map);
                    delayListener.remove(dl);   //移除已触发的事件
                }
            }
            tick ++;
        }else{
            tick = 0;
        }
    }
    //清除普通队列
    public void clear() {
        listeners.clear();
    }
    //清除延时队列
    public void delayClear() {
        delayListener.clear();
    }

    private class delayAction {
        public String key;
        public Map<String, Object> map;
        public int ticks;

        public delayAction(int ticks, String key, Map<String, Object> map) {
            this.key = key;
            this.map = map;
            this.ticks = ticks;
        }
    }
}

class Content {

    public List<Block> blocks;
    public List<Entity> entitys;
    public List<Item> items;
    public List<Trigger> triggers;
    public List<Trap> traps;
    public Player player;

    public Content(List<Block> bs1, List<Entity> es1, List<Item> is1, List<Trigger> tr1, List<Trap> trap1, Player player1 ) {
        blocks = bs1;
        entitys = es1;
        items = is1;
        player = player1;
        triggers = tr1;
        traps = trap1;
    }
}

interface Action {
    void action(Content cot, Map<String,Object> obj);
}