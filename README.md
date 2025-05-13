<h1>Tools</h1>

## 项目简介
本项目是一个用 javafx 开发的工具集合，可以按照设置批量重命名文件、将图片按照名称与设置批量插入 excel 、按照 excel 分组与查询设置统计并导出文件信息、将指定目录下文件信息导出到excel中。
可以编辑自动操作流程或录制自动操作流程，支持导入导出自动操作流程，支持循环自动操作。
自动流程文件为 .pmc 文件，本质为 json 文件，更改文件拓展名只为方便过滤可导入的文件。
其中 excel 相关功能是使用 apache poi 实现的， json 文件解析与封装为 jackson 实现，自动流程录制相关监听为 jnativehook 实现。
打包工具为 maven javafx:jlink 插件 + jpackage ，使用 jdk 版本为 Amazon Corretto 21.0.7 。

## 项目背景
开发这个项目主要目的是为了辅助资产审计业务中统计资产时资产信息归档的工作，目前遇到的资产审计业务需要给资产贴上资产标签并拍照归档。
当前业务资产标签为递增编号，一个资产会有多张照片，资产审计业务要求是相同资产的照片名称需要用对应资产编码作为前缀，后缀统一用指定分隔符加数字命名。
资产照片后续需要插入 excel 报表中进行储存和交付，批量插入照片功能需要对照片位置大小以及对应数据进行可视化设置，需要根据 excel 模板进行文件名匹配后再插入。

目前网络上大多数重命名程序都不直接支持或则需要收费支持可视化配置带后缀的文件名，大多都是用正则表达式实现类似功能，而负责审计工作的工作人员大多不具有较高的计算机知识，无法使用过于复杂的正则表达式，所以批量重命名功能就诞生了。
而读取文件信息与统计文件信息都是批量向 excel 插入的基础功能，也单独提供界面供用户使用。

## 如何打包
在 maven 依赖都下载完毕后需要对 SparseBitSet-1.3.jar 、 commons-math3-3.6.1.jar 、 log4j-api-2.24.3.jar 、 log4j-core-2.24.3.jar 这几个不支持模块化的包进行模块化注入，具体方法可参考： https://blog.csdn.net/weixin_44167999/article/details/135753822 

在注入模块化后即可使用 maven javafx:jlink 插件进行打包，打包后的程序文件在 ../target/app 中，其中启动文件为 ../app/bin 目录下的 app 脚本， win 系统为 bat 脚本， macOS 为不带拓展名的可执行文件。
因为程序启动需要读取配置文件，需要将 ../src/main/resources/priv/koishi/tools/config 文件夹和 log4j2.xml 文件复制到程序启动文件 app 所在目录下。

因为自动操作工具需要监听全局键盘与鼠标事件，所以项目中引入了 jnativehook 来实现，打包需要将 jnativehook-2.2.2.jar 所在文件夹下的 JNativeHook.x86_64.dll（win系统） 复制到 ../app/bin/ 下并更名为 JNativeHook.dll ;
libJJNativeHook.x86_64.dylib （macOS） 复制到 ../Contents/MacOS/ 下并更名为 libJJNativeHook.dylib 。

在使用 jlink 打包后 win 系统直接双击 app.bat 即可运行， macOS 需要修改 app 文件在最后一行前加入 cd $DIR ，即使用 cd 命令打开程序所在目录才可使用脚本启动。
程序逻辑部分在 ../app/lib 目录中，后续更新只需替换 lib 文件夹即可。

在使用 jlink 打包后可使用 jpackage 命令将 jlink 打包产物转换为各操作系统下的常规可执行文件， win 系统为 .exe 文件， macOS 为 .app 文件。
需要将各操作系统对应的可执行文件对应的图标复制到 ../target/ 目录， win 系统为 .ico 文件， macOS 为 .icns 文件。
之后在命令行进入 ../target/ 目录下执行对应操作系统的 jpackage 命令即可生成对应操作系统下的可执行文件。

jpackage 打包后 win 系统可直接使用 .exe 文件运行， macOS 需要将依赖的 .dylib 文件复制到 Tools.app/Contents/app/ 目录下。

需要注意 macOS 可能只能在应用程序文件夹下运行，且需要开启辅助操作权限。如果开启辅助操作权限仍然无法启动程序，需要将 Tools.app 从辅助操作权限列表中移除后再重新添加并开启。

如果打包后 macOS 的文件选择器 ui 为英文则需修改 Info.plist 将 CFBundleDevelopmentRegion 属性的值改为 zh_CN 。

jlink 打包后的操作都已写在 buildApp 脚本中，使用 jlink 打包后直接运行对应操作系统的 buildApp 脚本文件即可生成可执行文件。
程序的版本号相关信息将会由对应脚本从 ../src/main/java/priv/koishi/tools/CommonFinals.java 文件中的 version 属性读取，所以每次修改版本号信息时都需要修改该文件中的版本号。

jpackage 打包后如果需要修改 jvm 参数需要修改对应操作系统下的 .cfg 文件，项目中也有对应修改的代码，修改后下次启动程序即可生效。

win 的 .cfg 文件在 ../Tools/app/bin/ 目录下，macOS 的 .cfg 文件在 ../Tools/Contents/app/ 目录下。
项目中的 .cfg 文件仅供测试读取和修改功能，无法修改 idea 启动时的 jvm 参数。

修改参数只需要更改 java-options= 右侧的内容即可，如果需要添加参数则需在行末添加新的 java-options= 并在右侧写上需要的 jvm 参数，删除参数必须删除整行，只删除 java-options= 右侧的内容会导致程序无法启动，目前没有发现如何单行添加多个参数的写法。

# 项目地址
GitHub：https://github.com/Asukkaa/Tools

Gitee：https://gitee.com/wowxqt/tools