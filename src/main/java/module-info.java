module priv.koishi.tools {

    requires static lombok;
    requires org.apache.commons.lang3;
    requires org.apache.logging.log4j.core;
    requires jdk.management;
    requires com.github.kwhat.jnativehook;
    requires com.fasterxml.jackson.databind;
    requires nsmenufx;
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.poi.ooxml;
    requires org.apache.commons.collections4;
    requires org.apache.commons.io;
    requires java.net.http;
    requires com.sun.jna.platform;

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
    exports priv.koishi.tools.Finals;
    exports priv.koishi.tools.Bean.Vo;
}