table = {{}}
table[1]["key"] = "health"
table[1]["value"] = 0
queueEvent("setPlayer", table)
queueEvent("stopAll_msg", table)
table[1]["key"] = "key"
table[1]["value"] = "wow1"
queueEvent("playSound", table)
table[1]["key"] = "image"
table[1]["value"] = "icon/feet-1.png"
queueDelayEvent(0, "draw_image", table)
table[1]["key"] = "key"
table[1]["value"] = "scream"
queueDelayEvent(2500, "playSound", table)
table[1]["key"] = "image"
table[1]["value"] = "icon/feet-2.png"
queueDelayEvent(2500, "draw_image", table)
table[1]["key"] = "image"
table[1]["value"] = "icon/feet-3.png"
queueDelayEvent(4000, "draw_image", table)
table = {{},{}}
table[1]["key"] = "name"
table[1]["value"] = "system"
table[2]["key"] = "msg"
table[2]["value"] = "你撞到小脚趾了!!! 按R键重来"
queueDelayEvent(8000, "draw_msg", table)