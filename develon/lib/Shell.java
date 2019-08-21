package develon.lib;

import android.os.Build;
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
        init();
    }

    // 启动 sh
    private void init() {
        try {
            psh = Runtime.getRuntime().exec(SH_PATH);
            System.out.print("新的sh进程: " + run("echo $$", 200));
        } catch (Exception e) {
            throw new RuntimeException("无法启动 shell");
        }
    }

    /**
     * 先发送 ^C, 再使用全局 Shell 执行命令 <br>
     * 获取上一次执行的返回值, 请使用命令"echo $?"
     * @param cmd 要执行的命令
     * @param deley 延迟时间
     * @return
     */
    public static String exec(String cmd, int deley) {
        if (!sh.isAlive()) {
            // 尝试杀死旧的不可用psh进程
            try {
                sh.getProcess().destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        if (cmd == null)
            //throw new NullPointerException("COMMAND CAN'T NULL");
            cmd = "";
        synchronized (this) {
            if (deley < 100) {
                deley = 100;
            }
            InputStream is = psh.getInputStream(); // java.lang.UNIXProcess$ProcessPipeInputStream
            InputStream es = psh.getErrorStream();
            OutputStream os = psh.getOutputStream();
            InputStreamReader isr = new InputStreamReader(is);
            OutputStreamWriter osw = new OutputStreamWriter(os);

            try {
                flushInput(is);
                flushInput(es);
                osw.write(cmd + "\n");
                osw.flush();

                long et = System.currentTimeMillis();
                int w = deley / 10;
                w = w > 100 ? 100 : w;

                // 等待输出
                while (is.available() == 0 && es.available() == 0) {
                    if (System.currentTimeMillis() - et > deley) {
                        return null;
                    }
                    try {
                        Thread.sleep(w);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                String result = "";
                while (is.available() > 0) {
                    byte[] b = new byte[is.available() + 1024];
                    int n = is.read(b);
                    if (n > 0) {
                       result += new String(b, 0, n);
                    }
                }
                while (es.available() > 0) {
                    byte[] b = new byte[es.available() + 1024];
                    int n = es.read(b);
                    if (n > 0) {
                        result += new String(b, 0, n);
                    }
                }
                return result;
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e.toString());
            }
        }
    }

    // 清空缓冲, 确保输出是本次执行造成的
    private void flushInput(InputStream in) throws IOException {
        while (in.available() > 0) {
            Log.e("清空缓存", in.getClass().getName());
            byte[] b = new byte[in.available() + 1024];
            int n = in.read(b);
            if (n > 0)
                Log.e("Value", new String(b, 0, n));
        }
    }

    /**
     * 判断可用性
     * @return
     */
    public boolean isAlive() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!psh.isAlive())
                return false;
        }
        try {
            String abc = run("echo abc", 200);
            if (abc != null && "abc".equals(abc.trim()))
                return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("sh", this.psh.toString() + "唔可用");
        return false;
    }

    public Process getProcess() {
        return psh;
    }

    /**
     * 是否超级用户
     * @return
     */
    public boolean isSU() {
        if (!isAlive())
            return false;

        String user = run("echo $USER", 0);
        String ids = run("id", 0);

        if (user == null || ids == null)
            return false;

        user = user.trim();
        ids = ids.trim();

        if ("root".equals(user) && ids.matches("^.*(.*=0\\(root\\).*){3}.*"))
            return true;
        return false;
    }


    private static final String SUCHECK = "su exited";
    public boolean waitSU() {
        if (!isAlive()) {
            init();
        }

        String sucheck = run("su; echo " + SUCHECK, 200);
        if (sucheck == null) {
            // su 未结束, 或已取得权限
            while (!isAlive());
            return isSU();
        } else if (SUCHECK.equals(sucheck)) {
            return false;
        }

        Log.e("罕见情况", sucheck);
        return false;
    }

    /**
     * 获取上一次的返回值
     * @return [0, 255], -1 失败
     */
    public int getLastCode() {
        String r = run("echo $?", 200);
        try {
            int exitCode = Integer.parseInt(r);
            if (exitCode >= 0 && exitCode <= 255)
                return exitCode;
        } catch (Exception e) { }
        return -1;
    }

}
