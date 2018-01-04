
[![](https://jitpack.io/v/ooftf/PatternLock.svg)](https://jitpack.io/#ooftf/PatternLock)
# PatternLock
手势密码控件
## 效果图
![](https://github.com/ooftf/PatternLock/raw/master/art/PatternLock.gif)
## Gradle配置
```gradle
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
dependencies {
    compile 'com.github.ooftf:PatternLock:1.0.0'
}
```
## 使用方式
### XML布局
```xml
<com.ooftf.pattern.PatternLock
        app:selectedIconId="@drawable/ic_launcher_foreground"
        android:id="@+id/patternLock"
        android:padding="18dp"
        android:background="@color/colorPrimary"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
```
### Kotlin代码
```kotlin
patternLock.onSlideListener = object : OnSlideListener {
            override fun onStart() {

            }
            override fun onCompleted(list: List<Int>) {
                if (list.size < 4) {
                    patternLock.error()
                    Handler().postDelayed({ patternLock.reset() }, 1000)
                }
            }
        }
```
## XML属性
|属性名|描述|默认|
|---|---|---|
|nomalIconId|正常状态下图标Id|如图|
|selectedIconId|选中状态下图标Id|如图|
|errorIconId|错误状态下图标Id|如图|
|iconSize|图标大小|56dp|
|nomalLineColor|正常状态下连线颜色|#00FFFF|
|errorLineColor|错误状态下连线颜色|#FF0000|
|lineWidth|连线宽度|8dp|
## PatternLock方法
|方法名|描述|
|---|---|
|setOnSlideListener|设置监听事件|
