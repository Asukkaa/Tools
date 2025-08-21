package priv.koishi.tools.CopyVisitor;

import org.apache.commons.collections4.CollectionUtils;
import priv.koishi.tools.Configuration.CodeRenameConfig;
import priv.koishi.tools.Enum.CopyMode;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributes;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static priv.koishi.tools.Finals.CommonFinals.*;
import static priv.koishi.tools.Service.MoveFileService.notOverwritePath;
import static priv.koishi.tools.Utils.FileUtils.getFileType;

/**
 * 批量拷贝文件
 *
 * @author KOISHI
 * Date:2025-08-06
 * Time:15:50
 */
public class CopyVisitor extends SimpleFileVisitor<Path> {

    /**
     * 源目录
     */
    private final Path sourceRoot;

    /**
     * 目标目录
     */
    private final Path targetRoot;

    /**
     * 拷贝方式
     */
    private final CopyMode copyMode;

    /**
     * 源文件处理方式（保留、删除、放入回收站）
     */
    private final String sourceAction;

    /**
     * 是否处理隐藏文件
     */
    private final String hideFileType;

    /**
     * 过滤文件类型
     */
    private final List<String> filterExtensionList;

    /**
     * 重命名规则
     */
    private final CodeRenameConfig codeRenameConfig;

    /**
     * 是否反向过滤文件类型（true-反向过滤）
     */
    private final boolean reverseFileType;

    /**
     * 构造函数
     *
     * @param sourceRoot          源目录
     * @param targetRoot          目标目录
     * @param copyMode            拷贝方式
     * @param filterExtensionList 过滤文件类型
     * @param sourceAction        源文件处理方式（保留、删除、放入回收站）
     * @param hideFileType        隐藏文件处理方式
     * @param codeRenameConfig    重命名规则
     * @param reverseFileType     是否反向过滤文件类型（true-反向过滤）
     */
    public CopyVisitor(Path sourceRoot, Path targetRoot, CopyMode copyMode, List<String> filterExtensionList,
                       String sourceAction, String hideFileType, CodeRenameConfig codeRenameConfig, boolean reverseFileType) {
        this.sourceRoot = sourceRoot;
        this.targetRoot = targetRoot;
        this.copyMode = copyMode;
        this.filterExtensionList = filterExtensionList;
        this.sourceAction = sourceAction;
        this.hideFileType = hideFileType;
        this.codeRenameConfig = codeRenameConfig;
        this.reverseFileType = reverseFileType;
    }

    /**
     * 将源文件的隐藏属性复制到目标文件（仅限Windows系统）。
     *
     * @param file       源文件路径，用于读取原始文件属性
     * @param targetFile 目标文件路径，用于设置隐藏属性
     * @throws IOException 如果读取文件属性或设置属性时发生I/O错误
     */
    public static void hiddenWinFile(Path file, Path targetFile) throws IOException {
        if (isWin) {
            DosFileAttributes dosAttrs = Files.readAttributes(file, DosFileAttributes.class);
            if (dosAttrs.isHidden()) {
                Files.setAttribute(targetFile, "dos:hidden", true);
            }
        }
    }

    /**
     * 在访问目录前处理目录创建逻辑
     *
     * @param path  当前访问的目录路径
     * @param attrs 目录的基本文件属性
     * @return 控制后续遍历行为的FileVisitResult结果
     * @throws IOException 当文件操作异常时抛出
     */
    @Override
    public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) throws IOException {
        // 处理隐藏文件，顶层目录是否隐藏都要读取
        if (!path.equals(sourceRoot)) {
            File file = path.toFile();
            if (file.isHidden() && text_noHideFile.equals(hideFileType)) {
                return FileVisitResult.SKIP_SUBTREE;
            }
            if (!file.isHidden() && text_onlyHideFile.equals(hideFileType)) {
                return FileVisitResult.SKIP_SUBTREE;
            }
        }
        // 如果当前目录是目标目录或其子目录，跳过复制
        if (path.startsWith(targetRoot) || path.equals(targetRoot)) {
            return FileVisitResult.SKIP_SUBTREE;
        }
        if (copyMode == CopyMode.STRUCTURE_ONLY_FOLDERS || copyMode == CopyMode.STRUCTURE_WITH_FILES) {
            // 构建目标目录路径
            Path relativePath = sourceRoot.relativize(path);
            Path targetDir = targetRoot.resolve(relativePath);
            // 防重名处理
            targetDir = Paths.get(notOverwritePath(targetDir.toString(), codeRenameConfig));
            // 创建目录
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }
            // 处理 win 的隐藏文件夹
            hiddenWinFile(path, targetDir);
        }
        return FileVisitResult.CONTINUE;
    }

    /**
     * 处理文件复制核心逻辑
     *
     * @param path  当前访问的文件路径
     * @param attrs 文件的基本文件属性
     * @return 控制后续遍历行为的FileVisitResult结果
     * @throws IOException 当文件操作异常时抛出
     */
    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
        // 处理隐藏文件
        File file = path.toFile();
        if (file.isHidden() && text_noHideFile.equals(hideFileType)) {
            return FileVisitResult.SKIP_SUBTREE;
        }
        if (!file.isHidden() && text_onlyHideFile.equals(hideFileType)) {
            return FileVisitResult.SKIP_SUBTREE;
        }
        // 如果当前目录是目标目录或其子目录，跳过复制
        if (path.startsWith(targetRoot) || path.equals(targetRoot)) {
            return FileVisitResult.SKIP_SUBTREE;
        }
        // 处理拓展名过滤
        String fileType = getFileType(path.toFile());
        if (CollectionUtils.isNotEmpty(filterExtensionList)) {
            boolean isMatch = filterExtensionList.contains(fileType);
            // 反向过滤
            if (reverseFileType) {
                isMatch = !isMatch;
            }
            if (!isMatch) {
                return FileVisitResult.CONTINUE;
            }
        }
        Path targetFile;
        if (copyMode == CopyMode.ONLY_FILES) {
            // 仅复制文件，不保留目录结构
            targetFile = targetRoot.resolve(path.getFileName());
        } else {
            // 保留目录结构
            targetFile = targetRoot.resolve(sourceRoot.relativize(path));
        }
        // 防重名处理
        targetFile = Paths.get(notOverwritePath(targetFile.toString(), codeRenameConfig));
        // 确保父目录存在
        Path targetParent = targetFile.getParent();
        if (!Files.exists(targetParent)) {
            Files.createDirectories(targetParent);
        }
        // 执行复制
        if (copyMode != CopyMode.STRUCTURE_ONLY_FOLDERS) {
            Files.copy(path, targetFile, StandardCopyOption.COPY_ATTRIBUTES);
            // 处理 win 的隐藏文件
            hiddenWinFile(path, targetFile);
            // 源文件删除逻辑
            if (sourceAction_deleteFile.equals(sourceAction)) {
                Files.delete(path);
            } else if (sourceAction_trashFile.equals(sourceAction)) {
                Desktop.getDesktop().moveToTrash(path.toFile());
            }
        }
        return FileVisitResult.CONTINUE;
    }

    /**
     * 目录访问完成后的处理
     *
     * @param dir 当前已完成访问的目录路径
     * @param exc 目录访问过程中抛出的异常（若存在）
     * @return 控制后续遍历行为的FileVisitResult结果
     * @throws IOException 当传入的异常参数非空时抛出
     */
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        if (exc != null) {
            throw exc;
        }
        if (sourceAction.equals(sourceAction_deleteFolder)) {
            // 仅处理源目录及其子目录删除逻辑
            try (Stream<Path> stream = Files.walk(dir)) {
                stream.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
            }
        } else if (sourceAction.equals(sourceAction_trashFolder)) {
            Desktop.getDesktop().moveToTrash(dir.toFile());
        }
        return FileVisitResult.CONTINUE;
    }

    /**
     * 文件访问失败时的异常处理
     *
     * @param file 当前访问失败的文件路径
     * @param exc  文件访问失败抛出的异常
     * @return 控制后续遍历行为的FileVisitResult结果
     */
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        return FileVisitResult.CONTINUE;
    }

}
