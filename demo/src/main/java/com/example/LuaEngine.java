package com.example;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.JsePlatform;

public class LuaEngine {
    public Globals globals; //LUA的全局变量
    private static LuaEngine luaengine = new LuaEngine();
    private ExecutorService scriptExecutor = Executors.newCachedThreadPool(); //线程池
    // 存储所有正在执行的脚本任务
    private final CopyOnWriteArrayList<Future<?>> runningFutures = new CopyOnWriteArrayList<>();

    private LuaEngine() {
        globals = JsePlatform.standardGlobals();
        // 注册游戏 API
        registerGameAPI();
    }

    public static LuaEngine get() {
        return luaengine;
    }

    public void eval(String script) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                globals.load(Reader.read(script)).call();
            } catch (Exception e) {
                // 如果是中断引起的错误，不打印全栈，只提示
                if (e instanceof InterruptedException || 
                    (e.getCause() instanceof InterruptedException)) {
                    System.out.println("[Lua引擎] 脚本执行被中断: " + script);
                } else {
                    System.err.println("[Lua引擎] 脚本执行出错: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }, scriptExecutor);

        // 将future加入跟踪列表，执行完成后自动移除
        runningFutures.add(future);
        future.whenComplete((v, t) -> runningFutures.remove(future));
    }

    /**
     * 取消所有正在运行的脚本（强制中断）
     */
    public void cancelAll() {
        for (Future<?> f : runningFutures) {
            f.cancel(true);  // 中断执行线程
        }
        runningFutures.clear();
        System.out.println("[Lua引擎] 已取消所有正在运行的脚本");
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

                LuaTable buttons = LuaValue.tableOf();
                LuaTable triggers = LuaValue.tableOf();
                i = 1;
                for (Entity e : register.content.entitys) {
                    switch (e.getType()) {
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
                            System.out.println("[Lua引擎]没有 '" + e.getType() + "'这个类型");
                            break;
                    }
                }
                content.set("buttons", buttons);
                content.set("triggers", triggers);

                System.out.println("[Lua引擎]脚本获取了当前的游戏资源");

                return content ;
            }
        };

        //延时函数
        LuaValue func1 = new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                int millis = arg.checkint();
                if (millis < 0) return LuaValue.NIL;
                try {
                    Thread.sleep(millis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    // 可选择抛出 Lua 错误
                    error("sleep interrupted");
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
                System.out.println("[事件系统]脚本向事件队列推送了事件 [" + event.checkjstring() + "]");
                return LuaValue.NIL;
            }
        };

        //发送一些延时事件
        LuaValue func3 = new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                Map<String, Object> map = new HashMap<>();

                int tick = args.arg(1).checkint();
                LuaValue event = args.arg(2);
                LuaTable table = args.arg(3).checktable();

                for (int i = 1; ; i++) {
                    LuaValue val = table.get(i);
                    if (val.isnil()) break;
                    LuaTable ltab = val.checktable();
                    map.put(ltab.get("key").checkjstring(),ltab.get("value").toString());
                }

                Events.get().delayQueue(tick, event.checkjstring(), map);
                System.out.println("[事件系统]脚本向延时事件队列推送了一个延时 [" + tick + "] 毫秒的事件 [" + event.checkjstring() + "]");
                return LuaValue.NIL;
            }
        };

        //启动一些脚本
        LuaValue func4 = new OneArgFunction() {
            @Override 
            public LuaValue call(LuaValue script) {
                LuaEngine.get().eval(script.toString());
                return  null;
            }
        };

        //生成一些随机数
        LuaValue func5 = new OneArgFunction() {
            @SuppressWarnings("null")
            @Override 
            public LuaValue call(LuaValue i) {
                int num = Lottery.random(i.checkint());
                return LuaValue.valueOf(num);
            }
        };

        //抽取一些字符串
        LuaValue func6 = new OneArgFunction() {
            @Override 
            public LuaValue call(LuaValue args) {
                args.checktable();
                Target[] t = new Target[args.length()];
                for (int i = 0; ;i++) {
                    LuaTable table = args.get(i).checktable();
                    t[i] = new Target(table.get("name").tojstring(), table.get("weight").checkint());
                    if (args.isnil()) break;
                }

                String target = Lottery.weightRandom(t);

                return LuaValue.valueOf(target);
            }
        };

        globals.set("getContent", func); //获取一些数据
        globals.set("sleep", func1); //进行一些睡眠
        globals.set("queueEvent", func2); //发送一些事件
        globals.set("queueDelayEvent", func3); //发送一些延时事件
        globals.set("LuaScript", func4); //启动一些脚本
        globals.set("Random", func5); //生成一些随机数
        globals.set("weightRandom", func6); //抽取一些字符串
    }
}
