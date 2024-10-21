package priv.koishi.tools.Bean;

import javafx.beans.property.SimpleStringProperty;

/**
 * @author KOISHI
 * Date:2024-10-16
 * Time:下午1:34
 */
public class ImgFileBean {

    /**
     * 分组序号
     */
    private final SimpleStringProperty id;

    /**
     * 分组名称
     */
    private final SimpleStringProperty groupName;

    public ImgFileBean(){
        this.id = new SimpleStringProperty();
        this.groupName = new SimpleStringProperty();
    }

    public String getId() {
        return this.id.get();
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public String getGroupName() {
        return this.groupName.get();
    }

    public void setGroupName(String groupName) {
        this.groupName.set(groupName);
    }

}
