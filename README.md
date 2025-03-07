<h1>Perfect Mouse Control</h1>

## 项目简介
本项目是一个用 javafx 开发的自动操作工具。
可以编辑自动操作流程或录制自动操作流程，支持导入导出自动操作流程，支持循环自动操作。
自动流程文件为 .pmc 文件，本质为 json 文件，更改文件拓展名只为方便过滤可导入的文件。
打包工具为 maven javafx:jlink 插件 + jpackage ，使用 jdk 版本为 Amazon Corretto 21.0.6 。

## 项目背景
开发这个项目主要目的是为了 ios 手游自动操作，由于 ios 没有模拟器，目前通过 macOS 的 iPhone 连接手机，然后使用 macOS 的鼠标进行操作，也就可以通过自动操作工具来控制 iPhone 。
由于 macOS 上免费且容易使用的自动操作工具不是很多，所以就决定自己开发一个。

## 如何打包
在 maven 依赖都下载完毕后需要对 log4j-api-2.24.2.jar 、 log4j-core-2.24.2.jar 这几个不支持模块化的包进行模块化注入，具体方法可参考： https://blog.csdn.net/weixin_44167999/article/details/135753822 

在注入模块化后即可使用 maven javafx:jlink 插件进行打包，打包后的程序文件在 ../target/app 中，其中启动文件为 ../app/bin 目录下的 app 脚本， win 系统为 bat 脚本， macOS 为不带拓展名的可执行文件。
因为程序启动需要读取配置文件，需要将 src/main/resources/priv/koishi/pmc/config 文件夹和 log4j2.xml 文件复制到程序启动文件 app 所在目录下。

因为自动操作工具需要监听全局键盘与鼠标事件，所以项目中引入了 jnativehook 来实现，打包需要将 jnativehook-2.2.2.jar 所在文件夹下的 JNativeHook.x86_64.dll（win系统） 复制到 ../app/bin/ 下并更名为 JNativeHook.dll ;
libJJNativeHook.x86_64.dylib （macOS） 复制到 ../Contents/MacOS/ 下并更名为 libJJNativeHook.dylib 。

win系统直接双击 app.bat 即可运行， macOS 需要修改 app 文件在最后一行前加入 cd $DIR ，即使用 cd 命令打开程序所在目录才可使用脚本启动。
程序逻辑部分在 ../app/lib 目录中，后续更新只需替换 lib 文件夹即可。

在使用 jlink 打包后可使用项目中 jpackage 文件中的命令将 jlink 打包产物转换为各操作系统下的常规可执行文件， win 系统为 .exe 文件， macOS 为 .app 文件。
需要将各操作系统对应的可执行文件对应的图标复制到 ../target/ 目录， win 系统为 .ico 文件， macOS 为 .icns 文件。
之后在命令行进入 ../target/ 目录下执行对应操作系统的 jpackage 命令即可生成对应操作系统下的可执行文件。

jpackage 打包后 win 系统可直接使用 .exe 文件运行， macOS 需要将 libJJNativeHook.dylib 文件复制到 Perfect Mouse Control.app/Contents/app/ 目录下。

需要注意 macOS 只能在应用程序文件夹下运行，且需要开启辅助操作权限。如果开启辅助操作权限仍然无法启动程序，需要将 Perfect Mouse Control.app 从辅助操作权限移除后再重新添加并开启。

# 项目地址
GitHub：https://github.com/Asukkaa/PMC

Gitee：https://gitee.com/wowxqt/pmc