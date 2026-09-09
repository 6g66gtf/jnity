local content = getContent()
local player = content["player"]
local health = player["playerHealth"]

local table = {}
table[1] = {
    ["key"] = "name",
    ["value"] = "大运"
}
table[2] = {
    ["key"] = "msg",
    ["value"] = "不好意思借过一下(大运创掉了10点血量)"
}

queueEvent("draw_msg", table)

queueEvent("stopAll", table)

table = {}

if health - 10 < 0 then
    health = 0
else
    health = health - 10
end

table[1] = {
    ["key"] = "health",
    ["value"] = health
}

queueEvent("setPlayer", table)

LuaScript("Lua/大运.lua")

local block1 = 3

table = {{},{},{}}
table[3]["key"] = "id"
table[3]["value"] = block1

for i = 0, 20, 1 do
table[1]["key"] = "x"
table[1]["value"] = 9
table[2]["key"] = "y"
table[2]["value"] = i - 7
queueDelayEvent(i * 20, "setBlock", table)
end

table[1]["key"] = "x"
table[1]["value"] = 100
table[2]["key"] = "y"
table[2]["value"] = 100
queueDelayEvent(400, "setBlock", table)

if health == 0 then
    local block2 = 4

    table = {{},{}}
    table[1]["key"] = "name"
    table[1]["value"] = "system"
    table[2]["key"] = "msg"
    table[2]["value"] = "你寄了,按R键重来"
    queueDelayEvent(5000, "draw_msg", table)
    table = {{},{}}
    table[1]["key"] = "isDraw"
    table[1]["value"] = true
    table[2]["key"] = "id"
    table[2]["value"] = block2
    queueDelayEvent(1, "setBlock", table)
    table = {{},{},{}}
    table[1]["key"] = "x"
    table[1]["value"] = player["playerX"] - 1
    table[2]["key"] = "y"
    table[2]["value"] = -1
    table[3]["key"] = "id"
    table[3]["value"] = block2
    queueDelayEvent(2, "setBlock", table)
    table = {{},{},{}}
    table[3]["key"] = "id"
    table[3]["value"] = block2
    table[1]["key"] = "w"
    table[1]["value"] = 3
    for i = 0, 4, 1 do
        table[2]["key"] = "h"
        table[2]["value"] = i
        queueDelayEvent(3 + i * 20, "setBlock", table)
    end
else
    table = {{},{}}
    table[1]["key"] = "name"
    table[1]["value"] = "大运"
    table[2]["key"] = "msg"
    table[2]["value"] = "我这么用力你怎么还活着"
    queueDelayEvent(5000, "draw_msg", table)
    table = {{}}
    table[1]["key"] = "key"
    table[1]["value"] = "home"
    queueDelayEvent(5000, "playBGM", table)
    table = {{},{},{}}
    table[3]["key"] = "id"
    table[3]["value"] = 17
    table[1]["key"] = "x"
    table[1]["value"] = 11
    table[2]["key"] = "y"
    table[2]["value"] = -1
    queueDelayEvent(550, "setBlock", table)
    table = {{},{}}
    table[1]["key"] = "rotate"
    table[1]["value"] = 90
    table[2]["key"] = "id"
    table[2]["value"] = 17
    queueDelayEvent(550, "setBlock", table)
end