package priv.koishi.tools.Utils;

import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Color;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import priv.koishi.tools.Bean.TaskBean;
import priv.koishi.tools.Configuration.ExcelConfig;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

import static priv.koishi.tools.Utils.FileUtils.openFile;
import static priv.koishi.tools.Utils.FileUtils.saveExcel;
import static priv.koishi.tools.Utils.UiUtils.setDisableControls;

/**
 * @author KOISHI
 * Date:2024-10-28
 * Time:下午3:14
 */
public class TaskUtils {

    /**
     * 绑定带进度条的线程
     */
    public static void bindingProgressBarTask(Task<?> task, TaskBean<?> taskBean) {
        ProgressBar progressBar = taskBean.getProgressBar();
        Label massageLabel = taskBean.getMassageLabel();
        if (progressBar != null) {
            //绑定进度条的值属性
            progressBar.progressProperty().unbind();
            progressBar.setVisible(true);
            //给进度条设置初始值
            progressBar.setProgress(0);
            progressBar.progressProperty().bind(task.progressProperty());
        }
        if (massageLabel != null) {
            //绑定TextField的值属性
            massageLabel.textProperty().unbind();
            massageLabel.textProperty().bind(task.messageProperty());
        }
        throwTaskException(task, taskBean);
        System.gc();
    }

    /**
     * 线程执行成功后保存excel文件
     */
    public static void saveExcelOnSucceeded(ExcelConfig excelConfig, TaskBean<?> taskBean, Task<SXSSFWorkbook> buildExcelTask,
                                            CheckBox openDirectory, CheckBox openFile, ExecutorService executorService) {
        bindingProgressBarTask(buildExcelTask, taskBean);
        buildExcelTask.setOnSucceeded(event -> {
            SXSSFWorkbook workbook = buildExcelTask.getValue();
            Task<String> saveExcelTask = saveExceltask(excelConfig, workbook);
            bindingProgressBarTask(saveExcelTask, taskBean);
            saveExcelTask.setOnSucceeded(s -> {
                String excelPath = saveExcelTask.getValue();
                try {
                    if (openDirectory.isSelected()) {
                        openFile(new File(excelPath).getParent());
                    }
                    if (openFile.isSelected()) {
                        openFile(excelPath);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    taskUnbind(taskBean);
                }
                taskBean.getMassageLabel().setText("所有数据已保存到： " + excelPath);
                taskBean.getMassageLabel().setTextFill(Color.GREEN);
            });
            executorService.execute(saveExcelTask);
        });
        executorService.execute(buildExcelTask);
    }

    /**
     * 抛出task异常
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
     */
    public static void taskUnbind(TaskBean<?> taskBean) {
        //隐藏取消按钮
        Button cancelButton = taskBean.getCancelButton();
        if (cancelButton != null) {
            cancelButton.setVisible(false);
        }
        //解除防重复点击按钮不可点击限制
        setDisableControls(taskBean, false);
        //隐藏和解绑消息通知组件
        Label massageLabel = taskBean.getMassageLabel();
        if (massageLabel != null) {
            massageLabel.textProperty().unbind();
        }
        //隐藏和解绑进度条
        ProgressBar progressBar = taskBean.getProgressBar();
        if (progressBar != null) {
            progressBar.setVisible(false);
            progressBar.progressProperty().unbind();
        }
        System.gc();
    }

    /**
     * 保存excel线程
     */
    public static Task<String> saveExceltask(ExcelConfig excelConfig, SXSSFWorkbook workbook) {
        return new Task<>() {
            @Override
            protected String call() throws Exception {
                updateMessage("正在保存excel");
                return saveExcel(workbook, excelConfig);
            }
        };
    }

}
