package priv.koishi.tools.Service;

import javafx.concurrent.Task;
import priv.koishi.tools.Bean.FileBean;
import priv.koishi.tools.Bean.TaskBean;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import static priv.koishi.tools.Controller.MainController.moveFileController;
import static priv.koishi.tools.Finals.CommonFinals.text_addDirectory;
import static priv.koishi.tools.Utils.FileUtils.*;
import static priv.koishi.tools.Utils.UiUtils.changeDisableNodes;

/**
 * 批量移动文件任务类
 *
 * @author KOISHI
 * Date:2025-08-05
 * Time:18:46
 */
public class MoveFileService {

    /**
     * 移动文件任务
     *
     * @param taskBean 任务设置
     */
    public static Task<Void> moveFile(TaskBean<FileBean> taskBean) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                // 改变要防重复点击的组件状态
                changeDisableNodes(taskBean, true);
                updateMessage("正在校验要处理的路径");
                List<FileBean> fileBeanList = taskBean.getBeanList();
                String targetDirectory = moveFileController.outPath_MV.getText();
                if (text_addDirectory.equals(moveFileController.addFileType_MV.getValue())) {
                    List<File> fileList = new ArrayList<>();
                    for (FileBean fileBean : fileBeanList) {
                        File file = new File(fileBean.getPath());
                        fileList.add(file);
                    }
                    // 筛选出顶级目录
                    fileList = filterTopDirectories(fileList);
                    int fileListSize = fileList.size();
                    updateProgress(0, fileListSize);
                    // 收集所有文件（递归）
                    List<File> allFiles = new ArrayList<>();
                    for (int i = 0; i < fileListSize; i++) {
                        File dir = fileList.get(i);
                        collectFilesRecursively(dir, allFiles);
                        updateProgress(i + 1, fileListSize);
                    }
                    int allFilesSize = allFiles.size();
                    updateMessage("正在复制文件");
                    updateProgress(0, allFilesSize);
                    for (int i = 0; i < allFilesSize; i++) {
                        File file = allFiles.get(i);
                        try {
                            // 构建目标文件路径（不保留目录结构）
                            File targetFile = new File(targetDirectory, file.getName());
                            updateMessage("正在复制文件：" + file.getPath());
                            // 防止重名覆盖，自动重命名
                            String safePath = notOverwritePath(targetFile.getAbsolutePath());
                            File safeTargetFile = new File(safePath);
                            // 复制文件，替换已有文件
                            Files.copy(file.toPath(), safeTargetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        updateProgress(i + 1, allFilesSize);
                    }
                }
                updateMessage("所有文件以复制到：" + targetDirectory);
                return null;
            }
        };
    }

}
