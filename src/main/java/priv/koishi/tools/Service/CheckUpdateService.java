package priv.koishi.tools.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import priv.koishi.tools.Bean.CheckUpdateBean;
import priv.koishi.tools.Bean.UniCloudResponse;
import priv.koishi.tools.CustomUI.ProgressDialog.ProgressDialog;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static priv.koishi.tools.Controller.MainController.aboutController;
import static priv.koishi.tools.Finals.CommonFinals.*;
import static priv.koishi.tools.Utils.CommonUtils.getProcessId;
import static priv.koishi.tools.Utils.FileUtils.*;

/**
 * 检查更新服务类
 *
 * @author KOISHI
 * Date:2025-06-24
 * Time:14:50
 */
public class CheckUpdateService {

    /**
     * 日志记录器
     */
    private static final Logger logger = LogManager.getLogger(CheckUpdateService.class);

    /**
     * 检查是有新版本
     *
     * @param checkUpdateBean 检查更新信息
     * @return true:有新版本 false:无新版本
     */
    public static boolean isNewVersionAvailable(CheckUpdateBean checkUpdateBean) {
        // 无效数据视为无更新
        if (checkUpdateBean == null ||
                StringUtils.isBlank(checkUpdateBean.getVersion()) ||
                StringUtils.isBlank(checkUpdateBean.getBuildDate())) {
            return false;
        }
        String lastVersion = checkUpdateBean.getVersion();
        // 检测到的最新版本
        String[] lastVersionSplit = lastVersion.split("\\.");
        // 当前版本
        String[] nowVersionSplit = version.split("\\.");
        //  比较版本号的各个位数
        int length = Math.max(lastVersionSplit.length, nowVersionSplit.length);
        for (int i = 0; i < length; i++) {
            // 检测到的最新版本
            int last = i < lastVersionSplit.length ? Integer.parseInt(lastVersionSplit[i]) : 0;
            // 当前版本
            int now = i < nowVersionSplit.length ? Integer.parseInt(nowVersionSplit[i]) : 0;
            if (last != now) {
                return last > now;
            }
        }
        // 比较构建日期
        return checkUpdateBean.getBuildDate().compareTo(buildDate) > 0;
    }

    /**
     * 调用 uniCloud 查询最新版本
     *
     * @return 最新版本信息
     */
    public static Task<CheckUpdateBean> checkLatestVersion() {
        return new Task<>() {
            @Override
            protected CheckUpdateBean call() {
                updateMessage("检查更新中...");
                String osType = isWin ? win : mac;
                for (int attempt = 0; attempt < urls.length; attempt++) {
                    try (HttpClient client = HttpClient.newHttpClient()) {
                        // 构建请求体 osType:操作系统简称 version:当前版本（只有前两位版本号都相同时才增量更新，其余情况都为全量更新）
                        String requestBody = "{\"os\":\"" + osType + "\", \"version\":\"" + version + "\"}";
                        logger.info("查询版本更新，请求体: {}", requestBody);
                        logger.info("查询版本更新，请求URL: {}", urls[attempt]);
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(urls[attempt]))
                                .header("Content-Type", "application/json")
                                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                                .build();
                        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        logger.info("查询版本更新，响应体: {}", response);
                        String jsonData = response.body();
                        logger.info("查询版本更新，响应体body: {}", jsonData);
                        if (jsonData == null) {
                            throw new IOException("检查失败，响应体为空");
                        }
                        ObjectMapper objectMapper = new ObjectMapper();
                        // 配置忽略未知字段
                        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                        // 先解析为 UniCloudResponse
                        UniCloudResponse uniResponse = objectMapper.readValue(jsonData, UniCloudResponse.class);
                        logger.info("查询版本更新，解析后的响应体: {}", uniResponse);
                        if (uniResponse.getCode() == 200) {
                            // 成功时提取 data 字段
                            return uniResponse.getData();
                        } else {
                            // 错误处理
                            throw new IOException(text_checkFailed);
                        }
                    } catch (Exception e) {
                        // 判断是否需要重试
                        if (attempt == 0) {
                            logger.warn("主地址请求失败，准备重试备用地址", e);
                        } else {
                            // 最终失败处理
                            logger.error("更新检查最终失败", e);
                            throw new RuntimeException(text_checkFailed, e);
                        }
                    }
                }
                throw new RuntimeException(text_checkFailed);
            }
        };
    }

    /**
     * 下载并安装更新
     *
     * @param updateInfo     更新信息
     * @param progressDialog 进度对话框
     */
    public static Task<Void> downloadAndInstallUpdate(CheckUpdateBean updateInfo, ProgressDialog progressDialog) {
        return new Task<>() {
            @Override
            protected Void call() throws IOException, NoSuchAlgorithmException, KeyManagementException {
                // 显示下载进度对话框
                progressDialog.show("下载中...",
                        "下载更新",
                        "取消",
                        () -> {
                            aboutController.cancelUpdate();
                            progressDialog.close();
                        });
                // 创建temp文件夹
                File tempDir = new File(ToolsTempPath);
                if (!tempDir.exists()) {
                    if (!tempDir.mkdirs()) {
                        throw new RuntimeException("创建临时文件失败");
                    }
                }
                if (isWin) {
                    Files.setAttribute(tempDir.toPath(), "dos:hidden", true);
                }
                File tempFile = null;
                // 下载地址数组
                String[] downloadLinks = {
                        updateInfo.getAliyunFileLink(),
                        updateInfo.getAlipayFileLink()
                };
                for (int attempt = 0; attempt < downloadLinks.length; attempt++) {
                    // 在temp目录创建临时文件
                    tempFile = File.createTempFile("tools_update_", zip, tempDir);
                    // 创建使用TLSv1.2的SSLContext
                    SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
                    sslContext.init(null, null, null);
                    // 创建HttpClient，并设置SSLContext
                    Path tempPath = Path.of(ToolsTempPath);
                    try (HttpClient client = HttpClient.newBuilder()
                            .sslContext(sslContext)
                            .followRedirects(HttpClient.Redirect.ALWAYS)
                            .build()) {
                        String downloadLink = downloadLinks[attempt];
                        String encodedLink = downloadLink.replace(" ", "%20");
                        logger.info("下载更新，请求体: {}", encodedLink);
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(encodedLink))
                                .header("User-Agent", "Mozilla/5.0")
                                .GET()
                                .build();
                        // 发送请求并处理响应
                        HttpResponse<InputStream> response;
                        try {
                            response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
                        } catch (Exception e) {
                            throw new IOException(text_downloadFailed);
                        }
                        if (response != null && (response.statusCode() < 200 || response.statusCode() >= 300)) {
                            throw new IOException("下载更新失败，HTTP代码: " + response.statusCode());
                        }
                        if (response != null) {
                            try (InputStream inputStream = response.body();
                                 FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                                long contentLength = response.headers()
                                        .firstValueAsLong("Content-Length")
                                        .orElse(0);
                                byte[] buffer = new byte[8192];
                                long downloadedBytes = 0;
                                int bytesRead;
                                while ((bytesRead = inputStream.read(buffer)) != -1) {
                                    // 每次循环都检查取消状态
                                    if (isCancelled()) {
                                        break;
                                    }
                                    outputStream.write(buffer, 0, bytesRead);
                                    downloadedBytes += bytesRead;
                                    if (contentLength > 0) {
                                        double progress = (double) downloadedBytes / contentLength;
                                        int progressPercentage = (int) (progress * 100);
                                        String fileSize = getUnitSize(downloadedBytes) + " / " + getUnitSize(contentLength);
                                        String massage = "下载更新" + " : " + fileSize + " (" + progressPercentage + "%)";
                                        progressDialog.updateProgress(progress, massage);
                                        // 支付宝云可能无法显示下载进度
                                    } else if (updateInfo.getAlipayFileLink().equals(downloadLink)) {
                                        String fileSize = getUnitSize(downloadedBytes);
                                        Platform.runLater(() ->
                                                progressDialog.updateMassage("下载更新" + " : " + fileSize));
                                    }
                                }
                                // 如果任务被取消，删除临时文件夹
                                if (isCancelled()) {
                                    logger.info("任务被取消，删除临时文件夹： {}", ToolsTempPath);
                                    deleteDirectoryRecursively(tempPath);
                                    break;
                                }
                            }
                            break;
                        }
                    } catch (Exception e) {
                        logger.error("下载尝试失败", e);
                        if (attempt == downloadLinks.length - 1) {
                            // 删除不完整的临时文件
                            deleteDirectoryRecursively(tempPath);
                            throw new IOException(text_downloadFailed, e);
                        }
                    }
                }
                File finalTempFile = tempFile;
                progressDialog.updateProgress(1, "下载完成，等待安装更新");
                // 更新下载进度对话框功能按钮点击事件
                progressDialog.updateButton("重启并安装更新",
                        () -> {
                            progressDialog.close();
                            executeInstaller(finalTempFile, updateInfo.isFullUpdate());
                        });
                return null;
            }
        };
    }

    /**
     * 安装程序
     *
     * @param installerFile 安装程序文件
     * @param fullUpdate    是否为全量更新(true-全量更新 false-增量更新)
     */
    public static void executeInstaller(File installerFile, boolean fullUpdate) {
        logger.info("====================准备开始安装更新======================");
        try {
            //解压安装程序
            String destPath = new File(installerFile.getParentFile(), ToolsUpdateUnzipped).getAbsolutePath();
            unzip(installerFile.getAbsolutePath(), destPath);
            // 根据操作系统执行安装程序
            if (isWin) {
                updateWinApp(fullUpdate);
            } else if (isMac) {
                updateMacApp(fullUpdate);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 更新Mac端应用
     *
     * @param fullUpdate 是否为全量更新(true-全量更新 false-增量更新)
     * @throws Exception 更新脚本未找到、无法设置脚本可执行权限、脚本不可执行、脚本执行失败、脚本执行被中断
     */
    private static void updateMacApp(boolean fullUpdate) throws Exception {
        // 获取源目录
        String source = fullUpdate ? appName + app : "lib";
        String sourceDir = ToolsTempPath + ToolsUpdateUnzipped + File.separator + source;
        // 获取目标目录
        String targetDir = fullUpdate ? appRootPath : javaHome;
        // 在系统临时目录创建脚本
        File updateScriptFile = File.createTempFile(updateScript, sh);
        String pid = getProcessId();
        try (PrintWriter writer = new PrintWriter(updateScriptFile)) {
            // 从资源加载脚本
            try (InputStream is = CheckUpdateService.class.getResourceAsStream(resourcePath + "script/update.sh")) {
                if (is != null) {
                    new BufferedReader(new InputStreamReader(is))
                            .lines()
                            .forEach(line -> writer.println(line
                                    .replace("$APP_NAME", appName)
                                    .replace("$SOURCE_DIR", sourceDir)
                                    .replace("$TARGET_DIR", targetDir)
                                    .replace("$SYS_USER_NAME", sysUerName)
                                    .replace("$TEMP_DIR", ToolsTempPath)
                                    .replace("$APP_PATH", appLaunchPath)
                                    .replace("$APP_PID", pid)));
                } else {
                    throw new IOException(text_scriptNotFind);
                }
            } catch (Exception e) {
                throw new IOException(text_scriptNotFind, e);
            }
        }
        // 设置权限
        if (!updateScriptFile.setExecutable(true)) {
            throw new IOException("无法设置脚本可执行权限");
        }
        // 验证权限
        if (!updateScriptFile.canExecute()) {
            throw new IOException("脚本不可执行:" + updateScriptFile.getAbsolutePath());
        }
        // 构建执行命令
        String scriptCommand = String.format(
                "do shell script \"\\\"%s\\\"\" with administrator privileges " +
                        "with prompt \"%s\"",
                updateScriptFile.getAbsolutePath(),
                "更新并重启应用:\n" + appName + app
        );
        logger.info("-------------------------开始执行Mac更新脚本------------------------------");
        // 执行并捕获输出
        Process process = new ProcessBuilder("osascript", "-e", scriptCommand)
                .redirectErrorStream(true)
                .start();
        // 读取输出
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logger.info("脚本输出: {}", line);
                output.append(line).append("\n");
            }
        }
        // 等待完成
        int exitCode = process.waitFor();
        logger.info("脚本退出码: {}", exitCode);
        if (exitCode != 0) {
            logger.info("任务失败，删除临时文件夹： {}", ToolsTempPath);
            deleteDirectoryRecursively(Path.of(ToolsTempPath));
            deleteDirectoryRecursively(updateScriptFile.toPath());
            throw new IOException("脚本执行失败，退出码:" + exitCode +
                    "\n脚本输出内容:" + output);
        }
    }

    /**
     * 更新win端应用
     *
     * @param fullUpdate 是否为全量更新(true-全量更新 false-增量更新)
     * @throws Exception 找不到批处理脚本、获取进程PID异常
     */
    private static void updateWinApp(boolean fullUpdate) throws Exception {
        // 获取源目录
        String sourceDir = ToolsTempPath + ToolsUpdateUnzipped;
        // 获取目标目录
        String targetDir = fullUpdate ? appRootPath : javaHome;
        // 创建临时批处理文件
        File batFile = File.createTempFile(updateScript, bat);
        try (PrintWriter writer = new PrintWriter(batFile)) {
            // 从资源文件读取批处理脚本内容
            try (InputStream is = CheckUpdateService.class.getResourceAsStream(resourcePath + "script/update.bat")) {
                if (is != null) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            writer.println(line);
                        }
                    }
                } else {
                    throw new RuntimeException(text_scriptNotFind);
                }
            }
        }
        // 构建命令参数
        List<String> command = new ArrayList<>();
        command.add("cmd.exe");
        command.add("/c");
        command.add(batFile.getAbsolutePath());
        command.add(sourceDir);
        command.add(targetDir);
        command.add(appName + exe);
        command.add(ToolsTempPath);
        command.add(appRootPath);
        command.add(getProcessId());
        // 执行批处理
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(new File(targetDir));
        logger.info("-------------------------开始执行Win更新脚本------------------------------");
        builder.start();
    }

}
