package priv.koishi.tools.ThreadPool;

import java.util.concurrent.*;

/**
 * 公共线程池类
 *
 * @author KOISHI
 * Date:2024-10-30
 * Time:下午8:17
 */
public class CommonThreadPoolExecutor {

    /**
     * 核心线程池大小
     */
    int corePoolSize = 2;

    /**
     * 最大线程池大小
     */
    int maximumPoolSize = 3;

    /**
     * 线程最大空闲时间
     */
    long keepAliveTime = 10;

    /**
     * 时间单位
     */
    TimeUnit unit = TimeUnit.SECONDS;

    /**
     * 线程等待队列
     */
    BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(1);

    /**
     * 线程创建工厂
     */
    ThreadFactory threadFactory = new CommonThreadFactory();

    /**
     * 拒绝策略（严格拒绝重复任务）
     */
    RejectedExecutionHandler handler =  new ThreadPoolExecutor.AbortPolicy();

    /**
     * 构造函数
     */
    public CommonThreadPoolExecutor() {
    }

    /**
     * 默认线程池
     *
     * @return 线程池
     */
    public ExecutorService createNewThreadPool() {
        return new ThreadPoolExecutor(this.corePoolSize, this.maximumPoolSize, this.keepAliveTime, this.unit,
                this.workQueue, this.threadFactory, this.handler);
    }

}
