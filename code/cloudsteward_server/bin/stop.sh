#!/bin/bash

# 安全停止函数（精确匹配进程名）
safe_kill() {
    local process_names=("xjar" "cloudsteward-server.xjar" "cloudsteward-server.jar")
    local found=0

    for name in "${process_names[@]}"; do
        # 使用数组来存储所有匹配的PID
        pids=($(pgrep -f "$name"))
        if [ ${#pids[@]} -gt 0 ]; then
            found=1
            echo "停止进程: $name (PIDs: ${pids[@]})"
            # 逐个杀死进程
            for pid in "${pids[@]}"; do
                kill -9 "$pid"
            done
        fi
    done

    if [ "$found" -eq 0 ]; then
        echo "未找到运行中的服务进程，无需停止"
    else
        echo "所有服务进程已停止"
    fi
}

# 主流程
safe_kill