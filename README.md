MobileMouse
---
一个小玩具，用安卓手机当作鼠标或者键盘操控电脑

### 效果

![image](https://github.com/imengyu/MobileMouse/raw/main/demo.gif)

### 原理

程序分为两个端，使用WIFI作为桥梁，手机和电脑建立TCP连接，通过网络发送信号，电脑端接收并作出指令。

目前仅支持安卓和Windows。

### 安装和使用方法

1. 编译服务端程序，推荐使用Visual studio，生成MouseServer.exe，也可在[这里](https://github.com/imengyu/MobileMouse/raw/main/Debug/MouseServer.exe)下载已经编译好的版本。
2. 编译安卓端程序，也可在[这里](https://github.com/imengyu/MobileMouse/raw/main/app/release/app-release.apk)下载已经编译好的版本。
3. 在电脑开启服务端，并允许在防火墙弹出中允许。
4. 手机和电脑连接到同一个路由器的网络下（可ping通），点击手机的扫描按钮，就可以出现电脑的IP地址了，点击进行连接，即可用手机操控电脑了。

### 其他

这是作者为了学习而做的小软件，可能有问题或不足请见谅。

如果你发现了问题，可直接向我反馈哦，非常感谢你的支持！

喜欢的话给个小星星好嘛（＞人＜；）
给小星星的小哥哥们又帅又可爱

### 许可

本程序使用MIT协议。
