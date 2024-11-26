package priv.koishi.tools.Text;

import javafx.util.Duration;

/**
 * @author KOISHI
 * Date:2024-11-13
 * Time:下午2:41
 */
public class CommonTexts {

    public static final String tip_startSize = "只能填自然数，0为不限制编号位数，不填默认为0";

    public static final String tip_renameStr = "填写后会将匹配到的字符串替换为所填写的字符串";

    public static final String tip_sheetName = "须填与excel模板相同的表名才能正常读取模板";

    public static final String tip_renameValue = "填写后会根据其他配置项处理文件名中所匹配的字符";

    public static final String tip_nameNum = "只能填自然数，0为不使用分隔符进行分组重命名，不填默认为0";

    public static final String tip_rightValue = "将所填字符根据选项插入或替换目标字符左侧所匹配的字符";

    public static final String tip_leftValue = "将所填字符根据选项插入或替换目标字符右侧所匹配的字符";

    public static final String tip_tag = "只能填自然数，不填默认为1，会根据所填值设置相同文件名起始尾缀";

    public static final String tip_maxRow = "只能填正整数，不填默认不限制，会读取到有数据的最后一行，最小值为1";

    public static final String tip_addSpace = "win系统自动重命名规则为：文件名 + 空格 + 英文括号包裹的阿拉伯数字编号";

    public static final String tip_filterFileType = "填写后只会识别所填写的后缀名文件，多个文件后缀名用空格隔开，后缀名需带 '.'";

    public static final String tip_left = "只能填自然数，不填 0 默认匹配目标字符串左侧所有字符，填写后匹配目标字符串左侧所填写个数的单个字符";

    public static final String tip_right = "只能填自然数，不填为 0 默认匹配目标字符串右侧所有字符，填写后匹配目标字符串右侧所填写个数的单个字符";

    public static final String tip_recursion = "勾选后将会查询文件夹中的文件夹里的文件";

    public static final String tip_excelName = "如果导出地址和名称与模板一样则会覆盖模板excel文件";

    public static final String tip_sheet = "须填与excel模板相同的表名才能正常读取模板，若填表名不存在或不需要读取模板则会创建一个所填表";

    public static final String tip_subCode = "填写后会按所填写的字符串来分割文件名称，按照分割后的文件名称左侧字符串进行分组";

    public static final String tip_startRow = "只能填自然数，不填默认为0，不预留行";

    public static final String tip_startReadRow = "只能填自然数，不填默认与读取预留行相同";

    public static final String tip_removeExcelButton = "删除excel模板路径";

    public static final String tip_maxImgNum = "只能填正整数，不填默认为不限制";

    public static final String tip_imgHeightWidth = "只能填正整数，不填默认为 ";

    public static final String tip_rename = "文件名不能包含 <>:\"/\\|?*";

    public static final String tip_imgWidth = " 个字符宽度";

    public static final String tip_imgHeight = " 个像素";

    public static final String tip_noImg = "勾选后没有图片的数据将会在单元格中标记为 无图片";

    public static final String tip_filterImgType = "只会识别勾选的图片格式，至少要勾选一种图片格式才能查询";

    public static final String tip_option = """
            插入：在匹配的字符位置插入所填写的字符串
            替换：将匹配的字符串替换为所填写的字符串
            删除：只删除指定位置的字符
            移除：移除指定位置左侧或右侧所有字符串""";

    public static final String text_onlyNaturalNumber = "只能填自然数，不填默认为 ";

    public static final String text_formThe = " ，从第 ";

    public static final String text_row = " 行读取";

    public static final String text_cell = " 列读取";

    public static final String text_selectDirectory = "选择文件夹";

    public static final String text_dataListNull = "列表为空";

    public static final String text_fileListNull = "要读取的文件列表为空，需要选择一个有文件的文件夹";

    public static final String text_filePathNull = "要查询的文件夹位置为空，需要先设置要查询的文件夹位置再继续";

    public static final String text_selectNull = "未查询到符合条件的数据，需修改查询条件后再继续";

    public static final String text_outPathNull = "导出文件夹位置为空，需要先设置导出文件夹位置再继续";

    public static final String text_excelPathNull = "excel模板文件位置为空，需要先设置excel模板文件位置再继续";

    public static final String text_selectExcel = "选择excel模板文件";

    public static final String text_arabicNumerals = "阿拉伯数字：123";

    public static final String text_chineseNumerals = "中文数字：一二三";

    public static final String text_abc = "小写英文字母：abc";

    public static final String text_ABC = "大小英文字母：ABC";

    public static final String text_codeRename = "按编号规则重命名";

    public static final String text_strRename = "按指定字符重命名";

    public static final String text_excelRename = "按excel模板重命名";

    public static final String text_specifyString = "指定字符串";

    public static final String text_specifyIndex = "指定字符位置";

    public static final String text_matchString = "要匹配的字符串:";

    public static final String text_matchIndex = "要匹配的字符位置:";

    public static final String text_bothSides = "处理两侧字符";

    public static final String text_replace = "替换所有字符为:";

    public static final String text_remove = "移除指定字符";

    public static final String text_insert = "插入字符串为:";

    public static final String text_toUpperCase = "全部英文字符转为大写";

    public static final String text_toLowerCase = "全部英文字符转为小写";

    public static final String text_swapCase = "全部英文字符大小写互换";

    public static final String text_removeAll = "移除所有字符";

    public static final String text_delete = "删除指定位置字符";

    public static final String text_cnBracket = "中文括号";

    public static final String text_enBracket = "英文括号";

    public static final String text_enHorizontal = "英文横杠";

    public static final String text_cnHorizontal = "中文横杠";

    public static final String text_excelNotExists = "模板excel文件不存在";

    public static final String text_fileNotExists = "要读取的文件夹不存在";

    public static final String text_allHave = "共有 ";

    public static final String text_group = " 组数据，匹配到 ";

    public static final String text_picture = " 张图片";

    public static final String text_file = " 个文件 ";

    public static final String text_coordinate = " 数据坐标：";

    public static final String text_data = " 组数据";

    public static final String text_printing = "正在输出第 ";

    public static final String text_identify = "已识别到 ";

    public static final String text_printDown = "所有数据已输出完毕";

    public static final String text_printData = "正在导出数据";

    public static final String text_readData = "正在读取数据";

    public static final String text_noHideFile = "不查询隐藏文件";

    public static final String text_onlyHideFile = "只查询隐藏文件";

    public static final String text_onlyFile = "只查询文件";

    public static final String text_onlyDirectory = "只查询文件夹";

    public static final String text_FileDirectory = "文件和文件夹都查询";

    public static final String userHome = "user.home";

    public static final String xlsx = ".xlsx";

    public static final String jpg = ".jpg";

    public static final String png = ".png";

    public static final String jpeg = ".jpeg";

    public static final String activation = "1";

    public static final String unActivation = "0";

    public static final String key_inFilePath = "inFilePath";

    public static final String key_excelInPath = "excelInPath";

    public static final String key_outFilePath = "outFilePath";

    public static final String key_loadLastConfig = "loadLastConfig";

    public static final String key_loadLastFullWindow = "loadLastFullWindow";

    public static final String key_lastDirectoryNameType = "lastDirectoryNameType";

    public static final String key_lastHideFileType = "lastHideFileType";

    public static final String key_lastRecursion = "lastRecursion";

    public static final String key_lastShowFileType = "lastShowFileType";

    public static final String key_lastOpenDirectory = "lastOpenDirectory";

    public static final String key_lastOpenFile = "lastOpenFile";

    public static final String key_lastExcelName = "lastExcelName";

    public static final String key_lastSheetName = "lastSheetName";

    public static final String key_lastExcelType = "lastExcelType";

    public static final String key_lastStartRow = "lastStartRow";

    public static final String key_lastStartCell = "lastStartCell";

    public static final String key_lastFilterFileType = "lastFilterFileType";

    public static final String key_lastInPath = "lastInPath";

    public static final String key_lastOutPath = "lastOutPath";

    public static final String key_lastExcelPath = "lastExcelPath";

    public static final String key_lastSubCode = "lastSubCode";

    public static final String key_lastReadRow = "lastReadRow";

    public static final String key_lastReadCell = "lastReadCell";

    public static final String key_lastMaxRow = "lastMaxRow";

    public static final String key_lastImgWidth = "lastImgWidth";

    public static final String key_lastImgHeight = "lastImgHeight";

    public static final String key_lastMaxImgNum = "lastMaxImgNum";

    public static final String key_lastNoImg = "lastNoImg";

    public static final String key_lastRenameType = "lastRenameType";

    public static final String key_lastStartName = "lastStartName";

    public static final String key_lastStartSize = "lastStartSize";

    public static final String key_lastNameNum = "lastNameNum";

    public static final String key_lastTag = "lastTag";

    public static final String key_lastAddSpace = "lastAddSpace";

    public static final String key_lastDifferenceCode = "lastDifferenceCode";

    public static final String key_lastTargetStr = "lastTargetStr";

    public static final String key_lastRenameValue = "lastRenameValue";

    public static final String key_lastRenameBehavior = "lastRenameBehavior";

    public static final String key_lastRenameStr = "lastRenameStr";

    public static final String key_lastLeft = "lastLeft";

    public static final String key_lastLeftBehavior = "lastLeftBehavior";

    public static final String key_lastLeftValue = "lastLeftValue";

    public static final String key_lastRight = "lastRight";

    public static final String key_lastRightBehavior = "lastRightBehavior";

    public static final String key_lastRightValue = "lastRightValue";

    public static final String key_lastTab = "lastTab";

    public static final String key_lastFullWindow = "lastFullWindow";

    public static final String key_defaultOutFileName = "defaultOutFileName";

    public static final String key_defaultSheetName = "defaultSheetName";

    public static final String key_defaultStartCell = "defaultStartCell";

    public static final String key_defaultReadRow = "defaultReadRow";

    public static final String key_defaultReadCell = "defaultReadCell";

    public static final String key_defaultImgWidth = "defaultImgWidth";

    public static final String key_defaultImgHeight = "defaultImgHeight";

    public static final String key_defaultStartNameNum = "defaultStartNameNum";

    /**
     * excel插入图片功能配置文件路径
     */
    public static final String configFile_Img = "config/imgToExcelConfig.properties";

    /**
     * 重命名功能配置文件路径
     */
    public static final String configFile_Rename = "config/fileRenameConfig.properties";

    /**
     * 统计文件数量功能配置文件路径
     */
    public static final String configFile_Num = "config/fileNumToExcelConfig.properties";

    /**
     * 读取文件名称功能配置文件路径
     */
    public static final String configFile_Name = "config/fileNameToExcelConfig.properties";

    /**
     * app配置文件路径
     */
    public static final String configFile = "config/config.properties";

    public static final Duration showDuration = Duration.seconds(6000000);

}
