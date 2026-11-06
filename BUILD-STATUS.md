# 构建与验证状态

日期：2026-11-06。

## 构建结果

**完整构建和下载发布已成功。**

- 构建记录：https://github.com/susu2020oxo/qingwa/actions/runs/42157177033
- 构建源码提交：`7234137f15d1a9aacf2484b8126f3d3b9e1e57fa`
- Java 21、Minecraft 1.21.10、Yarn 1.21.10+build.2、Fabric API 0.138.4+1.21.10。
- `compileJava`、`compileClientJava`、资源处理、Mixin 注解处理、`remapJar`、`check` 和 `build` 均通过。
- 成长与批量容量测试：**PASS: 224 growth rule checks**。
- 发布文件已从公开下载直链重新下载，JAR 和 ZIP 均通过 ZIP 完整性及 SHA-256 校验。
- JAR 包含公共/客户端入口类、Mixin 配置及映射文件、中英文资源，版本为 1.0.0，游戏版本限制为 1.21.10。
- ZIP 内含同一个 JAR 和中文安装说明。

## 下载

- [模组 JAR](https://github.com/susu2020oxo/qingwa/raw/refs/heads/downloads/downloads/FrogDevour-Fabric-1.21.10-v1.0.0.jar)
- [ZIP 安装包](https://github.com/susu2020oxo/qingwa/raw/refs/heads/downloads/downloads/FrogDevour-Fabric-1.21.10-v1.0.0-install.zip)

```text
a110e746a87ccc9b3e55a2998ae728cde48ed5cb0263fa03c0ef0a28877c7431  FrogDevour-Fabric-1.21.10-v1.0.0.jar
237317fa152b9feb1342df04f9e434f3e1b8df53b184f7137535a72f902b5a25  FrogDevour-Fabric-1.21.10-v1.0.0-install.zip

```

## 验证范围

已经通过编译、自动规则检查和下载文件校验；**尚未进行真实游戏内的渲染、完整玩法和联机测试**。验收步骤见 `TESTING.md`。
