@echo off
chcp 65001 >nul
setlocal
cd /d "%~dp0"
echo 正在编译 Minecraft 1.21.10 Fabric 青蛙吞噬模组，需要 JDK 21 和网络连接。
call gradlew.bat clean build
if errorlevel 1 (
  echo 编译失败，请查看上方错误。确认 Java 21、Fabric Maven 和 Minecraft 下载地址可访问。
  pause
  exit /b 1
)
echo 编译成功！安装 build\libs\frog-devour-1.0.0.jar，不要安装 sources.jar。
explorer build\libs
pause
