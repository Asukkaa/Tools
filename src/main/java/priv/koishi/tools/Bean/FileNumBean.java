package priv.koishi.tools.Bean;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author KOISHI
 * Date:2024-10-09
 * Time:下午4:05
 */
public class FileNumBean {

    /**
     * 分组序号
     */
    private final SimpleIntegerProperty groupId;

    /**
     * 分组名称
     */
    private final SimpleStringProperty groupName;

    /**
     * 文件数量
     */
    private final SimpleIntegerProperty groupNumber;

    /**
     * 文件名称列表字符串
     */
    private final SimpleStringProperty fileName;

    /**
     * 带单位的文件总大小
     */
    private final SimpleStringProperty fileUnitSize;

    /**
     * 文件总大小
     */
    @Getter
    @Setter
    long fileSize;

    /**
     * 文件路径列表
     */
    @Getter
    @Setter
    List<String> filePathList;

    /**
     * 文件名称列表
     */
    @Getter
    @Setter
    List<String> fileNameList;

    public FileNumBean() {
        this.groupId = new SimpleIntegerProperty();
        this.groupName = new SimpleStringProperty();
        this.groupNumber = new SimpleIntegerProperty();
        this.fileName = new SimpleStringProperty();
        this.fileUnitSize = new SimpleStringProperty();
    }

    public int getGroupId() {
        return this.groupId.get();
    }

    public void setGroupId(int id) {
        this.groupId.set(id);
    }

    public String getGroupName() {
        return this.groupName.get();
    }

    public void setGroupName(String groupName) {
        this.groupName.set(groupName);
    }

    public int getGroupNumber() {
        return this.groupNumber.get();
    }

    public void setGroupNumber(int groupNumber) {
        this.groupNumber.set(groupNumber);
    }

    public String getFileName() {
        return this.fileName.get();
    }

    public void setFileName(String fileName) {
        this.fileName.set(fileName);
    }

    public String getFileUnitSize() {
        return this.fileUnitSize.get();
    }

    public void setFileUnitSize(String fileUnitSize) {
        this.fileUnitSize.set(fileUnitSize);
    }

}
