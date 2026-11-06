# v1.0.1 构建与验证状态

日期：2026-11-06。

## 本次改动

- 四档模型缩放：1 / 1.25 / 1.5 / 1.8 倍；最大碰撞箱 0.9 × 0.9 格，不超过监守者。
- 四档最大生命值：6 / 12 / 20 / 30 点，即 3 / 6 / 10 / 15 颗心。
- 生命上限由服务端同步并存档，进入世界、升级、重生和管理员体型指令都会应用；只在增加上限时补足新增生命，不会因重复同步持续回血。
- 舌头不再使用调试方块渲染层；改用标准不透明实体模型和渲染命令队列，携带纹理、光照与法线。
- 舌头动画在提取阶段保存不可变快照，绘制阶段只使用快照；保留多舌头、伸出/收回和第一人称效果。
- 旧存档的食量、阶段和可吞噬等级保留。

## 已通过

- GitHub Actions 完整构建和发布：https://github.com/susu2020oxo/qingwa/actions/runs/42160261146
- 源码提交：`9bbf76683f5b9848ff64520c7ff2b53509c228db`
- `compileJava`、`compileClientJava`、Mixin 注解处理、`remapJar`、`check`、`build` 成功。
- **PASS: 241 growth rule checks**，含成长门槛、吞噬容量、体型边界、三颗心开局、升级保留伤势、重置不回血和死亡不复活检查。
- 已从公开直链实际下载 JAR 和 ZIP，校验归档结构、生命逻辑类、新纹理、版本元数据和 SHA-256。

```text
f476dfb2240844133bbb3e2fdf70e09704ac1ed25e062c8428c5199d48604ba3  FrogDevour-Fabric-1.21.10-v1.0.1.jar
0a64b8f19744746950ef627d02ab45dd93dc99309a22c4854908ef9cb9341fab  FrogDevour-Fabric-1.21.10-v1.0.1-install.zip

```

## 下载

- [v1.0.1 模组 JAR](https://github.com/susu2020oxo/qingwa/raw/refs/heads/downloads/downloads/FrogDevour-Fabric-1.21.10-v1.0.1.jar)
- [v1.0.1 ZIP 安装包](https://github.com/susu2020oxo/qingwa/raw/refs/heads/downloads/downloads/FrogDevour-Fabric-1.21.10-v1.0.1-install.zip)

移除旧版 JAR，只保留新版本。多人游戏中服务端和客户端一起更新。

## 尚待确认

已针对 Iris 的渲染兼容做代码修复，但没有得到具体光影包及版本，也未在用户的 Iris/显卡环境复现和验证闪屏是否完全消失。游戏内显示、生命存档迁移和联机按 `TESTING.md` 验收，不能用编译成功代替游戏实测。
