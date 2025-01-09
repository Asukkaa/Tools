package priv.koishi.tools.Utils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.tools.Configuration.FileConfig;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static priv.koishi.tools.Text.CommonTexts.*;
import static priv.koishi.tools.Utils.CommonUtils.checkRunningInputStream;
import static priv.koishi.tools.Utils.CommonUtils.checkRunningOutputStream;

/**
 * 文件操作工具类
 *
 * @author KOISHI
 * Date 2024-09-30
 * Time 下午3:16
 */
public class FileUtils {

    /**
     * 获取文件类型
     *
     * @param file 文件
     * @return 文件类型
     */
    public static String getFileType(File file) {
        if (file.isDirectory()) {
            return "文件夹";
        }
        String filePath = file.getPath();
        if (filePath.lastIndexOf(".") == -1) {
            return "文件";
        }
        return filePath.substring(filePath.lastIndexOf(".")).toLowerCase();
    }

    /**
     * 根据操作系统计算文件大小
     *
     * @param file 要计算的文件
     * @return 带单位的文件大小
     */
    public static String getFileUnitSize(File file) {
        return getUnitSize(file.length(), true);
    }

    /**
     * 根据操作系统将数值转换为文件大小
     *
     * @param size          没带单位的文件大小
     * @param distinguishOS 区分操作系统文件大小单位进制，true区分，false不区分，macos文件大小为1000进制，内存大小为1024进制
     * @return 带单位的文件大小
     */
    public static String getUnitSize(long size, boolean distinguishOS) {
        long win = 1024;
        long mac = 1000;
        long kb;
        // macOS与Windows文件大小进制不同
        if (systemName.contains(macos) && distinguishOS) {
            kb = mac;
        } else {
            kb = win;
        }
        return getRet(kb, size);
    }

    /**
     * 将带单位的文件大小字符串转换为课比较类型
     *
     * @param value 带单位的文件大小
     * @return 不带单位的文件大小
     */
    public static double fileSizeCompareValue(String value) {
        if (StringUtils.isBlank(value)) {
            return 0;
        }
        String unit = value.substring(value.indexOf(" ") + 1);
        String size = value.substring(0, value.indexOf(" "));
        double compareValue;
        double win = 1024;
        double mac = 1000;
        double kb;
        // macOS与Windows文件大小进制不同
        if (systemName.contains(macos)) {
            kb = mac;
        } else {
            kb = win;
        }
        double mb = kb * kb;
        double gb = mb * kb;
        double tb = gb * kb;
        switch (unit) {
            case Byte: {
                compareValue = Double.parseDouble(size);
                break;
            }
            case KB: {
                compareValue = Double.parseDouble(size) * kb;
                break;
            }
            case MB: {
                compareValue = Double.parseDouble(size) * mb;
                break;
            }
            case GB: {
                compareValue = Double.parseDouble(size) * gb;
                break;
            }
            case TB: {
                compareValue = Double.parseDouble(size) * tb;
                break;
            }
            default: {
                compareValue = Double.parseDouble(size) * tb * kb;
            }
        }
        return compareValue;
    }

    /**
     * 格式化文件大小数据
     *
     * @param kb   文件大小单位进制
     * @param size 不带单位的文件大小
     * @return 带单位的文件大小
     */
    private static String getRet(long kb, long size) {
        long mb = kb * kb;
        long gb = mb * kb;
        long tb = gb * kb;
        String ret = "";
        DecimalFormat df = new DecimalFormat("0.00");
        if (size >= tb) {
            ret = df.format(size / (tb * 1.0)) + " " + TB;
        } else if (size >= gb) {
            ret = df.format(size / (gb * 1.0)) + " " + GB;
        } else if (size >= mb) {
            ret = df.format(size / (mb * 1.0)) + " " + MB;
        } else if (size >= kb) {
            ret = df.format(size / (kb * 1.0)) + " " + KB;
        } else if (size >= 0) {
            ret = df.format(size) + " " + Byte;
        }
        return ret;
    }

    /**
     * 读取文件夹下的文件名称
     *
     * @param fileConfig 文件查询设置
     * @return 查询到的文件列表
     */
    public static List<File> readAllFiles(FileConfig fileConfig) {
        List<File> fileList = new ArrayList<>();
        readFiles(fileConfig, fileList, fileConfig.getInFile());
        return fileList;
    }

    /**
     * 递归读取文件夹下的文件名称
     *
     * @param fileConfig 文件查询设置
     * @param fileList   上层文件夹查询的文件列表
     * @param directory  最外层文件夹
     */
    public static void readFiles(FileConfig fileConfig, List<File> fileList, File directory) {
        File[] files = directory.listFiles();
        String showHideFile = fileConfig.getShowHideFile();
        String showDirectoryName = fileConfig.getShowDirectoryName();
        boolean recursion = fileConfig.isRecursion();
        List<String> filterExtensionList = fileConfig.getFilterExtensionList();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    if ((text_noHideFile.equals(showHideFile) && file.isHidden()) || (text_onlyHideFile.equals(showHideFile) && !file.isHidden())) {
                        continue;
                    }
                    if (text_onlyFile.equals(showDirectoryName) || text_fileDirectory.equals(showDirectoryName) || StringUtils.isEmpty(showDirectoryName)) {
                        String extension = getFileType(file);
                        if (CollectionUtils.isEmpty(filterExtensionList) || filterExtensionList.contains(extension)) {
                            fileList.add(file);
                        }
                    }
                    if (recursion) {
                        readFiles(fileConfig, fileList, file);
                    }
                }
                if (file.isDirectory()) {
                    if ((text_noHideFile.equals(showHideFile) && file.isHidden()) || (text_onlyHideFile.equals(showHideFile) && !file.isHidden())) {
                        continue;
                    }
                    if (text_onlyDirectory.equals(showDirectoryName) || text_fileDirectory.equals(showDirectoryName)) {
                        fileList.add(file);
                    }
                    if (recursion) {
                        readFiles(fileConfig, fileList, file);
                    }
                }
            }
        }
    }

    /**
     * 获取文件不带拓展名的名称或文件夹的名称
     *
     * @param file 要获取文件名的文件
     * @return 文件夹或不带拓展名的文件名称
     * @throws IOException 文件不存在
     */
    public static String getFileName(File file) throws IOException {
        if (!file.exists()) {
            throw new IOException(text_fileNotExists);
        }
        String fileName = file.getName();
        if (!file.isDirectory() && fileName.contains(".")) {
            return fileName.substring(0, fileName.lastIndexOf("."));
        }
        return fileName;
    }

    /**
     * 更新配置文件
     *
     * @param properties 要更新的配置文件
     * @param key        要更新的配置名
     * @param value      要更新的值
     * @throws IOException io异常
     */
    public static void updateProperties(String properties, String key, String value) throws IOException {
        InputStream input = checkRunningInputStream(properties);
        Properties prop = new Properties();
        prop.load(input);
        prop.put(key, value);
        OutputStream output = checkRunningOutputStream(properties);
        prop.store(output, null);
        input.close();
        output.close();
    }

    /**
     * 校验输出文件夹是否存在，不存在就创建一个
     *
     * @param path 要校验的路径
     * @throws Exception 创建文件夹失败
     */
    public static void checkDirectory(String path) throws Exception {
        if (!new File(path).exists()) {
            if (!new File(path).mkdirs()) {
                throw new Exception("创建文件夹 " + path + " 失败");
            }
        }
    }

    /**
     * 打开文件
     *
     * @param openPath 要打开的路径
     * @throws IOException 文件不存在
     */
    public static void openFile(String openPath) throws IOException {
        if (StringUtils.isNotEmpty(openPath)) {
            File file = new File(openPath);
            if (!file.exists()) {
                throw new IOException(text_fileNotExists);
            }
            Desktop.getDesktop().open(file);
        }
    }

    /**
     * 打开文件夹并选中文件
     *
     * @param openPath 要打开的路径
     * @throws IOException 文件不存在
     */
    public static void openDirectory(String openPath) throws IOException {
        if (StringUtils.isNotEmpty(openPath)) {
            File file = new File(openPath);
            if (!file.exists()) {
                throw new IOException(text_fileNotExists);
            }
            if (file.isDirectory()) {
                Desktop.getDesktop().open(file);
            }
            if (file.isFile()) {
                ProcessBuilder processBuilder;
                if (systemName.contains(win)) {
                    processBuilder = new ProcessBuilder("cmd.exe", "/C", "explorer /select, " + openPath);
                } else {
                    processBuilder = new ProcessBuilder("bash", "-c", "open -R " + "'" + openPath + "'");
                }
                processBuilder.start();
            }
        }
    }

    /**
     * 获取文件创建时间
     *
     * @param file 要读取的文件
     * @return 格式化后的时间字符串
     * @throws IOException io异常
     */
    public static String getFileCreatTime(File file) throws IOException {
        Path path = Paths.get(file.getPath());
        BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
        Date creationTime = new Date(attr.creationTime().toMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(creationTime);
    }

    /**
     * 获取文件修改时间
     *
     * @param file 要读取的文件
     * @return 格式化后的时间字符串
     */
    public static String getFileUpdateTime(File file) {
        long lastModified = file.lastModified();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(lastModified));
    }

    /**
     * 验证文件名是否符合Windows和UNIX文件系统的命名规则。
     *
     * @param fileName 待验证的文件名
     * @return 如果文件名有效，返回true；否则返回false。
     */
    public static boolean isValidFileName(String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            return false;
        }
        // 文件名中不能包含以下字符
        String illegalChars = "<>:\"/\\|?*";
        if (fileName.contains("//") || fileName.contains("\\\\")) {
            return false;
        }
        // 检查文件名是否包含非法字符
        for (int i = 0; i < fileName.length(); i++) {
            if (illegalChars.indexOf(fileName.charAt(i)) >= 0) {
                return false;
            }
        }
        // 检查是否以空格或特殊字符开头
        char firstChar = fileName.charAt(0);
        return !Character.isSpaceChar(firstChar) && illegalChars.indexOf(firstChar) < 0;
    }

    /**
     * 判断字符串是否为文件路径
     *
     * @param path 要校验的文件路径
     * @return 如果是文件路径返回true，否则返回false
     */
    public static boolean isValidPath(String path) {
        try {
            Paths.get(path);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 校验文件是否存在
     *
     * @param filePath 要校验的非文件夹文件路径
     * @param errTex   校验不通过时提示文案
     * @throws IOException 文件校验不通过
     */
    public static void checkFileExists(String filePath, String errTex) throws IOException {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new IOException(errTex);
        }
    }

}
