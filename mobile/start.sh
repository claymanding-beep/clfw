#!/bin/bash
# 健康守护体系 - 移动端开发启动脚本
# 用法：./start.sh

set -e

echo " 启动健康守护体系 - 移动端开发环境..."

cd "$(dirname "$0")"

# 检查依赖
if [ ! -d "node_modules" ]; then
  echo "📦 首次运行，安装依赖..."
  npm install
fi

# 启动开发服务器（后台运行）
echo "⚙️  编译微信小程序..."
npm run dev:mp-weixin &
DEV_PID=$!

# 等待编译完成
echo "⏳ 等待编译完成..."
for i in {1..30}; do
  if [ -d "dist/dev/mp-weixin" ] && [ -f "dist/dev/mp-weixin/app.json" ]; then
    echo "✅ 编译完成！"
    break
  fi
  sleep 1
done

# 打开微信开发者工具
if [ -d "dist/dev/mp-weixin" ]; then
  echo "📱 打开微信开发者工具..."
  open -a "wechatwebdevtools" ./dist/dev/mp-weixin 2>/dev/null || echo "⚠️  未找到微信开发者工具，请手动打开 dist/dev/mp-weixin 目录"
  echo ""
  echo "========================================="
  echo "✅ 开发环境已启动！"
  echo "   - 开发服务器 PID: $DEV_PID"
  echo "   - 编译输出：dist/dev/mp-weixin/"
  echo "   - 修改 src/ 下的文件会自动重新编译"
  echo "========================================="
  echo ""
  echo "按 Ctrl+C 停止开发服务器"
  wait $DEV_PID
else
  echo "❌ 编译失败，请检查错误信息"
  kill $DEV_PID 2>/dev/null
  exit 1
fi
