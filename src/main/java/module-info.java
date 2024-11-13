module priv.koishi.tools {
    requires javafx.controls;
    requires javafx.fxml;

    requires static lombok;
    requires org.apache.poi.ooxml;
    requires java.desktop;
    requires org.apache.commons.collections4;
    requires org.apache.commons.lang3;
//    requires easyexcel.core;

    opens priv.koishi.tools to javafx.fxml;
    exports priv.koishi.tools;
    exports priv.koishi.tools.Controller;
    opens priv.koishi.tools.Controller to javafx.fxml;
    exports priv.koishi.tools.Utils;
    opens priv.koishi.tools.Utils to javafx.fxml;
    exports priv.koishi.tools.Bean;
    opens priv.koishi.tools.Bean to javafx.fxml;
    exports priv.koishi.tools.Enum;
    exports priv.koishi.tools.Configuration;
    exports priv.koishi.tools.Text;
}