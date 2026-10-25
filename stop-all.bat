@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion
title 星光国际影城 CMS 一键停止
cd /d F:\AI_full-stack_project\CMS

echo ============================================
echo   星光国际影城 CMS 一键停止（保留 MySQL / Redis）
echo ============================================
echo.

echo [1/3] 停止后端 (8080)...
for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":8080" ^| findstr "LISTENING"') do taskkill /F /PID %%p >nul 2>&1

echo [2/3] 停止前端 (5173)...
for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":5173" ^| findstr "LISTENING"') do taskkill /F /PID %%p >nul 2>&1

echo [3/3] 关闭启动窗口...
taskkill /F /FI "WINDOWTITLE eq CMS后端-8080" >nul 2>&1
taskkill /F /FI "WINDOWTITLE eq CMS前端-5173" >nul 2>&1

timeout /t 2 /nobreak >nul

echo.
echo 当前端口状态：
netstat -ano | findstr ":8080" | findstr "LISTENING" >nul && echo   8080 仍被占用 || echo   8080 已释放
netstat -ano | findstr ":5173" | findstr "LISTENING" >nul && echo   5173 仍被占用 || echo   5173 已释放
echo.
echo 说明：MySQL / Redis 未停止，避免影响本机其它项目；
echo       如需彻底停止请用 phpStudy / 任务管理器手动关闭。
pause
