package priv.koishi.tools.Utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import priv.koishi.tools.MainApplication;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author KOISHI
 * Date:2024-10-10
 * Time:下午1:14
 */
public class CommonUtils {

    /**
     * 正则表达式用于匹配指定范围的整数
     */
    public static boolean isInIntegerRange(String str, Integer min, Integer max) {
        Pattern integerPattern = Pattern.compile("^-?\\d{1,10}$");
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        //禁止出现0开头的非0数字
        if (str.indexOf("0") == 0 && str.length() > 1) {
            return false;
        }
        // 使用正则表达式判断字符串是否为整数
        if (!integerPattern.matcher(str).matches()) {
            return false;
        }
        // 将字符串转换为整数并判断是否在指定范围内
        int value = Integer.parseInt(str);
        if (max == null) {
            return value >= min;
        }
        if (min == null) {
            return value <= max;
        }
        return value >= min && value <= max;
    }

    /**
     * map分组并按key排序
     */
    public static Map<String, List<String>> getSortedByMap(List<String> keys, String nameSubstring) {
        Map<String, String> namMap = new HashMap<>();
        Map<String, List<String>> groupedMap;
        keys.forEach(k -> {
            String leftName = getLeftName(k, nameSubstring);
            namMap.put(k, leftName);
        });
        //根据名称分组
        groupedMap = namMap.entrySet().stream()
                .collect(Collectors.groupingBy(Map.Entry::getValue,
                        Collectors.mapping(Map.Entry::getKey, Collectors.toList())));
        //根据完整路径排序
        return groupedMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, _) -> e1, LinkedHashMap::new));
    }

    /**
     * 获取分隔符左侧文件名
     */
    public static String getLeftName(String data, String nameSubstring) {
        String leftName;
        if (data.contains(nameSubstring)) {
            leftName = data.substring(data.lastIndexOf("\\") + 1, data.lastIndexOf(nameSubstring))
                    .replaceAll("\\s+", " ").trim();
        } else {
            leftName = data.substring(data.lastIndexOf("\\") + 1, data.lastIndexOf("."))
                    .replaceAll("\\s+", " ").trim();
        }
        return leftName;
    }

    /**
     * 单元格自适应
     */
    public static void autoSizeExcel(XSSFSheet sheet, int maxCellNum, int startCellNum) {
        for (int i = startCellNum; i < maxCellNum; i++) {
            sheet.autoSizeColumn(i);
            //手动调整列宽，解决中文不能自适应问题,单元格单行最长支持255*256的宽度（每个单元格样式已经设置自动换行，超出即换行）,设置最低列宽度，列宽约六个中文字符
            int width = Math.max(15 * 256, Math.min(255 * 256, sheet.getColumnWidth(i) * 12 / 10));
            sheet.setColumnWidth(i, width);
        }
    }

    /**
     * 获取详细的错误信息
     */
    public static String errToString(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        e.printStackTrace(pw);
        pw.flush();
        sw.flush();
        return sw.toString();
    }

    /**
     * 判断程序是否打包运行
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
     * 根据不同运行环境来建立输入流
     */
    public static InputStream checkRunningInputStream(String path) throws IOException {
        InputStream input;
        if (isRunningFromJar()) {
            input = Objects.requireNonNull(MainApplication.class.getResource(path)).openStream();
        } else {
            input = new FileInputStream(path);
        }
        return input;
    }

    /**
     * 根据不同运行环境来建立输出流
     */
    public static OutputStream checkRunningOutputStream(String path) throws IOException {
        OutputStream output;
        if (isRunningFromJar()) {
            output = new FileOutputStream(Objects.requireNonNull( MainApplication.class.getResource(path)).getPath());
        } else {
            output = new FileOutputStream(path);
        }
        return output;
    }

}
