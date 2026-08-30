package com.example;

import java.util.HashMap;
import java.util.Map;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.JsePlatform;

public class LuaEngine {
    public Globals globals; //LUA的全局变量
    private static LuaEngine luaengine = new LuaEngine();

    private LuaEngine() {
        globals = JsePlatform.standardGlobals();
        // 注册游戏 API
        registerGameAPI();
    }

    public static LuaEngine get() {
        return luaengine;
    }

    public void eval(String script) {
        globals.load(Reader.read(script)).call();
    }

    private void registerGameAPI() {
        //获取register.content
        LuaValue func = new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                LuaTable content = LuaValue.tableOf();

                LuaTable blocks = LuaValue.tableOf();
                int i = 1;
                for (Block b : register.content.blocks) {
                    LuaTable block = LuaValue.tableOf();
                    block.set("blockX", b.x);
                    block.set("blockY", b.y);
                    block.set("blockW", b.w);
                    block.set("blockH", b.h);
                    block.set("blockID", b.id);
                    block.set("isDraw", LuaValue.valueOf(b.isDraw));
                    block.set("isCollide", LuaValue.valueOf(b.isCollide));
                    blocks.set(i, block);
                    i++;
                }
                content.set("blocks", blocks);

                LuaTable player = LuaValue.tableOf();
                Player p = register.content.player;
                player.set("playerX", p.getX());
                player.set("playerY", p.getY());
                player.set("playerHealth", p.health);
                player.set("face", p.getFace());
                content.set("player", player);

                LuaTable messages = LuaValue.tableOf();
                LuaTable buttons = LuaValue.tableOf();
                LuaTable triggers = LuaValue.tableOf();
                i = 1;
                for (Entity e : register.content.entitys) {
                    switch (e.getType()) {
                        case "message":
                            LuaTable message = LuaValue.tableOf();
                            message.set("x", e.getX()); 
                            message.set("y", e.getY()); 
                            break;
                        case "button":
                            LuaTable button = LuaValue.tableOf();
                            button.set("x", e.getX()); 
                            button.set("y", e.getY()); 
                            break;
                        case "trigger":
                            LuaTable trigger = LuaValue.tableOf();
                            trigger.set("x", e.getX()); 
                            trigger.set("y", e.getY()); 
                            break;
                    
                        default:
                            System.out.println("没有 '" + e.getType() + "'这个类型");
                            break;
                    }
                }
                content.set("messages", messages);
                content.set("buttons", buttons);
                content.set("triggers", triggers);

                return content ;
            }
        };

        //修改某个方块
        LuaValue func1 = new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                int bid = arg.get("blockID").checkint();
                for (Block b : register.content.blocks) {
                    if (b.id == bid) {
                        b.x = arg.get("blockX").checkint();
                        b.y = arg.get("blockY").checkint();
                        b.w = arg.get("blockW").checkint();
                        b.h = arg.get("blockH").checkint();
                        b.isDraw = arg.get("isDraw").checkboolean();
                        b.isCollide = arg.get("isCollide").checkboolean();
                        System.out.println("脚本修改了 方块[" + bid + "]");
                        break;
                    }
                }
                return LuaValue.NIL;
            }
        };

        //发送一些事件
        LuaValue func2 = new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue event, LuaValue table) {
                Map<String, Object> map = new HashMap<>();

                for (int i = 1; ; i++) {
                    LuaValue val = table.get(i);
                    if (val.isnil()) break;
                    LuaTable ltab = val.checktable();
                    map.put(ltab.get("key").checkjstring(),ltab.get("value").toString());
                }

                Events.get().queue(event.checkjstring(), register.content, map);
                return LuaValue.NIL;
            }
        };

        globals.set("getContent", func); //获取一些数据
        globals.set("setBlock", func1); //修改一些方块
        globals.set("queueEvent", func2); //发送一些事件
    }
}
