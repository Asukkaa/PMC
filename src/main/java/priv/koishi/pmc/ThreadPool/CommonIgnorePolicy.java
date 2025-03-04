package priv.koishi.pmc.ThreadPool;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池日志记录类
 *
 * @author KOISHI
 * Date:2024-10-30
 * Time:下午8:19
 */
public class CommonIgnorePolicy implements RejectedExecutionHandler {

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        doLog(r, e);
    }

    private void doLog(Runnable r, ThreadPoolExecutor e) {
        // 可做日志记录等
        System.err.println(r.toString());
        System.err.println(e.toString());
    }

}
