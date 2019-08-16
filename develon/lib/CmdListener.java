package develon.lib;

import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import java.lang.reflect.Method;

/**
 * 为了实现以命令驱动的测试, 可以在布局中放置一个 EditText, 并设置 OnKeyListener 为本类实例
 * cmdExecutor 中的测试方法应类似接口 CmdListener.CmdExecutor
 */
public class CmdListener implements View.OnKeyListener {
    Class<? extends CmdExecutor> cmdExecutor;

    public static interface CmdExecutor {
        public void test(String param);
    }

    public CmdListener(Class<? extends CmdExecutor> cmdExecutor) {
        this.cmdExecutor = cmdExecutor;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        // 按下回车键
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            EditText cmd = (EditText) v;
            if (cmd.getText().toString().equals("")) {
                return true;
            }
            try {
                String[] cmds = cmd.getText().toString().split(" ", 2);
                Object executor = cmdExecutor.newInstance();
                Method target = cmdExecutor.getMethod(cmds[0], String.class);
                target.invoke(executor, cmds.length == 1 ? null : cmds[1]);
            } catch (Exception e) {
                e.printStackTrace();
                App.log(e.toString());
            }
            cmd.setText("");
            return true;
        }
        return false;
    }
}
