@ECHO OFF
REM Chay server bang JDK 21 (khong can NetBeans). Tu khoi dong lai neu server bi crash.
REM Muon tat han: dong cua so nay (hoac Ctrl+C roi chon Y).
cd /d "%~dp0"
title NRO Server
set JAVA=C:\Java\jdk21\bin\java.exe
set JVM_ARGS=-Xms384m -Xmx1280m -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:+UseStringDeduplication -XX:G1PeriodicGCInterval=60000 -XX:-OmitStackTraceInFastThrow -Dfile.encoding=UTF-8

:loop
echo [%date% %time%] Khoi dong server...
"%JAVA%" %JVM_ARGS% -cp "build\classes;lib\*" nro.models.server.ServerManager
echo [%date% %time%] Server da dung (ma thoat %ERRORLEVEL%). Khoi dong lai sau 15 giay...
timeout /t 15
goto loop
