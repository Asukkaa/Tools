@echo off
set JLINK_VM_OPTIONS=-Xmx12g
set DIR=%~dp0
"%DIR%\java" %JLINK_VM_OPTIONS% -m priv.koishi.tools/priv.koishi.tools.MainApplication %*
