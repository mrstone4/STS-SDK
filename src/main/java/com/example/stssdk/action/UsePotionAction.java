package com.example.stssdk.action;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.potions.AbstractPotion;

/**
 * 使用药水的游戏操作
 * 根据药水的ID或槽位使用对应的药水
 */
public class UsePotionAction extends AbstractGameAction {
    private final String potionId;
    private final Integer slotIndex;

    /**
     * 根据药水ID使用药水
     * @param potionId 药水的ID
     */
    public UsePotionAction(String potionId) {
        this.potionId = potionId;
        this.slotIndex = null;
    }

    /**
     * 根据药水槽位使用药水
     * @param slotIndex 药水的槽位索引（从0开始）
     */
    public UsePotionAction(int slotIndex) {
        this.potionId = null;
        this.slotIndex = slotIndex;
    }

    @Override
    public void update() {
        try {
            AbstractPlayer p = AbstractDungeon.player;
            if (p == null) {
                this.isDone = true;
                return;
            }

            // 如果提供了药水ID，则根据ID查找并使用
            if (potionId != null) {
                for (int i = 0; i < p.potions.size(); i++) {
                    AbstractPotion potion = p.potions.get(i);
                    if (potion.ID.equals(potionId)) {
                        potion.use(p);
                        // 从药水列表中移除已使用的药水
                        p.potions.remove(i);
                        break;
                    }
                }
            }
            // 如果提供了槽位索引，则根据索引使用药水
            else if (slotIndex != null && slotIndex >= 0 && slotIndex < p.potions.size()) {
                AbstractPotion potion = p.potions.get(slotIndex);
                potion.use(p);
                // 从药水列表中移除已使用的药水
                p.potions.remove(slotIndex.intValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.isDone = true;
    }
}