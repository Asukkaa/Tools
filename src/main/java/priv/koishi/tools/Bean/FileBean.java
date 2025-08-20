package priv.koishi.tools.Bean;

import javafx.application.Platform;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.tools.Annotate.UsedByReflection;
import priv.koishi.tools.Configuration.CodeRenameConfig;
import priv.koishi.tools.Configuration.CopyConfig;

import java.io.File;

import static priv.koishi.tools.Finals.CommonFinals.extension_file;
import static priv.koishi.tools.Finals.CommonFinals.extension_folder;
import static priv.koishi.tools.Utils.FileUtils.isImgFile;

/**
 * javafx列表展示文件信息类
 *
 * @author KOISHI
 * Date 2024-10-01
 * Time 下午1:50
 */
@Data
@Accessors(chain = true)
public class FileBean implements Indexable {

    /**
     * 序号
     */
    Integer index;

    /**
     * 文件列表展示id
     */
    int id;

    /**
     * 文件列表展示名称
     */
    String name;

    /**
     * 文件列表展示修改后的名称
     */
    String rename;

    /**
     * 文件列表展示路径
     */
    String path;

    /**
     * 文件列表展示复制路径
     */
    String copyPath;

    /**
     * 文件列表展示文件拓展名
     */
    String fileType;

    /**
     * 文件列表展示修改后的文件拓展名
     */
    String newFileType;

    /**
     * 文件列表展示文件大小
     */
    String size;

    /**
     * 文件创建时间
     */
    String creatDate;

    /**
     * 文件修改时间
     */
    String updateDate;

    /**
     * 文件是否隐藏
     */
    String showStatus;

    /**
     * 文件重命名功能临时名称文件
     */
    File tempFile;

    /**
     * 根据按编号规则重命名文件重命名前缀编号
     */
    String codeRename;

    /**
     * 根据按编号规则重命名文件重命名后缀编号
     */
    String tagRename;

    /**
     * 根据按编号规则重命名文件重命名后缀编号数字
     */
    int tagRenameCode;

    /**
     * 根据按编号规则重命名文件重命名设置
     */
    CodeRenameConfig codeRenameConfig;

    /**
     * 缩略图
     */
    Image thumb;

    /**
     * 要显示缩略图的列表
     */
    TableView<FileBean> tableView;

    /**
     * 拷贝功能配置
     */
    CopyConfig copyConfig;

    /**
     * 复制文件配置详情页删除标志（true-删除）
     */
    boolean remove;

    /**
     * 加载缩略图线程
     */
    private transient Thread currentThumbThread;

    /**
     * 获取缩略图
     *
     * @return 当前图片表格的缩略图
     */
    @UsedByReflection
    public Image loadThumb() {
        if (StringUtils.isBlank(getPath())) {
            return null;
        }
        if (thumb == null) {
            // 异步加载缩略图（防止阻塞UI）
            loadThumbnailAsync();
        }
        return thumb;
    }

    /**
     * 异步加载并更新缩略图
     */
    private void loadThumbnailAsync() {
        try {
            String path = getPath();
            if (isImgFile(new File(path))) {
                // 终止进行中的线程
                if (currentThumbThread != null && currentThumbThread.isAlive()) {
                    currentThumbThread.interrupt();
                }
                // 创建新的虚拟线程
                currentThumbThread = Thread.ofVirtual()
                        .name("thumbnail-loader-")
                        .unstarted(() -> {
                            try {
                                if (StringUtils.isNotBlank(path)) {
                                    Image image = new Image("file:" + path,
                                            100,
                                            100,
                                            true,
                                            true,
                                            true);
                                    Platform.runLater(() -> {
                                        thumb = image;
                                        tableView.refresh();
                                    });
                                }
                            } catch (Exception e) {
                                if (!Thread.currentThread().isInterrupted()) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });
                currentThumbThread.start();
            } else {
                thumb = null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取完整重命名
     *
     * @return 完整重命名
     */
    public String getFullRename() {
        return getRename() + getNewFileType();
    }

    /**
     * 获取完整文件名
     *
     * @return 完整文件名
     */
    public String getFullName() {
        String type = getFileType();
        if (extension_folder.equals(type) || extension_file.equals(type)) {
            type = "";
        }
        return getName() + type;
    }

    /**
     * 清理资源
     */
    public void clearResources() {
        if (currentThumbThread != null && currentThumbThread.isAlive()) {
            currentThumbThread.interrupt();
            currentThumbThread = null;
        }
        thumb = null;
        tempFile = null;
    }

    /**
     * 获取复制路径
     *
     * @return 复制路径
     */
    public String getCopyPath() {
        if (StringUtils.isBlank(copyPath)) {
            return new File(path).getParent();
        }
        return copyPath;
    }

    /**
     * 为列表数据设置序号接口
     *
     * @param index 要设置的序号
     */
    @Override
    public void setIndex(int index) {
        this.index = index;
    }

}
