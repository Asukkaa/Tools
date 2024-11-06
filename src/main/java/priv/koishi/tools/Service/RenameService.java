package priv.koishi.tools.Service;

import priv.koishi.tools.Bean.FileBean;
import priv.koishi.tools.Configuration.CodeRenameConfig;
import priv.koishi.tools.Configuration.ExcelConfig;
import priv.koishi.tools.Configuration.StringRenameConfig;

import static priv.koishi.tools.Utils.CommonUtils.convertToAlpha;
import static priv.koishi.tools.Utils.CommonUtils.intToChineseNum;

/**
 * @author KOISHI
 * Date:2024-11-06
 * Time:下午5:47
 */
public class RenameService {

    /**
     * 构建文件重命名
     */
    public static void buildRename(CodeRenameConfig codeRenameConfig, FileBean fileBean, StringRenameConfig stringRenameConfig,
                                   ExcelConfig excelConfig, int startName, int tag) {
        String fileRename = null;
        if (codeRenameConfig != null) {
            fileRename = getCodeRename(codeRenameConfig, startName, tag);
        } else if (stringRenameConfig != null) {

        } else if (excelConfig != null) {

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

}
