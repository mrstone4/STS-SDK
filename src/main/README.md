1. **准备 BaseMod / 游戏 JAR**
- 本项目不包含 `desktop-1.0.jar`（游戏本体）与 BaseMod.jar。编译与运行需要这些依赖在 classpath 中。
- 将 `BaseMod.jar`（或者对应的 basemod 依赖）放在项目的 `lib/` 目录，并在 IDE 中把它加入到 classpath，或通过 Maven 的 system scope 引入（不推荐）。


2. **编译**
- `mvn package` 将生成 `target/stssdk-1.0-SNAPSHOT.jar`。把生成的 jar 放到 SlayTheSpire/mods/ 下即可（注意：mod 的 meta 信息和加载器要求请参考 ModTheSpire 文档）。


3. **测试**
- 启动游戏，确保 BaseMod 已加载并且你的 mod 出现在 mod 列表。
- 进入游戏后在控制台或日志中查看 `[STSSDK] Server started on port 9191`。
- 在本机运行：`curl -X GET http://127.0.0.1:9191/ping` 应该返回 `{"status":"ok"}`。
- 测试获取状态：
```bash
curl -X POST http://127.0.0.1:9191/ -H "Content-Type: application/json" -d '{"cmd":"get_state"}'
```


4. **注意事项**
- 所有会修改游戏状态的动作都通过 `AbstractDungeon.actionManager.addToBottom` 排队执行，确保线程安全。
- 如果要暴露网络访问，请务必仅绑定到 127.0.0.1，并加入认证（token）以避免安全问题。


---


祝你构建顺利！如果你需要，我可以：
- 把这个项目打包成一个 zip（包含全部文件）并提供下载链接；
- 继续把 `pom.xml` 修改为把 BaseMod.jar 作为本地依赖，然后生成可直接 `mvn package` 的配置；
- 把更多命令（如 add_card / get_monsters / use_potion）补充到 CommandExecutor 与 Bridge。