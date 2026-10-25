@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion
title 星光国际影城 CMS 一键启动 (v1.1)
cd /d F:\AI_full-stack_project\CMS

echo ============================================
echo   星光国际影城 CMS 一键启动 v1.1
echo   后端 http://localhost:8080/api
echo   前端 http://localhost:5173
echo ============================================
echo.

echo [1/5] 检查 MySQL (3306)...
netstat -ano | findstr ":3306" | findstr "LISTENING" >nul
if errorlevel 1 (
    if exist "D:\phpstudy_pro\Extensions\MySQL8.0.12\bin\mysqld.exe" (
        echo     正在启动 phpStudy MySQL...
        start "CMS-MySQL" /min "D:\phpstudy_pro\Extensions\MySQL8.0.12\bin\mysqld.exe" --defaults-file="D:\phpstudy_pro\Extensions\MySQL8.0.12\my.ini"
        timeout /t 6 /nobreak >nul
    ) else (
        echo     [警告] 未找到 MySQL，请先用 phpStudy 手动启动
    )
) else (
    echo     MySQL 已在运行
)

echo [2/5] 检查 Redis (6379)...
netstat -ano | findstr ":6379" | findstr "LISTENING" >nul
if errorlevel 1 (
    if exist "F:\Redis\redis-server.exe" (
        echo     正在启动 Redis...
        start "CMS-Redis" /min "F:\Redis\redis-server.exe" "F:\Redis\redis.windows.conf"
        timeout /t 3 /nobreak >nul
    ) else (
        echo     [警告] 未找到 Redis，请手动启动
    )
) else (
    echo     Redis 已在运行
)

echo [3/5] 清理占用端口 8080 / 5173...
for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":8080" ^| findstr "LISTENING"') do taskkill /F /PID %%p >nul 2>&1
for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":5173" ^| findstr "LISTENING"') do taskkill /F /PID %%p >nul 2>&1

echo [4/5] 启动后端 (端口 8080)...
start "CMS后端-8080" cmd /k "cd /d F:\AI_full-stack_project\CMS\backend && mvn spring-boot:run"
echo     等待后端启动（约 20 秒）...
timeout /t 20 /nobreak >nul

echo [5/5] 启动前端 (端口 5173)...
start "CMS前端-5173" cmd /k "cd /d F:\AI_full-stack_project\CMS\frontend && npm run dev"

echo.
echo ============================================
echo   启动完成
echo   用户购票端: http://localhost:5173
echo   员工端/管理后台: 登录页切换（同一工程）
echo   账号信息请查阅项目根目录 README.md
echo ============================================
echo   关闭窗口或运行 stop-all.bat 可停止服务
pause
