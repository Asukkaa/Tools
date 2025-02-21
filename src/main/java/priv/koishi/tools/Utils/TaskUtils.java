package priv.koishi.tools.Utils;

import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Color;
import org.apache.poi.ss.usermodel.Workbook;
import priv.koishi.tools.Bean.TaskBean;
import priv.koishi.tools.Configuration.ExcelConfig;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import static priv.koishi.tools.Finals.CommonFinals.text_saveSuccess;
import static priv.koishi.tools.Utils.ExcelUtils.saveExcel;
import static priv.koishi.tools.Utils.FileUtils.*;
import static priv.koishi.tools.Utils.UiUtils.changeDisableControls;

/**
 * 多线程任务工具的方法
 *
 * @author KOISHI
 * Date:2024-10-28
 * Time:下午3:14
 */
public class TaskUtils {

    /**
     * 绑定带进度条的线程
     *
     * @param task     要绑定的线程任务
     * @param taskBean 绑定线程任务所需参数
     */
    public static void bindingProgressBarTask(Task<?> task, TaskBean<?> taskBean) {
        ProgressBar progressBar = taskBean.getProgressBar();
        Label massageLabel = taskBean.getMassageLabel();
        if (progressBar != null) {
            // 绑定进度条的值属性
            progressBar.progressProperty().unbind();
            progressBar.setVisible(true);
            // 给进度条设置初始值
            progressBar.setProgress(0);
            progressBar.progressProperty().bind(task.progressProperty());
        }
        if (massageLabel != null) {
            // 绑定TextField的值属性
            massageLabel.textProperty().unbind();
            massageLabel.textProperty().bind(task.messageProperty());
        }
        throwTaskException(task, taskBean);
        System.gc();
    }

    /**
     * 线程执行成功后保存excel文件
     *
     * @param excelConfig     excel设置
     * @param taskBean        线程任务所需参数
     * @param buildExcelTask  构建excel线程任务
     * @param openDirectory   打开文件夹选项
     * @param openFile        打开文件选项
     * @param executorService 线程池
     * @throws RuntimeException io异常
     */
    public static void saveExcelOnSucceeded(ExcelConfig excelConfig, TaskBean<?> taskBean, Task<Workbook> buildExcelTask,
                                            CheckBox openDirectory, CheckBox openFile, ExecutorService executorService) {
        bindingProgressBarTask(buildExcelTask, taskBean);
        buildExcelTask.setOnSucceeded(event -> {
            Workbook workbook = buildExcelTask.getValue();
            Task<String> saveExcelTask = saveExcelTask(excelConfig, workbook);
            bindingProgressBarTask(saveExcelTask, taskBean);
            saveExcelTask.setOnSucceeded(s -> {
                String excelPath = saveExcelTask.getValue();
                try {
                    if (openDirectory.isSelected()) {
                        openDirectory(excelPath);
                    }
                    if (openFile.isSelected()) {
                        openFile(excelPath);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    taskUnbind(taskBean);
                }
                taskBean.getMassageLabel().setText(text_saveSuccess + excelPath);
                taskBean.getMassageLabel().setTextFill(Color.GREEN);
            });
            executorService.execute(saveExcelTask);
        });
        executorService.execute(buildExcelTask);
    }

    /**
     * 抛出task异常
     *
     * @param task     有异常的线程任务
     * @param taskBean 线程任务所需参数
     * @throws RuntimeException 线程的异常
     */
    public static void throwTaskException(Task<?> task, TaskBean<?> taskBean) {
        task.setOnFailed(event -> {
            taskUnbind(taskBean);
            // 获取抛出的异常
            Throwable ex = task.getException();
            throw new RuntimeException(ex);
        });
    }

    /**
     * 线程组件解绑
     *
     * @param taskBean 要解绑的线程组件信息
     */
    public static void taskUnbind(TaskBean<?> taskBean) {
        // 隐藏取消按钮
        Button cancelButton = taskBean.getCancelButton();
        if (cancelButton != null) {
            cancelButton.setVisible(false);
        }
        // 解除防重复点击按钮不可点击限制
        changeDisableControls(taskBean, false);
        // 隐藏和解绑消息通知组件
        Label massageLabel = taskBean.getMassageLabel();
        if (massageLabel != null) {
            massageLabel.textProperty().unbind();
        }
        // 隐藏和解绑进度条
        ProgressBar progressBar = taskBean.getProgressBar();
        if (progressBar != null) {
            progressBar.setVisible(false);
            progressBar.progressProperty().unbind();
        }
        System.gc();
    }

    /**
     * 保存excel线程
     *
     * @param excelConfig excel设置
     * @param workbook    excel工作簿
     * @return 保存excel线程任务
     */
    public static Task<String> saveExcelTask(ExcelConfig excelConfig, Workbook workbook) {
        return new Task<>() {
            @Override
            protected String call() throws Exception {
                updateMessage("正在保存excel");
                return saveExcel(workbook, excelConfig);
            }
        };
    }

}
