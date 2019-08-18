package develon.lib;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class Shell {
    private Process psh;

    public static Shell sh = new Shell();

    private static final String SH_PATH = "/system/bin/sh";

    /**
     * 启动新的 shell 进程
     */
    public Shell() {
        try {
            psh = Runtime.getRuntime().exec(SH_PATH);
        } catch (Exception e) {
            throw new Error("无法启动 shell");
        }
    }

    /**
     * 使用全局 Shell 执行命令 <br>
     * 获取上一次执行的返回值, 请使用命令"echo $?"
     * @param cmd 要执行的命令
     * @param deley 延迟时间
     * @return
     */
    public static String exec(String cmd, int deley) {
        if (!sh.isAlive()) {
            sh = new Shell();
        }
        return sh.run(cmd, deley);
    }

    /**
     * 执行命令 <br>
     * 获取上一次执行的返回值, 请使用命令"echo $?"
     * @param cmd 要执行的命令
     * @param deley 延迟时间
     * @return
     */
    public String run(String cmd, int deley) {
        synchronized (psh) {
            if (deley < 100) {
                deley = 100;
            }
            InputStream is = psh.getInputStream(); // java.lang.UNIXProcess$ProcessPipeInputStream
            OutputStream os = psh.getOutputStream();
            InputStreamReader isr = new InputStreamReader(is);
            OutputStreamWriter osw = new OutputStreamWriter(os);

            try {
                while (is.available() > 0) { // 确保输出是本次执行造成的
                    Log.e("执行异常", "上次的输出");
                    byte[] b = new byte[is.available() + 1024];
                    int n = is.read(b);
                    if (n > 0)
                        Log.e("Value", new String(b, 0, n));
                }
                osw.write(cmd + "\n");
                osw.flush();
                long et = System.currentTimeMillis();
                while (is.available() == 0) {
                    if (System.currentTimeMillis() - et > deley) {
                        return null;
                    }
                    try {
                        int w = deley / 10;
                        w = w > 100 ? 100 : w;
                        Thread.sleep(w);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                byte[] b = new byte[is.available() + 1024];
                int n = is.read(b);
                if (n < 1)
                    throw new RuntimeException("{SHELL ERROR}");
                if (is.available() > 0)
                    Log.e("执行异常", "尚有输出");
                return new String(b, 0, n);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e.toString());
            }
        }
    }

    /**
     * 判断可用性
     * @return
     */
    public boolean isAlive() {
        try {
            String pwd = run("pwd", 1000);
            if (pwd != null)
                return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Process getProcess() {
        return psh;
    }
}
