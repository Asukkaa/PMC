/**
 * 项目模块化信息
 */
module priv.koishi.pmc {

    requires static lombok;
    requires org.apache.commons.collections4;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires jdk.management;
    requires com.github.kwhat.jnativehook;
    requires org.apache.commons.lang3;
    requires nsmenufx;
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.commons.io;
    requires org.bytedeco.javacv;
    requires org.bytedeco.opencv;
    requires com.sun.jna;
    requires jdk.localedata;
    requires java.net.http;
    requires com.sun.jna.platform;
    requires atlantafx.base;
    requires tools.jackson.databind;
    requires org.bytedeco.tesseract;
    requires com.github.oshi.ffm;
    requires org.apache.commons.exec;

    opens priv.koishi.pmc to javafx.fxml;
    exports priv.koishi.pmc;
    exports priv.koishi.pmc.controller;
    opens priv.koishi.pmc.controller to javafx.fxml;
    exports priv.koishi.pmc.utils;
    opens priv.koishi.pmc.utils to javafx.fxml;
    exports priv.koishi.pmc.bean;
    opens priv.koishi.pmc.bean to javafx.fxml;
    exports priv.koishi.pmc.finals;
    exports priv.koishi.pmc.service;
    opens priv.koishi.pmc.service to javafx.fxml;
    opens priv.koishi.pmc.finals to javafx.fxml;
    exports priv.koishi.pmc.bean.vo;
    opens priv.koishi.pmc.bean.vo to javafx.fxml;
    exports priv.koishi.pmc.queue;
    exports priv.koishi.pmc.ui.progressdialog;
    exports priv.koishi.pmc.bean.result;
    opens priv.koishi.pmc.bean.result to javafx.fxml;
    exports priv.koishi.pmc.bean.config;
    opens priv.koishi.pmc.bean.config to javafx.fxml;
    opens priv.koishi.pmc.ui.floatingwindow to javafx.fxml;
    exports priv.koishi.pmc.ui.floatingwindow;
    exports priv.koishi.pmc.jnanative.windowmonitor;
    exports priv.koishi.pmc.bean.beaninterface;
    opens priv.koishi.pmc.bean.beaninterface to javafx.fxml;
    exports priv.koishi.pmc.serializer to tools.jackson.databind;
    opens priv.koishi.pmc.bean.dto to javafx.fxml;
    exports priv.koishi.pmc.bean.dto;
    exports priv.koishi.pmc.listener;
    exports priv.koishi.pmc.callback;
    exports priv.koishi.pmc.ui.messagebubble;
    exports priv.koishi.pmc.finals.defaultconfig;
    opens priv.koishi.pmc.finals.defaultconfig to javafx.fxml;
    exports priv.koishi.pmc.thumb;
    opens priv.koishi.pmc.thumb to javafx.fxml;
    exports priv.koishi.pmc.bean.task;
    opens priv.koishi.pmc.bean.task to javafx.fxml;
}