module priv.koishi.pmc {

    requires static lombok;
    requires org.apache.commons.collections4;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires jdk.management;
    requires com.github.kwhat.jnativehook;
    requires com.fasterxml.jackson.databind;
    requires org.apache.commons.lang3;
    requires nsmenufx;
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.commons.io;
    requires org.bytedeco.javacv;
    requires org.bytedeco.opencv;

    opens priv.koishi.pmc to javafx.fxml;
    exports priv.koishi.pmc;
    exports priv.koishi.pmc.Controller;
    opens priv.koishi.pmc.Controller to javafx.fxml;
    exports priv.koishi.pmc.Utils;
    opens priv.koishi.pmc.Utils to javafx.fxml;
    exports priv.koishi.pmc.Bean;
    opens priv.koishi.pmc.Bean to javafx.fxml;
    exports priv.koishi.pmc.Finals;
}