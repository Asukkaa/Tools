package priv.koishi.tools.ThreadPool;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author KOISHI
 * Date:2024-10-30
 * Time:下午8:18
 */
public class CommonTreadFactory implements ThreadFactory {

    private final AtomicInteger threadNum = new AtomicInteger(1);

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, String.valueOf(threadNum.getAndIncrement()));
    }

}
