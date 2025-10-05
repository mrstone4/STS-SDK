package com.example.stssdk.core;


import com.google.gson.JsonObject;
import com.example.stssdk.core.Bridge;
import com.google.gson.JsonArray;

/**
 * 命令执行器，负责处理从HTTP请求中接收的游戏操作命令
 * 将命令转发给Bridge执行实际的游戏操作
 */
public class CommandExecutor {

    // 游戏桥接器，负责与游戏核心系统交互
    private final Bridge bridge = new Bridge();

    /**
     * 执行命令的核心方法
     * @param req 包含命令信息的JSON对象
     * @return 命令执行结果的JSON对象
     */
    public JsonObject execute(JsonObject req) {
        JsonObject res = new JsonObject();
        try {
            // 检查是否包含cmd字段
            if (!req.has("cmd")) {
                res.addProperty("error", "missing cmd field");
                return res;
            }

            // 根据命令类型执行不同的操作
            String cmd = req.get("cmd").getAsString();
            switch (cmd) {
                case "get_state":
                    return bridge.getGameState();
                case "play_card":
                    if (!req.has("uuid")) {
                        res.addProperty("error", "missing uuid");
                        return res;
                    }
                    String uuid = req.get("uuid").getAsString();
                    bridge.playCard(uuid);
                    res.addProperty("status", "queued");
                    return res;
                case "end_turn":
                    bridge.endTurn();
                    res.addProperty("status", "queued");
                    return res;
                case "get_monsters":
                    JsonArray monsters = bridge.getMonstersInfo();
                    res.add("monsters", monsters);
                    return res;
                default:
                    res.addProperty("error", "unknown cmd: " + cmd);
                    return res;
            }
        } catch (Exception e) {
            res.addProperty("error", e.getMessage());
            return res;
        }
    }
}