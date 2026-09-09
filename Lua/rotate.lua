table = {{},{}}
table[2]["key"] = "id"
table[2]["value"] = 13

for i = 0, 8, 1 do
table[1]["key"] = "rotate"
table[1]["value"] = i * 45
queueDelayEvent(i * 100 + 1, "setBlock", table)
end

table[2]["key"] = "id"
table[2]["value"] = 18
table[1]["key"] = "isDraw"
table[1]["value"] = true
queueDelayEvent(1600, "setBlock", table)

table[1]["key"] = "name"
table[1]["value"] = "system"
table[2]["key"] = "msg"
table[2]["value"] = "此处展示方块贴图的旋转功能(不包括碰撞箱)"
queueEvent("draw_msg", table)
queueDelayEvent(1600, "stopAll_msg", table)

table = {{}}
table[1]["key"] = "key"
table[1]["value"] = "wow1"
queueDelayEvent(1600, "playSound", table)
table[1]["key"] = "key"
table[1]["value"] = "大运"
queueDelayEvent(3200, "playSound", table)
table[1]["key"] = "key"
table[1]["value"] = "天真橡皮"
queueDelayEvent(3200, "playSound", table)

table = {{},{},{}}
table[3]["key"] = "id"
table[3]["value"] = 3
table[1]["key"] = "x"
table[1]["value"] = 9

for i = 0, 20 ,1 do
    table[2]["key"] = "y"
    table[2]["value"] = -15 + i
    queueDelayEvent(3200 + i * 20, "setBlock", table)
end

sleep(3620)
content = getContent()
px = content["player"]["playerX"]
py = content["player"]["playerY"]
if py <= 9 then
    table = {{}}
    table[1] = {
        ["key"] = "health",
        ["value"] = 0
    }
    queueEvent("setPlayer", table)
    table = {{},{}}
    table[1]["key"] = "name"
    table[1]["value"] = "大运"
    table[2]["key"] = "msg"
    table[2]["value"] = "借过一下"
    queueEvent("draw_msg", table)
    table = {{},{},{},{},{},{}}
    table[6]["key"] = "id"
    table[6]["value"] = 4
    table[2]["key"] = "y"
    table[2]["value"] = py - 1
    table[4]["key"] = "h"
    table[4]["value"] = 3
    table[5]["key"] = "isDraw"
    table[5]["value"] = true
    table[3]["key"] = "w"
    table[3]["value"] = 3
    table[1]["key"] = "x"
    table[1]["value"] = px - 1
    queueEvent("setBlock", table)
else
    table = {{},{}}
    table[1]["key"] = "name"
    table[1]["value"] = "大运"
    table[2]["key"] = "msg"
    table[2]["value"] = "可恶让你跑掉了"
    queueEvent("draw_msg", table)
end
sleep(8000)
table = {{}}
table[1]["key"] = "key"
table[1]["value"] = "door"
queueEvent("playSound", table)
table = {{},{},{}}
table[1]["key"] = "id"
table[1]["value"] = 24
table[2]["key"] = "isCollide"
table[2]["value"] = false
table[3]["key"] = "isDraw"
table[3]["value"] = false
queueEvent("setBlock", table)
table[3]["key"] = "id"
table[3]["value"] = 4
table[1]["key"] = "x"
table[1]["value"] = 100
table[2]["key"] = "y"
table[2]["value"] = 100
queueEvent("setBlock", table)