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
    requires com.sun.jna;
    requires jdk.localedata;
    requires jdk.httpserver;
    requires java.net.http;

    opens priv.koishi.pmc to javafx.fxml;
    exports priv.koishi.pmc;
    exports priv.koishi.pmc.Controller;
    opens priv.koishi.pmc.Controller to javafx.fxml;
    exports priv.koishi.pmc.Utils;
    opens priv.koishi.pmc.Utils to javafx.fxml;
    exports priv.koishi.pmc.Bean;
    opens priv.koishi.pmc.Bean to javafx.fxml;
    exports priv.koishi.pmc.Finals;
    exports priv.koishi.pmc.Service;
    opens priv.koishi.pmc.Service to javafx.fxml;
    opens priv.koishi.pmc.Finals to javafx.fxml;
    exports priv.koishi.pmc.Bean.VO;
    opens priv.koishi.pmc.Bean.VO to javafx.fxml;
    exports priv.koishi.pmc.Serializer to com.fasterxml.jackson.databind;
    exports priv.koishi.pmc.Queue;
    exports priv.koishi.pmc.CustomUI.ProgressDialog;
    exports priv.koishi.pmc.Bean.Result;
    opens priv.koishi.pmc.Bean.Result to javafx.fxml;
    exports priv.koishi.pmc.Bean.Config;
    opens priv.koishi.pmc.Bean.Config to javafx.fxml;
}