package priv.koishi.tools.Utils;

import javafx.concurrent.Task;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Color;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import priv.koishi.tools.Bean.ExcelConfigBean;
import priv.koishi.tools.Bean.TaskBean;

import java.io.File;

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
        //绑定进度条的值属性
        progressBar.progressProperty().unbind();
        progressBar.setVisible(true);
        //给进度条设置初始值
        progressBar.setProgress(0.0);
        progressBar.progressProperty().bind(task.progressProperty());
        //绑定TextField的值属性
        massageLabel.textProperty().unbind();
        massageLabel.textProperty().bind(task.messageProperty());
    }

    /**
     * 线程执行成功后保存excel文件
     */
    public static void saveExcelOnSucceeded(ExcelConfigBean excelConfigBean, TaskBean<?> taskBean, Task<XSSFWorkbook> buildExcelTask,
                                            CheckBox openDirectory, CheckBox openFile) {
        bindingProgressBarTask(buildExcelTask, taskBean);
        buildExcelTask.setOnSucceeded(t -> {
            XSSFWorkbook xssfWorkbook = buildExcelTask.getValue();
            String excelPath;
            Label label = taskBean.getMassageLabel();
            try {
                label.textProperty().unbind();
                label.setText("正在保存excel");
                excelPath = saveExcel(xssfWorkbook, excelConfigBean);
                label.setText("所有数据已保存到： " + excelPath);
                if (openDirectory.isSelected()) {
                    openFile(getFileMkdir(new File(excelPath)));
                }
                if (openFile.isSelected()) {
                    openFile(excelPath);
                }
                taskBean.getProgressBar().setVisible(false);
                label.setTextFill(Color.GREEN);
            } catch (Exception e) {
                label.setTextFill(Color.RED);
                throw new RuntimeException(e);
            }
        });
        new Thread(buildExcelTask).start();
    }

    /**
     * 抛出task异常
     */
    public static void throwTaskException(Task<?> task) {
        task.setOnFailed(event -> {
            // 获取抛出的异常
            Throwable ex = task.getException();
            // 处理异常，例如打印堆栈跟踪信息
            throw new RuntimeException(ex);
        });
    }

}
