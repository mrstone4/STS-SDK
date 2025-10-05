package com.example.stssdk.action;


import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;


public class PlayCardByUuidAction extends AbstractGameAction {
    private final String uuid;


    public PlayCardByUuidAction(String uuid) {
        this.uuid = uuid;
    }


    @Override
    public void update() {
        try {
            AbstractPlayer p = AbstractDungeon.player;
            if (p == null) {
                this.isDone = true;
                return;
            }


            AbstractMonster target = (AbstractDungeon.getCurrRoom() != null && AbstractDungeon.getCurrRoom().monsters != null)
                    ? AbstractDungeon.getCurrRoom().monsters.getRandomMonster(true)
                    : null;


            for (AbstractCard c : p.hand.group) {
                if (c.uuid.toString().equals(uuid)) {
// 注意：调用 use() 需要在主线程，并且会走正常的消耗/触发逻辑
                    c.use(p, target);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.isDone = true;
    }
}