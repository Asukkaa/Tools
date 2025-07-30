package priv.koishi.tools.Annotate;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 在Bean类中添加注解表明该方法被反射调用
 *
 * @author KOISHI
 * Date:2025-03-24
 * Time:19:56
 */
@SuppressWarnings("unused")
@Retention(RetentionPolicy.RUNTIME)
public @interface UsedByReflection {
}
