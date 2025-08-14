package priv.koishi.tools.Service;

import javafx.concurrent.Task;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableView;
import priv.koishi.tools.Bean.FileBean;
import priv.koishi.tools.Bean.TaskBean;
import priv.koishi.tools.Configuration.FileConfig;
import priv.koishi.tools.CopyVisitor.CopyVisitor;
import priv.koishi.tools.Enum.CopyMode;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import static priv.koishi.tools.Controller.MainController.moveFileController;
import static priv.koishi.tools.Finals.CommonFinals.*;
import static priv.koishi.tools.Utils.FileUtils.*;
import static priv.koishi.tools.Utils.UiUtils.*;

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
                boolean isAllDirectory = false;
                for (File file : inFileList) {
                    if (text_addFile.equals(addType)) {
                        if (file.isFile()) {
                            addFiles.add(file);
                        } else if (file.isDirectory()) {
                            List<String> filterExtensionList = getFilterExtensionList(moveFileController.filterFileType_MV);
                            FileConfig fileConfig = new FileConfig();
                            fileConfig.setFilterExtensionList(filterExtensionList)
                                    .setShowDirectoryName(text_onlyFile)
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
                addRemoveSameFile(inFileList, isAllDirectory, moveFileController.tableView_MV);
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
                removeSameFilePath(moveFileController.tableView_MV, fileBeanList);
                return null;
            }
        };
    }

    /**
     * 移动文件任务
     *
     * @param taskBean 任务设置
     */
    public static Task<Void> moveFile(TaskBean<FileBean> taskBean) {
        return new Task<>() {
            @Override
            protected Void call() throws IOException {
                // 改变要防重复点击的组件状态
                changeDisableNodes(taskBean, true);
                updateMessage("正在校验要处理的路径");
                List<FileBean> fileBeanList = taskBean.getBeanList();
                String moveType = moveFileController.moveType_MV.getValue();
                String targetDirectory = moveFileController.outPath_MV.getText();
                String addFileType = moveFileController.addFileType_MV.getValue();
                String sourceAction = moveFileController.sourceAction_MV.getValue();
                String hideFileType = moveFileController.hideFileType_MV.getValue();
                List<String> filterExtensionList = getFilterExtensionList(moveFileController.filterFileType_MV);
                List<File> fileList = new ArrayList<>();
                for (FileBean fileBean : fileBeanList) {
                    File file = new File(fileBean.getPath());
                    fileList.add(file);
                }
                int fileListSize = fileList.size();
                updateMessage("开始移动文件");
                updateProgress(0, fileListSize);
                if (text_addDirectory.equals(addFileType)) {
                    // 筛选顶级目录
                    List<File> topDirs = filterTopDirectories(fileList);
                    CopyMode copyMode = determineCopyMode(moveType);
                    int topDirsSize = topDirs.size();
                    for (int i = 0; i < topDirsSize; i++) {
                        File source = topDirs.get(i);
                        updateMessage("正在移动：" + source.getPath());
                        Path sourcePath = source.toPath();
                        Path targetPath = determineTargetPath(source, targetDirectory, moveType);
                        Files.walkFileTree(sourcePath,
                                new CopyVisitor(sourcePath,
                                        targetPath,
                                        copyMode,
                                        filterExtensionList,
                                        sourceAction,
                                        hideFileType));
                        updateProgress(i + 1, fileListSize);
                    }
                } else if (text_addFile.equals(addFileType)) {
                    for (int i = 0; i < fileListSize; i++) {
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
                        String safePath = notOverwritePath(targetFile.getAbsolutePath());
                        File safeTargetFile = new File(safePath);
                        Files.copy(file.toPath(),
                                safeTargetFile.toPath(),
                                StandardCopyOption.COPY_ATTRIBUTES);
                        if (sourceAction_deleteFile.equals(sourceAction)) {
                            Files.delete(file.toPath());
                        } else if (sourceAction_trashFile.equals(sourceAction)) {
                            Desktop.getDesktop().moveToTrash(file);
                        }
                        updateProgress(i + 1, fileListSize);
                    }
                }
                updateMessage("所有文件以移动到：" + targetDirectory);
                return null;
            }
        };
    }

    /**
     * 根据移动类型确定移动模式
     *
     * @param moveType 指定的移动类型标识符
     * @return 返回对应的CopyMode枚举值
     */
    private static CopyMode determineCopyMode(String moveType) {
        if (moveType_file.equals(moveType)) {
            return CopyMode.ONLY_FILES;
        } else if (moveType_folder.equals(moveType) || moveType_noTopFolder.equals(moveType)) {
            return CopyMode.STRUCTURE_ONLY_FOLDERS;
        } else {
            return CopyMode.STRUCTURE_WITH_FILES;
        }
    }

    /**
     * 确定目标路径的构建策略
     *
     * @param source    源文件对象
     * @param targetDir 目标基础目录路径
     * @param moveType  移动类型标识符
     * @return 构建完成的目标路径对象
     */
    private static Path determineTargetPath(File source, String targetDir, String moveType) {
        Path targetPath = Paths.get(targetDir);
        if (moveType_all.equals(moveType) || moveType_folder.equals(moveType)) {
            String safeDir = notOverwritePath(targetDir + File.separator + source.getName());
            return Paths.get(safeDir);
        }
        return targetPath;
    }

}
