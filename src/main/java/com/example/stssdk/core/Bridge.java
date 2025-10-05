package com.example.stssdk.core;
import com.example.stssdk.action.EndTurnAction;


import com.example.stssdk.action.PlayCardByUuidAction;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.example.stssdk.core.Serializer;

/**
 * 游戏桥接器，负责与游戏核心系统交互
 * 提供获取游戏状态和执行游戏操作的方法
 */
public class Bridge {

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
// energy: try-catch if energy field name differs by version
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
     */
    public void playCard(String uuid) {
        AbstractDungeon.actionManager.addToBottom(new PlayCardByUuidAction(uuid));
    }

    /**
     * 结束当前回合
     */
    public void endTurn() {
        AbstractDungeon.actionManager.addToBottom(new EndTurnAction());
    }
}