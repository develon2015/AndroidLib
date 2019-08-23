package example;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.lang.reflect.Field;
import java.util.Locale;

import a.test.A;
import develon.lib.CmdListener;
import develon.lib.Shell;

public class CmdExecutor implements CmdListener.CmdExecutor {

    public void call(String param) {
        try {
            Intent i = new Intent(Intent.ACTION_CALL);
            i.setData(Uri.parse("tel:10086"));

            if (i.resolveActivity(App.getAppInst().getPackageManager()) == null) {
                App.log("无法拨号");
                return;
            }

            // Permission 检测
            if (ContextCompat.checkSelfPermission(App.getAppInst(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                App.toast("没有权限, 请先赋予权限");
                new Thread(()-> {
                    try {
                        Thread.sleep(2000); // 给用户一点时间做好赋权准备
                        ActivityCompat.requestPermissions(MainUI.ui, new String[]{Manifest.permission.CALL_PHONE}, 1);
                    } catch (Exception e) {
                        App.toast("申请权限出现异常");
                    }
                }).start();
                return;
            }

            App.log("响应者: " + i.resolveActivity(App.getAppInst().getPackageManager()).getPackageName());
            MainUI.ui.startActivity(i);
        } catch (Exception e) {
            App.log(e.toString());
        }
    }

    public void go(String param) {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
               new Thread(()->{
                   App.log("网络状态变化");
               }).start();
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(param == null ? "android.net.conn.CONNECTIVITY_CHANGE" : param);
        App.getAppInst().registerReceiver(broadcastReceiver, filter);
    }

    public void intent(String param) {
        try {
            Intent i = new Intent();
            i.setAction(Intent.ACTION_VIEW);
            //i.setDataAndType(Uri.parse("new://a.com"), param);
            //i.setType(param);
            i.setDataAndType(Uri.parse("ok://helo"), param);

            i.putExtra("file", "java");

            // 即使有应用程序响应, 也未必是 Activity 组件
            if (i.resolveActivity(App.getAppInst().getPackageManager()) == null) {
                App.toast("沒有應用程序可執行該操作");
                return;
            }

            /**
             * 如果有 Activity 可供选择
             */
            Intent chooser = Intent.createChooser(i, "請選擇");
            MainUI.ui.startActivity(chooser);
        } catch (Exception e) {
            App.log(e.toString());
            e.printStackTrace();
        }
    }

    public void locale(String param) {
        App.log(Locale.getDefault().getDisplayName());
        App.log(App.getAppInst().getResources().getConfiguration().locale.getDisplayName());
        App.log(App.getAppInst().getString(R.string.app_name));
    }

    public void reflect(String param) {
        try {
            Class<?> clazz = Class.forName("a.test.A");
            Field a = clazz.getDeclaredField("a");
            Object obj = new A();
            a.setAccessible(true);
            a.setInt(obj, 89);
            int valuea = a.getInt(obj);
            App.log("值: " + valuea);
        } catch (Exception e) {
            e.printStackTrace();
            App.log(e.toString());
        }
    }

    public void setlang(String param) {
        try {
            String[] p = param.split("_r", 2);
            Locale newlocale = null;
            if (p.length > 1)
                newlocale = new Locale(p[0], p[1]);
            else
                newlocale = new Locale(p[0]);
            if (App.getAppInst().setDefaultLanguage(newlocale))
                re(null);
        } catch (Exception e) {
            e.printStackTrace();
            App.log(e.toString());
        }
    }

    public void exit(String parma) {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    public void re(String param) {
        App.getAppInst().restart();
    }

    @Override
    public void foo(String param) {
        boolean b = Shell.sh.waitSU();
        if (b) {
            App.toast("已获得权限");
        } else {
            App.toast("没有获得权限");
        }
        App.toast("" + Shell.sh.getLastCode());
    }

    public void sh(String param) {
        String result = Shell.exec(param, 800);
        App.toast(result + "返回值: " + Shell.sh.getLastCode());
    }

    public void t(String param) {
        try {
            App.toast(Shell.sh.isSU() ? "root用户" : "不是root用户");
        } catch (Exception e) {
            e.printStackTrace();
            App.log(e.toString());
        }
    }
}
