#!/bin/bash

# 第一步：检查进程是否存在
check_process() {
    local process_names=("xjar" "cloudsteward-server.xjar" "cloudsteward-server.jar")
    for name in "${process_names[@]}"; do
        if pgrep -f "$name" > /dev/null; then
            echo "系统已在运行中，无法启动。请先执行 sh stop.sh"
            exit 1
        fi
    done
}

# 第二步：加载 .env 文件（如果存在）
load_env() {
    if [ -f ".env" ]; then
        echo "加载环境变量文件: .env"
        export $(grep -v '^#' .env | xargs)
    else
        echo "未找到 .env 文件，跳过环境变量加载"
    fi
}

# 第三步：启动服务
start_service() {
    if [ -f "cloudsteward-server.xjar" ] && [ -f "xjar" ]; then
        echo "检测到 xjar 和 cloudsteward-server.xjar，使用 xjar 启动..."
        nohup ./xjar java -jar cloudsteward-server.xjar &
    elif [ -f "cloudsteward-server.jar" ]; then
        echo "检测到 cloudsteward-server.jar，直接启动..."
        nohup java -jar cloudsteward-server.jar &
    else
        echo "系统无法启动，文件不存在"
        exit 1
    fi
}

# 主流程
check_process
load_env
start_service

# 第四步：提示查看日志
echo "请使用命令查看启动日志：tail -1000f nohup.out"