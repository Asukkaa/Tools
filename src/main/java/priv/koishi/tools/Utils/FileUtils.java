package priv.koishi.tools.Utils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.tools.Configuration.FileConfig;
import priv.koishi.tools.Finals.CommonFinals;

import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import static priv.koishi.tools.Finals.CommonFinals.*;

/**
 * 文件操作工具类
 *
 * @author KOISHI
 * Date 2024-09-30
 * Time 下午3:16
 */
public class FileUtils {

    /**
     * 资源文件夹地址前缀
     */
    static String resourcesPath = "src/main/resources/priv/koishi/tools/";

    /**
     * 获取文件类型
     *
     * @param file 文件
     * @return 文件类型
     */
    public static String getFileType(File file) {
        if (file.isDirectory()) {
            return extension_folder;
        }
        String filePath = file.getPath();
        if (filePath.lastIndexOf(".") == -1) {
            return extension_file;
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
        if (systemName.contains(CommonFinals.mac) && distinguishOS) {
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
        if (systemName.contains(CommonFinals.mac)) {
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
    public static void readFiles(FileConfig fileConfig, List<? super File> fileList, File directory) {
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
                openParentDirectory(openPath);
            }
        }
    }

    /**
     * 打开上级目录并选中目标文件
     *
     * @param openPath 目标文件的路径
     * @throws IOException 文件不存在
     */
    public static void openParentDirectory(String openPath) throws IOException {
        if (StringUtils.isNotEmpty(openPath)) {
            File file = new File(openPath);
            if (!file.exists()) {
                throw new IOException(text_fileNotExists);
            }
            ProcessBuilder processBuilder;
            if (systemName.contains(win)) {
                processBuilder = new ProcessBuilder("cmd.exe", "/C", "explorer /select, " + openPath);
            } else {
                processBuilder = new ProcessBuilder("bash", "-c", "open -R " + "'" + openPath + "'");
            }
            processBuilder.start();
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

    /**
     * 判断程序是否打包运行
     *
     * @return 在jar环境运为true，其他环境为false
     */
    public static boolean isRunningFromJar() {
        // 获取当前运行的JVM的类加载器
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        // 获取URL资源
        URL resource = classLoader.getResource("");
        // 检查URL的协议是否是jar或者file协议，file协议表示不是从JAR中加载
        String protocol = null;
        if (resource != null) {
            protocol = resource.getProtocol();
        }
        return "jar".equals(protocol);
    }

    /**
     * 根据不同运行环境来创建输入流
     *
     * @param path 输入流路径
     * @return 根据不同运行环境创建的输入流
     * @throws IOException io异常
     */
    public static InputStream checkRunningInputStream(String path) throws IOException {
        InputStream input;
        if (isRunningFromJar()) {
            input = new FileInputStream(resourcesPath + path);
        } else {
            input = new FileInputStream(getAppResourcePath(path));
        }
        return input;
    }

    /**
     * 根据不同运行环境来创建输出流
     *
     * @param path 输出流路径
     * @return 根据不同运行环境创建的输出流
     * @throws IOException io异常
     */
    public static OutputStream checkRunningOutputStream(String path) throws IOException {
        OutputStream output;
        if (isRunningFromJar()) {
            output = new FileOutputStream(resourcesPath + path);
        } else {
            output = new FileOutputStream(getAppResourcePath(path));
        }
        return output;
    }

    /**
     * 文件重名不覆盖
     *
     * @param path 要判断的文件路径
     * @return 不会重名文件路径
     * @throws IOException 路径不能为空
     */
    public static String notOverwritePath(String path) throws IOException {
        if (StringUtils.isBlank(path)) {
            throw new IOException("路径不能为空");
        }
        File file = new File(path);
        if (!file.exists()) {
            return path;
        }
        String parentDir = file.getParent();
        String fileName = getFileName(file);
        String extension = getFileType(file);
        if (extension_file.equals(extension) || extension_folder.equals(extension)) {
            extension = "";
        }
        // 递归添加尾缀
        int counter = 1;
        while (file.exists()) {
            path = parentDir + File.separator + fileName + "-" + counter + extension;
            file = new File(path);
            counter++;
        }
        return path;
    }

    /**
     * 递归寻找存在的文件或上级文件
     *
     * @param path 要寻找的文件路径
     * @return 存在的文件或上级文件
     */
    public static File getExistsFile(String path) {
        File defaultFile = new File(defaultFileChooserPath);
        // 验证输入有效性
        if (StringUtils.isBlank(path)) {
            return defaultFile;
        }
        File currentFile = new File(path);
        // 直接验证文件存在性
        if (currentFile.exists()) {
            return currentFile;
        }
        // 获取父目录并验证递归终止条件
        File parentFile = currentFile.getParentFile();
        if (parentFile == null || parentFile.getPath().equals(currentFile.getPath())) {
            return defaultFile;
        }
        return getExistsFile(parentFile.getPath());
    }

    /**
     * 获取app资源文件绝对路径
     *
     * @param path 资源文件相对路径
     * @return 资源文件绝对路径
     */
    public static String getAppResourcePath(String path) {
        String resourcePath = packagePath + path;
        // 处理macos打包成.app文件后的路径
        if (systemName.contains(mac)) {
            resourcePath = javaHome + "/bin/" + path;
        }
        if (!new File(resourcePath).exists()) {
            resourcePath = path;
        }
        return resourcePath;
    }

    /**
     * 获取logs文件夹地址
     *
     * @return 不同操作系统下logs文件夹地址
     */
    public static String getLogsPath() {
        String logsPath = userDir + File.separator + logs;
        // 处理macos打包成.app文件后的路径
        if (systemName.contains(mac) && !isRunningFromJar()) {
            logsPath = javaHome + logsDir;
        }
        return logsPath;
    }

    /**
     * 获取程序启动路径
     *
     * @return 不同操作系统下程序启动路径
     */
    public static String getAppPath() {
        if (systemName.contains(win)) {
            return new File(javaHome).getParent() + File.separator + appName + exe;
        } else if (systemName.contains(mac)) {
            return javaHome.substring(0, javaHome.indexOf(app) + app.length());
        }
        return javaHome;
    }

    /**
     * 获取用户桌面路径
     *
     * @return 用户桌面路径
     */
    public static String getDesktopPath() {
        FileSystemView fsv = FileSystemView.getFileSystemView();
        File desktopFile = fsv.getHomeDirectory();
        String path = desktopFile.getAbsolutePath();
        if (!path.contains(desktop)) {
            String desktopPath = path + File.separator + desktop;
            if (new File(desktopPath).exists()) {
                return desktopPath;
            }
        }
        return path;
    }

    /**
     * 根据jvm参数key读取cfg文件对应设置值
     *
     * @param optionKeys 要查询的jvm参数key
     * @return jvm参数key与对应的参数右侧的值
     * @throws IOException 配置文件读取异常
     */
    public static Map<String, String> getJavaOptionValue(List<String> optionKeys) throws IOException {
        Map<String, String> jvmOptions = new HashMap<>();
        List<String> jvmOptionsList = new ArrayList<>(optionKeys);
        try (BufferedReader reader = Files.newBufferedReader(Path.of(cfgFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // 检测配置段
                if (line.startsWith(javaOptions)) {
                    String arg = line.substring(javaOptions.length());
                    Iterator<String> iterator = jvmOptionsList.iterator();
                    while (iterator.hasNext()) {
                        String optionKey = iterator.next();
                        if (arg.contains(optionKey)) {
                            String value = arg.substring(arg.indexOf(optionKey) + optionKey.length());
                            jvmOptions.put(optionKey, value);
                            iterator.remove();
                        }
                    }
                }
            }
        }
        jvmOptionsList.forEach(optionKey -> jvmOptions.put(optionKey, ""));
        return jvmOptions;
    }

    /**
     * 更新cfg文件中jvm参数设置
     *
     * @param options 要修改的jvm参数键值对
     * @throws IOException 配置文件读取或写入异常
     */
    public static void setJavaOptionValue(Map<String, String> options) throws IOException {
        Path configPath = Path.of(cfgFilePath);
        List<String> lines = Files.readAllLines(configPath);
        boolean modified = false;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.startsWith(javaOptions)) {
                Iterator<Map.Entry<String, String>> iterator = options.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, String> entry = iterator.next();
                    String optionKey = entry.getKey();
                    String optionValue = entry.getValue();
                    if (line.contains(optionKey)) {
                        if (StringUtils.isNotBlank(optionValue)) {
                            // 处理修改参数
                            String newLineContent = javaOptions + optionKey + optionValue;
                            lines.set(i, line.replace(line, newLineContent));
                        } else {
                            // 处理删除参数
                            lines.remove(i);
                        }
                        iterator.remove();
                        modified = true;
                    }
                }
            }
        }
        // 处理新增参数
        for (Map.Entry<String, String> entry : options.entrySet()) {
            String optionValue = entry.getValue();
            if (StringUtils.isNotBlank(optionValue)) {
                lines.add(javaOptions + entry.getKey() + optionValue);
                modified = true;
            }
        }
        if (modified) {
            Files.write(configPath, lines);
        }
    }

    /**
     * 获取cfg文件路径
     *
     * @return cfg文件路径
     */
    public static String getCFGPath() {
        String cfgPath;
        if (isRunningFromJar()) {
            cfgPath = appName + cfg;
        } else {
            String appPath = getAppPath();
            String cfgFileName = "/" + appName + cfg;
            if (systemName.contains(win)) {
                cfgPath = new File(appPath).getParent() + appDirectory + cfgFileName;
            } else {
                cfgPath = appPath + contentsDirectory + appDirectory + cfgFileName;
            }
        }
        return cfgPath;
    }

}
