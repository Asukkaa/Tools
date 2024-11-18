package priv.koishi.tools.Bean;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.File;

/**
 * @author KOISHI
 * Date 2024-10-01
 * Time 下午1:50
 */
public class FileBean {

    /**
     * 文件列表展示id
     */
    private final SimpleIntegerProperty id;

    /**
     * 文件列表展示名称
     */
    private final SimpleStringProperty name;

    /**
     * 文件列表展示修改后的名称
     */
    private final SimpleStringProperty rename;

    /**
     * 文件列表展示路径
     */
    private final SimpleStringProperty path;

    /**
     * 文件列表展示文件类型
     */
    private final SimpleStringProperty fileType;

    /**
     * 文件列表展示文件大小
     */
    private final SimpleStringProperty size;

    /**
     * 文件创建时间
     */
    private final SimpleStringProperty creatDate;

    /**
     * 文件修改时间
     */
    private final SimpleStringProperty updateDate;

    /**
     * 文件重命名功能临时名称文件
     */
    @Getter
    @Setter
    File tempFile;

    public FileBean() {
        this.id = new SimpleIntegerProperty();
        this.name = new SimpleStringProperty();
        this.rename = new SimpleStringProperty();
        this.path = new SimpleStringProperty();
        this.fileType = new SimpleStringProperty();
        this.size = new SimpleStringProperty();
        this.creatDate = new SimpleStringProperty();
        this.updateDate = new SimpleStringProperty();
    }

    public int getId() {
        return this.id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getName() {
        return this.name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getRename() {
        return this.rename.get();
    }

    public void setRename(String rename) {
        this.rename.set(rename);
    }

    public String getPath() {
        return this.path.get();
    }

    public void setPath(String filePath) {
        this.path.set(filePath);
    }

    public String getFileType() {
        return this.fileType.get();
    }

    public void setFileType(String fileType) {
        this.fileType.set(fileType);
    }

    public String getSize() {
        return this.size.get();
    }

    public void setSize(String fileSize) {
        this.size.set(fileSize);
    }

    public String getCreatDate() {
        return this.creatDate.get();
    }

    public void setCreatDate(String creatDate) {
        this.creatDate.set(creatDate);
    }

    public String getUpdateDate() {
        return this.updateDate.get();
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate.set(updateDate);
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

}
