package priv.koishi.tools.Service;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.input.MouseButton;
import javafx.scene.robot.Robot;
import priv.koishi.tools.Bean.ClickPositionBean;
import priv.koishi.tools.Bean.TaskBean;

import java.util.List;

import static javafx.scene.input.MouseButton.PRIMARY;
import static priv.koishi.tools.Finals.CommonFinals.clickTypeMap;

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
                // 执行自动流程前点击第一个起始坐标
                if (taskBean.isFirstClick()) {
                    ClickPositionBean clickPositionBean = tableViewItems.getFirst();
                    double x = Double.parseDouble(clickPositionBean.getStartX());
                    double y = Double.parseDouble(clickPositionBean.getStartY());
                    Platform.runLater(() -> {
                        robot.mouseMove(x, y);
                        robot.mousePress(PRIMARY);
                        robot.mouseRelease(PRIMARY);
                        updateMessage("已切换到目标窗口");
                    });
                }
                int loopTime = taskBean.getLoopTime();
                if (loopTime == 0) {
                    int i = 0;
                    while (!isCancelled()) {
                        i++;
                        String loopTimeText = "第 " + i + " / ∞" + " 轮操作\n";
                        if (isCancelled()) {
                            break;
                        }
                        // 执行点击任务
                        clicks(tableViewItems, loopTimeText);
                    }
                } else {
                    for (int i = 0; i < loopTime && !isCancelled(); i++) {
                        String loopTimeText = "第 " + (i + 1) + " / " + loopTime + " 轮操作\n";
                        if (isCancelled()) {
                            break;
                        }
                        // 执行点击任务
                        clicks(tableViewItems, loopTimeText);
                    }
                }
                return null;
            }

            // 执行点击任务
            private void clicks(List<ClickPositionBean> tableViewItems, String loopTimeText) {
                int dataSize = tableViewItems.size();
                updateProgress(0, dataSize);
                for (int j = 0; j < dataSize; j++) {
                    ClickPositionBean clickPositionBean = tableViewItems.get(j);
                    double startX = Double.parseDouble(clickPositionBean.getStartX());
                    double startY = Double.parseDouble(clickPositionBean.getStartY());
                    double endX = Double.parseDouble(clickPositionBean.getEndX());
                    double endY = Double.parseDouble(clickPositionBean.getEndY());
                    String waitTime = clickPositionBean.getWaitTime();
                    String clickTime = clickPositionBean.getClickTime();
                    String name = clickPositionBean.getName();
                    String clickNum = clickPositionBean.getClickNum();
                    Platform.runLater(() -> {
                        updateMessage(loopTimeText + waitTime + " 毫秒后将执行: " + name + "\n" +
                                "操作内容：" + clickPositionBean.getType() + " X：" + startX + " Y：" + startY + " 在 " +
                                clickTime + " 毫秒内移动到 X：" + endX + " Y：" + endY + " 共 " + clickNum + " 次");
                        System.out.println(loopTimeText + waitTime + " 毫秒后将执行: " + name + "\n" +
                                "操作内容：" + clickPositionBean.getType() + " X：" + startX + " Y：" + startY + " 在 " +
                                clickTime + " 毫秒内移动到 X：" + endX + " Y：" + endY + " 共 " + clickNum + " 次");
                    });
                    try {
                        Thread.sleep(Long.parseLong(waitTime));
                    } catch (InterruptedException e) {
                        if (isCancelled()) {
                            break;
                        }
                    }
                    click(clickPositionBean, robot);
                    Platform.runLater(() -> {
                        updateMessage(loopTimeText + name + "执行完毕");
                        System.out.println(loopTimeText + name + "执行完毕");
                    });
                    updateProgress(j + 1, dataSize);
                }
            }
        };
    }

    /**
     * 按照操作设置执行操作
     *
     * @param clickPositionBean 操作设置
     * @param robot             Robot实例
     */
    public static void click(ClickPositionBean clickPositionBean, Robot robot) {
        // 操作次数
        int clickNum = Integer.parseInt(clickPositionBean.getClickNum());
        double startX = Double.parseDouble(clickPositionBean.getStartX());
        double startY = Double.parseDouble(clickPositionBean.getStartY());
        double endX = Double.parseDouble(clickPositionBean.getEndX());
        double endY = Double.parseDouble(clickPositionBean.getEndY());
        long clickTime = Long.parseLong(clickPositionBean.getClickTime());
        long clickInterval = Long.parseLong(clickPositionBean.getClickInterval());
        for (int i = 0; i < clickNum; i++) {
            // 每次操作的间隔时间
            if (i > 0) {
                try {
                    Thread.sleep(clickInterval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            MouseButton mouseButton = clickTypeMap.get(clickPositionBean.getType());
            Platform.runLater(() -> {
                robot.mouseMove(startX, startY);
                robot.mousePress(mouseButton);
            });
            // 计算鼠标移动的轨迹
            double deltaX = endX - startX;
            double deltaY = endY - startY;
            int steps = 10;
            long stepDuration = clickTime / steps;
            for (int j = 0; j <= steps; j++) {
                double x = startX + deltaX * j / steps;
                double y = startY + deltaY * j / steps;
                Platform.runLater(() -> robot.mouseMove(x, y));
                try {
                    Thread.sleep(stepDuration);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            Platform.runLater(() -> robot.mouseRelease(mouseButton));
        }
    }

}
