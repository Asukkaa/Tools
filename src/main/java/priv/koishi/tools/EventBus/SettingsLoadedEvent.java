package priv.koishi.tools.EventBus;

import lombok.Getter;
import priv.koishi.tools.Bean.TabBean;

import java.util.List;

/**
 * 设置页加载完成事件
 *
 * @author KOISHI
 * Date:2025-06-04
 * Time:13:18
 */
@Getter
public class SettingsLoadedEvent {

    /**
     * 激活的标签页
     */
    List<TabBean> tabBeanList;

    /**
     * 构造函数
     *
     * @param tabBeanList 激活的标签页
     */
    public SettingsLoadedEvent(List<TabBean> tabBeanList) {
        this.tabBeanList = tabBeanList;
    }

}
