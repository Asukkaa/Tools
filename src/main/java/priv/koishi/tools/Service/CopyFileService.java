package priv.koishi.tools.Service;

import javafx.concurrent.Task;
import priv.koishi.tools.Bean.FileBean;
import priv.koishi.tools.Bean.TaskBean;
import priv.koishi.tools.Configuration.CodeRenameConfig;
import priv.koishi.tools.Configuration.CopyConfig;
import priv.koishi.tools.Configuration.FileConfig;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
     * @return 复制文件预览
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
                List<File> sources = new ArrayList<>();
                // 读取要复制的文件
                if (sourceFile.isDirectory()) {
                    if (copyType_all.equals(copyType) || moveType_folder.equals(copyType)) {
                        sources.add(sourceFile);
                    } else {
                        String showDirectory = text_fileDirectory;
                        boolean recursion = false;
                        if (copyType_file.equals(copyType)) {
                            showDirectory = text_onlyFile;
                            recursion = true;
                        } else if (copyType_noTopFolderFile.equals(copyType)) {
                            showDirectory = text_fileDirectory;
                        } else if (moveType_noTopFolder.equals(copyType)) {
                            showDirectory = text_onlyDirectory;
                        }
                        FileConfig fileConfig = new FileConfig();
                        fileConfig.setFilterExtensionList(getFilterExtensionList(copyConfig.getFilterFileType()))
                                .setReverseFileType(copyConfig.isReverseFileType())
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
                // 计算复制后的文件名称
                CodeRenameConfig codeRenameConfig = new CodeRenameConfig()
                        .setDifferenceCode(copyConfig.getDifferenceCode())
                        .setSubCode(copyConfig.getSubCode())
                        .setAddSpace(copyConfig.isAddSpace())
                        .setPrefix(copyConfig.getPrefix())
                        .setTag(copyConfig.getTag())
                        .setStartSize(-1)
                        .setNameNum(1);
                int sourcesSize = sources.size();
                List<FileBean> fileBeanList = new ArrayList<>();
                int copyNum = copyConfig.getCopyNum();
                updateProgress(0, sourcesSize);
                for (int i = 0; i < sourcesSize; i++) {
                    File source = sources.get(i);
                    int tag = codeRenameConfig.getTag();
                    for (int j = 0; j < copyNum; j++) {
                        FileBean fileBean = creatFileBean(taskBean.getTableView(), source)
                                .setCopyPath(outPath);
                        String newName = getCodeRename(codeRenameConfig, fileBean, -1, tag + j);
                        String extension = getFileType(source);
                        if (extension_file.equals(extension) || extension_folder.equals(extension)) {
                            extension = "";
                        }
                        String newPath = fileBean.getCopyPath() + File.separator + newName + extension;
                        File newFile = new File(newPath);
                        while (newFile.exists()) {
                            tag++;
                            newName = getCodeRename(codeRenameConfig, fileBean, -1, tag + j);
                            newPath = fileBean.getCopyPath() + File.separator + newName + extension;
                            newFile = new File(newPath);
                        }
                        fileBean.setName(newName);
                        fileBeanList.add(fileBean);
                    }
                    updateProgress(i + 1, sourcesSize);
                }
                return fileBeanList;
            }
        };
    }

}
