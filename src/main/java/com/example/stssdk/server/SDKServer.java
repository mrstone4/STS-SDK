package com.example.stssdk.server;


import com.example.stssdk.core.CommandExecutor;
import fi.iki.elonen.NanoHTTPD;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.example.stssdk.core.Serializer;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * STS-SDK HTTP服务器实现
 * 基于NanoHTTPD轻量级HTTP服务器库，提供游戏数据API接口
 */
public class SDKServer extends NanoHTTPD {

    // 命令执行器，负责处理POST请求中的游戏操作命令
    private final CommandExecutor executor = new CommandExecutor();

    /**
     * 构造函数，创建并初始化HTTP服务器
     * @param port 服务器监听端口
     * @throws IOException 服务器启动失败时抛出异常
     */
    public SDKServer(int port) throws IOException {
        super("127.0.0.1", port);
    }

    /**
     * 处理HTTP请求的核心方法
     * 根据请求方法(GET/POST)和URI路径分发到不同的处理逻辑
     * @param session HTTP会话对象，包含请求信息
     * @return HTTP响应对象
     */
    @Override
    public Response serve(IHTTPSession session) {
        try {
            String uri = session.getUri();
            Method method = session.getMethod();

            // 处理POST请求 - 用于执行游戏操作命令
            if (Method.POST.equals(method)) {
                // 仅支持 application/json 的 POST
                Map<String, String> body = new HashMap<>();
                session.parseBody(body);
                String postData = body.getOrDefault("postData", "{}");


                JsonObject req = JsonParser.parseString(postData).getAsJsonObject();
                JsonObject res = executor.execute(req);
                return newFixedLengthResponse(Response.Status.OK, "application/json", res.toString());
            }


            // 处理GET请求 - 用于查看游戏元素数据   
            if (Method.GET.equals(method)) {

                AbstractPlayer p = AbstractDungeon.player;
                // 检查是否有玩家(游戏是否已开始)
                if (p == null) {
                    JsonObject error = new JsonObject();
                    error.addProperty("error", "player is null");
                    return newFixedLengthResponse(Response.Status.OK, "application/json", error.toString());
                }

                // 根据URI路径分发到不同的处理方法
                // 玩家基本信息
                if ("/api/player".equals(uri)) {
                    return handleGetPlayer(p);
                }
                // 手牌信息
                else if ("/api/hand".equals(uri)) {
                    return handleGetHand(p);
                }
                // 抽牌堆信息
                else if ("/api/drawpile".equals(uri)) {
                    return handleGetDrawPile(p);
                }
                // 弃牌堆信息
                else if ("/api/discardpile".equals(uri)) {
                    return handleGetDiscardPile(p);
                }
                // 完整牌组信息
                else if ("/api/deck".equals(uri)) {
                    return handleGetDeck(p);
                }
                // 玩家遗物信息
                else if ("/api/relics".equals(uri)) {
                    return handleGetRelics(p);
                }
                // 玩家药水信息
                else if ("/api/potions".equals(uri)) {
                    return handleGetPotions(p);
                }
                // 敌方信息
                else if ("/api/monsters".equals(uri)) {
                    return handleGetMonsters();
                }
                // 完整游戏状态
                else if ("/api/state".equals(uri)) {
                    JsonObject state = executor.execute(createGetStateCommand());
                    return newFixedLengthResponse(Response.Status.OK, "application/json", state.toString());
                }
                // 检测连接状态
                else if ("/ping".equals(uri)) {
                    JsonObject ok = new JsonObject();
                    ok.addProperty("status", "ok");
                    return newFixedLengthResponse(Response.Status.OK, "application/json", ok.toString());
                }
            }

            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not found");

        } catch (Exception e) {
            e.printStackTrace();
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", e.getMessage());
        }
    }

    // 辅助方法：创建获取游戏状态的命令
    private JsonObject createGetStateCommand() {
        JsonObject cmd = new JsonObject();
        cmd.addProperty("cmd", "get_state");
        return cmd;
    }

    /**
     * 处理获取玩家基本信息的请求
     * @param p 玩家对象
     * @return 包含玩家信息的HTTP响应
     */
    private Response handleGetPlayer(AbstractPlayer p) {
        JsonObject playerInfo = new JsonObject();
        playerInfo.addProperty("hp", p.currentHealth);
        playerInfo.addProperty("maxHp", p.maxHealth);
        try {
            playerInfo.addProperty("energy", p.energy.energy);
        } catch (Exception ex) {
            playerInfo.addProperty("energy", -1);
        }
        playerInfo.addProperty("gold", p.gold);
        playerInfo.addProperty("currentBlock", p.currentBlock);
        playerInfo.addProperty("ascensionLevel", AbstractDungeon.ascensionLevel);
        playerInfo.addProperty("character", p.chosenClass.name());
        return newFixedLengthResponse(Response.Status.OK, "application/json", playerInfo.toString());
    }

    /**
     * 处理获取手牌信息的请求
     * @param p 玩家对象
     * @return 包含手牌信息的HTTP响应
     */
    private Response handleGetHand(AbstractPlayer p) {
        JsonObject result = new JsonObject();
        JsonArray handCards = Serializer.cardsToJson(p.hand.group);
        result.add("cards", handCards);
        result.addProperty("count", p.hand.size());
        return newFixedLengthResponse(Response.Status.OK, "application/json", result.toString());
    }

    /**
     * 处理获取抽牌堆信息的请求
     * @param p 玩家对象
     * @return 包含抽牌堆信息的HTTP响应
     */
    private Response handleGetDrawPile(AbstractPlayer p) {
        JsonObject result = new JsonObject();
        JsonArray drawPileCards = Serializer.cardsToJson(p.drawPile.group);
        result.add("cards", drawPileCards);
        result.addProperty("count", p.drawPile.size());
        return newFixedLengthResponse(Response.Status.OK, "application/json", result.toString());
    }

    /**
     * 处理获取弃牌堆信息的请求
     * @param p 玩家对象
     * @return 包含弃牌堆信息的HTTP响应
     */
    private Response handleGetDiscardPile(AbstractPlayer p) {
        JsonObject result = new JsonObject();
        JsonArray discardPileCards = Serializer.cardsToJson(p.discardPile.group);
        result.add("cards", discardPileCards);
        result.addProperty("count", p.discardPile.size());
        return newFixedLengthResponse(Response.Status.OK, "application/json", result.toString());
    }

    /**
     * 处理获取完整牌组信息的请求
     * @param p 玩家对象
     * @return 包含完整牌组信息的HTTP响应
     */
    private Response handleGetDeck(AbstractPlayer p) {
        JsonObject result = new JsonObject();

        // 合并所有卡牌组
        JsonArray allCards = new JsonArray();

        JsonArray handCards = Serializer.cardsToJson(p.hand.group);
        JsonArray drawPileCards = Serializer.cardsToJson(p.drawPile.group);
        JsonArray discardPileCards = Serializer.cardsToJson(p.discardPile.group);

        // 添加所有卡牌到一个数组
        handCards.forEach(allCards::add);
        drawPileCards.forEach(allCards::add);
        discardPileCards.forEach(allCards::add);

        result.add("allCards", allCards);
        result.add("hand", handCards);
        result.add("drawPile", drawPileCards);
        result.add("discardPile", discardPileCards);
        result.addProperty("totalCount", allCards.size());

        return newFixedLengthResponse(Response.Status.OK, "application/json", result.toString());
    }
    
    /**
     * 处理获取玩家遗物信息的请求
     * @param p 玩家对象
     * @return 包含遗物信息的HTTP响应
     */
    private Response handleGetRelics(AbstractPlayer p) {
        JsonObject result = new JsonObject();
        JsonArray relics = Serializer.relicsToJson(p.relics);
        result.add("relics", relics);
        result.addProperty("count", relics.size());
        return newFixedLengthResponse(Response.Status.OK, "application/json", result.toString());
    }
    
    /**
     * 处理获取玩家药水信息的请求
     * @param p 玩家对象
     * @return 包含药水信息的HTTP响应
     */
    private Response handleGetPotions(AbstractPlayer p) {
        JsonObject result = new JsonObject();
        JsonArray potions = Serializer.potionsToJson(p.potions);
        result.add("potions", potions);
        result.addProperty("count", potions.size());
        return newFixedLengthResponse(Response.Status.OK, "application/json", result.toString());
    }
    
    /**
     * 处理获取敌方信息的请求
     * @return 包含敌方信息的HTTP响应
     */
    private Response handleGetMonsters() {
        JsonObject result = new JsonObject();
        JsonArray monsters = executor.execute(createGetMonstersCommand()).getAsJsonArray("monsters");
        result.add("monsters", monsters);
        result.addProperty("count", monsters.size());
        return newFixedLengthResponse(Response.Status.OK, "application/json", result.toString());
    }
    
    // 辅助方法：创建获取敌方信息的命令
    private JsonObject createGetMonstersCommand() {
        JsonObject cmd = new JsonObject();
        cmd.addProperty("cmd", "get_monsters");
        return cmd;
    }
}