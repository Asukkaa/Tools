package priv.koishi.tools.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.robot.Robot;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.tools.Bean.AutoClickTaskBean;
import priv.koishi.tools.Bean.ClickPositionBean;
import priv.koishi.tools.Bean.TaskBean;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static javafx.scene.input.MouseButton.NONE;
import static javafx.scene.input.MouseButton.PRIMARY;
import static priv.koishi.tools.Finals.CommonFinals.*;
import static priv.koishi.tools.Utils.FileUtils.notOverwritePath;
import static priv.koishi.tools.Utils.UiUtils.changeDisableNodes;

/**
 * 自动点击线程任务类
 *
 * @author KOISHI
 * Date:2025-02-18
 * Time:14:42
 */
public class AutoClickService {

    /**
     * 加载 PMC 文件
     *
     * @param taskBean 线程任务参数
     * @param files    文件列表
     * @return PMC 文件列表
     */
    public static Task<List<ClickPositionBean>> loadPMC(TaskBean<ClickPositionBean> taskBean, List<File> files) {
        return new Task<>() {
            @Override
            protected List<ClickPositionBean> call() {
                changeDisableNodes(taskBean, true);
                updateMessage(text_readData);
                List<ClickPositionBean> clickPositionBeans = new ArrayList<>();
                for (File file : files) {
                    // 读取 JSON 文件并转换为 List<ClickPositionBean>
                    ObjectMapper objectMapper = new ObjectMapper();
                    String path = file.getPath();
                    File jsonFile = new File(path);
                    try {
                        clickPositionBeans.addAll(objectMapper.readValue(jsonFile, objectMapper.getTypeFactory()
                                .constructCollectionType(List.class, ClickPositionBean.class)));
                    } catch (Exception e) {
                        throw new RuntimeException(text_loadAutoClick + path + text_formatError);
                    }
                }
                return clickPositionBeans;
            }
        };
    }

    /**
     * 导出 PMC 文件
     *
     * @param taskBean    线程任务参数
     * @param fileName    要导出的文件名
     * @param outFilePath 导出文件夹路径
     * @return 导出文件路径
     */
    public static Task<String> exportPMC(TaskBean<ClickPositionBean> taskBean, String fileName, String outFilePath) {
        return new Task<>() {
            @Override
            protected String call() throws IOException {
                changeDisableNodes(taskBean, true);
                updateMessage("正在导出自动操作流程");
                ObservableList<ClickPositionBean> tableViewItems = taskBean.getTableView().getItems();
                if (CollectionUtils.isEmpty(tableViewItems)) {
                    throw new RuntimeException(text_noAutoClickList);
                }
                if (StringUtils.isBlank(outFilePath)) {
                    throw new RuntimeException(text_outPathNull);
                }
                ObjectMapper objectMapper = new ObjectMapper();
                String path = notOverwritePath(outFilePath + File.separator + fileName + PMC);
                objectMapper.writeValue(new File(path), tableViewItems);
                updateMessage(text_saveSuccess + path);
                return path;
            }
        };
    }

    /**
     * 自动点击
     *
     * @param taskBean 线程任务参数
     * @param robot    自动任务执行机器人
     */
    public static Task<Void> autoClick(AutoClickTaskBean taskBean, Robot robot) {
        return new Task<>() {
            @Override
            protected Void call() {
                Timeline timeline = taskBean.getRunTimeline();
                if (timeline != null) {
                    timeline.stop();
                }
                List<ClickPositionBean> tableViewItems = taskBean.getBeanList();
                Label floatingLabel = taskBean.getFloatingLabel();
                // 执行自动流程前点击第一个起始坐标
                if (taskBean.isFirstClick()) {
                    ClickPositionBean clickPositionBean = tableViewItems.getFirst();
                    double x = Double.parseDouble(clickPositionBean.getStartX());
                    double y = Double.parseDouble(clickPositionBean.getStartY());
                    Platform.runLater(() -> {
                        robot.mouseMove(x, y);
                        robot.mousePress(PRIMARY);
                        robot.mouseRelease(PRIMARY);
                        updateMessage(text_changeWindow);
                        floatingLabel.setText(text_cancelTask + text_changeWindow);
                    });
                }
                int loopTime = taskBean.getLoopTime();
                if (loopTime == 0) {
                    int i = 0;
                    while (!isCancelled()) {
                        i++;
                        String loopTimeText = text_execution + i + " / ∞" + text_executionTime;
                        if (isCancelled()) {
                            break;
                        }
                        // 执行点击任务
                        clicks(tableViewItems, loopTimeText);
                    }
                } else {
                    for (int i = 0; i < loopTime && !isCancelled(); i++) {
                        String loopTimeText = text_execution + (i + 1) + " / " + loopTime + text_executionTime;
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
            private void clicks(List<? extends ClickPositionBean> tableViewItems, String loopTimeText) {
                int dataSize = tableViewItems.size();
                Label floatingLabel = taskBean.getFloatingLabel();
                updateProgress(0, dataSize);
                for (int j = 0; j < dataSize; j++) {
                    updateProgress(j + 1, dataSize);
                    ClickPositionBean clickPositionBean = tableViewItems.get(j);
                    int startX = Integer.parseInt((clickPositionBean.getStartX()));
                    int startY = Integer.parseInt((clickPositionBean.getStartY()));
                    int endX = Integer.parseInt((clickPositionBean.getEndX()));
                    int endY = Integer.parseInt((clickPositionBean.getEndY()));
                    String waitTime = clickPositionBean.getWaitTime();
                    String clickTime = clickPositionBean.getClickTime();
                    String name = clickPositionBean.getName();
                    String clickNum = clickPositionBean.getClickNum();
                    Platform.runLater(() -> {
                        String text = loopTimeText + waitTime + " 毫秒后将执行: " + name +
                                "\n操作内容：" + clickPositionBean.getType() + " X：" + startX + " Y：" + startY +
                                "\n在 " + clickTime + " 毫秒内移动到 X：" + endX + " Y：" + endY +
                                "\n每次操作间隔：" + clickPositionBean.getClickInterval() + " 毫秒，共 " + clickNum + " 次";
                        updateMessage(text);
                        floatingLabel.setText(text_cancelTask + text);
                    });
                    // 执行前等待时间
                    try {
                        Thread.sleep(Long.parseLong(waitTime));
                    } catch (InterruptedException e) {
                        if (isCancelled()) {
                            break;
                        }
                    }
                    // 执行自动流程
                    click(clickPositionBean, robot);
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
    private static void click(ClickPositionBean clickPositionBean, Robot robot) {
        // 操作次数
        int clickNum = Integer.parseInt(clickPositionBean.getClickNum());
        double startX = Double.parseDouble(clickPositionBean.getStartX());
        double startY = Double.parseDouble(clickPositionBean.getStartY());
        double endX = Double.parseDouble(clickPositionBean.getEndX());
        double endY = Double.parseDouble(clickPositionBean.getEndY());
        long clickTime = Long.parseLong(clickPositionBean.getClickTime());
        long clickInterval = Long.parseLong(clickPositionBean.getClickInterval());
        // 按照操作次数执行
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
            MouseButton mouseButton = runClickTypeMap.get(clickPositionBean.getType());
            Platform.runLater(() -> {
                robot.mouseMove(startX, startY);
                if (mouseButton != NONE) {
                    robot.mousePress(mouseButton);
                }
            });
            // 计算鼠标移动的轨迹
            double deltaX = endX - startX;
            double deltaY = endY - startY;
            int steps = 10;
            long stepDuration = clickTime / steps;
            for (int j = 1; j <= steps; j++) {
                double x = startX + deltaX * j / steps;
                double y = startY + deltaY * j / steps;
                CompletableFuture<Void> moveFuture = new CompletableFuture<>();
                Platform.runLater(() -> {
                    robot.mouseMove(x, y);
                    moveFuture.complete(null);
                });
                // 等待任务完成
                try {
                    moveFuture.get();
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                // 单次操作时间
                try {
                    Thread.sleep(stepDuration);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            CompletableFuture<Void> releaseFuture = new CompletableFuture<>();
            Platform.runLater(() -> {
                if (mouseButton != NONE) {
                    robot.mouseRelease(mouseButton);
                }
                releaseFuture.complete(null);
            });
            // 等待任务完成
            try {
                releaseFuture.get();
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

}
