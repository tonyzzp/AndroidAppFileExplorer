[![](https://jitpack.io/v/tonyzzp/AndroidAppFileExplorer.svg)](https://jitpack.io/#tonyzzp/AndroidAppFileExplorer)

### 这是什么
这是一个内置在应用内的文件浏览器，可以方便查看`/data/data/PACKAGE`和`/sdcard/android/data/PACKAGE`目录内的文件，便于开发调试

### 如何使用

#### 在根目录的build.gradle文件内添加
```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

#### 在module目录内的build.gradle文件内添加
```
dependencies {
        compile 'com.github.tonyzzp:AndroidAppFileExplorer:0.2'
}
```

#### 在通知栏显示入口
```java
AppFileExplorer.showNotification(context);
```

#### 取消通知栏的入口
```
AppFileExplorer.dismissNotification(context);
```

#### 打开文件浏览器
```java
AppFileExplorer.open(context);
```