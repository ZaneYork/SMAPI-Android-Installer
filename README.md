# SMAPI-Android-Installer
本软件可以提供安装SMAPI框架到安卓系统，SMAPI是星露谷物语的MOD加载框架

## 使用方法
1. 在Release页下载最新的apk安装包安装
2. 安装完成后点击SMAPI安装器的安装按钮
3. 等待安装完成
4. 下载的Mod需要添加到 `StardewValley/Mods` 文件夹
5. 点击新生成从SMAPI开头的星露谷物语进入Mod版游戏

## 其它
### BUG反馈
1. 填写这个[数据收集表](https://docs.qq.com/form/edit/DWlJZc0paV2xxR2JL)
2. 加入QQ群 860453392 反馈

### 工作原理
1. 抽取游戏本体的安装包
2. 生成SMAPI依赖的文件
3. 修改安装包添加SMAPI的启动代码
4. 签名安装包并发起Mod版游戏的安装
