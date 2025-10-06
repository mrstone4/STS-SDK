package com.example.stssdk;

import basemod.BaseMod;
import basemod.interfaces.PostInitializeSubscriber;
import com.example.stssdk.server.SDKServer;

import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;

import java.io.IOException;

/**
 * STS-SDK 入口类，负责在游戏启动后初始化并启动HTTP服务器
 * 使用@SpireInitializer注解使ModTheSpire能够识别并初始化该类
 */
@SpireInitializer
public class SDKEntry implements PostInitializeSubscriber {

    // SDK服务器实例
    private static SDKServer server;

    /**
     * ModTheSpire调用的初始化方法
     * 该方法会在游戏启动时被自动调用
     */
    public static void initialize() {
        new SDKEntry();
    }

    /**
     * 构造函数，将当前实例注册为PostInitializeSubscriber
     * 确保receivePostInitialize方法会在游戏初始化后被调用
     */
    public SDKEntry() {
        BaseMod.subscribe(this);
    }

    /**
     * 游戏初始化完成后执行的方法
     * 在独立线程中启动SDK服务器，避免阻塞游戏主线程
     */
    @Override
    public void receivePostInitialize() {
        // 创建新线程启动服务器，避免阻塞游戏主线程
        new Thread(() -> {
            try {
                server = new SDKServer(9191);
                server.start();
                System.out.println("[STSSDK] Server started on port 9191");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, "STSSDK-Server-Thread").start();
    }
}
