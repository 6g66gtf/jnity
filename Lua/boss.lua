function isCollide(x,y,w,h,px,py)
    return (
        px >= x and 
        px <= x + w - 1 and
        py >= y and
        py <= y + h - 1
    )
end

--[[
table = {{}}
table[1]["key"] = "key"
table[1]["value"] = "door"
queueEvent("playSound", table)
table = {{}}
table[1]["key"] = "key"
table[1]["value"] = "bomb"
queueEvent("playBGM", table)
table = {{},{},{}}
table[1]["key"] = "id"
table[1]["value"] = 23
table[2]["key"] = "isDraw"
table[2]["value"] = true
table[3]["key"] = "isCollide"
table[3]["value"] = true
queueEvent("setBlock", table)
table = {{},{}}
table[1]["key"] = "name"
table[1]["value"] = "大运"
table[2]["key"] = "msg"
table[2]["value"] = "这次我不会让你跑掉的"
queueEvent("draw_msg", table)
queueDelayEvent(3928, "stopAll_msg", table)

sleep(2900)

for i = 0,10,1 do
    table = {{}}
    table[1]["key"] = "key"
    table[1]["value"] = "wow1"
    queueEvent("playSound", table)
    table = {{},{},{},{},{},{},{}}
    table[1]["key"] = "id"
    table[1]["value"] = 18
    table[3]["key"] = "y"
    table[3]["value"] = 12
    table[4]["key"] = "w"
    table[4]["value"] = 5
    table[5]["key"] = "h"
    table[5]["value"] = 10
    table[7]["key"] = "isRepeat"
    table[7]["value"] = true
    en = false
    for i = 0,9,1 do
        content = getContent()
        player = content["player"]
        table[2]["key"] = "x"
        table[2]["value"] = player["playerX"] - 2
        table[6]["key"] = "isDraw"
        table[6]["value"] = not en
        en = not en
        queueEvent("setBlock", table)
        sleep(50)
    end
    content = getContent()
    player = content["player"]
    table = {{}}
    table[1]["key"] = "key"
    table[1]["value"] = "大运"
    queueEvent("playSound", table)
    table = {{},{},{}}
    table[1]["key"] = "id"
    table[1]["value"] = 3
    table[2]["key"] = "x"
    table[2]["value"] = player["playerX"] - 2

    for i = 0,20,1 do
        table[3]["key"] = "y"
        table[3]["value"] = 5 + i
        content = getContent()
        player = content["player"]
        if isCollide(table[2]["value"], table[3]["value"], 5, 5, player["playerX"], player["playerY"]) then
            table1 = {{},{}}
            table1[1]["key"] = "name"
            table1[1]["value"] = "大运"
            table1[2]["key"] = "msg"
            table1[2]["value"] = "过家家的游戏就到此为止吧"
            queueEvent("draw_msg", table1)
            queueEvent("stopAll", table1)
            table1 = {{}}
            table1[1]["key"] = "key"
            table1[1]["value"] = "dead"
            queueEvent("playSound", table1)
            table1[1]["key"] = "health"
            table1[1]["value"] = 0
            queueEvent("setPlayer", table1)
            table1 = {{},{},{},{},{},{}}
            table1[1]["key"] = "id"
            table1[1]["value"] = 4
            table1[2]["key"] = "x"
            table1[2]["value"] = player["playerX"] - 1
            table1[3]["key"] = "y"
            table1[3]["value"] = player["playerY"] - 1
            table1[4]["key"] = "w"
            table1[4]["value"] = 3
            table1[5]["key"] = "h"
            table1[5]["value"] = 3
            table1[6]["key"] = "isDraw"
            table1[6]["value"] = true
            queueEvent("setBlock", table1)
            while true do
                sleep(6000)
                table1 = {{},{}}
                table1[1]["key"] = "name"
                table1[1]["value"] = "你变成喵喵酱了，按R键再次踏上轮回"
                table1[2]["key"] = "msg"
                table1[2]["value"] = "你变成喵喵酱了，按R键再次踏上轮回"
                queueEvent("draw_msg", table1)
                while true do
                    sleep(1)
                end
            end
        end
        queueEvent("setBlock", table)
        sleep(50)
    end
    table[2]["key"] = "y"
    table[2]["value"] = 100
    table[2]["key"] = "x"
    table[2]["value"] = 100
    queueDelayEvent(21 * 50, "setBlock", table)
    sleep(21*50)
end

table = {{},{}}
table[1]["key"] = "name"
table[1]["value"] = "大运"
table[2]["key"] = "msg"
table[2]["value"] = "kesa!你这家伙"
queueEvent("draw_msg", table)
table = {{},{}}
table[1]["key"] = "name"
table[1]["value"] = "大运"
table[2]["key"] = "msg"
table[2]["value"] = "看来我得开始认真了"
queueDelayEvent(1000, "draw_msg", table)

sleep(2000)

queueEvent("stopAll_msg", table)
table = {{}}
table[1]["key"] = "key"
table[1]["value"] = "大运"
queueEvent("playSound", table)

table = {{},{},{},{},{}}
table[1]["key"] = "id"
table[1]["value"] = 18
table[2]["key"] = "x"
table[2]["value"] = 15
table[3]["key"] = "w"
table[3]["value"] = 9
table[4]["key"] = "h"
table[4]["value"] = 5
table[5]["key"] = "isRepeat"
table[5]["value"] = true
queueEvent("setBlock", table)
for i = 0, 20, 1 do
    for i = 0, 9, 1 do
        content = getContent()
        player = content["player"]
        table = {{},{},{}}
        table[3]["key"] = "id"
        table[3]["value"] = 18
        table[1]["key"] = "y"
        table[1]["value"] = player["playerY"] - 2
        table[2]["key"] = "isDraw"
        table[2]["value"] = not en
        en = not en
        queueDelayEvent(i * 100, "setBlock", table)
    end
    content = getContent()
    player = content["player"]
    table = {{},{},{}}
    table[2]["key"] = "y"
    table[2]["value"] = player["playerY"] - 2
    for i = 0, 20, 1 do
        table[3]["key"] = "id"
        table[3]["value"] = 3
        table[1]["key"] = "x"
        table[1]["value"] = 8 + i
        content = getContent()
        player = content["player"]
        if isCollide(table[1]["value"], table[2]["value"], 5, 5, player["playerX"], player["playerY"]) then
            table1 = {{},{}}
            table1[1]["key"] = "name"
            table1[1]["value"] = "大运"
            table1[2]["key"] = "msg"
            table1[2]["value"] = "过家家的游戏就到此为止吧"
            queueEvent("draw_msg", table1)
            queueEvent("stopAll", table1)
            table1 = {{}}
            table1[1]["key"] = "key"
            table1[1]["value"] = "dead"
            queueEvent("playSound", table1)
            table1[1]["key"] = "health"
            table1[1]["value"] = 0
            queueEvent("setPlayer", table1)
            table1 = {{},{},{},{},{},{}}
            table1[1]["key"] = "id"
            table1[1]["value"] = 4
            table1[2]["key"] = "x"
            table1[2]["value"] = player["playerX"] - 1
            table1[3]["key"] = "y"
            table1[3]["value"] = player["playerY"] - 1
            table1[4]["key"] = "w"
            table1[4]["value"] = 3
            table1[5]["key"] = "h"
            table1[5]["value"] = 3
            table1[6]["key"] = "isDraw"
            table1[6]["value"] = true
            queueEvent("setBlock", table1)
            while true do
                sleep(6000)
                table1 = {{},{}}
                table1[1]["key"] = "name"
                table1[1]["value"] = "你变成喵喵酱了，按R键再次踏上轮回"
                table1[2]["key"] = "msg"
                table1[2]["value"] = "你变成喵喵酱了，按R键再次踏上轮回"
                queueEvent("draw_msg", table1)
                while true do
                    sleep(1)
                end
            end
        end
        queueEvent("setBlock",table)
        sleep(35)
    end
end
--]]

table = {{}}
table[1]["key"] = "key"
table[1]["value"] = "door"
queueDelayEvent(1, "playSound", table)
table = {{}}
table[1]["key"] = "key"
table[1]["value"] = "home"
queueEvent("playBGM", table)
sleep(2900)
table = {{},{}}
table[1]["key"] = "name"
table[1]["value"] = "大运"
table[2]["key"] = "msg"
table[2]["value"] = "等着吧，我们还会再见的"
queueEvent("draw_msg", table)
table = {{},{},{}}
table[1]["key"] = "id"
table[1]["value"] = 23
table[2]["key"] = "isDraw"
table[2]["value"] = false
table[3]["key"] = "isCollide"
table[3]["value"] = false
queueEvent("setBlock", table)
table[1]["key"] = "id"
table[1]["value"] = 4
table[2]["key"] = "x"
table[2]["value"] = 100
table[3]["key"] = "y"
table[3]["value"] = 100
queueEvent("setBlock", table)