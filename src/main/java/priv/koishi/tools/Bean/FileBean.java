package priv.koishi.tools.Bean;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.tools.Annotate.UsedByReflection;
import priv.koishi.tools.Configuration.CodeRenameConfig;

import java.io.File;
import java.io.IOException;

import static priv.koishi.tools.Utils.FileUtils.isImgFile;
import static priv.koishi.tools.Utils.UiUtils.tableViewImageService;

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
     * 文件列表展示文件类型
     */
    String fileType;

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
     * 加载缩略图线程
     */
    private transient Service<Image> currentThumbService;

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
            if (isImgFile(new File(getPath()))) {
                // 终止进行中的服务
                if (currentThumbService != null && currentThumbService.isRunning()) {
                    currentThumbService.cancel();
                }
                currentThumbService = tableViewImageService(getPath());
                currentThumbService.setOnSucceeded(e -> {
                    thumb = currentThumbService.getValue();
                    Platform.runLater(() -> tableView.refresh());
                });
                currentThumbService.start();
            } else {
                thumb = null;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取完整重命名
     */
    public String getFullRename() {
        return getRename() + getFileType();
    }

    /**
     * 获取完整文件名
     */
    public String getFullName() {
        return getName() + getFileType();
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
