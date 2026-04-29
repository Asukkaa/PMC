@echo off
chcp 65001 > nul
set JLINK_VM_OPTIONS=-Djavafx.enablePreview=true -Djavafx.suppressPreviewWarning=true --enable-final-field-mutation=com.sun.jna --enable-native-access=javafx.graphics,com.github.kwhat.jnativehook,com.sun.jna,org.bytedeco.javacpp,org.bytedeco.opencv,org.bytedeco.leptonica,org.bytedeco.tesseract
set DIR=%~dp0
"%DIR%\java" %JLINK_VM_OPTIONS% -m priv.koishi.pmc/priv.koishi.pmc.MainApplication %*
pause