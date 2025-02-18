package priv.koishi.tools.Service;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.robot.Robot;
import priv.koishi.tools.Bean.ClickPositionBean;
import priv.koishi.tools.Bean.TaskBean;

import java.util.List;

import static javafx.scene.input.MouseButton.PRIMARY;
import static priv.koishi.tools.Finals.CommonFinals.macos;
import static priv.koishi.tools.Finals.CommonFinals.systemName;

/**
 * 自动点击线程任务类
 *
 * @author KOISHI
 * Date:2025-02-18
 * Time:14:42
 */
public class AutoClickService {

    /**
     * 自动点击
     *
     * @param taskBean 线程任务参数
     */
    public static Task<Void> autoClick(TaskBean<ClickPositionBean> taskBean, Robot robot) {
        return new Task<>() {
            @Override
            protected Void call() {
                List<ClickPositionBean> tableViewItems = taskBean.getBeanList();
                // macos系统需要先点击一下将焦点切换到目标窗口
                if (systemName.contains(macos)) {
                    ClickPositionBean clickPositionBean = tableViewItems.getFirst();
                    double x = Double.parseDouble(clickPositionBean.getStartX());
                    double y = Double.parseDouble(clickPositionBean.getStartY());
                    Platform.runLater(() -> {
                        robot.mouseMove(x, y);
                        robot.mousePress(PRIMARY);
                        robot.mouseRelease(PRIMARY);
                    });
                    updateMessage("已切换到目标窗口");
                }
                int loopTime = taskBean.getLoopTime();
                if (loopTime == 0) {
                    int i = 0;
                    while (!isCancelled()) {
                        i++;
                        String loopTimeText = "正在执行第 " + i + " / ∞" + " 轮操作\n";
                        click(tableViewItems, loopTimeText);
                    }
                } else {
                    for (int i = 0; i < loopTime; i++) {
                        String loopTimeText = "正在执行第 " + (i + 1) + " / " + loopTime + " 轮操作\n";
                        click(tableViewItems, loopTimeText);
                    }
                }
                updateMessage("所有操作都以执行完毕");
                return null;
            }

            private void click(List<ClickPositionBean> tableViewItems, String loopTimeText) {
                int dataSize = tableViewItems.size();
                updateProgress(0, dataSize);
                for (int j = 0; j < dataSize; j++) {
                    ClickPositionBean clickPositionBean = tableViewItems.get(j);
                    double x = Double.parseDouble(clickPositionBean.getStartX());
                    double y = Double.parseDouble(clickPositionBean.getStartY());
                    String waitTime = clickPositionBean.getWaitTime();
                    updateMessage(loopTimeText + waitTime + " 秒后将执行 " + clickPositionBean.getType() + " X：" + x + " Y：" + y);
                    try {
                        Thread.sleep(Long.parseLong(waitTime) * 1000);
                    } catch (InterruptedException e) {
                        if (isCancelled()) {
                            break;
                        }
                        throw new RuntimeException(e);
                    }
                    Platform.runLater(() -> {
                        robot.mouseMove(x, y);
                        robot.mousePress(PRIMARY);
                    });
                    updateMessage(loopTimeText + "正在执行 " + clickPositionBean.getType() + " X：" + x + " Y：" + y);
                    Platform.runLater(() -> robot.mouseRelease(PRIMARY));
                    updateMessage(loopTimeText + clickPositionBean.getType() + " X：" + x + " Y：" + y + "执行完毕");
                    updateProgress(j + 1, dataSize);
                }
            }
        };
    }

}
