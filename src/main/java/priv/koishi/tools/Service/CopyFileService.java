package priv.koishi.tools.Service;

import javafx.concurrent.Task;
import priv.koishi.tools.Bean.FileBean;
import priv.koishi.tools.Bean.TaskBean;
import priv.koishi.tools.Configuration.CodeRenameConfig;
import priv.koishi.tools.Configuration.CopyConfig;
import priv.koishi.tools.Configuration.FileConfig;
import priv.koishi.tools.Visitor.CopyVisitor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import static priv.koishi.tools.Visitor.CopyVisitor.determineCopyMode;
import static priv.koishi.tools.Finals.CommonFinals.*;
import static priv.koishi.tools.Service.FileRenameService.getCodeRename;
import static priv.koishi.tools.Utils.FileUtils.getFileType;
import static priv.koishi.tools.Utils.FileUtils.readAllFiles;
import static priv.koishi.tools.Utils.UiUtils.*;

/**
 * 文件复制工具服务类
 *
 * @author KOISHI
 * Date:2025-08-20
 * Time:14:53
 */
public class CopyFileService {

    /**
     * 复制文件预览功能任务
     *
     * @param taskBean 任务设置
     * @param fileBean 文件信息
     * @return 复制文件预览列表
     */
    public static Task<List<FileBean>> readCopyFile(TaskBean<FileBean> taskBean, FileBean fileBean) {
        return new Task<>() {
            @Override
            protected List<FileBean> call() throws IOException {
                changeDisableNodes(taskBean, true);
                updateMessage(text_readData);
                CopyConfig copyConfig = fileBean.getCopyConfig();
                String copyType = copyConfig.getCopyType();
                String sourcePath = fileBean.getPath();
                File sourceFile = new File(sourcePath);
                String outPath = copyConfig.getOutPath();
                List<File> sources = getSources(sourceFile, copyType, copyConfig);
                // 计算复制后的文件名称
                CodeRenameConfig codeRenameConfig = creatCodeRenameConfig(copyConfig);
                int sourcesSize = sources.size();
                List<FileBean> fileBeanList = new ArrayList<>();
                int copyNum = copyConfig.getCopyNum();
                updateProgress(0, sourcesSize);
                List<String> newPathList = new ArrayList<>();
                for (int i = 0; i < sourcesSize; i++) {
                    File source = sources.get(i);
                    int tag = codeRenameConfig.getTag();
                    for (int j = 0; j < copyNum; j++) {
                        FileBean fileBean = creatFileBean(taskBean.getTableView(), source)
                                .setCopyPath(outPath);
                        String newName = fileBean.getName();
                        if (copyConfig.isFirstRename()) {
                            newName = getCodeRename(codeRenameConfig, fileBean, -1, tag + j);
                            tag++;
                        }
                        String extension = getFileType(source);
                        if (extension_file.equals(extension) || extension_folder.equals(extension)) {
                            extension = "";
                        }
                        String newPath = fileBean.getCopyPath() + File.separator + newName + extension;
                        File newFile = new File(newPath);
                        while (newFile.exists() || newPathList.contains(newPath)) {
                            newName = getCodeRename(codeRenameConfig, fileBean, -1, tag + j);
                            newPath = fileBean.getCopyPath() + File.separator + newName + extension;
                            newFile = new File(newPath);
                            tag++;
                        }
                        fileBean.setName(newName);
                        fileBeanList.add(fileBean);
                        newPathList.add(newPath);
                    }
                    updateProgress(i + 1, sourcesSize);
                }
                return fileBeanList;
            }
        };
    }

    /**
     * 复制文件功能任务
     *
     * @param taskBean 任务设置
     * @return 复制后要打开的目录
     */
    public static Task<List<String>> copyFile(TaskBean<FileBean> taskBean) {
        return new Task<>() {
            @Override
            protected List<String> call() throws IOException {
                changeDisableNodes(taskBean, true);
                taskBean.getCancelButton().setVisible(true);
                List<FileBean> fileBeans = taskBean.getBeanList();
                int fileBeanListSize = fileBeans.size();
                updateMessage("正在复制文件");
                updateProgress(0, fileBeanListSize);
                List<String> newPathList = new ArrayList<>();
                for (int i = 0; i < fileBeanListSize; i++) {
                    if (isCancelled()) {
                        break;
                    }
                    FileBean fileBean = fileBeans.get(i);
                    updateMessage("即将复制：" + fileBean.getName());
                    CopyConfig copyConfig = fileBean.getCopyConfig();
                    String copyType = copyConfig.getCopyType();
                    String sourcePath = fileBean.getPath();
                    File sourceFile = new File(sourcePath);
                    String outPath = copyConfig.getOutPath();
                    List<File> sources = getSources(sourceFile, copyType, copyConfig);
                    // 计算复制后的文件名称
                    CodeRenameConfig codeRenameConfig = creatCodeRenameConfig(copyConfig);
                    int copyNum = copyConfig.getCopyNum();
                    for (File source : sources) {
                        if (isCancelled()) {
                            break;
                        }
                        int tag = codeRenameConfig.getTag();
                        for (int j = 0; j < copyNum; j++) {
                            if (isCancelled()) {
                                break;
                            }
                            FileBean bean = creatFileBean(taskBean.getTableView(), source)
                                    .setCopyPath(outPath);
                            String newName = bean.getName();
                            if (copyConfig.isFirstRename()) {
                                newName = getCodeRename(codeRenameConfig, bean, -1, tag + j);
                                tag++;
                            }
                            String extension = getFileType(source);
                            if (extension_file.equals(extension) || extension_folder.equals(extension)) {
                                extension = "";
                            }
                            String copyPath = bean.getCopyPath();
                            String newPath = copyPath + File.separator + newName + extension;
                            File newFile = new File(newPath);
                            while (newFile.exists()) {
                                newName = getCodeRename(codeRenameConfig, bean, -1, tag + j);
                                newPath = copyPath + File.separator + newName + extension;
                                newFile = new File(newPath);
                                tag++;
                            }
                            File targetDirectory = new File(copyPath);
                            if (!targetDirectory.exists()) {
                                if (!targetDirectory.mkdirs()) {
                                    throw new RuntimeException("创建文件夹 " + targetDirectory + " 失败");
                                }
                            }
                            if (isCancelled()) {
                                break;
                            }
                            if (source.isDirectory()) {
                                Path sourceRoot = source.toPath();
                                Files.walkFileTree(sourceRoot, new CopyVisitor(
                                        sourceRoot,
                                        newFile.toPath(),
                                        determineCopyMode(copyType),
                                        getFilterExtensionList(copyConfig.getFilterFileType()),
                                        sourceAction_saveFile,
                                        copyConfig.getHideFileType(),
                                        codeRenameConfig,
                                        copyConfig.isReverseFileType()));
                            } else {
                                Files.copy(source.toPath(), newFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
                            }
                            if (copyConfig.isOpenDirectory()) {
                                if (!newPathList.contains(copyPath)) {
                                    newPathList.add(copyPath);
                                }
                            }
                            updateMessage("已复制文件：" + newName + extension + " 到：" + copyPath);
                        }
                    }
                    updateProgress(i + 1, fileBeanListSize);
                    updateMessage("所有文件已复制完毕");
                }
                return newPathList.stream().distinct().toList();
            }
        };
    }

    /**
     * 创建按变编号重命名设置参数类
     *
     * @param copyConfig 复制配置
     * @return 按变编号重命名设置参数类
     */
    private static CodeRenameConfig creatCodeRenameConfig(CopyConfig copyConfig) {
        return new CodeRenameConfig()
                .setDifferenceCode(copyConfig.getDifferenceCode())
                .setSubCode(copyConfig.getSubCode())
                .setAddSpace(copyConfig.isAddSpace())
                .setPrefix(copyConfig.getPrefix())
                .setTag(copyConfig.getTag())
                .setStartSize(-1)
                .setNameNum(1);
    }

    /**
     * 读取要复制的文件
     *
     * @param sourceFile 源文件
     * @param copyType   复制类型
     * @param copyConfig 复制配置
     * @return 要复制的文件
     */
    private static List<File> getSources(File sourceFile, String copyType, CopyConfig copyConfig) {
        List<File> sources = new ArrayList<>();
        // 读取要复制的文件
        if (sourceFile.isDirectory()) {
            if (copyType_all.equals(copyType) || moveType_folder.equals(copyType)) {
                sources.add(sourceFile);
            } else {
                String showDirectory = text_fileDirectory;
                boolean recursion = false;
                switch (copyType) {
                    case copyType_file:
                        showDirectory = text_onlyFile;
                        recursion = true;
                        break;
                    case copyType_noTopFolderFile:
                        showDirectory = text_fileDirectory;
                        break;
                    case moveType_noTopFolder:
                        showDirectory = text_onlyDirectory;
                        break;
                    case copyType_rootFile:
                        showDirectory = text_onlyFile;
                        break;
                }
                FileConfig fileConfig = new FileConfig();
                fileConfig.setFilterExtensionList(getFilterExtensionList(copyConfig.getFilterFileType()))
                        .setReverseFileType(copyConfig.isReverseFileType())
                        .setShowHideFile(copyConfig.getHideFileType())
                        .setShowDirectory(showDirectory)
                        .setRecursion(recursion)
                        .setShowFileType(false)
                        .setInFile(sourceFile);
                List<File> files = readAllFiles(fileConfig);
                sources.addAll(files);
            }
        } else if (sourceFile.isFile()) {
            sources.add(sourceFile);
        }
        return sources;
    }

}
