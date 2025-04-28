package priv.koishi.tools.ThreadPool;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程工厂类
 *
 * @author KOISHI
 * Date:2024-10-30
 * Time:下午8:18
 */
public class CommonThreadFactory implements ThreadFactory {

    /**
     * 线程编号生成器，使用原子整数保证线程安全的自增操作，初始值为1，每次获取后自动递增
     */
    private final AtomicInteger threadNum = new AtomicInteger(1);

    /**
     * 创建并返回一个新线程，自动生成唯一递增的线程名称
     *
     * @param r 需要在新线程中执行的任务，实现Runnable接口的对象
     * @return Thread 新创建的线程对象，名称格式为递增的整数值字符串
     */
    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, String.valueOf(threadNum.getAndIncrement()));
    }

}
