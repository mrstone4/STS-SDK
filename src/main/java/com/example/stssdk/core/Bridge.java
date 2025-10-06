package com.example.stssdk.core;
import com.example.stssdk.action.EndTurnAction;
import com.example.stssdk.action.PlayCardByUuidAction;
import com.example.stssdk.action.UsePotionAction;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.example.stssdk.core.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 游戏桥接器，负责与游戏核心系统交互
 * 提供获取游戏状态和执行游戏操作的方法
 */
public class Bridge {
    private static final Logger logger = LoggerFactory.getLogger(Bridge.class);

    /**
     * 获取当前游戏状态
     * @return 包含游戏状态信息的JSON对象
     */
    public JsonObject getGameState() {
        JsonObject state = new JsonObject();
        try {
            AbstractPlayer p = AbstractDungeon.player;
            if (p == null) {
                state.addProperty("error", "player is null");
                return state;
            }

            // 获取玩家基本信息
            state.addProperty("hp", p.currentHealth);
            state.addProperty("maxHp", p.maxHealth);

            // 获取能量信息，使用try-catch处理不同版本可能的字段差异
            try {
                state.addProperty("energy", p.energy.energy);
            } catch (Exception ex) {
                state.addProperty("energy", -1);
            }

            // 获取手牌信息和各牌堆数量
            JsonArray hand = Serializer.cardsToJson(p.hand.group);
            state.add("hand", hand);
            state.addProperty("drawPileCount", p.drawPile.size());
            state.addProperty("discardPileCount", p.discardPile.size());
            
            // 添加遗物信息
            JsonArray relics = Serializer.relicsToJson(p.relics);
            state.add("relics", relics);
            
            // 添加药水信息
            JsonArray potions = Serializer.potionsToJson(p.potions);
            state.add("potions", potions);
            
            // 添加敌方信息
            MonsterGroup monsters = AbstractDungeon.getCurrRoom() != null ? AbstractDungeon.getCurrRoom().monsters : null;
            JsonArray monsterInfo = Serializer.monstersToJson(monsters);
            state.add("monsters", monsterInfo);

            return state;
        } catch (Exception e) {
            state.addProperty("error", e.getMessage());
            logger.error("Error getting game state", e);
            return state;
        }
    }

    /**
     * 获取玩家遗物信息
     * @return 包含遗物信息的JSON数组
     */
    public JsonArray getPlayerRelics() {
        AbstractPlayer p = AbstractDungeon.player;
        if (p != null) {
            return Serializer.relicsToJson(p.relics);
        }
        return new JsonArray();
    }
    
    /**
     * 获取玩家药水信息
     * @return 包含药水信息的JSON数组
     */
    public JsonArray getPlayerPotions() {
        AbstractPlayer p = AbstractDungeon.player;
        if (p != null) {
            return Serializer.potionsToJson(p.potions);
        }
        return new JsonArray();
    }
    
    /**
     * 获取敌方信息
     * @return 包含敌方信息的JSON数组
     */
    public JsonArray getMonstersInfo() {
        MonsterGroup monsters = AbstractDungeon.getCurrRoom() != null ? AbstractDungeon.getCurrRoom().monsters : null;
        return Serializer.monstersToJson(monsters);
    }

    /**
     * 播放指定UUID的卡牌
     * @param uuid 卡牌的唯一标识符
     * @return 包含操作结果的JSON对象
     */
    public JsonObject playCard(String uuid) {
        return playCard(uuid, null);
    }

    /**
     * 播放指定UUID的卡牌，并指定目标
     * @param uuid 卡牌的唯一标识符
     * @param targetId 目标怪物的ID
     * @return 包含操作结果的JSON对象
     */
    public JsonObject playCard(String uuid, String targetId) {
        JsonObject result = new JsonObject();
        try {
            logger.info("Request to play card with UUID: {}", uuid);
            PlayCardByUuidAction action = new PlayCardByUuidAction(uuid, targetId);
            
            // 因为是异步执行，我们不能立即获取结果
            // 所以先返回排队状态，实际结果需要通过其他方式获取
            AbstractDungeon.actionManager.addToBottom(action);
            result.addProperty("status", "queued");
            logger.info("Card play action queued");
        } catch (Exception e) {
            result.addProperty("error", e.getMessage());
            logger.error("Error queueing card play action", e);
        }
        return result;
    }

    /**
     * 结束当前回合
     * @return 包含操作结果的JSON对象
     */
    public JsonObject endTurn() {
        JsonObject result = new JsonObject();
        try {
            logger.info("Request to end turn");
            AbstractDungeon.actionManager.addToBottom(new EndTurnAction());
            result.addProperty("status", "queued");
        } catch (Exception e) {
            result.addProperty("error", e.getMessage());
            logger.error("Error ending turn", e);
        }
        return result;
    }
    
    /**
     * 使用指定ID的药水
     * @param potionId 药水的ID
     * @return 包含操作结果的JSON对象
     */
    public JsonObject usePotionById(String potionId) {
        JsonObject result = new JsonObject();
        try {
            logger.info("Request to use potion by ID: {}", potionId);
            AbstractDungeon.actionManager.addToBottom(new UsePotionAction(potionId));
            result.addProperty("status", "queued");
        } catch (Exception e) {
            result.addProperty("error", e.getMessage());
            logger.error("Error using potion by ID", e);
        }
        return result;
    }
    
    /**
     * 使用指定槽位的药水
     * @param slotIndex 药水的槽位索引（从0开始）
     * @return 包含操作结果的JSON对象
     */
    public JsonObject usePotionBySlot(int slotIndex) {
        JsonObject result = new JsonObject();
        try {
            logger.info("Request to use potion by slot index: {}", slotIndex);
            AbstractDungeon.actionManager.addToBottom(new UsePotionAction(slotIndex));
            result.addProperty("status", "queued");
        } catch (Exception e) {
            result.addProperty("error", e.getMessage());
            logger.error("Error using potion by slot index", e);
        }
        return result;
    }
}