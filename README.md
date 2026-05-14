# AniFocus

## 项目简介

AniFocus（动漫专注）是一款基于 Android 的二次元风格学习打卡应用，帮助用户管理学习任务、记录每日学习进度，并以动漫语录激励学习动力。

## 主要功能

1. **学习任务管理**：创建、编辑、删除学习任务，支持设置截止日期和优先级（高/中/低）
2. **每日打卡**：记录每日学习打卡，连续打卡天数统计，激励坚持学习
3. **任务详情**：查看任务详情，支持标记完成/未完成，记录学习进度（0~100%）
4. **动漫语录**：从 hitokoto.cn 获取每日一言，展示经典动漫台词与出处
5. **通知提醒**：支持学习提醒通知、打卡提醒通知及任务截止提醒
6. **深色模式**：支持系统深色/浅色主题自动切换

## 使用技术

1. **Java** — 主要开发语言
2. **Android Studio** — 集成开发环境，Gradle 构建系统（Kotlin DSL）
3. **Room Database** — 本地数据库存储任务与打卡记录
4. **OkHttp + Gson** — 网络请求，获取动漫语录数据
5. **RecyclerView** — 列表展示
6. **Material Design** — UI 组件与设计规范
7. **AndroidX Lifecycle** — ViewModel、LiveData 组件化开发
8. **Android 通知系统** — 学习与打卡提醒
9. **Git** — 版本管理

## 项目结构

```
app/src/main/java/com/example/anifocus/
├── data/
│   ├── local/
│   │   ├── AppDatabase.java          # 数据库入口，单例模式
│   │   ├── dao/
│   │   │   ├── TaskDao.java          # 任务数据操作接口
│   │   │   └── CheckInDao.java       # 打卡数据操作接口
│   │   └── entity/
│   │       ├── TaskEntity.java       # 任务数据实体（Room Entity）
│   │       └── CheckInEntity.java    # 打卡记录实体
│   ├── remote/
│   │   └── QuoteRepository.java      # 动漫语录数据仓库（OkHttp + Gson）
│   └── provider/
│       └── AniFocusProvider.java     # ContentProvider，数据共享
├── ui/
│   ├── main/
│   │   ├── MainActivity.java         # 主页面，任务列表 + 动漫语录
│   │   └── TaskAdapter.java         # 任务列表适配器
│   ├── task/
│   │   └── TaskEditActivity.java     # 任务编辑页面（新建/编辑）
│   ├── checkin/
│   │   ├── CheckInActivity.java     # 每日打卡页面
│   │   └── CheckInAdapter.java      # 打卡历史适配器
│   └── detail/
│       └── TaskDetailActivity.java   # 任务详情页面
└── util/
    └── NotificationHelper.java       # 通知辅助类
```

## 运行方式

1. 使用 Android Studio 打开项目根目录
2. 等待 Gradle 同步完成（首次打开需下载依赖，请保持网络连接）
3. 连接 Android 模拟器或真机设备
4. 点击 Android Studio 工具栏的 **Run** 按钮（或按 `Shift + F10`）编译并运行

> **最低要求**：Android 7.0（API 24）及以上版本

## 开发过程说明

本项目使用 Git 进行版本管理，主要提交记录如下：

| 提交 | 说明 |
|------|------|
| 1 | 初始化项目：添加 Gradle wrapper 及根项目配置 |
| 2 | 添加 app 模块：build.gradle.kts、AndroidManifest 及测试文件 |
| 3 | 实现数据层：Room 数据库实体、DAO 及 AppDatabase |
| 4 | 实现 UI 层：MainActivity、TaskAdapter、TaskEditActivity、CheckInActivity、TaskDetailActivity |
| 5 | 实现通知与网络功能：NotificationHelper、QuoteRepository（hitokoto API） |
| 6 | 添加应用资源：布局文件、Drawable 资源、主题与字符串资源 |
| 7 | 配置 .gitignore，规范化项目忽略规则 |

## 作者信息

姓名：
班级：
学号：
