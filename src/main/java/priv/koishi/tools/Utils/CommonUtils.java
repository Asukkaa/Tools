package priv.koishi.tools.Utils;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author KOISHI
 * Date:2024-10-10
 * Time:下午1:14
 */
public class CommonUtils {

    /**
     * 资源文件夹地址前缀
     */
    static String resourcesPath = "src/main/resources/priv/koishi/tools/";

    /**
     * 正则表达式用于匹配指定范围的整数
     */
    public static boolean isInIntegerRange(String str, Integer min, Integer max) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        //禁止出现0开头的非0数字
        if (str.indexOf("0") == 0 && str.length() > 1) {
            return false;
        }
        Pattern integerPattern = Pattern.compile("^-?\\d{1,10}$");
        //使用正则表达式判断字符串是否为整数
        if (!integerPattern.matcher(str).matches()) {
            return false;
        }
        //将字符串转换为整数并判断是否在指定范围内
        int value = Integer.parseInt(str);
        //只判断是否为整数，不限定范围
        if (max == null && min == null) {
            return true;
        }
        //限定最小值
        if (max == null) {
            return value >= min;
        }
        //限定最大值
        if (min == null) {
            return value <= max;
        }
        return value >= min && value <= max;
    }

    /**
     * int转汉字
     *
     * @param intNum 要转换为汉字的数字
     */
    public static String intToChineseNum(int intNum) {
        String[] cnNum = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
        String[] cnUnit = {"", "十", "百", "千", "万", "十", "百", "千", "亿", "十", "百", "千"};
        String cnNegative = "负";
        StringBuilder sb = new StringBuilder();
        boolean isNegative = false;
        if (intNum < 0) {
            isNegative = true;
            intNum *= -1;
        }
        int count = 0;
        while (intNum > 0) {
            sb.insert(0, cnNum[intNum % 10] + cnUnit[count]);
            intNum = intNum / 10;
            count++;
        }
        if (isNegative)
            sb.insert(0, cnNegative);
        String chineseNum = sb.toString().replaceAll("零[千百十]", "零").replaceAll("零+万", "万")
                .replaceAll("零+亿", "亿").replaceAll("亿万", "亿零")
                .replaceAll("零+", "零").replaceAll("零$", "");
        if (chineseNum.indexOf("一") == 0 && chineseNum.indexOf("十") == 1 && chineseNum.length() < 4) {
            chineseNum = chineseNum.substring(1);
        }
        return chineseNum;
    }

    /**
     * int转英文字母
     */
    public static String convertToAlpha(int number, boolean toLowerCase) {
        char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        if (toLowerCase) {
            ALPHABET = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        }
        if (number <= 0) {
            return "";
        }
        StringBuilder alphaNum = new StringBuilder();
        while (number > 0) {
            int remainder = (number - 1) % ALPHABET.length;
            alphaNum.append(ALPHABET[remainder]);
            number = (number - 1) / ALPHABET.length;
        }
        // 因为我们从'A'开始，所以需要翻转字符串
        return alphaNum.reverse().toString();
    }

    /**
     * map分组并按key排序
     */
    public static Map<String, List<String>> getSortedByMap(List<String> keys, String nameSubstring, int maxValue) {
        Map<String, String> nameMap = new HashMap<>();
        Map<String, List<String>> groupedMap;
        keys.forEach(k -> {
            String leftName = getLeftName(k, nameSubstring);
            nameMap.put(k, leftName);
        });
        //根据名称分组
        if (maxValue > 0) {
            groupedMap = groupByValueWithLimit(nameMap, maxValue);
        } else {
            groupedMap = nameMap.entrySet().stream().collect(Collectors.groupingBy(Map.Entry::getValue,
                    Collectors.mapping(Map.Entry::getKey, Collectors.toList())));
        }
        //根据完整路径排序
        return groupedMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    /**
     * 根据value分组并指定每组数量
     */
    public static Map<String, List<String>> groupByValueWithLimit(Map<String, String> originalMap, int listSizeLimit) {
        Map<String, List<String>> groupedMap = new HashMap<>();
        for (Map.Entry<String, String> entry : originalMap.entrySet()) {
            String value = entry.getValue();
            List<String> list = groupedMap.getOrDefault(value, new ArrayList<>());
            if (list.size() < listSizeLimit) {
                list.add(entry.getKey());
            }
            // 无论是否添加了元素，都需要将更新后的列表放回 groupedMap 中 因为 getOrDefault 会返回一个新的列表，如果我们不添加元素，则不需要更新 但如果添加了元素，或者列表已经存在，我们需要确保 groupedMap 中的是最新状态
            groupedMap.put(value, list);
        }
        return groupedMap;
    }


    /**
     * 获取分隔符左侧文件名
     */
    public static String getLeftName(String data, String nameSubstring) {
        String leftName;
        if (data.contains(nameSubstring) && StringUtils.isNotEmpty(nameSubstring)) {
            leftName = data.substring(data.lastIndexOf("\\") + 1, data.lastIndexOf(nameSubstring))
                    .replaceAll("\\s+", " ").trim();
        } else {
            leftName = data.substring(data.lastIndexOf("\\") + 1, data.lastIndexOf("."))
                    .replaceAll("\\s+", " ").trim();
        }
        if (leftName.isEmpty()) {
            leftName = " ";
        }
        return leftName;
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
            input = new FileInputStream(resourcesPath + path);
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
            output = new FileOutputStream(resourcesPath + path);
        } else {
            output = new FileOutputStream(path);
        }
        return output;
    }

    /**
     * 字符串大小写互换
     */
    public static String swapCase(String s) {
        char[] charArray = s.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            if (Character.isUpperCase(charArray[i])) {
                charArray[i] = Character.toLowerCase(charArray[i]);
            } else if (Character.isLowerCase(charArray[i])) {
                charArray[i] = Character.toUpperCase(charArray[i]);
            }
        }
        return new String(charArray);
    }

    /**
     * 替换字符串中的指定内容
     *
     * @param input  输入字符串
     * @param target 要被替换的目标字符串
     * @param repl   替换内容
     * @return 替换后的字符串
     */
    public static String replaceString(String input, String target, String repl) {
        // 对target进行转义，防止正则表达式特殊字符影响
        String regex = Pattern.quote(target);
        // 创建Pattern对象
        Pattern pattern = Pattern.compile(regex);
        // 创建Matcher对象
        Matcher matcher = pattern.matcher(input);
        // 替换所有匹配项
        return matcher.replaceAll(repl);
    }


    /**
     * 调用获取属性的方法
     */
    public static boolean isGetterMethod(Method method) {
        return method.getName().startsWith("get")
                && method.getParameterCount() == 0
                && !method.getReturnType().equals(void.class)
                && !method.isAnnotationPresent(Override.class);
    }

    /**
     * 获取javafxBean属性名称
     */
    public static String getPropertyName(String getterName) {
        return Character.toLowerCase(getterName.charAt(3)) + getterName.substring(4);
    }

}
