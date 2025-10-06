package com.example.stssdk.core;


import com.google.gson.JsonObject;
import com.example.stssdk.core.Bridge;
import com.google.gson.JsonArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 命令执行器，负责处理从HTTP请求中接收的游戏操作命令
 * 将命令转发给Bridge执行实际的游戏操作
 */
public class CommandExecutor {
    private static final Logger logger = LoggerFactory.getLogger(CommandExecutor.class);
    
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
            logger.debug("Received command: {}", req.toString());
            
            // 检查是否包含cmd字段
            if (!req.has("cmd")) {
                res.addProperty("error", "missing cmd field");
                logger.warn("Command missing 'cmd' field");
                return res;
            }

            // 根据命令类型执行不同的操作
            String cmd = req.get("cmd").getAsString();
            switch (cmd) {
                case "get_state":
                    logger.info("Executing command: get_state");
                    return bridge.getGameState();
                case "play_card":
                    logger.info("Executing command: play_card");
                    if (!req.has("uuid")) {
                        res.addProperty("error", "missing uuid");
                        logger.warn("play_card command missing 'uuid' field");
                        return res;
                    }
                    String uuid = req.get("uuid").getAsString();
                    
                    // 检查是否指定了目标
                    String targetId = null;
                    if (req.has("targetId")) {
                        targetId = req.get("targetId").getAsString();
                        logger.debug("play_card command with targetId: {}", targetId);
                    }
                    
                    return bridge.playCard(uuid, targetId);
                case "end_turn":
                    logger.info("Executing command: end_turn");
                    return bridge.endTurn();
                case "use_potion":
                    logger.info("Executing command: use_potion");
                    if (req.has("potionId")) {
                        String potionId = req.get("potionId").getAsString();
                        logger.debug("use_potion command with potionId: {}", potionId);
                        return bridge.usePotionById(potionId);
                    } else if (req.has("slotIndex")) {
                        int slotIndex = req.get("slotIndex").getAsInt();
                        logger.debug("use_potion command with slotIndex: {}", slotIndex);
                        return bridge.usePotionBySlot(slotIndex);
                    } else {
                        res.addProperty("error", "missing potionId or slotIndex");
                        logger.warn("use_potion command missing required fields");
                        return res;
                    }
                case "get_monsters":
                    logger.info("Executing command: get_monsters");
                    JsonArray monsters = bridge.getMonstersInfo();
                    res.add("monsters", monsters);
                    return res;
                case "execute_action":
                    // 统一的动作执行接口
                    if (!req.has("action_type")) {
                        res.addProperty("error", "missing action_type");
                        return res;
                    }
                    
                    String actionType = req.get("action_type").getAsString();
                    
                    switch (actionType) {
                        case "play_card":
                            if (!req.has("uuid")) {
                                res.addProperty("error", "missing uuid for play_card action");
                                return res;
                            }
                            bridge.playCard(req.get("uuid").getAsString());
                            break;
                        case "end_turn":
                            bridge.endTurn();
                            break;
                        case "use_potion":
                            if (req.has("potionId")) {
                                bridge.usePotionById(req.get("potionId").getAsString());
                            } else if (req.has("slotIndex")) {
                                bridge.usePotionBySlot(req.get("slotIndex").getAsInt());
                            } else {
                                res.addProperty("error", "missing potionId or slotIndex for use_potion action");
                                return res;
                            }
                            break;
                        default:
                            res.addProperty("error", "unknown action_type: " + actionType);
                            return res;
                    }
                    
                    res.addProperty("status", "queued");
                    return res;
                default:
                    res.addProperty("error", "unknown cmd: " + cmd);
                    logger.warn("Unknown command: {}", cmd);
                    return res;
            }
        } catch (Exception e) {
            res.addProperty("error", e.getMessage());
            logger.error("Error executing command", e);
            return res;
        }
    }
}