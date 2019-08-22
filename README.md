AndroidLib:smirk:
===

特色功能
---

* 执行 root 命令

```java
    public void foo() {
	// 等待用户授权, 属于耗时操作
        boolean b = Shell.sh.waitSU();
        if (b) {
            App.toast("已获得权限");
	    // 执行命令
	    String result = Shell.exec("echo `id`", 0);
	    App.toast(result);

	    Shell.exec("sleep 2000 && reboot", 1000);
	    App.toast("" + Shell.sh.getLastCode());
        } else {
            App.toast("没有获得权限");
        }
    }
```

![](https://raw.githubusercontent.com/develon2015/AndroidLib/master/res/su.png "执行 su 提升权限")

