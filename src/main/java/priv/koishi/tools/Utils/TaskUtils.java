package priv.koishi.tools.Utils;

import javafx.concurrent.Task;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Color;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import priv.koishi.tools.Bean.ExcelConfigBean;
import priv.koishi.tools.Bean.TaskBean;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

import static priv.koishi.tools.Utils.FileUtils.*;

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
            progressBar.setProgress(0.0);
            progressBar.progressProperty().bind(task.progressProperty());
        }
        if (massageLabel != null) {
            //绑定TextField的值属性
            massageLabel.textProperty().unbind();
            massageLabel.textProperty().bind(task.messageProperty());
        }
    }

    /**
     * 线程执行成功后保存excel文件
     */
    public static void saveExcelOnSucceeded(ExcelConfigBean excelConfigBean, TaskBean<?> taskBean, Task<SXSSFWorkbook> buildExcelTask,
                                            CheckBox openDirectory, CheckBox openFile, ExecutorService executorService) {
        bindingProgressBarTask(buildExcelTask, taskBean);
        buildExcelTask.setOnSucceeded(t -> {
            SXSSFWorkbook workbook = buildExcelTask.getValue();
            Task<String> saveExceltask = saveExceltask(excelConfigBean, workbook);
            throwTaskException(saveExceltask,taskBean);
            bindingProgressBarTask(saveExceltask, taskBean);
            saveExceltask.setOnSucceeded(s -> {
                String excelPath = saveExceltask.getValue();
                try {
                    if (openDirectory.isSelected()) {
                        openFile(getFileMkdir(new File(excelPath)));
                    }
                    if (openFile.isSelected()) {
                        openFile(excelPath);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                ProgressBar progressBar = taskBean.getProgressBar();
                progressBar.setVisible(false);
                progressBar.progressProperty().unbind();
                Label massageLabel = taskBean.getMassageLabel();
                massageLabel.setTextFill(Color.GREEN);
                massageLabel.textProperty().unbind();
            });
            executorService.execute(saveExceltask);
        });
        executorService.execute(buildExcelTask);
    }

    /**
     * 抛出task异常
     */
    public static void throwTaskException(Task<?> task, TaskBean<?> taskBean) {
        task.setOnFailed(event -> {
            taskBean.getMassageLabel().textProperty().unbind();
            taskBean.getProgressBar().setVisible(false);
            taskBean.getProgressBar().progressProperty().unbind();
            // 获取抛出的异常
            Throwable ex = task.getException();
            throw new RuntimeException(ex);
        });
    }

    /**
     * 保存excel线程
     */
    public static Task<String> saveExceltask(ExcelConfigBean excelConfigBean, SXSSFWorkbook workbook) {
        return new Task<>() {
            @Override
            protected String call() throws Exception {
                updateMessage("正在保存excel");
                String excelPath = saveExcel(workbook, excelConfigBean);
                updateMessage("所有数据已保存到： " + excelPath);
                return excelPath;
            }
        };
    }


}
