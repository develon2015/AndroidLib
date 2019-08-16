package develon.lib;

import android.app.Application;
import android.widget.Toast;

import android.os.Handler;

import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class App extends Application {
    public static final boolean DEBUG = true;

    private static App app;
    private static Thread mainThread;
    private static Handler handler;

    {
        this.app = this;
        this.mainThread = Thread.currentThread();
        this.handler = new Handler();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static App getAppInst() {
        return app;
    }

    public static Handler getHandler() {
        return handler;
    }

    static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss ");

    public static void log(String msg) {
        if (msg == null)
            msg = "{NULL}";
        if (msg.trim().equals(""))
            msg = "{空白}";
        if (!DEBUG)
            return;
        toast(sdf.format(new Date()) + "[" + msg + "]");
    }

    static Toast toast;
    static long lastTime = 0L;
    static String lastMsg;

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
}
