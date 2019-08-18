AndroidLib:smirk:
===

特色功能
---

* 执行 root 命令

```java
    public void sh(String param) {
        String result = Shell.exec(param, 1000); // 我可以等待 1 秒
        App.toast(param + ":" + result);
    }

    ...

    sh("su");
```

![](https://raw.githubusercontent.com/develon2015/AndroidLib/master/res/su.png "执行 su 提升权限")

