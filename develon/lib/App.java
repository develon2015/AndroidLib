package develon.lib;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Process;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public abstract class App extends Application {
    public static SharedPreferences pre;
    public static final boolean DEBUG = true;

    private static final String defLang = "app_default_language";

    private static App app;
    private static Thread mainThread;
    private static Handler handler;

    {
        app = this;
        mainThread = Thread.currentThread();
        handler = new Handler();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        pre = PreferenceManager.getDefaultSharedPreferences(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            String[] loc = pre.getString(defLang, "").split("_r", 2);
            Locale locale = Locale.getDefault();
            if (loc.length != 2) {
                setDefaultLanguage(locale);
            } else {
                if ("??".equals(loc[1]))
                    // 无区域设置
                    locale = new Locale(loc[0]);
                else
                    locale = new Locale(loc[0], loc[1]);
                getResources().getConfiguration().setLocale(locale);
                getResources().updateConfiguration(getResources().getConfiguration(), getResources().getDisplayMetrics());
            }
        } else {
            toast("系统版本太低, 无法设置语言首选项");
        }
    }

    public static App getAppInst() {
        return app;
    }

    public static Handler getHandler() {
        return handler;
    }

    static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss ");

    /**
     * 对 toast(String msg) 的包装, 以日志方式显示消息, DEBUG 为 false 时不操作
     * @param msg
     */
    public static void log(String msg) {
        if (msg == null)
            msg = "{NULL}";
        if (msg.trim().equals(""))
            msg = "{空白}";
        if (!DEBUG)
            return;
        toast(sdf.format(new Date()) + "[" + msg + "]");
    }

    private static Toast toast;
    private static long lastTime = 0L;
    private static String lastMsg;

    /**
     * 在任何线程显示 toast 消息 <br>
     * 低延迟, 带缓存
     * @param msg
     */
    public static void toast(String msg) {
        if (msg == null)
            msg = "{NULL}";
        if (msg.trim().equals(""))
            msg = "{空白}";

        long cTime = System.currentTimeMillis();
        if (cTime - lastTime < 3500) {
            msg = lastMsg + "\n" + msg;
        }

        final String tmsg = msg.toString();

        Runnable task = () -> {
            try {
                try {
                    toast.cancel();
                } catch (Exception e) {
                }
                toast = Toast.makeText(app, tmsg, Toast.LENGTH_LONG);
                toast.show();
                lastTime = System.currentTimeMillis();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        lastMsg = tmsg; // Save msg

        if (Thread.currentThread().equals(mainThread)) {
            task.run();
            return;
        }
        handler.post(task);
    }

    /**
     * 保存语言首选项<br>
     *     在 onCreate(...) 中处理
     * @param locale
     */
    public boolean setDefaultLanguage(Locale locale) {
        String country = locale.getCountry();
        if (country == null || "".equals(country)) {
            country = "??";
        }
        String value = locale.getLanguage() + "_r" + country;
        if (!value.matches("^...?_r..$")) { // 有效的 Locale 值类似 "zh_rTW zh_rHK ja_JP"
            log(value + "不是有效的 Locale 值");
            return false;
        }
        boolean r = pre.edit().putString(defLang, value).commit();
        return r;
    }


    /**
     * 重启 Application
     */
    public void restart() {
        Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        PendingIntent restartIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mgr.setExact(AlarmManager.RTC, System.currentTimeMillis() + 500, restartIntent); // 重启应用
        } else {
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 500, restartIntent); // 重启应用
        }
        Process.killProcess(Process.myPid());
        System.exit(0);
    }
}
