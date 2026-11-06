# 构建与验证状态

日期：2026-11-06。

## 已完成的验证

- 使用真实 JDK 21 编译 `GrowthRules.java`、`FeedingBudget.java` 和 `GrowthRulesTest.java`。
- 执行结果：**PASS: 224 growth rule checks**。
- 检查覆盖成长阈值、3:1 换算、余量保留、猎物等级门槛、批量/混合容量、舌头数量限制和整数溢出。
- Gradle 8.14.3 Wrapper 分发下载成功。
- 19 个 Java 文件通过 Java 21 语法解析（仅语法，不代表完成类型检查）。
- JSON 资源、Mixin 对应源码、中文/英文翻译键、Shell 脚本语法检查通过；公共源码未导入客户端类。
- 核对了 FabricMC `yarn` 和 `fabric` 仓库的 **1.21.10** 分支，使用本版本的按键分类、渲染命令队列、HUD、世界渲染事件和持久化附件接口。

## 完整构建阻塞

执行 `gradlew --no-daemon build` 后，在解析 Fabric Loom 插件阶段失败：

```text
Plugin [id: 'fabric-loom', version: '1.11.8'] was not found
could not resolve plugin artifact
fabric-loom:fabric-loom.gradle.plugin:1.11.8
```

当前执行环境采用受限网络策略，直接检测依赖地址的结果为：

```text
https://maven.fabricmc.net/ -> CONNECT tunnel failed, response 403
https://piston-meta.mojang.com/ -> CONNECT tunnel failed, response 403
```

因此游戏代码尚未被完整编译，Mixin 注入目标和渲染效果尚未经过运行验证；没有生成安装用 JAR。项目包含完整源码和构建脚本，后续仍可能需要根据真实编译或游戏日志修正兼容问题。

完成构建需要可访问 Fabric Maven、Maven Central、Gradle 和 Minecraft 下载服务；通常包括：

- `maven.fabricmc.net`
- `piston-meta.mojang.com`
- `piston-data.mojang.com`
- `libraries.minecraft.net`
- `repo.maven.apache.org`
- `services.gradle.org` 及其官方下载跳转

开发客户端还会下载 `resources.download.minecraft.net` 上的游戏资源。当前没有修改网络策略、没有上传 GitHub，也没有发布模组。

在正常可联网的 JDK 21 环境中运行 `build-mod.bat` 或 `sh build-mod.sh`。编译成功后，按 `TESTING.md` 进行游戏验收。
