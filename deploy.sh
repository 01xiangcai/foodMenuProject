#!/bin/bash

###############################################################################
# 家宴菜单系统 - 一键部署脚本
# 适用于 Ubuntu 20.04+ / Debian 10+
# 使用方法：sudo ./deploy.sh
###############################################################################

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 配置变量（请根据实际情况修改）
PROJECT_NAME="food-menu"
BACKEND_PORT=8080
DB_NAME="food_menu"
DB_USER="food_menu_user"
DB_PASSWORD="Change_This_Password_123!"  # ⚠️ 请修改为强密码
DOMAIN="your-domain.com"  # ⚠️ 请修改为您的域名或服务器IP

# OSS 配置（请修改为您的实际配置）
OSS_ENDPOINT="https://your-bucket.oss-cn-region.aliyuncs.com"
OSS_ACCESS_KEY="YOUR_ACCESS_KEY_ID"
OSS_SECRET_KEY="YOUR_ACCESS_KEY_SECRET"
OSS_BUCKET="your-bucket-name"

# 目录配置
BACKEND_DIR="/opt/food-menu"
FRONTEND_DIR="/var/www/food-menu-admin"
BACKUP_DIR="/opt/backup"

###############################################################################
# 工具函数
###############################################################################

print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

check_root() {
    if [ "$EUID" -ne 0 ]; then 
        print_error "请使用 sudo 运行此脚本"
        exit 1
    fi
}

###############################################################################
# 步骤 1: 检查系统环境
###############################################################################

check_system() {
    print_info "检查系统环境..."
    
    # 检查操作系统
    if [ -f /etc/os-release ]; then
        . /etc/os-release
        OS=$NAME
        VER=$VERSION_ID
        print_info "操作系统: $OS $VER"
    else
        print_error "无法识别操作系统"
        exit 1
    fi
    
    # 检查是否为 Ubuntu/Debian
    if [[ ! "$OS" =~ "Ubuntu" ]] && [[ ! "$OS" =~ "Debian" ]]; then
        print_warn "此脚本主要针对 Ubuntu/Debian，其他系统可能需要手动调整"
    fi
}

###############################################################################
# 步骤 2: 安装依赖软件
###############################################################################

install_dependencies() {
    print_info "更新系统并安装依赖..."
    
    apt update
    apt upgrade -y
    
    # 安装基础工具
    apt install -y curl wget git unzip software-properties-common
    
    # 安装 Java 17
    print_info "安装 Java 17..."
    apt install -y openjdk-17-jdk
    java -version
    
    # 安装 MySQL
    print_info "安装 MySQL..."
    apt install -y mysql-server
    
    # 安装 Nginx
    print_info "安装 Nginx..."
    apt install -y nginx
    
    # 安装 Certbot（用于 HTTPS）
    print_info "安装 Certbot..."
    apt install -y certbot python3-certbot-nginx
    
    print_info "所有依赖安装完成！"
}

###############################################################################
# 步骤 3: 配置 MySQL
###############################################################################

setup_mysql() {
    print_info "配置 MySQL 数据库..."
    
    # 启动 MySQL
    systemctl start mysql
    systemctl enable mysql
    
    # 创建数据库和用户
    mysql -e "CREATE DATABASE IF NOT EXISTS ${DB_NAME} DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    mysql -e "CREATE USER IF NOT EXISTS '${DB_USER}'@'localhost' IDENTIFIED BY '${DB_PASSWORD}';"
    mysql -e "GRANT ALL PRIVILEGES ON ${DB_NAME}.* TO '${DB_USER}'@'localhost';"
    mysql -e "FLUSH PRIVILEGES;"
    
    print_info "MySQL 配置完成！"
    print_warn "数据库名: ${DB_NAME}"
    print_warn "数据库用户: ${DB_USER}"
    print_warn "数据库密码: ${DB_PASSWORD}"
}

###############################################################################
# 步骤 4: 创建目录结构
###############################################################################

create_directories() {
    print_info "创建项目目录..."
    
    mkdir -p ${BACKEND_DIR}
    mkdir -p ${FRONTEND_DIR}
    mkdir -p ${BACKUP_DIR}
    
    # 设置权限
    chown -R www-data:www-data ${BACKEND_DIR}
    chown -R www-data:www-data ${FRONTEND_DIR}
    
    print_info "目录创建完成！"
}

###############################################################################
# 步骤 5: 配置后端服务
###############################################################################

setup_backend() {
    print_info "配置后端服务..."
    
    # 创建生产配置文件
    cat > ${BACKEND_DIR}/application-prod.yml << EOF
server:
  port: ${BACKEND_PORT}

spring:
  application:
    name: food_menu
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/${DB_NAME}?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: ${DB_USER}
    password: ${DB_PASSWORD}
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: GMT+8

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
    map-underscore-to-camel-case: true

# 生产环境关闭 Swagger
knife4j:
  enable: false
springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false

aliyun:
  oss:
    endpoint: ${OSS_ENDPOINT}
    access-key-id: ${OSS_ACCESS_KEY}
    access-key-secret: ${OSS_SECRET_KEY}
    bucket-name: ${OSS_BUCKET}
    folder: food-menu/
    avatar-folder: avatars/
    presigned-url-expiration-hours: 24
EOF
    
    # 创建 systemd 服务文件
    cat > /etc/systemd/system/food-menu.service << EOF
[Unit]
Description=Food Menu Backend Service
After=network.target mysql.service

[Service]
Type=simple
User=www-data
WorkingDirectory=${BACKEND_DIR}
ExecStart=/usr/bin/java -Xms512m -Xmx1024m -jar ${BACKEND_DIR}/food_menu.jar --spring.profiles.active=prod --spring.config.additional-location=${BACKEND_DIR}/application-prod.yml
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOF
    
    print_info "后端服务配置完成！"
    print_warn "请将 JAR 包上传到: ${BACKEND_DIR}/food_menu.jar"
}

###############################################################################
# 步骤 6: 配置 Nginx
###############################################################################

setup_nginx() {
    print_info "配置 Nginx..."
    
    # 创建 Nginx 配置
    cat > /etc/nginx/sites-available/food-menu << 'EOF'
upstream backend_api {
    server 127.0.0.1:8080;
}

server {
    listen 80;
    server_name DOMAIN_PLACEHOLDER;
    
    # 前端静态资源
    location / {
        root /var/www/food-menu-admin;
        try_files $uri $uri/ /index.html;
        
        # 静态资源缓存
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
            expires 1y;
            add_header Cache-Control "public, immutable";
        }
    }
    
    # API 代理
    location /api/ {
        proxy_pass http://backend_api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        
        client_max_body_size 20M;
    }
    
    # Gzip 压缩
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css text/xml text/javascript application/javascript application/xml+rss application/json;
}
EOF
    
    # 替换域名
    sed -i "s/DOMAIN_PLACEHOLDER/${DOMAIN}/g" /etc/nginx/sites-available/food-menu
    
    # 启用站点
    ln -sf /etc/nginx/sites-available/food-menu /etc/nginx/sites-enabled/
    rm -f /etc/nginx/sites-enabled/default
    
    # 测试配置
    nginx -t
    
    # 重启 Nginx
    systemctl restart nginx
    systemctl enable nginx
    
    print_info "Nginx 配置完成！"
}

###############################################################################
# 步骤 7: 配置防火墙
###############################################################################

setup_firewall() {
    print_info "配置防火墙..."
    
    # 安装 UFW（如果未安装）
    apt install -y ufw
    
    # 配置规则
    ufw --force enable
    ufw allow 22/tcp   # SSH
    ufw allow 80/tcp   # HTTP
    ufw allow 443/tcp  # HTTPS
    
    print_info "防火墙配置完成！"
}

###############################################################################
# 步骤 8: 配置 HTTPS
###############################################################################

setup_https() {
    print_info "配置 HTTPS..."
    
    if [ "$DOMAIN" == "your-domain.com" ]; then
        print_warn "跳过 HTTPS 配置（请先设置正确的域名）"
        return
    fi
    
    print_info "正在为 ${DOMAIN} 申请 SSL 证书..."
    certbot --nginx -d ${DOMAIN} --non-interactive --agree-tos --email admin@${DOMAIN}
    
    # 设置自动续期
    systemctl enable certbot.timer
    
    print_info "HTTPS 配置完成！"
}

###############################################################################
# 步骤 9: 创建备份脚本
###############################################################################

setup_backup() {
    print_info "创建数据库备份脚本..."
    
    cat > ${BACKUP_DIR}/backup-db.sh << EOF
#!/bin/bash
DATE=\$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="${BACKUP_DIR}"
DB_NAME="${DB_NAME}"
DB_USER="${DB_USER}"
DB_PASS="${DB_PASSWORD}"

# 创建备份
mysqldump -u \$DB_USER -p\$DB_PASS \$DB_NAME > \$BACKUP_DIR/food_menu_\$DATE.sql

# 压缩备份
gzip \$BACKUP_DIR/food_menu_\$DATE.sql

# 删除7天前的备份
find \$BACKUP_DIR -name "food_menu_*.sql.gz" -mtime +7 -delete

echo "Backup completed: food_menu_\$DATE.sql.gz"
EOF
    
    chmod +x ${BACKUP_DIR}/backup-db.sh
    
    # 添加定时任务（每天凌晨2点）
    (crontab -l 2>/dev/null; echo "0 2 * * * ${BACKUP_DIR}/backup-db.sh >> /var/log/backup.log 2>&1") | crontab -
    
    print_info "备份脚本创建完成！"
}

###############################################################################
# 步骤 10: 显示部署信息
###############################################################################

show_deployment_info() {
    echo ""
    echo "=========================================="
    echo "  部署配置完成！"
    echo "=========================================="
    echo ""
    echo "📋 系统信息："
    echo "  - 后端目录: ${BACKEND_DIR}"
    echo "  - 前端目录: ${FRONTEND_DIR}"
    echo "  - 备份目录: ${BACKUP_DIR}"
    echo ""
    echo "🗄️  数据库信息："
    echo "  - 数据库名: ${DB_NAME}"
    echo "  - 用户名: ${DB_USER}"
    echo "  - 密码: ${DB_PASSWORD}"
    echo ""
    echo "🌐 访问地址："
    echo "  - HTTP: http://${DOMAIN}"
    if [ "$DOMAIN" != "your-domain.com" ]; then
        echo "  - HTTPS: https://${DOMAIN}"
    fi
    echo ""
    echo "⚠️  接下来的步骤："
    echo ""
    echo "1. 上传后端 JAR 包："
    echo "   scp target/food_menu.jar root@${DOMAIN}:${BACKEND_DIR}/"
    echo ""
    echo "2. 导入数据库表结构："
    echo "   mysql -u ${DB_USER} -p${DB_PASSWORD} ${DB_NAME} < schema.sql"
    echo ""
    echo "3. 启动后端服务："
    echo "   sudo systemctl start food-menu"
    echo "   sudo systemctl status food-menu"
    echo ""
    echo "4. 上传前端文件："
    echo "   scp -r dist/* root@${DOMAIN}:${FRONTEND_DIR}/"
    echo ""
    echo "5. 访问系统并修改默认密码！"
    echo ""
    echo "📚 查看日志："
    echo "  - 后端日志: sudo journalctl -u food-menu -f"
    echo "  - Nginx 日志: sudo tail -f /var/log/nginx/error.log"
    echo ""
    echo "=========================================="
}

###############################################################################
# 主函数
###############################################################################

main() {
    print_info "开始部署家宴菜单系统..."
    echo ""
    
    check_root
    check_system
    
    read -p "是否继续部署？(y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_info "部署已取消"
        exit 0
    fi
    
    install_dependencies
    setup_mysql
    create_directories
    setup_backend
    setup_nginx
    setup_firewall
    setup_https
    setup_backup
    
    show_deployment_info
    
    print_info "部署脚本执行完成！"
}

# 执行主函数
main
