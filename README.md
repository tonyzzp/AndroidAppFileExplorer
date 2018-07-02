[![](https://jitpack.io/v/tonyzzp/AndroidAppFileExplorer.svg)](https://jitpack.io/#tonyzzp/AndroidAppFileExplorer)

### 这是什么
这是一个内置在应用内的文件浏览器，可以方便查看`/data/data/PACKAGE`和`/sdcard/android/data/PACKAGE`目录内的文件，便于开发调试


### 支持的功能
1. 查看文本
2. 查看SharedPreferences
3. 查看音频
4. 查看视频
5. 查看图片
6. 查看数据库
7. 把文件复制到sd卡并使用外部软件打开

### 如何使用

#### 在根目录的build.gradle文件内添加
```gradle
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

#### 在module目录内的build.gradle文件内添加
```gradle
dependencies {
        compile 'com.github.tonyzzp:AndroidAppFileExplorer:1.1'
        compile 'com.github.tonyzzp:AndroidAppFileExplorer:master-SNAPSHOT' //使用master最新版
}
```


#### 在通知栏显示入口

```java
AppFileExplorer.showNotification(context);
```

#### 取消通知栏的入口
```java
AppFileExplorer.dismissNotification(context);
```

#### 打开文件浏览器
```java
AppFileExplorer.open(context);
```
