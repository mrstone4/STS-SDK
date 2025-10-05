package com.example.stssdk.action;


import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;


public class EndTurnAction extends AbstractGameAction {


    @Override
    public void update() {
        try {
            AbstractDungeon.actionManager.addToBottom(new com.megacrit.cardcrawl.actions.common.EndTurnAction());
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.isDone = true;
    }
}