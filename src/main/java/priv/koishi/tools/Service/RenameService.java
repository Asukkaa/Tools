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

import static priv.koishi.tools.Utils.CommonUtils.*;

/**
 * @author KOISHI
 * Date:2024-11-06
 * Time:下午5:47
 */
public class RenameService {

    public static Task<String> fileRename(TaskBean<FileBean> taskBean) {
        return new Task<>() {
            @Override
            protected String call() throws Exception {
                //防止命名重复先将所有慰文件重命名为uuid生成的临时名称
                updateMessage("正在将文件重命名为临时名称");
                List<FileBean> fileBeanList = taskBean.getBeanList();
                int fileBeanListSize = fileBeanList.size();
                for (int i = 0; i < fileBeanListSize; i++) {
                    FileBean fileBean = fileBeanList.get(i);
                    String oldName = fileBean.getName();
                    String ext = fileBean.getFileType();
                    if ("文件夹".equals(ext) || "文件".equals(ext)) {
                        ext = "";
                    }
                    String tempName = UUID.randomUUID() + ext;
                    File tempFile = tempRename(fileBean, tempName);
                    fileBean.setTempFile(tempFile);
                    updateMessage("已修改编号为 " + fileBean.getId() + " 的文件 " + oldName + " 临时名称为 " + tempName);
                    updateProgress(i, fileBeanListSize);
                }
                //将重命名为临时名称的文件重命名为正式名称
                updateMessage("正在将文件重命名为正式名称");
                updateProgress(0, fileBeanListSize);
                for (int i = 0; i < fileBeanListSize; i++) {
                    FileBean fileBean = taskBean.getBeanList().get(i);
                    File tempFile = fileBean.getTempFile();
                    String oldName = fileBean.getName();
                    String ext = fileBean.getFileType();
                    if ("文件夹".equals(ext) || "文件".equals(ext)) {
                        ext = "";
                    }
                    String newName = fileBean.getRename() + ext;
                    File newFile = new File(tempFile.getParent(), newName);
                    if (!tempFile.renameTo(newFile)) {
                        throw new Exception("修改编号为 " + fileBean.getId() + " 的文件名称失败");
                    }
                    updateMessage("已修改编号为 " + fileBean.getId() + " 的文件 " + oldName + " 临时名称为 " + newName);
                    updateProgress(i, fileBeanListSize);
                }
                updateMessage("所有文件已重命名完毕");
                return new File(fileBeanList.getFirst().getPath()).getParent();
            }

            //给文件一个临时重命名
            private static File tempRename(FileBean fileBean, String tempName) throws Exception {
                String oldPath = fileBean.getPath();
                File oldFile = new File(oldPath);
                if (!oldFile.exists()) {
                    throw new Exception("编号为 " + fileBean.getId() + " 的文件不存在，列表中的地址为 " + oldPath);
                }
                File tempFile = new File(oldFile.getParent(), tempName);
                if (!oldFile.renameTo(tempFile)) {
                    throw new Exception("修改编号为 " + fileBean.getId() + " 的文件临时名称失败");
                }
                return tempFile;
            }
        };
    }

    /**
     * 构建文件重命名
     */
    public static void buildRename(CodeRenameConfig codeRenameConfig, FileBean fileBean,
                                   StringRenameConfig stringRenameConfig, int startName, int tag) {
        String fileRename = fileBean.getName();
        if (codeRenameConfig != null) {
            fileRename = getCodeRename(codeRenameConfig, startName, tag);
        } else if (stringRenameConfig != null) {
            fileRename = getStringRename(stringRenameConfig, fileBean);
        }
        fileBean.setRename(fileRename);
    }

    /**
     * 根据按编号规则重命名
     */
    private static String getCodeRename(CodeRenameConfig codeRenameConfig, int startName, int tag) {
        String differenceCode = codeRenameConfig.getDifferenceCode();
        int startSize = codeRenameConfig.getStartSize();
        String paddedNum = String.valueOf(startName);
        // 使用String.format()函数进行补齐操作
        if (startSize > 0) {
            paddedNum = String.format("%0" + startSize + "d", startName);
        }
        String fileRename = paddedNum;
        switch (differenceCode) {
            case "阿拉伯数字：123": {
                fileRename = getSubCodeRename(codeRenameConfig, String.valueOf(tag), fileRename);
                break;
            }
            case "中文数字：一二三": {
                fileRename = getSubCodeRename(codeRenameConfig, intToChineseNum(tag), fileRename);
                break;
            }
            case "小写英文字母：abc": {
                fileRename = getSubCodeRename(codeRenameConfig, convertToAlpha(tag, true), fileRename);
                break;
            }
            case "大小英文字母：ABC": {
                fileRename = getSubCodeRename(codeRenameConfig, convertToAlpha(tag, false), fileRename);
                break;
            }
        }
        return fileRename;
    }

    /**
     * 获取带有分隔符的文件名
     */
    private static String getSubCodeRename(CodeRenameConfig codeRenameConfig, String tag, String fileRename) {
        //只有同名文件超过0个才需要分隔符
        if (codeRenameConfig.getNameNum() > 0) {
            String subCode = codeRenameConfig.getSubCode();
            String space = "";
            if (codeRenameConfig.isAddSpace()) {
                space = " ";
            }
            switch (subCode.substring(0, 4)) {
                case "英文括号": {
                    fileRename += space + "(" + tag + ")";
                    break;
                }
                case "中文括号": {
                    fileRename += space + "（" + tag + "）";
                    break;
                }
                case "英文横杠": {
                    fileRename += space + "-" + tag;
                    break;
                }
                case "中文横杠": {
                    fileRename += space + "—" + tag;
                    break;
                }
            }
        }
        return fileRename;
    }

    /**
     * 按指定字符重命名
     */
    private static String getStringRename(StringRenameConfig stringRenameConfig, FileBean fileBean) {
        String targetStr = stringRenameConfig.getTargetStr();
        String fileName = fileBean.getName();
        String fileRename = fileName;
        switch (targetStr) {
            case "指定字符串": {
                fileRename = getTargetStringRename(fileName, stringRenameConfig);
                break;
            }
            case "指定字符位置": {
                fileRename = getTargetIndexRename(fileName, stringRenameConfig);
                break;
            }
            case "全部英文字符转为大写": {
                fileRename = fileName.toUpperCase();
                break;
            }
            case "全部英文字符转为小写": {
                fileRename = fileName.toLowerCase();
                break;
            }
            case "全部英文字符大小写互换": {
                fileRename = swapCase(fileName);
                break;
            }
        }
        return fileRename;
    }

    /**
     * 根据指定字符位置重命名
     */
    private static String getTargetIndexRename(String fileName, StringRenameConfig stringRenameConfig) {
        String renameValue = stringRenameConfig.getRenameValue();
        if (StringUtils.isNotBlank(renameValue)) {
            int renameValueInt = Integer.parseInt(stringRenameConfig.getRenameValue());
            String renameBehavior = stringRenameConfig.getRenameBehavior();
            if (renameValueInt >= 0 && renameValueInt < fileName.length()) {
                switch (renameBehavior) {
                    case "替换所有字符为:": {
                        String renameStr = stringRenameConfig.getRenameStr();
                        StringBuilder sb = new StringBuilder(fileName);
                        sb.deleteCharAt(renameValueInt);
                        sb.insert(renameValueInt, renameStr);
                        fileName = sb.toString();
                        break;
                    }
                    case "移除指定字符": {
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
     */
    private static String getTargetStringRename(String fileName, StringRenameConfig stringRenameConfig) {
        String renameValue = stringRenameConfig.getRenameValue();
        if (StringUtils.isNotEmpty(renameValue)) {
            String renameBehavior = stringRenameConfig.getRenameBehavior();
            switch (renameBehavior) {
                case "替换所有字符为:": {
                    String renameStr = stringRenameConfig.getRenameStr();
                    fileName = replaceString(fileName, renameValue, renameStr);
                    break;
                }
                case "移除指定字符": {
                    fileName = replaceString(fileName, renameValue, "");
                    break;
                }
                case "处理两侧字符": {
                    fileName = bothSidesRename(fileName, stringRenameConfig);
                    break;
                }
            }
        }
        return fileName;
    }

    /**
     * 处理两侧字符
     */
    private static String bothSidesRename(String fileName, StringRenameConfig stringRenameConfig) {
        String renameValue = stringRenameConfig.getRenameValue();
        if (!fileName.contains(renameValue)) {
            return fileName;
        }
        //匹配最后一次相同字符串
        int lastIndexOf = fileName.lastIndexOf(renameValue);
        //组装左侧重命名
        String leftName = fileName.substring(0, lastIndexOf);
        int leftSize = leftName.length();
        int left = Math.min(stringRenameConfig.getLeft(), leftSize);
        String leftRename = leftName.substring(0, leftSize - left);
        String leftBehavior = stringRenameConfig.getLeftBehavior();
        String renameLeft = getOneSidesRename(leftRename, leftBehavior, true, stringRenameConfig.getLeftValue());
        //组装右侧重命名
        String rightName = fileName.substring(lastIndexOf + renameValue.length());
        int rightSize = rightName.length();
        int right = Math.min(stringRenameConfig.getRight(), rightSize);
        String rightRename = rightName.substring(right);
        String rightBehavior = stringRenameConfig.getRightBehavior();
        String renameRight = getOneSidesRename(rightRename, rightBehavior, false, stringRenameConfig.getRightValue());
        //中间字符串
        String middleName = leftName.substring(leftSize - left) + renameValue + rightName.substring(0, right);
        return renameLeft + middleName + renameRight;
    }

    /**
     * 处理单侧字符
     */
    private static String getOneSidesRename(String fileName, String behavior, boolean isLeft, String replaceString) {
        switch (behavior) {
            case "插入字符串为:": {
                fileName = isLeft ? (fileName + replaceString) : (replaceString + fileName);
                break;
            }
            case "替换所有字符串为:": {
                fileName = replaceString;
                break;
            }
            case "删除指定位置字符": {
                if (!fileName.isEmpty()) {
                    fileName = isLeft ? fileName.substring(0, fileName.length() - 1) : fileName.substring(1);
                }
                break;
            }
            case "移除所有字符": {
                fileName = "";
                break;
            }
            case "全部英文字符转为大写": {
                fileName = fileName.toUpperCase();
                break;
            }
            case "全部英文字符转为小写": {
                fileName = fileName.toLowerCase();
                break;
            }
            case "全部英文字符大小写互换": {
                fileName = swapCase(fileName);
                break;
            }
        }
        return fileName;
    }

}
