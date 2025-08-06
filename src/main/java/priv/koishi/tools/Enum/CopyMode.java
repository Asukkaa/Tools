package priv.koishi.tools.Enum;

/**
 * @author KOISHI
 * Date:2025-08-06
 * Time:15:52
 */
public enum CopyMode {

    /**
     * 仅复制文件
     */
    ONLY_FILES,

    /**
     * 保留结构，复制文件与目录（含空目录）
     */
    STRUCTURE_WITH_FILES,

    /**
     * 仅复制目录（不含文件）
     */
    STRUCTURE_ONLY_FOLDERS

}
