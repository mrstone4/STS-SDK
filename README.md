## 安装与配置

### 1. 环境准备
- 需要Java开发环境（仅支持JAVA8 or JDK1.8，其他版本极易出现错误）
- 需要Maven构建工具
- 游戏需要安装ModTheSpire和BaseMod框架，可以从steam中订阅（推荐），或在git仓库下载构建

### 2. 依赖配置
- 本项目不包含`desktop-1.0.jar`、`BaseMod.jar`、`ModTheSpire.jar`
- 请自行将上述jar包导入到maven本地仓库
- 请更改pom.xml中的绝对地址，改成自己maven仓库的jar的地址！！！

### 3. 编译
```bash
mvn package
```
编译后将在`target/`目录生成`stssdk-1.0-SNAPSHOT.jar`文件

### 4. 部署
将生成的jar文件放到SlayTheSpire/mods/目录下即可

## API使用示例

### 1. 检查服务器状态
```bash
curl -X GET http://127.0.0.1:9191/ping
```
返回：`{"status":"ok"}`

### 2. 获取游戏状态
```bash
curl -X POST http://127.0.0.1:9191/ -H "Content-Type: application/json" -d '{"cmd":"get_state"}'
```

### 3. 执行游戏操作

#### 出牌
```bash
curl -X POST http://127.0.0.1:9191/ -H "Content-Type: application/json" -d '{
  "cmd":"play_card",
  "uuid":"card-uuid-here",
  "targetId":"monster-id-here" 
}'
```

#### 使用药水
```bash
# 通过药水ID使用药水
curl -X POST http://127.0.0.1:9191/ -H "Content-Type: application/json" -d '{
  "cmd":"use_potion",
  "potionId":"potion-id-here"
}'

# 通过药水槽位使用药水
curl -X POST http://127.0.0.1:9191/ -H "Content-Type: application/json" -d '{
  "cmd":"use_potion",
  "slotIndex":0
}'
```

#### 结束回合
```bash
curl -X POST http://127.0.0.1:9191/ -H "Content-Type: application/json" -d '{"cmd":"end_turn"}'
```

#### 获取怪物信息
```bash
curl -X POST http://127.0.0.1:9191/ -H "Content-Type: application/json" -d '{"cmd":"get_monsters"}'
```

### 4. 获取游戏数据

#### 获取玩家信息
```bash
curl -X GET http://127.0.0.1:9191/api/player
```

#### 获取手牌信息
```bash
curl -X GET http://127.0.0.1:9191/api/hand
```

#### 获取药水信息
```bash
curl -X GET http://127.0.0.1:9191/api/potions
```

## 注意事项

1. **安全考虑**
   - 服务器默认绑定到127.0.0.1，仅允许本地访问
   - 如果需要外部访问，请添加适当的认证机制

2. **线程安全**
   - 所有修改游戏状态的动作都通过`AbstractDungeon.actionManager.addToBottom`排队执行

3. **游戏兼容性**
   - 该Mod依赖于特定版本的游戏API，可能需要根据游戏更新进行调整

## 作者的话
   - 本项目大量使用AI生成代码，所以有很多比较阴间的错误以及没考虑的问题（比如在命令执行里有两套出牌的命令？还有没有处理有弃牌效果的卡的弃牌效果。。。）
   - 目前项目还在测试阶段，很多问题还需要测试及更改，请见谅喵

## 开发指南

### 添加新命令
1. 在`CommandExecutor.java`中添加新的命令处理逻辑
2. 在`Bridge.java`中实现具体的游戏操作
3. 根据需要创建新的Action类

### 调试技巧
- 启动游戏后，在控制台查看`[STSSDK] Server started on port 9191`确认服务器启动成功
- 使用curl命令或其他HTTP客户端测试API接口
- 查看游戏日志了解详细的执行情况

## 常见问题

### Q: 服务器无法启动
A: 检查端口是否被占用，尝试修改`SDKEntry.java`中的端口号

### Q: API请求返回错误
A: 检查请求格式是否正确，特别是JSON格式和必需的字段

### Q: 游戏崩溃
A: 确保使用了兼容的ModTheSpire和BaseMod版本