<h1>Tools</h1>

## 项目简介
本项目是一个用 javafx 开发的工具集合，可以按照设置批量重命名文件、将图片按照名称与设置批量插入 excel 、按照 excel 分组与查询设置统计并导出文件信息、将指定目录下文件信息导出到excel中。
可以编辑自动操作流程或录制自动操作流程，支持导入导出自动操作流程，支持循环自动操作。
自动流程文件为 .pmc 文件，本质为 json 文件，更改文件拓展名只为方便过滤可导入的文件。
其中 excel 相关功能是使用 apache poi 实现的， json 文件解析与封装为 jackson 实现，自动流程录制相关监听为 jnativehook 实现。
打包工具为 maven javafx:jlink 插件，使用 jdk 版本为 Amazon Corretto 21.0.6 。

## 项目背景
开发这个项目主要目的是为了辅助资产审计业务中统计资产时资产信息归档的工作，目前遇到的资产审计业务需要给资产贴上资产标签并拍照归档。
当前业务资产标签为递增编号，一个资产会有多张照片，资产审计业务要求是相同资产的照片名称需要用对应资产编码作为前缀，后缀统一用指定分隔符加数字命名。
资产照片后续需要插入 excel 报表中进行储存和交付，批量插入照片功能需要对照片位置大小以及对应数据进行可视化设置，需要根据 excel 模板进行文件名匹配后再插入。

目前网络上大多数重命名程序都不直接支持或则需要收费支持可视化配置带后缀的文件名，大多都是用正则表达式实现类似功能，而负责审计工作的工作人员大多不具有较高的计算机知识，无法使用过于复杂的正则表达式，所以批量重命名功能就诞生了。
而读取文件信息与统计文件信息都是批量向 excel 插入的基础功能，也单独提供界面供用户使用。

## 如何打包
在 maven 依赖都下载完毕后需要对 SparseBitSet-1.3.jar 、 commons-math3-3.6.1.jar 、 log4j-api-2.24.2.jar 、 log4j-core-2.24.2.jar 这几个不支持模块化的包进行模块化注入，具体方法可参考： https://blog.csdn.net/weixin_44167999/article/details/135753822 

在注入模块化后即可使用 maven javafx:jlink 插件进行打包，打包后的程序文件在 ../target/app 中，其中启动文件为 ../app/bin 目录下的 app 脚本， win 系统为 bat 脚本， macOS 为不带拓展名的 shell 可执行文件。
程序逻辑部分在 ../app/lib 目录中，后续更新只需替换 lib 文件夹即可。

因为需要向 excel 批量插入图片，本项目对内存大小有一定要求，所以提供了界面供用户自行配置 jvm 最大内存。
jvm 最大内存配置原理为使用带 jvm 参数的 app 脚本启动程序，程序可对 app 脚本中 jvm 参数进行配置。

win 系统下程序最终打包为 exe 文件，直接将 app.bat 转换为 exe 则无法再次修改 jvm 参数，所以需要另一个脚本 Tools.bat 去启动 app.bat ，之后将 Tools.bat 转换为 exe 文件即可完成打包。
bat 转 exe 工具使用的是 Bat To Exe Converter ，转换过程中 win 的安全中心报毒为正常现象信任即可。

macOS 的 app 文件本质为文件夹，无需额外封装，所以可直接对启动脚本进行编辑，无需使用启动脚本的启动脚本这么绕的启动方式打包，只需替换项目中的 Tools.app 文件夹下的 lib 文件夹即可更新程序，其中 Info.plist 为 app 的基础信息，可进行版本号和文件名等基础信息的配置。

因为自动操作工具需要监听全局键盘与鼠标时间，使用 jnativehook 实现，打包需要将 jnativehook-2.2.2.jar 所在文件夹下的 JNativeHook.x86_64.dll（win系统） 复制到 ../app/bin/ 下并更名为 JNativeHook.dll ;
libJJNativeHook.x86_64.dylib （macOS） 复制到 ../Contents/MacOS/ 下并更名为 libJJNativeHook.dylib 。
macOS下只能在应用程序文件夹下运行，且需要开启辅助操作权限。

# 项目地址
GitHub：https://github.com/Asukkaa/Tools

Gitee：https://gitee.com/wowxqt/tools