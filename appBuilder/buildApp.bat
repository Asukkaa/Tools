@echo off
chcp 65001 > nul

set "source=win"
set "target=..\target"
set "src=..\src"
set "bin=%target%\app\bin"
set "lib=%target%\app\lib"
set "appIcon=..\appBuilder\Tools.ico"
set "appName=Tools"
set "appMainClass=priv.koishi.tools/priv.koishi.tools.MainApplication"
set "runtimeImage=app"
set "appPath=%target%\%appName%"

::关闭正在运行的程序
echo 关闭正在运行的程序...
taskkill /f /im "%appName%.exe" >nul 2>&1

:: 解析版本号
set "javaFile=%src%\main\java\priv\koishi\tools\Finals\CommonFinals.java"
for /f "delims=" %%i in ('powershell -Command "(Select-String -Path '%javaFile%' -Pattern 'public static final String version = \x22(.*?)\x22;').Matches.Groups[1].Value"') do (
    set "appVersion=%%i"
)
:: 验证解析的版本号
if "%appVersion%" == "" (
    echo 版本号解析失败
    exit /b 1
)
echo 版本号：%appVersion%

:: 处理ZIP文件
for /r "%source%" %%F in (*.zip) do (
    set "zipPath=%%F"
    setlocal enabledelayedexpansion
    :: 使用绝对路径解压
    tar -xf "!zipPath!" -C "%bin%" --exclude="__MACOSX"
    endlocal
)

:: 复制其他非ZIP文件
robocopy "%source%" "%bin%" /E /XF *.zip
echo 已复制非ZIP文件到 [%bin%]

:: 清理旧构建
pushd "%target%"
if exist "%appName%" (
    echo 发现已存在的 [%appName%] 目录，正在清理...
    rmdir /s /q "%appName%"
)

:: 执行打包
jpackage --name "%appName%" --type app-image -m "%appMainClass%" --runtime-image "%runtimeImage%" --icon "%appIcon%" --app-version "%appVersion%" --java-options "-XX:+UseZGC"
echo 已完成 jpackage 打包

:: 生成压缩包后打开目录并选中生成的应用程序文件夹
set "libZipName=lib-%appVersion%-win.zip"
if exist "%target%\lib-*.zip" (
    echo 正在清理旧的 lib zip 文件...
    del /q "%target%\lib-*.zip"
)
if exist "%lib%" (
    :: 压缩打包生成的文件
    echo 正在生成压缩包: %libZipName%
    powershell -Command "Compress-Archive -Path '%lib%' -DestinationPath \"%target%\%libZipName%\" -Force"
    echo 压缩完成
) else (
    echo 错误：生成的应用程序目录不存在
)

:: 生成压缩包后打开目录并选中生成的应用程序文件夹
set "appZipName=%appName%-%appVersion%-win.zip"
if exist "%target%\%appName%-*.zip" (
    echo 正在清理旧的应用程序 zip 文件...
    del /q "%target%\%appName%-*.zip"
)
if exist "%appPath%" (
    :: 压缩打包生成的文件
    echo 正在生成压缩包: %appZipName%
    powershell -Command "Compress-Archive -Path '%appPath%\*' -DestinationPath \"%target%\%appZipName%\" -Force"
    echo 压缩完成
    echo 正在打开目录: %appPath%
    :: 打开目录并选中生成的应用程序文件夹
    explorer /select,"%appPath%"
) else (
    echo 错误：生成的应用程序目录不存在
)

pause
