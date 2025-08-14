package priv.koishi.tools.Utils;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseListener;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.tools.Bean.FileNumBean;
import priv.koishi.tools.Bean.Vo.FileNumVo;
import priv.koishi.tools.Configuration.FileConfig;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static priv.koishi.tools.Utils.FileUtils.getFileName;
import static priv.koishi.tools.Utils.FileUtils.getUnitSize;

/**
 * @author KOISHI
 * Date:2024-10-10
 * Time:下午1:14
 */
public class CommonUtils {

    /**
     * 正则表达式用于匹配指定范围的整数
     *
     * @param str 要校验的字符串
     * @param min 最小值，为空则不限制
     * @param max 最大值，为空则不限制
     * @return 在设置范围内为true，不在范围内为false
     */
    public static boolean isInIntegerRange(String str, Integer min, Integer max) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        // 禁止出现0开头的非0数字
        if (str.indexOf("0") == 0 && str.length() > 1) {
            return false;
        }
        // 禁止出现负数开头的0
        if (str.indexOf("-0") == 0) {
            return false;
        }
        Pattern integerPattern = Pattern.compile("^-?\\d{1,10}$");
        // 使用正则表达式判断字符串是否为整数
        if (!integerPattern.matcher(str).matches()) {
            return false;
        }
        // 将字符串转换为整数并判断是否在指定范围内
        int value = Integer.parseInt(str);
        // 只判断是否为整数，不限定范围
        if (max == null && min == null) {
            return true;
        }
        // 限定最小值
        if (max == null) {
            return value >= min;
        }
        // 限定最大值
        if (min == null) {
            return value <= max;
        }
        return value >= min && value <= max;
    }

    /**
     * int转汉字
     *
     * @param intNum 要转换为汉字的数字
     * @return 转为汉字的数字
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
        if (isNegative) {
            sb.insert(0, cnNegative);
        }
        String chineseNum = sb.toString().replaceAll("零[千百十]", "零").replaceAll("零+万", "万")
                .replaceAll("零+亿", "亿").replaceAll("亿万", "亿零")
                .replaceAll("零+", "零").replaceAll("零$", "");
        // 去掉10~19的一
        if (chineseNum.indexOf("一") == 0 && chineseNum.indexOf("十") == 1 && chineseNum.length() < 4) {
            chineseNum = chineseNum.substring(1);
        }
        return chineseNum;
    }

    /**
     * int转英文字母
     *
     * @param number      要转为英文字母的数字
     * @param toLowerCase 英文字母大小写标识，true小写，false大写
     * @return 转换后的对应字母
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
        // 因为从'A'开始，所以需要翻转字符串
        return alphaNum.reverse().toString();
    }


    /**
     * 匹配excel分组与文件夹下文件
     *
     * @param fileNumBeans 分组信息
     * @param inFileList   需要分组的文件
     * @param fileConfig   文件查询设置，用来获取文件名分隔符、分组最大匹配数量、是否展示文件拓展名
     * @return 用于列表统计展示的对象
     */
    public static FileNumVo matchGroupData(List<? extends FileNumBean> fileNumBeans, List<? extends File> inFileList, FileConfig fileConfig) {
        List<String> paths = new ArrayList<>();
        inFileList.forEach(file -> paths.add(file.getPath()));
        List<FileNumBean> fileNumList = buildNameGroupData(paths, fileConfig);
        AtomicInteger imgNum = new AtomicInteger();
        AtomicLong totalFileSize = new AtomicLong();
        fileNumBeans.forEach(bean1 -> {
            bean1.setGroupNumber(0);
            bean1.setFileName("");
            Optional<FileNumBean> matchedBeans = fileNumList.stream()
                    .filter(bean2 -> bean2.getGroupName().equals(bean1.getGroupName()))
                    .findFirst();
            matchedBeans.ifPresent(matched -> {
                bean1.setFileName(matched.getFileName());
                bean1.setGroupNumber(matched.getGroupNumber());
                bean1.setFileNameList(matched.getFileNameList());
                bean1.setFilePathList(matched.getFilePathList());
                bean1.setFileUnitSize(getUnitSize(matched.getFileSize()));
                totalFileSize.addAndGet(matched.getFileSize());
                imgNum.addAndGet(matched.getFilePathList().size());
            });
        });
        FileNumVo fileNumVo = new FileNumVo();
        fileNumVo.setImgNum(imgNum.get())
                .setDataNum(fileNumBeans.size())
                .setImgSize(getUnitSize(totalFileSize.get()));
        return fileNumVo;
    }

    /**
     * 分组组装javafx列表数据
     *
     * @param paths      要分组的文件的路径
     * @param fileConfig 文件查询设置，用来获取文件名分隔符、分组最大匹配数量、是否展示文件拓展名
     * @return javafx列表数据对象list
     */
    private static List<FileNumBean> buildNameGroupData(List<String> paths, FileConfig fileConfig) {
        List<FileNumBean> fileNumBeans = new ArrayList<>();
        Map<String, List<String>> sortedByKey = getSortedByMap(paths, fileConfig.getSubCode(), fileConfig.getMaxImgNum());
        sortedByKey.forEach((k, v) -> {
            FileNumBean fileNumBean = new FileNumBean();
            fileNumBean.setGroupName(k);
            List<String> names = new ArrayList<>();
            long fileSize = 0;
            for (String path : v) {
                String fileName;
                File file = new File(path);
                if (fileConfig.isShowFileType()) {
                    fileName = file.getName();
                } else {
                    fileName = getFileName(file);
                }
                fileSize += file.length();
                names.add(fileName);
            }
            names.sort(String.CASE_INSENSITIVE_ORDER);
            fileNumBean.setFileNameList(names);
            fileNumBean.setFileName(String.join("、", names));
            fileNumBean.setGroupNumber(v.size());
            fileNumBean.setFilePathList(v);
            fileNumBean.setFileSize(fileSize);
            fileNumBeans.add(fileNumBean);
        });
        return fileNumBeans;
    }

    /**
     * map分组并按key排序
     *
     * @param keys          分组用的唯一标识，完整的字符串
     * @param nameSubstring 分隔符
     * @param maxValue      每组最大匹配数
     * @return 分组后的map
     */
    public static Map<String, List<String>> getSortedByMap(List<String> keys, String nameSubstring, int maxValue) {
        // key为完整且唯一的字符串，value为分隔后的部分，可重复，作为分组依据
        Map<String, String> nameMap = new HashMap<>();
        Map<String, List<String>> groupedMap;
        keys.forEach(k -> {
            String leftName = getLeftName(k, nameSubstring);
            nameMap.put(k, leftName);
        });
        // 根据value分组
        if (maxValue > 0) {
            groupedMap = groupByValueWithLimit(nameMap, maxValue);
        } else {
            groupedMap = nameMap.entrySet().stream().collect(Collectors.groupingBy(Map.Entry::getValue,
                    Collectors.mapping(Map.Entry::getKey, Collectors.toList())));
        }
        // 根据完整路径排序
        return groupedMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    /**
     * 根据value分组并指定每组数量
     *
     * @param originalMap   分组前的map
     * @param listSizeLimit 每组最大匹配数
     * @return 分组后的map
     */
    public static Map<String, List<String>> groupByValueWithLimit(Map<String, String> originalMap, int listSizeLimit) {
        Map<String, List<String>> groupedMap = new HashMap<>();
        for (Map.Entry<String, String> entry : originalMap.entrySet()) {
            String value = entry.getValue();
            List<String> list = groupedMap.getOrDefault(value, new ArrayList<>());
            if (list.size() < listSizeLimit) {
                list.add(entry.getKey());
            }
            // 无论是否添加了元素都需要将更新后的列表放回groupedMap中，因为getOrDefault会返回一个新的列表，如果不添加元素则不需要更新，但如果添加了元素或者列表已经存在，需要确保groupedMap中的是最新状态
            groupedMap.put(value, list);
        }
        return groupedMap;
    }

    /**
     * 获取分隔符左侧字符串
     *
     * @param data          要处理的完整字符串
     * @param nameSubstring 分隔符
     * @return 分隔符左侧字符串
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
     * 获取详细的异常信息
     *
     * @param e 要获取的异常
     * @return 详细异常详细
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
     * 字符串大小写互换
     *
     * @param s 要转换的字符串
     * @return 转换后的字符串
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
     * 调用获取属性的get方法
     *
     * @param method 要获取属性的对象的方法
     * @return get方法为true，其他为false
     */
    public static boolean isGetterMethod(Method method) {
        return method.getName().startsWith("get")
                && method.getParameterCount() == 0
                && !method.getReturnType().equals(void.class)
                && !method.isAnnotationPresent(Override.class);
    }

    /**
     * 获取javafxBean属性名称
     *
     * @param getterName get方法名
     * @return 去掉get的属性名
     */
    public static String getPropertyName(String getterName) {
        return Character.toLowerCase(getterName.charAt(3)) + getterName.substring(4);
    }

    /**
     * 移除全局输入监听
     *
     * @param listener 要移除的监听器
     */
    public static void removeNativeListener(EventListener listener) {
        if (listener != null) {
            if (listener instanceof NativeMouseListener) {
                GlobalScreen.removeNativeMouseListener((NativeMouseListener) listener);
            } else if (listener instanceof NativeKeyListener) {
                GlobalScreen.removeNativeKeyListener((NativeKeyListener) listener);
            }
        }
    }

    /**
     * 自动复制同名属性（包含父类）
     *
     * @param source 源对象
     * @param target 目标对象
     * @throws IllegalArgumentException 如果源对象和目标对象类型不匹配，则抛出此异常
     */
    public static void copyProperties(Object source, Object target) throws IllegalAccessException {
        Class<?> sourceClass = source.getClass();
        Class<?> targetClass = target.getClass();
        // 遍历源对象继承链
        for (Class<?> clazz = sourceClass; clazz != Object.class; clazz = clazz.getSuperclass()) {
            for (Field sourceField : clazz.getDeclaredFields()) {
                try {
                    // 遍历目标对象继承链查找同名字段
                    Field targetField = findFieldInHierarchy(targetClass, sourceField.getName());
                    copyFieldValue(source, target, sourceField, targetField);
                } catch (NoSuchFieldException e) {
                    // 忽略目标类不存在的字段
                }
            }
        }
    }

    /**
     * 在类继承链中递归查找指定字段
     *
     * @param targetClass 查找字段的起始目标类
     * @param fieldName   需要查找的字段名称
     * @return 查找到的字段对象
     * @throws NoSuchFieldException 若整个继承链中均未找到字段时抛出
     */
    private static Field findFieldInHierarchy(Class<?> targetClass, String fieldName) throws NoSuchFieldException {
        for (Class<?> clazz = targetClass; clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                // 继续向上查找父类
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    /**
     * 复制字段值
     *
     * @param source      源对象，从中获取字段值
     * @param target      目标对象，将值设置到该对象
     * @param sourceField 源对象中需要复制的字段
     * @param targetField 目标对象中需要设置的字段
     * @throws IllegalAccessException 当字段访问权限不足时抛出
     */
    private static void copyFieldValue(Object source, Object target, Field sourceField, Field targetField) throws IllegalAccessException {
        sourceField.setAccessible(true);
        targetField.setAccessible(true);
        Object value = sourceField.get(source);
        if (value != null) {
            // 处理集合类型深拷贝
            if (value instanceof List) {
                targetField.set(target, new ArrayList<>((List<?>) value));
            } else {
                targetField.set(target, value);
            }
        }
    }

    /**
     * 获取当前GC类型
     *
     * @return 当前GC类型
     */
    public static String getCurrentGCType() {
        List<String> gcNames = ManagementFactory.getGarbageCollectorMXBeans().stream()
                .map(GarbageCollectorMXBean::getName).collect(Collectors.toList());
        if (gcNames.contains("G1 Young Generation") || gcNames.contains("G1 Old Generation")) {
            return "G1GC";
        } else if (gcNames.contains("PS Scavenge") || gcNames.contains("PS MarkSweep")) {
            return "ParallelGC";
        } else if (gcNames.contains("ZGC Cycles") || gcNames.contains("ZGC Pauses")) {
            return "ZGC";
        } else if (gcNames.contains("Shenandoah Pauses") || gcNames.contains("Shenandoah Cycles")) {
            return "ShenandoahGC";
        } else if (gcNames.contains("Copy") || gcNames.contains("MarkSweepCompact")) {
            return "SerialGC";
        } else {
            return "未知GC类型: " + String.join(", ", gcNames);
        }
    }

    /**
     * 获取当前进程PID
     *
     * @return 当前进程PID字符串
     * @throws Exception 获取PID时抛出的异常
     */
    public static String getProcessId() throws Exception {
        Class<?> processHandleClass = Class.forName("java.lang.ProcessHandle");
        Object currentProcessHandle = processHandleClass.getMethod("current").invoke(null);
        Object pid = processHandleClass.getMethod("pid").invoke(currentProcessHandle);
        return String.valueOf(pid);
    }

}
