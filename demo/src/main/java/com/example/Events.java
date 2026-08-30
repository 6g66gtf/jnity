 package com.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Events {
    private Map<String, Action> listeners = new HashMap<>();
    private List<String> LOG = new ArrayList<>();

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

    public void register(String key, Action value) {
        listeners.put(key ,value);
    }

    public void queue(String key, Content cot, Map<String, Object> map) {
        if (!key.equals("test")) {
            listeners.get(key).action(cot,map);
            LOG.add(key);
        }else{
            LOG.add((String) map.get("key"));
        }
    }

    public void clear() {
        listeners.clear();
    }
}

class Content {

    public List<Block> blocks;
    public List<Entity> entitys;
    public List<Item> items;
    public List<Trigger> triggers;
    public Player player;

    public Content(List<Block> bs1, List<Entity> es1, List<Item> is1, List<Trigger> tr1, Player player1 ) {
        blocks = bs1;
        entitys = es1;
        items = is1;
        player = player1;
        triggers = tr1;
    }
}

interface Action {
    void action(Content cot, Map<String,Object> obj);
}