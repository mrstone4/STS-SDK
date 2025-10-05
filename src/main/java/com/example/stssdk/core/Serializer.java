package com.example.stssdk.core;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.MonsterGroup;


import java.util.List;

/**
 * 序列化工具类，负责将游戏对象转换为JSON格式
 * 主要用于将卡牌信息序列化为JSON数组
 */
public class Serializer {

    /**
     * 将卡牌列表转换为JSON数组
     * @param cards 卡牌对象列表
     * @return 包含卡牌信息的JSON数组
     */
    public static JsonArray cardsToJson(List<AbstractCard> cards) {
        JsonArray arr = new JsonArray();
        for (AbstractCard c : cards) {
            try {
                JsonObject obj = new JsonObject();
                obj.addProperty("uuid", c.uuid.toString());
                obj.addProperty("id", c.cardID);
                obj.addProperty("name", c.name);
                obj.addProperty("cost", c.cost);
                obj.addProperty("type", c.type.name());
                obj.addProperty("rarity", c.rarity.name());
                obj.addProperty("upgraded", c.upgraded);
                arr.add(obj);
            } catch (Exception e) {
// 忽略无法序列化的卡
            }
        }
        return arr;
    }
    /**
     * 将遗物列表转换为JSON数组
     * @param relics 遗物对象列表
     * @return 包含遗物信息的JSON数组
     */
    public static JsonArray relicsToJson(List<AbstractRelic> relics) {
        JsonArray arr = new JsonArray();
        for (AbstractRelic r : relics) {
            try {
                JsonObject obj = new JsonObject();
                obj.addProperty("id", r.relicId);
                obj.addProperty("name", r.name);
                obj.addProperty("description", r.description);
                obj.addProperty("tier", r.tier.name());
                arr.add(obj);
            } catch (Exception e) {
                // 忽略无法序列化的遗物
            }
        }
        return arr;
    }
    
    /**
     * 将药水列表转换为JSON数组
     * @param potions 药水对象列表
     * @return 包含药水信息的JSON数组
     */
    public static JsonArray potionsToJson(List<AbstractPotion> potions) {
        JsonArray arr = new JsonArray();
        for (AbstractPotion p : potions) {
            try {
                JsonObject obj = new JsonObject();
                obj.addProperty("id", p.ID);
                obj.addProperty("name", p.name);
                obj.addProperty("description", p.description);
                // obj.addProperty("color", p.potionColor.name());
                obj.addProperty("slot", p.slot);
                arr.add(obj);
            } catch (Exception e) {
                // 忽略无法序列化的药水
            }
        }
        return arr;
    }
    
    /**
     * 将敌方怪物信息转换为JSON数组
     * @param monsters 怪物组
     * @return 包含敌方信息的JSON数组
     */
    public static JsonArray monstersToJson(MonsterGroup monsters) {
        JsonArray arr = new JsonArray();
        if (monsters != null) {
            for (AbstractMonster m : monsters.monsters) {
                try {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("id", m.id);
                    obj.addProperty("name", m.name);
                    obj.addProperty("currentHp", m.currentHealth);
                    obj.addProperty("maxHp", m.maxHealth);
                    obj.addProperty("currentBlock", m.currentBlock);
                    obj.addProperty("isDead", m.isDeadOrEscaped());
                    obj.addProperty("isEscaped", m.isEscaping);
                    
                    // 添加怪物的下次攻击信息
                    if (m.intent != null) {
                        obj.addProperty("intent", m.intent.name());
                    }
                    
                    // 添加怪物的位置信息
                    obj.addProperty("x", m.drawX);
                    obj.addProperty("y", m.drawY);
                    
                    arr.add(obj);
                } catch (Exception e) {
                    // 忽略无法序列化的怪物
                }
            }
        }
        return arr;
    }
}