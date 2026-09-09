local content = getContent()
local player = content["player"]

local newH = player["playerHealth"] + 10

tab = {
    {["key"] = "health", ["value"] = newH}
}

queueEvent("setPlayer", tab)

tab = {
    {["key"] = "key", ["value"] = "wow2"}
}

queueEvent("playSound", tab)