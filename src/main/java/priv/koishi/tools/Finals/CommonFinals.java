package priv.koishi.tools.Finals;

import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import javafx.scene.input.MouseButton;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 通用常量类
 *
 * @author KOISHI
 * Date:2024-11-13
 * Time:下午2:41
 */
public class CommonFinals {

    public static final String tip_startSize = "只能填自然数，0为不限制编号位数，不填默认为0";

    public static final String tip_renameStr = "填写后会将匹配到的字符串替换为所填写的字符串";

    public static final String tip_sheetName = "须填与excel模板相同的表名才能正常读取模板";

    public static final String tip_renameValue = "填写后会根据其他配置项处理文件名中所匹配的字符";

    public static final String tip_nameNum = "只能填自然数，0为不使用分隔符进行分组重命名，不填默认为0";

    public static final String tip_rightValue = "将所填字符根据选项插入或替换目标字符右侧所匹配的字符";

    public static final String tip_leftValue = "将所填字符根据选项插入或替换目标字符左侧所匹配的字符";

    public static final String tip_tag = "只能填自然数，不填默认为1，会根据所填值设置相同文件名起始尾缀";

    public static final String tip_maxRow = "只能填正整数，不填默认不限制，会读取到有数据的最后一行，最小值为1";

    public static final String tip_addSpace = "win系统自动重命名规则为：文件名 + 空格 + 英文括号包裹的阿拉伯数字编号";

    public static final String tip_filterFileType = "填写后只会识别所填写的后缀名文件，多个文件后缀名用空格隔开，后缀名需带 '.'";

    public static final String tip_left = "只能填自然数，不填 0 默认匹配目标字符串左侧所有字符，填写后匹配目标字符串左侧所填写个数的单个字符";

    public static final String tip_right = "只能填自然数，不填为 0 默认匹配目标字符串右侧所有字符，填写后匹配目标字符串右侧所填写个数的单个字符";

    public static final String tip_recursion = "勾选后将会查询文件夹中的文件夹里的文件";

    public static final String tip_sheet = "须填与excel模板相同的表名才能正常读取模板，若填表名不存在或不需要读取模板则会创建一个所填表";

    public static final String tip_subCode = "填写后会按所填写的字符串来分割文件名称，按照分割后的文件名称左侧字符串进行分组";

    public static final String tip_startRow = "只能填自然数，不填默认为0，不预留行";

    public static final String tip_startReadRow = "只能填自然数，不填默认与读取预留行相同";

    public static final String tip_removeExcelButton = "删除excel模板路径";

    public static final String tip_maxImgNum = "只能填正整数，不填默认为不限制";

    public static final String tip_imgHeightWidth = "只能填正整数，不填默认为 ";

    public static final String tip_rename = "点击后将会开始按照列表数据进行文件批量重命名，文件名不能包含 <>:\"/\\|?*";

    public static final String tip_imgWidth = " 个字符宽度";

    public static final String tip_imgHeight = " 个像素";

    public static final String tip_reLaunch = "保存所有改动并重启程序，如果重启后修改内存设置不生效可能需要再次重启";

    public static final String tip_noImg = "勾选后导出文件时没有图片的数据将会在单元格中标记为 无图片";

    public static final String tip_filterImgType = "只会识别勾选的图片格式，至少要勾选一种图片格式才能查询";

    public static final String tip_exportFullList = "勾选后将导出完整数据，不勾选只导出文件名称";

    public static final String tip_logsNum = "logs 文件夹下只会保留该配置所填写数量的 log 日志";

    public static final String tip_sort = "本设置将影响 按指定规则批量重命名文件 与 获取文件夹下的文件信息 功能的文件查询默认排序";

    public static final String tip_reverseSort = "默认不勾选，排序为从小到大，勾选后排序为从大到小";

    public static final String tip_excelType = "如果需要使用excel模板则导出文件类型只能与excel模板文件类型一致";

    public static final String tip_reselectButton = "点击将会按配置项重新查信息到列表中";

    public static final String tip_learButton = "点击将会清空列表中的数据";

    public static final String tip_exportButton = "点击将会按照列表数据和配置项导出数据到指定位置excel中";

    public static final String tip_excelPathButton = "点击后可选择excel模板位置";

    public static final String tip_fileButton = "点击后可选择要读取的文件夹位置，选择后将按照配置项读取数据到列表中";

    public static final String tip_outPathButton = "点击后可选择数据导出位置";

    public static final String tip_updateRenameButton = "点击后将会按照配置项更新列表中 修改后的文件名称 ，可配合排序等操作使用";

    public static final String tip_showFileType = "勾选后文件名称会显示文件拓展名";

    public static final String tip_exportTitle = "勾选后导出文件时导出起始行会添加每项数据的名称作为表头";

    public static final String tip_directoryNameType = "点击可选择文件与文件夹查询逻辑";

    public static final String tip_hideFileType = "点击可选择隐藏文件查询逻辑";

    public static final String tip_openFile = "勾选后任务结束将会打开对应文件";

    public static final String tip_openDirectory = "勾选后任务结束将会打开对应文件夹";

    public static final String tip_exportFileNum = "勾选后导出文件时会在各分组匹配到的文件信息左侧单元格填写匹配的文件数量";

    public static final String tip_exportFileSize = "勾选后导出文件时会在各分组匹配到的文件信息左侧单元格填写匹配的文件大小";

    public static final String tip_renameType = "点击可选择文件重命名依据";

    public static final String tip_differenceCode = "点击可选择区分编码类型";

    public static final String tip_subCodeSelect = "点击可选择尾缀分隔符";

    public static final String tip_targetStr = "点击可选择匹配字符规则";

    public static final String tip_renameBehavior = "点击可选择重命名方法";

    public static final String tip_tabSwitch = "点击即可启用或禁用该功能页";

    public static final String tip_openLink = "点击即可跳转对应网盘分享页";

    public static final String tip_defaultNextRunMemory = "当前配置值为空，程序最大内存设置为操作系统最大内存1/4，填写其他值关闭程序即可保存修改，重启动程序即可生效";

    public static final String tip_wait = "每步操作执行前等待时间，单位为毫秒，只能填自然数，不填默认为 0";

    public static final String tip_mouseStartX = "鼠标点击位置起始横坐标，与结束位置横坐标不同则执行拖拽操作，只能填自然数，不填默认为 0";

    public static final String tip_mouseStartY = "鼠标点击位置起始纵坐标，与结束位置纵坐标不同则执行拖拽操作，只能填自然数，不填默认为 0";

    public static final String tip_mouseEndX = "鼠标点击位置结束横坐标，与起始位置横坐标不同则执行拖拽操作，只能填自然数，不填默认为 起始横坐标";

    public static final String tip_mouseEndY = "鼠标点击位置结束纵坐标，与起始位置纵坐标不同则执行拖拽操作，只能填自然数，不填默认为 起始纵坐标";

    public static final String tip_runClick = "点击后将会按照列表中的步骤执行自动操作，执行自动化任务时按下 esc 即可取消任务";

    public static final String tip_addPosition = "点击后将会根据设置在列表中添加一条操作步骤";

    public static final String tip_clickTest = "点击后将会按照设置位置点击";

    public static final String tip_loopTime = "自动操作循环次数，只能填自然数，不填默认为 1，填 0 为无限循环";

    public static final String tip_clickNumBer = "每步操作执行次数，点击为多次点击，长按为多次长按，拖拽为多次拖拽，只能填自然数，不填默认为 1";

    public static final String tip_clickType = "每步操作需要按下的键";

    public static final String tip_clickTime = "每步操作中，单次操作的点击时长，单位为毫秒，只能填自然数，不填默认为 0";

    public static final String tip_clickInterval = "每步操作中，单次操作的时间间隔，只能填自然数，不填默认为 0";

    public static final String tip_clickName = "每步操作的名称，不填将给一个默认名称";

    public static final String tip_outAutoClickPath = "点击可设置操作流程导出文件夹地址";

    public static final String tip_loadAutoClick = "点击后选择要导入的操作流程即可在列表中追加";

    public static final String tip_exportAutoClick = "点击即可按照设置导出文件夹与文件名导出列表中的操作流程";

    public static final String tip_hideWindowRun = "勾选后运行或测试自动操作开始前将会隐藏本程序的窗口";

    public static final String tip_showWindowRun = "勾选后运行或测试自动操作结束后将会弹出本程序的窗口";

    public static final String tip_hideWindowRecord = "勾选后录制自动操作开始前将会隐藏本程序的窗口";

    public static final String tip_showWindowRecord = "勾选后录制自动操作结束后将会弹出本程序的窗口";

    public static final String tip_preparationRecordTime = "在录制自动操作前将会等待的时间，只能填自然数，单位秒，不填默认为 ";

    public static final String tip_preparationRunTime = "在运行自动操作前将会等待的时间，只能填自然数，单位秒，不填默认为 ";

    public static final String tip_recordClick = """
            点击录制自动操作按钮将会等待设置的准备时间后开始录制自动操作
            每次鼠标点击并松开为一个步骤，每次点击间隔为操作前等待时间""";

    public static final String tip_excelName = """
            不用填写文件拓展名，如果导出文件夹已经存在同名文件将会覆盖模板excel文件
            文件名不能包含 <>:"/\\|?*
            设置为空或者不合法将会以默认名称命名，默认名称为：""";

    public static final String tip_autoClickFileName = """
            不用填写文件拓展名，导出文件为 .pmc 格式，如果导出文件夹已经存在同名文件不会覆盖
            文件名不能包含  <>:"/\\|?*
            设置为空或者不合法将会以默认名称命名，默认名称为：""";

    public static final String tip_firstClick = """
            勾选后：
            如果是运行 测试操作流程 则会 鼠标左键 点击一次设置栏设置的起始坐标后再执行测试操作
            如果是运行 自动化操作 则会 鼠标左键 点击一次第一步操作的起始坐标后再执行自动化操作
            建议 Windows 用户不要勾选， macOS 用户需要勾选
            Windows 会直接点击对应窗口的对应坐标，macOS 需要先点击对应窗口将焦点切换过去才能点中对应窗口的对应坐标""";

    public static final String tip_version = """
            version：2.1.0
            2025年3月构建""";

    public static final String tip_updateSameCode = """
            选中列表中的数据后点击这个按钮，会将选中数据的第一行到最后一行，
            所有数据的修改后的文件名称的文件编号替换为第一行数据的文件编号，
            文件名尾缀将延续所选的第一行数据继续递增""";

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

    public static final String text_fileListNull = "要读取的文件列表为空，需要先读取数据再继续";

    public static final String text_filePathNull = "要查询的文件夹位置为空，需要先设置要查询的文件夹位置再继续";

    public static final String text_selectNull = "未查询到符合条件的数据，需修改查询条件后再继续";

    public static final String text_outPathNull = "导出文件夹位置为空，需要先设置导出文件夹位置再继续";

    public static final String text_excelPathNull = "excel模板文件位置为空，需要先设置excel模板文件位置再继续";

    public static final String text_selectExcel = "选择excel模板文件";

    public static final String text_selectAutoFile = "选择自动化操作流程文件";

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

    public static final String text_fileNotExists = "文件不存在";

    public static final String text_directoryNotExists = "要读取的文件夹不存在";

    public static final String text_allHave = "共有 ";

    public static final String text_group = " 组数据，匹配到 ";

    public static final String text_picture = " 张图片，";

    public static final String text_totalFileSize = "总大小 ";

    public static final String text_file = " 个文件 ";

    public static final String text_coordinate = " 数据坐标：";

    public static final String text_data = " 组数据";

    public static final String text_process = " 步操作";

    public static final String text_printing = "正在输出第 ";

    public static final String text_identify = "已识别到 ";

    public static final String text_printDown = "所有数据已输出完毕";

    public static final String text_printData = "正在导出数据";

    public static final String text_readData = "正在读取数据";

    public static final String text_noHideFile = "不查询隐藏文件";

    public static final String text_onlyHideFile = "只查询隐藏文件";

    public static final String text_onlyFile = "只查询文件";

    public static final String text_onlyDirectory = "只查询文件夹";

    public static final String text_fileDirectory = "文件和文件夹都查询";

    public static final String text_copySuccess = "复制成功";

    public static final String text_nowSetting = "当前配置值为 ";

    public static final String text_memorySetting = " GB ，关闭程序即可保存修改，重启动程序即可生效";

    public static final String text_nowValue = "当前所填值为 ";

    public static final String text_VMOptions = "JLINK_VM_OPTIONS=";

    public static final String text_nullSelect = "未选中任何数据";

    public static final String text_activation = "启用";

    public static final String text_saveSuccess = "所有数据已导出到： ";

    public static final String text_loadSuccess = "已导入自动操作流程：";

    public static final String text_changeWindow = "已切换到目标窗口";

    public static final String text_executionTime = " 轮操作\n";

    public static final String text_execution = "正在执行第 ";

    public static final String text_cancelTask = "按下 esc 即可取消任务\n";

    public static final String text_loadAutoClick = "导入自动化流程文件：";

    public static final String text_formatError = " 内容格式不正确";

    public static final String text_noAutoClickList = "列表中没有要导出的自动操作流程";

    public static final String text_noAutoClickToRun = "列表中没有要执行的操作";

    public static final String text_LackKeyData = "导入文件缺少关键数据";

    public static final String text_step = "步骤 ";

    public static final String text_recordClicking = " 正在录制操作";

    public static final String text_preparation = " 秒后开始录制操作";

    public static final String text_run = " 秒后开始自动操作";

    public static final String text_recorded = "已记录 ";

    public static final String text_taskFailed = "出现错误，任务终止";

    public static final String text_taskCancelled = "任务已取消";

    public static final String text_taskFinished = "所有操作都以执行完毕";

    public static final String text_isAdd = " (添加)";

    public static final String text_isRecord = " (录制)";

    public static final String xlsx = ".xlsx";

    public static final String xls = ".xls";

    public static final String jpg = ".jpg";

    public static final String png = ".png";

    public static final String jpeg = ".jpeg";

    public static final String log = ".log";

    public static final String PMC = ".pmc";

    public static final String exe = ".exe";

    public static final String app = ".app";

    public static final String macos = "mac";

    public static final String win = "win";

    public static final String Byte = "Byte";

    public static final String KB = "KB";

    public static final String MB = "MB";

    public static final String GB = "GB";

    public static final String TB = "TB";

    public static final String g = "g";

    public static final String activation = "1";

    public static final String unActivation = "0";

    public static final String id_settingTab = "settingTab";

    public static final String id_aboutTab = "aboutTab";

    public static final String id_autoClickTab = "autoClickTab";

    public static final String id_fileNameToExcelTab = "fileNameToExcelTab";

    public static final String id_fileNumToExcelTab = "fileNumToExcelTab";

    public static final String id_imgToExcelTab = "imgToExcelTab";

    public static final String id_fileRenameTab = "fileRenameTab";

    public static final String key_sort = "sort";

    public static final String key_reverseSort = "reverseSort";

    public static final String key_logsNum = "logsNum";

    public static final String key_inFilePath = "inFilePath";

    public static final String key_excelInPath = "excelInPath";

    public static final String key_outFilePath = "outFilePath";

    public static final String key_appWidth = "appWidth";

    public static final String key_appHeight = "appHeight";

    public static final String key_tabIds = "tabIds";

    public static final String key_margin = "margin";

    public static final String key_floatingX = "floatingX";

    public static final String key_floatingY = "floatingY";

    public static final String key_detailWidth = "detailWidth";

    public static final String key_detailHeight = "detailHeight";

    public static final String key_floatingWidth = "floatingWidth";

    public static final String key_floatingHeight = "floatingHeight";

    public static final String key_loadLastConfig = "loadLastConfig";

    public static final String key_loadLastFullWindow = "loadLastFullWindow";

    public static final String key_loadLastMaxWindow = "loadLastMaxWindow";

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

    public static final String key_lastMaxWindow = "lastMaxWindow";

    public static final String key_lastExportTitle = "lastExportTitle";

    public static final String key_lastExportFileNum = "lastExportFileNum";

    public static final String key_lastExportFileSize = "lastExportFileSize";

    public static final String key_lastExportFullList = "lastExportFullList";

    public static final String key_lastLoopTime = "lastLoopTime";

    public static final String key_lastFirstClick = "lastFirstClick";

    public static final String key_lastOutFileName = "lastOutFileName";

    public static final String key_lastHideWindowRun = "lastHideWindowRun";

    public static final String key_lastShowWindowRun = "lastShowWindowRun";

    public static final String key_lastHideWindowRecord = "lastHideWindowRecord";

    public static final String key_lastShowWindowRecord = "lastShowWindowRecord";

    public static final String key_lastPreparationRecordTime = "lastPreparationRecordTime";

    public static final String key_lastPreparationRunTime = "lastPreparationRunTime";

    public static final String key_defaultOutFileName = "defaultOutFileName";

    public static final String key_defaultSheetName = "defaultSheetName";

    public static final String key_defaultStartCell = "defaultStartCell";

    public static final String key_defaultReadRow = "defaultReadRow";

    public static final String key_defaultReadCell = "defaultReadCell";

    public static final String key_defaultImgWidth = "defaultImgWidth";

    public static final String key_defaultImgHeight = "defaultImgHeight";

    public static final String key_defaultStartNameNum = "defaultStartNameNum";

    public static final String key_defaultPreparationRecordTime = "defaultPreparationRecordTime";

    public static final String key_defaultPreparationRunTime = "defaultPreparationRunTime";

    public static final String mouseButton_primary = "鼠标左键点击";

    public static final String mouseButton_secondary = "鼠标右键点击";

    public static final String mouseButton_middle = "鼠标中键点击";

    public static final String mouseButton_forward = "鼠标前侧键点击";

    public static final String mouseButton_back = "鼠标后侧键点击";

    public static final String mouseButton_none = "鼠标仅移动";

    public static final String menuItem_insertUp = "插入设置步骤到所选行第一行上一行";

    public static final String menuItem_insertDown = "插入设置步骤到所选行最后一行下一行";

    public static final String menuItem_recordUp = "插入录制步骤到所选行第一行上一行";

    public static final String menuItem_recordDown = "插入录制步骤到所选行最后一行下一行";

    public static final String menuItem_insertTop = "插入设置步骤到列表顶部";

    public static final String menuItem_recordTop = "插入录制步骤到列表顶部";

    public static final String menuItem_upCopy = "复制所选数据到所选行第一行上方";

    public static final String menuItem_downCopy = "复制所选数据到所选行最后一行下方";

    public static final String menuItem_appendCopy = "复制所选数据到列表最后一行";

    public static final String menuItem_topCopy = "复制所选数据到列表顶部";

    public static String extension_file = "文件";

    public static final String extension_folder = "文件夹";

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

    /**
     * 读取自动操作工具功能配置文件路径
     */
    public static final String configFile_Click = "config/autoClickConfig.properties";

    /**
     * 当前程序运行位置
     */
    public static final String userDir = System.getProperty("user.dir");

    /**
     * 当前程序运行操作系统
     */
    public static final String systemName = System.getProperty("os.name").toLowerCase();

    /**
     * 用户目录
     */
    public static final String userHome = System.getProperty("user.home");

    /**
     * 百度网盘地址
     */
    public static final String baiduLink = "https://pan.baidu.com/s/1q4nwoz-ABjZLIwuBep09ZA?pwd=ju2b";

    /**
     * 夸克网盘地址
     */
    public static final String quarkLink = "https://pan.quark.cn/s/962190eadcb1";

    /**
     * 迅雷网盘地址
     */
    public static final String xunleiLink = "https://pan.xunlei.com/s/VOGTX2MnWwCrqIwhUV6GEVOyA1?pwd=7pau#";

    /**
     * 程序名称
     */
    public static final String appName = "Tools";

    /**
     * 资源文件夹地址前缀
     */
    public static String resourcePath = "/priv/koishi/tools/";

    /**
     * 程序根目录
     */
    public static final String appNameSeparator = appName + File.separator;

    /**
     * win系统下启动脚本名称
     */
    public static final String appBat = "app.bat";

    /**
     * 启动脚本最大内存设置参数
     */
    public static final String Xmx = "-Xmx";

    /**
     * log4j配置文件名称
     */
    public static String log4j2 = "log4j2.xml";

    /**
     * 默认浮窗横轴偏移量
     */
    public static final int defaultOffsetX = 30;

    /**
     * 默认浮纵轴窗偏移量
     */
    public static final int defaultOffsetY = 30;

    /**
     * 在列表所选行第一行上方插入
     */
    public static final int upAdd = 1;

    /**
     * 在列表所选行最后一行下方插入
     */
    public static final int downAdd = 2;

    /**
     * 向列表最后一行追加
     */
    public static final int append = -1;

    /**
     * 向列表第一行上方插入
     */
    public static final int topAdd = 0;

    /**
     * 自动操作的操作类型选项对应的鼠标行为（操作用）
     */
    public static final Map<String, MouseButton> runClickTypeMap = new HashMap<>();

    static {
        runClickTypeMap.put(mouseButton_primary, MouseButton.PRIMARY);
        runClickTypeMap.put(mouseButton_secondary, MouseButton.SECONDARY);
        runClickTypeMap.put(mouseButton_middle, MouseButton.MIDDLE);
        runClickTypeMap.put(mouseButton_forward, MouseButton.FORWARD);
        runClickTypeMap.put(mouseButton_back, MouseButton.BACK);
        runClickTypeMap.put(mouseButton_none, MouseButton.NONE);
    }

    /**
     * 自动操作的操作类型选项对应的鼠标行为（录制用）
     */
    public static final Map<Integer, String> recordClickTypeMap = new HashMap<>();

    static {
        recordClickTypeMap.put(NativeMouseEvent.BUTTON1, mouseButton_primary);
        recordClickTypeMap.put(NativeMouseEvent.BUTTON2, mouseButton_secondary);
        recordClickTypeMap.put(NativeMouseEvent.BUTTON3, mouseButton_middle);
        recordClickTypeMap.put(NativeMouseEvent.BUTTON4, mouseButton_back);
        recordClickTypeMap.put(NativeMouseEvent.BUTTON5, mouseButton_forward);
    }

}
