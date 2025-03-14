package priv.koishi.tools.Service;

import javafx.concurrent.Task;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.tools.Bean.FileBean;
import priv.koishi.tools.Bean.TaskBean;
import priv.koishi.tools.Configuration.CodeRenameConfig;
import priv.koishi.tools.Configuration.StringRenameConfig;

import java.io.File;
import java.util.List;
import java.util.UUID;

import static priv.koishi.tools.Finals.CommonFinals.*;
import static priv.koishi.tools.Utils.CommonUtils.*;
import static priv.koishi.tools.Utils.UiUtils.changeDisableNodes;

/**
 * 文件批量重命名线程任务类
 *
 * @author KOISHI
 * Date:2024-11-06
 * Time:下午5:47
 */
public class FileRenameService {

    /**
     * 批量重命名
     *
     * @param taskBean 线程任务参数
     * @return 处理的文件所在文件夹
     */
    public static Task<String> fileRename(TaskBean<FileBean> taskBean) {
        return new Task<>() {
            @Override
            protected String call() throws Exception {
                // 改变要防重复点击的组件状态
                changeDisableNodes(taskBean, true);
                // 防止命名重复先将所有慰文件重命名为uuid生成的临时名称
                updateMessage("正在将文件重命名为临时名称");
                List<FileBean> fileBeanList = taskBean.getBeanList();
                int fileBeanListSize = fileBeanList.size();
                for (int i = 0; i < fileBeanListSize; i++) {
                    FileBean fileBean = fileBeanList.get(i);
                    String ext = fileBean.getFileType();
                    if ("文件夹".equals(ext) || "文件".equals(ext)) {
                        ext = "";
                    }
                    String tempName = UUID.randomUUID() + ext;
                    fileBean.setTempFile(tempRename(fileBean, tempName));
                    updateMessage("已修改编号为 " + fileBean.getId() + " 的文件 " + fileBean.getName() + " 临时名称为 " + tempName);
                    updateProgress(i, fileBeanListSize);
                }
                // 将重命名为临时名称的文件重命名为正式名称
                updateMessage("正在将文件重命名为正式名称");
                updateProgress(0, fileBeanListSize);
                for (int i = 0; i < fileBeanListSize; i++) {
                    FileBean fileBean = taskBean.getBeanList().get(i);
                    File tempFile = fileBean.getTempFile();
                    String ext = fileBean.getFileType();
                    if ("文件夹".equals(ext) || "文件".equals(ext)) {
                        ext = "";
                    }
                    String newName = fileBean.getRename() + ext;
                    File newFile = new File(tempFile.getParent(), newName);
                    if (!tempFile.renameTo(newFile)) {
                        throw new Exception("修改编号为 " + fileBean.getId() + " 的文件名称失败");
                    }
                    updateMessage("已修改编号为 " + fileBean.getId() + " 的文件 " + fileBean.getName() + " 临时名称为 " + newName);
                    updateProgress(i, fileBeanListSize);
                }
                updateMessage("所有文件已重命名完毕");
                return new File(fileBeanList.getFirst().getPath()).getParent();
            }

            // 给文件一个临时重命名
            private static File tempRename(FileBean fileBean, String tempName) throws Exception {
                String oldPath = fileBean.getPath();
                File oldFile = new File(oldPath);
                if (!oldFile.exists()) {
                    throw new Exception("编号为 " + fileBean.getId() + " 的文件 " + fileBean.getFullName() + " 不存在，列表中的地址为 " + oldPath);
                }
                File tempFile = new File(oldFile.getParent(), tempName);
                if (!oldFile.renameTo(tempFile)) {
                    throw new Exception("修改编号为 " + fileBean.getId() + " 的文件临时名称 " + fileBean.getTempFile().getName() + " 失败");
                }
                return tempFile;
            }
        };
    }

    /**
     * 构建文件重命名
     *
     * @param codeRenameConfig   按变编号重命名设置参数
     * @param fileBean           要处理的文件信息
     * @param stringRenameConfig 按指定字符重命名设置参数
     * @param startName          按变编号重命名起始编号
     * @param tag                按变编号重命名后缀
     */
    public static void buildRename(CodeRenameConfig codeRenameConfig, FileBean fileBean, StringRenameConfig stringRenameConfig, int startName, int tag) {
        String fileRename = fileBean.getName();
        if (codeRenameConfig != null) {
            fileRename = getCodeRename(codeRenameConfig, fileBean, startName, tag);
            fileBean.setCodeRenameConfig(codeRenameConfig);
        } else if (stringRenameConfig != null) {
            fileRename = getStringRename(stringRenameConfig, fileBean);
        }
        fileBean.setRename(fileRename);
    }

    /**
     * 根据按编号规则重命名
     *
     * @param codeRenameConfig 按变编号重命名设置参数
     * @param fileBean         要重命名的文件信息
     * @param startName        按变编号重命名起始编号
     * @param tag              按变编号重命名后缀
     * @return 重命名后不带后缀的文件名
     */
    public static String getCodeRename(CodeRenameConfig codeRenameConfig, FileBean fileBean, int startName, int tag) {
        String differenceCode = codeRenameConfig.getDifferenceCode();
        String fileRename = String.valueOf(startName);
        fileBean.setTagRenameCode(tag);
        fileBean.setCodeRename(fileRename);
        switch (differenceCode) {
            case text_arabicNumerals: {
                fileRename = getSubCodeRename(codeRenameConfig, fileBean, String.valueOf(tag));
                break;
            }
            case text_chineseNumerals: {
                fileRename = getSubCodeRename(codeRenameConfig, fileBean, intToChineseNum(tag));
                break;
            }
            case text_abc: {
                fileRename = getSubCodeRename(codeRenameConfig, fileBean, convertToAlpha(tag, true));
                break;
            }
            case text_ABC: {
                fileRename = getSubCodeRename(codeRenameConfig, fileBean, convertToAlpha(tag, false));
                break;
            }
        }
        return fileRename;
    }

    /**
     * 获取带有分隔符的文件名
     *
     * @param codeRenameConfig 按变编号重命名设置参数
     * @param fileBean         要重命名的文件信息
     * @param tag              按变编号重命名后缀
     * @return 带后缀的重命名文件名
     */
    private static String getSubCodeRename(CodeRenameConfig codeRenameConfig, FileBean fileBean, String tag) {
        int startSize = codeRenameConfig.getStartSize();
        String fileRename = fileBean.getCodeRename();
        // 使用String.format()函数进行补齐操作
        if (startSize > 0) {
            fileRename = String.format("%0" + startSize + "d", Integer.parseInt(fileRename));
        }
        fileBean.setTagRename("");
        // 只有同名文件超过0个才需要分隔符
        if (codeRenameConfig.getNameNum() > 0) {
            String subCode = codeRenameConfig.getSubCode();
            String space = "";
            if (codeRenameConfig.isAddSpace()) {
                space = " ";
            }
            String tagRename;
            switch (subCode.substring(0, 4)) {
                case text_enBracket: {
                    tagRename = space + "(" + tag + ")";
                    fileBean.setTagRename(tagRename);
                    fileRename += tagRename;
                    break;
                }
                case text_cnBracket: {
                    tagRename = space + "（" + tag + "）";
                    fileBean.setTagRename(tagRename);
                    fileRename += space + "（" + tag + "）";
                    break;
                }
                case text_enHorizontal: {
                    tagRename = space + "-" + tag;
                    fileBean.setTagRename(tagRename);
                    fileRename += space + "-" + tag;
                    break;
                }
                case text_cnHorizontal: {
                    tagRename = space + "—" + tag;
                    fileBean.setTagRename(tagRename);
                    fileRename += space + "—" + tag;
                    break;
                }
            }
        }
        return fileRename;
    }

    /**
     * 按指定字符重命名
     *
     * @param stringRenameConfig 按指定字符重命名设置参数
     * @param fileBean           文要处理的件信息
     * @return 重命名后的文件名
     */
    private static String getStringRename(StringRenameConfig stringRenameConfig, FileBean fileBean) {
        String targetStr = stringRenameConfig.getTargetStr();
        String fileName = fileBean.getName();
        String fileRename = fileName;
        switch (targetStr) {
            case text_specifyString: {
                fileRename = getTargetStringRename(fileName, stringRenameConfig);
                break;
            }
            case text_specifyIndex: {
                fileRename = getTargetIndexRename(fileName, stringRenameConfig);
                break;
            }
            case text_toUpperCase: {
                fileRename = fileName.toUpperCase();
                break;
            }
            case text_toLowerCase: {
                fileRename = fileName.toLowerCase();
                break;
            }
            case text_swapCase: {
                fileRename = swapCase(fileName);
                break;
            }
        }
        return fileRename;
    }

    /**
     * 根据指定字符位置重命名
     *
     * @param fileName           要处理的文件名
     * @param stringRenameConfig 按指定字符重命名设置参数
     * @return 重命名后的文件名
     */
    private static String getTargetIndexRename(String fileName, StringRenameConfig stringRenameConfig) {
        String renameValue = stringRenameConfig.getRenameValue();
        if (StringUtils.isNotBlank(renameValue)) {
            int renameValueInt = Integer.parseInt(stringRenameConfig.getRenameValue());
            String renameBehavior = stringRenameConfig.getRenameBehavior();
            if (renameValueInt >= 0 && renameValueInt < fileName.length()) {
                switch (renameBehavior) {
                    case text_replace: {
                        String renameStr = stringRenameConfig.getRenameStr();
                        StringBuilder sb = new StringBuilder(fileName);
                        sb.deleteCharAt(renameValueInt);
                        sb.insert(renameValueInt, renameStr);
                        fileName = sb.toString();
                        break;
                    }
                    case text_remove: {
                        StringBuilder sb = new StringBuilder(fileName);
                        sb.deleteCharAt(renameValueInt);
                        fileName = sb.toString();
                        break;
                    }
                }
            }
        }
        return fileName;
    }

    /**
     * 根据指定字符串重命名
     *
     * @param fileName           要处理的文件名
     * @param stringRenameConfig 按指定字符重命名设置参数
     * @return 重命名后的文件名
     */
    private static String getTargetStringRename(String fileName, StringRenameConfig stringRenameConfig) {
        String renameValue = stringRenameConfig.getRenameValue();
        if (StringUtils.isNotEmpty(renameValue)) {
            String renameBehavior = stringRenameConfig.getRenameBehavior();
            switch (renameBehavior) {
                case text_replace: {
                    String renameStr = stringRenameConfig.getRenameStr();
                    fileName = replaceString(fileName, renameValue, renameStr);
                    break;
                }
                case text_remove: {
                    fileName = replaceString(fileName, renameValue, "");
                    break;
                }
                case text_bothSides: {
                    fileName = bothSidesRename(fileName, stringRenameConfig);
                    break;
                }
            }
        }
        return fileName;
    }

    /**
     * 处理两侧字符
     *
     * @param fileName           要处理的文件名
     * @param stringRenameConfig 按指定字符重命名设置参数
     * @return 重命名后的文件名
     */
    private static String bothSidesRename(String fileName, StringRenameConfig stringRenameConfig) {
        String renameValue = stringRenameConfig.getRenameValue();
        if (!fileName.contains(renameValue)) {
            return fileName;
        }
        // 匹配最后一次相同字符串
        int lastIndexOf = fileName.lastIndexOf(renameValue);
        // 组装左侧重命名
        String leftName = fileName.substring(0, lastIndexOf);
        int leftSize = leftName.length();
        int left = Math.min(stringRenameConfig.getLeft(), leftSize);
        String leftRename = leftName.substring(0, leftSize - left);
        String leftBehavior = stringRenameConfig.getLeftBehavior();
        String renameLeft = getOneSideRename(leftRename, leftBehavior, true, stringRenameConfig.getLeftValue());
        // 组装右侧重命名
        String rightName = fileName.substring(lastIndexOf + renameValue.length());
        int rightSize = rightName.length();
        int right = Math.min(stringRenameConfig.getRight(), rightSize);
        String rightRename = rightName.substring(right);
        String rightBehavior = stringRenameConfig.getRightBehavior();
        String renameRight = getOneSideRename(rightRename, rightBehavior, false, stringRenameConfig.getRightValue());
        // 中间字符串
        String middleName = leftName.substring(leftSize - left) + renameValue + rightName.substring(0, right);
        return renameLeft + middleName + renameRight;
    }

    /**
     * 处理单侧字符
     *
     * @param fileName      要处理的单侧文件名
     * @param behavior      重命名行为
     * @param isLeft        左侧标识，true为左侧文件名，false为右侧文件名
     * @param replaceString 用于替换文件名的字符串
     * @return 重命名后的文件名
     */
    private static String getOneSideRename(String fileName, String behavior, boolean isLeft, String replaceString) {
        switch (behavior) {
            case text_insert: {
                fileName = isLeft ? (fileName + replaceString) : (replaceString + fileName);
                break;
            }
            case text_replace: {
                fileName = replaceString;
                break;
            }
            case text_delete: {
                if (!fileName.isEmpty()) {
                    fileName = isLeft ? fileName.substring(0, fileName.length() - 1) : fileName.substring(1);
                }
                break;
            }
            case text_removeAll: {
                fileName = "";
                break;
            }
            case text_toUpperCase: {
                fileName = fileName.toUpperCase();
                break;
            }
            case text_toLowerCase: {
                fileName = fileName.toLowerCase();
                break;
            }
            case text_swapCase: {
                fileName = swapCase(fileName);
                break;
            }
        }
        return fileName;
    }

}
