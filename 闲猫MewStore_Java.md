# 闲猫MewStore 安卓端ヾ(≧▽≦*)o

> ###### 队伍名：**闲猫吃咸鱼(Android分队)**
>
> 平台名：闲猫MewStore

 本项目是一个**二手游戏账号交易**平台，主要提供游戏账号交易服务，为玩家提供便利和安全性。本项目前端采用Kotlin原生安卓编写，后端涉及到跨语言，分别为Springboot框架和flask框架编写。

## 合作成员信息

### 安卓端

- ##### 成员： 吴荣榜

- ##### 语言与框架：Kotlin 原生安卓

- ##### 学号：222200314

### 美术

- ##### 成员：马雁语

- ##### 相关软件：XD PS procreate

- ##### 学号：832204101

### 后端

- ##### 成员1：叶宇滟

- ##### 语言与框架：Java (采用Springboot编写)

- ##### 学号：222200307

- ##### 成员2：林荣达

- ##### 语言与框架：Python（Flask）

- ##### 学号：062200237

  ------

  

### 项目仓库地址

Java后端项目地址：https://github.com/Kikikisum/MewStore

------



### 技术栈

- mybatis-plus

- hutool 

- jwt

- websocket

  ------

  

### 项目目录树

└─example
    │  MewStoreApplication.java
    │
    ├─Config
    │      MybatisPlusConfig.java
    │      WebSocketConfig.java
    │
    ├─Controller
    │      FavoriteController.java
    │      MessageController.java
    │      OrderController.java
    │      ReportController.java
    │      UserController.java
    │
    ├─Entity
    │      Favorite.java
    │      Freeze.java
    │      Good.java
    │      Message.java
    │      Order.java
    │      Report.java
    │      User.java
    │
    ├─Mapper
    │      FavoriteMapper.java
    │      FreezeMapper.java
    │      GoodMapper.java
    │      MessageMapper.java
    │      OrderMapper.java
    │      ReportMapper.java
    │      UserMapper.java
    │
    ├─mewstore
    ├─Service
    │  │  FavoriteService.java
    │  │  FreezeService.java
    │  │  GoodService.java
    │  │  MessageService.java
    │  │  OrderService.java
    │  │  ReportService.java
    │  │  UserService.java
    │  │
    │  └─Impl
    │          FavoriteServiceImpl.java
    │          FreezeServiceImpl.java
    │          GoodServiceImpl.java
    │          MessageServiceImpl.java
    │          OrderServiceImpl.java
    │          ReportServiceImpl.java
    │          UserServiceImpl.java
    │          WebSocketServer.java
    │
    ├─Util
    │      DecodeJwtUtils.java
    │      SnowFlakeUtil.java
    │      SpringUtil.java
    │
    └─WebSocket
            MyWebSocket.java

------



### 个人任务完成项

- 支持基本交易功能

- 三种事故处理

黑名单、找回账户、交易取消

- 支持聊天功能(系统消息的发放)

  ------

  

### 项目亮点

- 实现python和java的后端**跨端**合作

- 使用**hutool**对python的jwt库生成token进行解码

- 使用**雪花算法**保证id的有序性，并对不同内容进行区分

- 实现**系统消息**（websocket）发放

  ------

  

### 接口文档

https://console-docs.apipost.cn/preview/9ee328c89b7c6eea/fd7048006ceb2e1f?target_id=8fd76ee6-e270-4026-917b-802fd7970ab5

------

### 当前进度

后端编写完毕，等待前端使用mock对接

### 加分项

- 支持聊天功能

- 支持要求的事故处理

- 服务安全性能