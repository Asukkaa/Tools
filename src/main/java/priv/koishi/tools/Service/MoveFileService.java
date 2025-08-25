package priv.koishi.tools.Service;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableView;
import org.apache.commons.collections4.CollectionUtils;
import priv.koishi.tools.Bean.FileBean;
import priv.koishi.tools.Bean.TaskBean;
import priv.koishi.tools.Configuration.CodeRenameConfig;
import priv.koishi.tools.Configuration.FileConfig;
import priv.koishi.tools.Visitor.CopyVisitor;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import static priv.koishi.tools.Controller.MainController.moveFileController;
import static priv.koishi.tools.Finals.CommonFinals.*;
import static priv.koishi.tools.Utils.FileUtils.*;
import static priv.koishi.tools.Utils.UiUtils.*;
import static priv.koishi.tools.Visitor.CopyVisitor.determineCopyMode;

/**
 * 批量移动文件任务类
 *
 * @author KOISHI
 * Date:2025-08-05
 * Time:18:46
 */
public class MoveFileService {

    /**
     * 读取要处理的文件任务
     *
     * @param taskBean 读取文件任务设置
     * @return 读取文件任务
     */
    public static Task<Void> readMoveFile(TaskBean<FileBean> taskBean) {
        return new Task<>() {
            @Override
            protected Void call() throws IOException {
                // 改变要防重复点击的组件状态
                changeDisableNodes(taskBean, true);
                updateMessage(text_readData);
                List<File> inFileList = taskBean.getInFileList();
                List<File> addFiles = new ArrayList<>();
                ChoiceBox<String> addFileType = moveFileController.addFileType_MV;
                String addType = addFileType.getValue();
                TableView<FileBean> tableView = moveFileController.tableView_MV;
                List<String> filterExtensionList = getFilterExtensionList(moveFileController.filterFileType_MV);
                boolean reverseFileType = moveFileController.reverseFileType_MV.isSelected();
                boolean isAllDirectory = false;
                for (File file : inFileList) {
                    if (text_addFile.equals(addType)) {
                        if (file.isFile()) {
                            String fileType = getFileType(file);
                            boolean matches = filterExtensionList.contains(fileType);
                            // 反向匹配文件类型
                            if (reverseFileType) {
                                matches = !matches;
                            }
                            if (CollectionUtils.isEmpty(filterExtensionList) || matches) {
                                addFiles.add(file);
                            }
                        } else if (file.isDirectory()) {
                            FileConfig fileConfig = new FileConfig();
                            fileConfig.setFilterExtensionList(filterExtensionList)
                                    .setReverseFileType(reverseFileType)
                                    .setShowDirectory(text_onlyFile)
                                    .setRecursion(true)
                                    .setInFile(file);
                            addFiles.addAll(readAllFiles(fileConfig));
                        }
                    } else if (text_addDirectory.equals(addType)) {
                        isAllDirectory = true;
                        if (file.isDirectory()) {
                            addFiles.add(file);
                        } else if (file.isFile()) {
                            addFiles.add(file.getParentFile());
                        }
                    }
                }
                addRemoveSameFile(addFiles, isAllDirectory, tableView);
                return null;
            }
        };
    }

    /**
     * 添加文件任务
     *
     * @param taskBean       添加文件任务设置
     * @param isAllDirectory 添加的文件是否为目录
     */
    public static Task<Void> addMoveFile(TaskBean<FileBean> taskBean, boolean isAllDirectory) {
        return new Task<>() {
            @Override
            protected Void call() throws IOException {
                // 改变要防重复点击的组件状态
                changeDisableNodes(taskBean, true);
                updateMessage(text_readData);
                List<File> inFileList = taskBean.getInFileList();
                addRemoveSameFile(inFileList, isAllDirectory, taskBean.getTableView());
                return null;
            }
        };
    }

    /**
     * 根据文件路径去重任务
     *
     * @param taskBean 删除重复文件任务设置
     */
    public static Task<Void> removeSameMoveFile(TaskBean<FileBean> taskBean) {
        return new Task<>() {
            @Override
            protected Void call() {
                // 改变要防重复点击的组件状态
                changeDisableNodes(taskBean, true);
                updateMessage("正在校验要处理路径");
                List<FileBean> fileBeanList = taskBean.getBeanList();
                removeSameFilePath(taskBean.getTableView(), fileBeanList);
                return null;
            }
        };
    }

    /**
     * 移动文件任务
     *
     * @param taskBean 任务设置
     */
    public static Task<String> moveFile(TaskBean<FileBean> taskBean, CodeRenameConfig codeRenameConfig) {
        return new Task<>() {
            @Override
            protected String call() throws IOException {
                changeDisableNodes(taskBean, true);
                taskBean.getCancelButton().setVisible(true);
                updateMessage("正在校验要处理的路径");
                List<FileBean> fileBeanList = taskBean.getBeanList();
                String moveType = moveFileController.moveType_MV.getValue();
                String targetDirectory = moveFileController.outPath_MV.getText();
                String addFileType = moveFileController.addFileType_MV.getValue();
                String sourceAction = moveFileController.sourceAction_MV.getValue();
                String hideFileType = moveFileController.hideFileType_MV.getValue();
                List<String> filterExtensionList = getFilterExtensionList(moveFileController.filterFileType_MV);
                List<File> fileList = new ArrayList<>();
                int fileListSize = fileBeanList.size();
                updateProgress(0, fileListSize);
                List<FileBean> errorFileBeans = new ArrayList<>();
                String errorMsg = "检测到错误的目标目录设置：\n";
                for (int i = 0; i < fileListSize; i++) {
                    if (isCancelled()) {
                        break;
                    }
                    FileBean fileBean = fileBeanList.get(i);
                    String sourcePath = fileBean.getPath();
                    File file = new File(sourcePath);
                    fileList.add(file);
                    if (text_addDirectory.equals(addFileType)) {
                        updateMessage("正在校验文件路径设置");
                        if (targetDirectory.startsWith(sourcePath)) {
                            errorFileBeans.add(fileBean);
                            errorMsg += "\n序号为：" + fileBean.getIndex() + " 的文件夹 " + fileBean.getName() +
                                    " 目标目录设置在源文件夹目录下" + "\n文件夹路径为：" + sourcePath +
                                    "\n目标目录为：" + targetDirectory + "\n";
                        }
                    }
                    updateProgress(i + 1, fileListSize);
                }
                if (CollectionUtils.isNotEmpty(errorFileBeans)) {
                    String finalErrorMsg = errorMsg;
                    Platform.runLater(() -> {
                        Alert alert = creatErrorAlert(finalErrorMsg);
                        alert.setHeaderText("目标目录不能设置在源文件夹目录下");
                        alert.showAndWait();
                    });
                    return null;
                }
                updateMessage("开始移动文件");
                updateProgress(0, fileListSize);
                if (text_addDirectory.equals(addFileType)) {
                    // 筛选顶级目录
                    List<File> topDirs = filterTopDirectories(fileList);
                    int topDirsSize = topDirs.size();
                    for (int i = 0; i < topDirsSize; i++) {
                        if (isCancelled()) {
                            break;
                        }
                        File source = topDirs.get(i);
                        updateMessage("正在移动：" + source.getPath());
                        Path sourcePath = source.toPath();
                        Path targetPath = determineTargetPath(source, targetDirectory, moveType, codeRenameConfig);
                        Files.walkFileTree(sourcePath,
                                new CopyVisitor(sourcePath,
                                        targetPath,
                                        determineCopyMode(moveType),
                                        filterExtensionList,
                                        sourceAction,
                                        hideFileType,
                                        codeRenameConfig,
                                        moveFileController.reverseFileType_MV.isSelected(),
                                        taskBean.getWorkTask()));
                        updateProgress(i + 1, fileListSize);
                    }
                } else if (text_addFile.equals(addFileType)) {
                    for (int i = 0; i < fileListSize; i++) {
                        if (isCancelled()) {
                            break;
                        }
                        File file = fileList.get(i);
                        if (file.isHidden() && text_noHideFile.equals(hideFileType)) {
                            continue;
                        }
                        if (!file.isHidden() && text_onlyHideFile.equals(hideFileType)) {
                            continue;
                        }
                        updateMessage("正在移动：" + file.getPath());
                        File targetFile = new File(targetDirectory, file.getName());
                        // 防止重名覆盖，自动重命名
                        String safePath = notOverwritePath(targetFile.getAbsolutePath(), codeRenameConfig);
                        File safeTargetFile = new File(safePath);
                        Files.copy(file.toPath(), safeTargetFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
                        if (sourceAction_deleteFile.equals(sourceAction)) {
                            Files.delete(file.toPath());
                        } else if (sourceAction_trashFile.equals(sourceAction)) {
                            Desktop.getDesktop().moveToTrash(file);
                        }
                        updateProgress(i + 1, fileListSize);
                    }
                }
                updateMessage("所有文件以移动到：" + targetDirectory);
                return targetDirectory;
            }
        };
    }

    /**
     * 确定目标路径的构建策略
     *
     * @param source           源文件对象
     * @param targetDir        目标基础目录路径
     * @param moveType         移动类型标识符
     * @param codeRenameConfig 重命名规则
     * @return 构建完成的目标路径对象
     */
    private static Path determineTargetPath(File source, String targetDir, String moveType, CodeRenameConfig codeRenameConfig) {
        Path targetPath = Paths.get(targetDir);
        if (moveType_all.equals(moveType) || moveType_folder.equals(moveType)) {
            String safeDir = notOverwritePath(targetDir + File.separator + source.getName(), codeRenameConfig);
            return Paths.get(safeDir);
        }
        return targetPath;
    }

}
