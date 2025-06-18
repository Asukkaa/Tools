package priv.koishi.tools.Controller;

import priv.koishi.tools.Properties.CommonProperties;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 根控制器
 *
 * @author KOISHI
 * Date:2025-06-18
 * Time:17:36
 */
public class RootController extends CommonProperties {

    /**
     * 全局控制器容器
     */
    private static final Map<Class<?>, WeakReference<RootController>> controllers = new ConcurrentHashMap<>();

    /**
     * 构造方法
     */
    public RootController() {
        controllers.put(getClass(), new WeakReference<>(this));
    }

    /**
     * 获取控制器实例
     */
    public static <T extends RootController> T getController(Class<T> type) {
        WeakReference<RootController> ref = controllers.get(type);
        RootController instance = (ref != null) ? ref.get() : null;
        // 类型安全校验
        return (instance != null) ? type.cast(instance) : null;
    }

}