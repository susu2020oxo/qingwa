# v1.0.2 构建与验证状态

日期：2026-11-06。

## 本次改动

- 所有成长阶段的青蛙均永久水下呼吸：覆盖原版 `LivingEntity.canBreatheInWater` 判定，并在服务端恢复旧存档、重生或进入世界时残留的低氧气值，无需药水。
- 舌头最大长度与实际捕食距离按阶段变为 **6 / 12 / 24 / 40 格**；空放和命中动画继续使用同一服务端距离判定。
- 核对 Iris 源码，确认默认 R 同时触发“重新加载光影包”和吞噬。用户已确认调整光影重载按键后闪屏解决。
- 新版仅在支持吞噬的世界、非观察者且未打开菜单时，对与吞噬按键重合的 Iris 重载点击进行拦截；不会修改按键设置，也不影响其他 Iris 功能或不同按键的重载。
- 保留 v1.0.1 的四档体型、3/6/10/15 颗心和标准实体舌头渲染。

## 已通过

- GitHub Actions 完整构建和发布：https://github.com/susu2020oxo/qingwa/actions/runs/42161975391
- 源码提交：`b770851f00c91349db54486ee23f5da1109a8025`
- `compileJava`、`compileClientJava`、Mixin 注解处理、`remapJar`、`check`、`build` 成功。
- **PASS: 253 gameplay rule checks**，覆盖成长、吞噬容量、体型、血量、重载按键冲突及重绑定例外、舌头分档距离。
- 从公开直链下载并验证 JAR 与 ZIP：版本 1.0.2，水下呼吸和按键兼容 Mixin 已打包，目标方法映射已生成，文件 SHA-256 和 ZIP 完整性通过。
- 直接运行下载 JAR 中的纯规则类，确认舌头距离为 6/12/24/40 格，血量为 3/6/10/15 颗心。

```text
44d84890ab00f69cce6f41d3c4dcdfcf39a5cf1761e31a624418e327f6dd51db  FrogDevour-Fabric-1.21.10-v1.0.2.jar
3acc2748aaa14bc33204ca40a4ddd1f4355dad6cc8247232b39c23750ecb309a  FrogDevour-Fabric-1.21.10-v1.0.2-install.zip

```

## 下载

- [v1.0.2 JAR](https://github.com/susu2020oxo/qingwa/raw/refs/heads/downloads/downloads/FrogDevour-Fabric-1.21.10-v1.0.2.jar)
- [v1.0.2 ZIP 安装包](https://github.com/susu2020oxo/qingwa/raw/refs/heads/downloads/downloads/FrogDevour-Fabric-1.21.10-v1.0.2-install.zip)

移除旧版 JAR，只保留此版本；多人游戏中服务器和客户端一起更新。原存档食量保留。

## 验证范围

已确认 Iris 默认按键冲突，用户已反馈手动调整按键后闪屏解决。v1.0.2 的自动按键处理、无限潜水与联机效果尚未在游戏内实际复测；验收步骤见 `TESTING.md`。

Iris 代码依据：[R 键注册](https://github.com/IrisShaders/Iris/blob/11e691d94343a6e372affc1a80a0a1f06d6378ff/common/src/main/java/net/irisshaders/iris/Iris.java#L782)、[重载触发](https://github.com/IrisShaders/Iris/blob/11e691d94343a6e372affc1a80a0a1f06d6378ff/common/src/main/java/net/irisshaders/iris/Iris.java#L179)。
