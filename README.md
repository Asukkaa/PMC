<h1>PMC</h1>

## 项目简介
本项目是一个用 javafx 开发的自动操作工具。
可以编辑自动操作流程或录制自动操作流程，支持导入导出自动操作流程，支持循环自动操作。
自动流程文件为 .pmc 文件，本质为 json 文件，更改文件拓展名只为方便过滤可导入的文件。
打包工具为 maven javafx:jlink 插件，使用 jdk 版本为 Amazon Corretto 21.0.6 。

## 项目背景
开发这个项目主要目的是为了 ios 手游自动操作，由于 ios 没有模拟器，目前通过 macOS 的 iPhone 连接手机，然后使用 macOS 的鼠标进行操作，也就可以通过自动操作工具来控制 iPhone 。
由于 macOS 上免费且容易使用的自动操作工具不是很多，所以就决定自己开发一个。

## 如何打包
在 maven 依赖都下载完毕后需要对 log4j-api-2.24.2.jar 、 log4j-core-2.24.2.jar 这几个不支持模块化的包进行模块化注入，具体方法可参考： https://blog.csdn.net/weixin_44167999/article/details/135753822 

在注入模块化后即可使用 maven javafx:jlink 插件进行打包，打包后的程序文件在 ../target/app 中，其中启动文件为 ../app/bin 目录下的 app 脚本， win 系统为 bat 脚本， macOS 为不带拓展名的 shell 可执行文件。
程序逻辑部分在 ../app/lib 目录中，后续更新只需替换 lib 文件夹即可。

因为自动操作工具需要监听全局键盘与鼠标时间，使用 jnativehook 实现，打包需要将 jnativehook-2.2.2.jar 所在文件夹下的 JNativeHook.x86_64.dll（win系统） 复制到 ../app/bin/ 下并更名为 JNativeHook.dll ;
libJJNativeHook.x86_64.dylib （macOS） 复制到 ../Contents/MacOS/ 下并更名为 libJJNativeHook.dylib 。
macOS下只能在应用程序文件夹下运行，且需要开启辅助操作权限。

# 项目地址
GitHub：https://github.com/Asukkaa/PMC

Gitee：https://gitee.com/wowxqt/pmc

'/Applications/Perfect Mouse Control.app/Contents/runtime/Contents/Home'