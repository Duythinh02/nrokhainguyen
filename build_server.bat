@ECHO OFF
REM Build server bang JDK 21 (khong can NetBeans). Chi chay khi server DA TAT.
cd /d "%~dp0"
set JDK=C:\Java\jdk21\bin

echo Dang liet ke file nguon...
dir /s /b src\*.java > "%TEMP%\nro_sources.txt"

echo Dang bien dich...
"%JDK%\javac.exe" -encoding UTF-8 --release 17 -nowarn -proc:full ^
  -cp "lib\*" -processorpath "lib\lombok.jar" ^
  -d build\classes @"%TEMP%\nro_sources.txt"

IF ERRORLEVEL 1 (
  echo.
  echo ===== BUILD LOI - xem thong bao o tren =====
) ELSE (
  echo.
  echo ===== BUILD THANH CONG =====
)
PAUSE
