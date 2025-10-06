package com.example.stssdk.action;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDiscardAction;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayCardByUuidAction extends AbstractGameAction {
    private static final Logger logger = LoggerFactory.getLogger(PlayCardByUuidAction.class);
    private final String uuid;
    private final String targetId;
    private ActionResult result;

    // 用于存储操作结果的静态内部类
    public static class ActionResult {
        public boolean success;
        public String message;
        public String error;

        public ActionResult(boolean success, String message, String error) {
            this.success = success;
            this.message = message;
            this.error = error;
        }
    }

    // 默认构造函数，随机选择目标
    public PlayCardByUuidAction(String uuid) {
        this.uuid = uuid;
        this.targetId = null;
    }

    // 支持指定目标的构造函数
    public PlayCardByUuidAction(String uuid, String targetId) {
        this.uuid = uuid;
        this.targetId = targetId;
    }

    // 获取操作结果
    public ActionResult getResult() {
        return result;
    }

    @Override
    public void update() {
        try {
            AbstractPlayer p = AbstractDungeon.player;
            if (p == null) {
                result = new ActionResult(false, null, "Player is null");
                logger.error("Failed to play card with UUID {}: Player is null", uuid);
                this.isDone = true;
                return;
            }

            // 查找目标怪物
            AbstractMonster target = null;
            if (targetId != null && !targetId.isEmpty()) {
                MonsterGroup monsters = AbstractDungeon.getCurrRoom() != null ? AbstractDungeon.getCurrRoom().monsters : null;
                if (monsters != null) {
                    for (AbstractMonster m : monsters.monsters) {
                        if (m.id.equals(targetId)) {
                            target = m;
                            break;
                        }
                    }
                }
                if (target == null) {
                    logger.warn("Target monster with ID {} not found, using random target", targetId);
                }
            }
            
            // 如果没有找到指定目标或未指定目标，则使用随机目标
            if (target == null) {
                target = (AbstractDungeon.getCurrRoom() != null && AbstractDungeon.getCurrRoom().monsters != null)
                        ? AbstractDungeon.getCurrRoom().monsters.getRandomMonster(true)
                        : null;
            }

            // 查找要使用的卡牌
            AbstractCard cardToPlay = null;
            for (AbstractCard c : p.hand.group) {
                if (c.uuid.toString().equals(uuid)) {
                    cardToPlay = c;
                    break;
                }
            }

            if (cardToPlay == null) {
                result = new ActionResult(false, null, "Card not found in hand");
                logger.error("Failed to play card: Card with UUID {} not found in hand", uuid);
            } else {
                // 检查能量是否足够
                if (p.energy.energy >= cardToPlay.cost) {
                    logger.info("Playing card: {} (UUID: {}, Cost: {})", cardToPlay.name, uuid, cardToPlay.cost);
                    
                    // 扣除能量
                    p.energy.use(cardToPlay.cost);
                    
                    // 播放卡牌使用动画和音效
                    cardToPlay.triggerOnScry();
                    cardToPlay.applyPowers();
                    
                    // 执行卡牌效果
                    cardToPlay.use(p, target);
                    
                    // 将卡牌从手牌中移除并添加到弃牌堆
                    p.hand.removeCard(cardToPlay);
                    
                    // 处理卡牌升级状态（如果有）
                    if (cardToPlay.upgraded) {
                        cardToPlay.upgraded = false;
                    }
                    
                    // 将卡牌添加到弃牌堆
                    AbstractDungeon.actionManager.addToBottom(
                        new MakeTempCardInDiscardAction(cardToPlay, 1));
                    
                    // 触发卡牌使用后的钩子（如遗物效果）
                    AbstractDungeon.player.hand.refreshHandLayout();
                    AbstractDungeon.onModifyPower();
                    
                    result = new ActionResult(true, "Card played successfully", null);
                    logger.info("Card played successfully: {}", cardToPlay.name);
                } else {
                    result = new ActionResult(false, null, "Not enough energy");
                    logger.error("Failed to play card: Not enough energy. Required: {}, Available: {}", 
                        cardToPlay.cost, p.energy.energy);
                }
            }
        } catch (Exception e) {
            result = new ActionResult(false, null, "Error: " + e.getMessage());
            logger.error("Error playing card with UUID {}", uuid, e);
        }
        this.isDone = true;
    }
}