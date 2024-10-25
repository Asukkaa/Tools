package priv.koishi.tools.Utils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import priv.koishi.tools.Bean.ExcelConfigBean;
import priv.koishi.tools.Bean.FileConfigBean;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static priv.koishi.tools.Utils.CommonUtils.checkRunningInputStream;
import static priv.koishi.tools.Utils.CommonUtils.checkRunningOutputStream;

/**
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
     * 获取文件夹路径
     *
     * @param file 文件
     * @return 文件夹路径
     */
    public static String getFileMkdir(File file) {
        String filePath = file.getPath();
        return filePath.substring(0, filePath.lastIndexOf("\\"));
    }

    /**
     * 根据操作系统计算文件大小
     *
     * @return ret 带单位的文件大小
     */
    public static String getFileSize(File file) {
        long size = file.length();
        String sys = System.getProperty("os.name");
        long win = 1024;
        long mac = 1000;
        long kb;
        //macOS与Windows文件大小进制不同
        if ("Mac OS X".equals(sys) || "Mac OS".equals(sys)) {
            kb = mac;
        } else {
            kb = win;
        }
        long mb = kb * kb;
        return getRet(mb, kb, size);
    }

    /**
     * 将带单位的文件大小字符串转换为课比较类型
     */
    public static double fileSizeCompareValue(String value) {
        String unit = value.substring(value.indexOf(" ") + 1);
        String size = value.substring(0, value.indexOf(" "));
        double compareValue;
        double win = 1024;
        double mac = 1000;
        double kb;
        String sys = System.getProperty("os.name");
        //macOS与Windows文件大小进制不同
        if ("Mac OS X".equals(sys) || "Mac OS".equals(sys)) {
            kb = mac;
        } else {
            kb = win;
        }
        double mb = kb * kb;
        double gb = mb * kb;
        double tb = gb * kb;
        switch (unit) {
            case "Byte": {
                compareValue = Double.parseDouble(size);
                break;
            }
            case "KB": {
                compareValue = Double.parseDouble(size) * kb;
                break;
            }
            case "MB": {
                compareValue = Double.parseDouble(size) * mb;
                break;
            }
            case "GB": {
                compareValue = Double.parseDouble(size) * gb;
                break;
            }
            case "TB": {
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
     */
    private static String getRet(long mb, long kb, long size) {
        long gb = mb * kb;
        long tb = gb * kb;
        String ret = "";
        DecimalFormat df = new DecimalFormat("0.00");
        if (size >= tb) {
            ret = df.format(size / (tb * 1.0)) + " TB";
        } else if (size >= gb) {
            ret = df.format(size / (gb * 1.0)) + " GB";
        } else if (size >= mb) {
            ret = df.format(size / (mb * 1.0)) + " MB";
        } else if (size >= kb) {
            ret = df.format(size / (kb * 1.0)) + " KB";
        } else if (size >= 0) {
            ret = df.format(size) + " Byte";
        }
        return ret;
    }

    /**
     * 读取文件夹下的文件名称
     */
    public static List<File> readAllFiles(FileConfigBean fileConfigBean) {
        List<File> fileList = new ArrayList<>();
        readFiles(fileConfigBean, fileList);
        return fileList;
    }

    /**
     * 递归读取文件夹下的文件名称
     */
    public static void readFiles(FileConfigBean fileConfigBean, List<File> fileList) {
        File[] files = fileConfigBean.getInFile().listFiles();
        String showHideFile = fileConfigBean.getShowHideFile();
        String showDirectoryName = fileConfigBean.getShowDirectoryName();
        boolean recursion = fileConfigBean.isRecursion();
        List<String> filterExtensionList = fileConfigBean.getFilterExtensionList();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    if (("不查询隐藏文件".equals(showHideFile) && file.isHidden()) || ("只查询隐藏文件".equals(showHideFile) && !file.isHidden())) {
                        continue;
                    }
                    if ("只查询文件".equals(showDirectoryName) || "文件和文件夹都查询".equals(showDirectoryName) || StringUtils.isEmpty(showDirectoryName)) {
                        String extension = getFileType(file);
                        if (CollectionUtils.isEmpty(filterExtensionList) || filterExtensionList.contains(extension)) {
                            fileList.add(file);
                        }
                    }
                    if (recursion) {
                        readFiles(fileConfigBean, fileList);
                    }
                } else if (file.isDirectory()) {
                    if (("不查询隐藏文件".equals(showHideFile) && file.isHidden()) || ("只查询隐藏文件".equals(showHideFile) && !file.isHidden())) {
                        continue;
                    }
                    if ("只查询文件夹".equals(showDirectoryName) || "文件和文件夹都查询".equals(showDirectoryName)) {
                        fileList.add(file);
                    }
                    if (recursion) {
                        readFiles(fileConfigBean, fileList);
                    }
                }
            }
        }
    }

    /**
     * 获取文件不带拓展名的名称或文件夹的名称
     */
    public static String getFileName(File file) throws IOException {
        if (!file.exists()) {
            throw new IOException("文件不存在");
        }
        String fileName = file.getName();
        if (!file.isDirectory() && fileName.contains(".")) {
            return fileName.substring(0, fileName.lastIndexOf("."));
        }
        return fileName;
    }

    /**
     * 更新设置文件中的文件路径设置
     */
    public static void updatePath(String properties, String key, String value) throws IOException {
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
     */
    public static void checkDirectory(String path) throws Exception {
        if (!new File(path).exists()) {
            if (!new File(path).mkdirs()) {
                throw new Exception("创建文件夹 " + path + " 失败");
            }
        }
    }

    /**
     * 保存excel
     */
    public static String saveExcel(XSSFWorkbook workbook, ExcelConfigBean excelConfigBean) throws Exception {
        String filePath = excelConfigBean.getOutPath() + "\\" + excelConfigBean.getOutName() + excelConfigBean.getOutExcelExtension();
        checkDirectory(getFileMkdir(new File(filePath)));
        // 将Excel写入文件
        try {
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(Files.newOutputStream(Paths.get(filePath)));
            SXSSFWorkbook sxssfWorkbook = new SXSSFWorkbook(workbook, 2);
            sxssfWorkbook.write(bufferedOutputStream);
            bufferedOutputStream.flush();
            bufferedOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        workbook.close();
        return filePath;
    }

    /**
     * 打开文件
     */
    public static void openFile(String openPath) throws IOException {
        if (StringUtils.isNotEmpty(openPath)) {
            Desktop desktop = Desktop.getDesktop();
            desktop.open(new File(openPath));
        }
    }

    /**
     * 获取文件创建时间
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
     */
    public static String getFileUpdateTime(File file) {
        long lastModified = file.lastModified();
        Date date = new Date(lastModified);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
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
     * 校验excel输出路径是否与模板一致，若不一致则复制一份模板文件到输出路径
     */
    public static void checkCopyDestination(ExcelConfigBean excelConfigBean) throws Exception {
        String excelInPath = excelConfigBean.getInPath();
        String outPath = excelConfigBean.getOutPath();
        String excelName = excelConfigBean.getOutName();
        String outExcelExtension = excelConfigBean.getOutExcelExtension();
        Path sourcePath = Paths.get(excelInPath);
        checkDirectory(outPath);
        String path = outPath + "\\" + excelName + outExcelExtension;
        Path destinationPath = Paths.get(path);
        if (!excelInPath.equals(path)) {
            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            File destinationPathFile = new File(String.valueOf(destinationPath));
            if (!destinationPathFile.canWrite()) {
                if (!destinationPathFile.setWritable(true)) {
                    throw new Exception("文件 " + destinationPath + " 设置为可写失败");
                }
            }
        }
    }

}
