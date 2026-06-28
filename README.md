# FocusList 专注清单

FocusList 是一款 Android「待办清单 + 番茄钟专注」应用。用户可以记录待办、设置提醒、关联任务开始专注，并在统计页查看最近 7 天专注记录与累计数据。

项目使用 Kotlin + Material 3 + MVVM + Room 实现，覆盖 Activity、Service、BroadcastReceiver、ContentProvider 四大组件，也包含桌面小组件、通知、网络请求、多媒体提示音与横竖屏适配。

## 功能特性

- 待办管理：新增、编辑、删除、完成状态、分类、优先级、到期提醒。
- 番茄钟专注：前台服务倒计时，支持暂停、继续、结束、通知栏控制。
- 任务关联：可从待办列表直接对某个任务开始专注。
- 专注统计：今日/累计专注分钟数、番茄数、待办数量、最近 7 天记录。
- 提醒通知：AlarmManager + BroadcastReceiver 实现任务到点提醒，开机后自动重排提醒。
- 桌面小组件：通过 ContentProvider 读取待办/已完成数量。
- 每日一言：Retrofit 请求一言接口，离线时自动显示内置兜底文案。
- 设置页：默认专注时长、提示音、白噪音、振动等偏好设置。
- UI 适配：Material 3 主题、明暗色适配、横屏专用专注页和统计页布局。

## 技术栈

| 类别 | 选型 |
| --- | --- |
| 语言 | Kotlin |
| 架构 | MVVM、Repository、DAO |
| UI | Material 3、ViewBinding、Fragment、BottomNavigationView、RecyclerView |
| 数据 | Room、SharedPreferences |
| 异步 | Kotlin Coroutines、LiveData |
| 网络 | Retrofit 2、Gson、OkHttp |
| 多媒体 | SoundPool、MediaPlayer、Vibrator |
| 构建 | Gradle 8.7、AGP 8.5.2、Kotlin 2.0.20、KSP |

## 环境要求

- Android Studio Koala 或更高版本
- JDK 17（可直接使用 Android Studio 自带 JBR）
- Android SDK API 34
- Android 8.0 / API 26 及以上设备或模拟器

## 快速开始

```bash
git clone https://github.com/lgydhei/FocusList.git
cd FocusList

# Windows
gradlew.bat assembleDebug

# macOS / Linux
./gradlew assembleDebug
```

Debug APK 输出位置：

```text
app/build/outputs/apk/debug/app-debug.apk
```

也可以用 Android Studio 打开项目根目录，等待 Gradle Sync 完成后直接点击 Run。

## 项目结构

```text
FocusList/
├── app/
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/bistu/focuslist/
│       │   ├── data/        # Room 实体、DAO、数据库、Repository
│       │   ├── network/     # Retrofit API 与客户端
│       │   ├── provider/    # ContentProvider
│       │   ├── receiver/    # 任务提醒、开机重排
│       │   ├── service/     # 番茄钟前台服务
│       │   ├── ui/          # Activity、Fragment、ViewModel、Adapter
│       │   ├── util/        # 通知、闹钟、音效、时间、偏好设置
│       │   └── widget/      # 桌面小组件
│       └── res/
│           ├── layout/      # 竖屏布局
│           ├── layout-land/ # 横屏布局
│           ├── values/      # 颜色、主题、字符串
│           └── drawable/    # 图标与背景资源
├── 项目说明.md
└── 部署文档.md
```

## 数据说明

应用数据保存在本地 Room 数据库中：

- `tasks`：待办任务表。
- `focus_sessions`：专注记录表。

统计页只展示最近 7 天的专注记录，历史记录不会被自动删除；今日和累计统计仍基于数据库完整数据计算。

## 文档

- [项目说明.md](项目说明.md)：功能、架构、四大组件与数据模型说明。
- [部署文档.md](部署文档.md)：环境准备、运行步骤和功能验收路线。

## 版本

当前版本：`v1`
