#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
生成微信小程序 TabBar 图标
需要安装: pip install Pillow
"""

try:
    from PIL import Image, ImageDraw, ImageFont
    import os
except ImportError:
    print("请先安装 Pillow: pip install Pillow")
    exit(1)

# 图标尺寸
SIZE = 81

# 颜色定义
COLOR_UNSELECTED = (139, 143, 163)  # #8b8fa3 灰色
COLOR_SELECTED = (20, 184, 255)     # #14b8ff 蓝色
COLOR_BG = (10, 17, 32)              # #0a1120 背景色

def create_icon(icon_type, is_active=False):
    """创建图标"""
    # 创建画布
    img = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # 主颜色
    main_color = COLOR_SELECTED if is_active else COLOR_UNSELECTED
    
    # 绘制图标
    center = SIZE // 2
    radius = SIZE // 3
    
    if icon_type == 'home':
        # 首页图标：房子
        # 屋顶
        roof_points = [
            (center, center - radius),
            (center - radius * 0.8, center - radius * 0.3),
            (center + radius * 0.8, center - radius * 0.3)
        ]
        draw.polygon(roof_points, fill=main_color)
        # 房子主体
        house_rect = [
            center - radius * 0.6,
            center - radius * 0.3,
            center + radius * 0.6,
            center + radius * 0.8
        ]
        draw.rectangle(house_rect, fill=main_color)
        # 门
        door_rect = [
            center - radius * 0.15,
            center + radius * 0.2,
            center + radius * 0.15,
            center + radius * 0.8
        ]
        draw.rectangle(door_rect, fill=COLOR_BG)
        
    elif icon_type == 'menu':
        # 点餐图标：菜单/列表
        line_width = 4
        line_spacing = 8
        start_y = center - radius * 0.6
        for i in range(3):
            y = start_y + i * line_spacing
            draw.rectangle(
                [center - radius * 0.7, y, center + radius * 0.7, y + line_width],
                fill=main_color
            )
        # 添加小圆点表示菜品
        dot_y = center + radius * 0.3
        dot_radius = 3
        for x_offset in [-radius * 0.4, 0, radius * 0.4]:
            draw.ellipse(
                [center + x_offset - dot_radius, dot_y - dot_radius,
                 center + x_offset + dot_radius, dot_y + dot_radius],
                fill=main_color
            )
        
    elif icon_type == 'profile':
        # 我的图标：用户头像
        # 头部（圆形）
        head_radius = radius * 0.4
        head_center_y = center - radius * 0.2
        draw.ellipse(
            [center - head_radius, head_center_y - head_radius,
             center + head_radius, head_center_y + head_radius],
            fill=main_color
        )
        # 身体（半圆/梯形）
        body_points = [
            (center - radius * 0.5, head_center_y + head_radius),
            (center + radius * 0.5, head_center_y + head_radius),
            (center + radius * 0.4, center + radius * 0.7),
            (center - radius * 0.4, center + radius * 0.7)
        ]
        draw.polygon(body_points, fill=main_color)
    
    # 如果选中状态，添加发光效果
    if is_active:
        # 添加外圈光晕
        glow_img = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 0))
        glow_draw = ImageDraw.Draw(glow_img)
        for i in range(3):
            glow_radius = radius + 3 + i * 2
            alpha = 30 - i * 10
            glow_color = (*COLOR_SELECTED, alpha)
            glow_draw.ellipse(
                [center - glow_radius, center - glow_radius,
                 center + glow_radius, center + glow_radius],
                fill=glow_color
            )
        img = Image.alpha_composite(img, glow_img)
    
    return img

def main():
    """主函数"""
    # 确保 images 目录存在
    images_dir = 'images'
    if not os.path.exists(images_dir):
        os.makedirs(images_dir)
    
    # 图标类型和文件名映射
    icons = [
        ('home', 'tab-home'),
        ('menu', 'tab-menu'),
        ('profile', 'tab-profile')
    ]
    
    print("开始生成图标...")
    
    for icon_type, filename in icons:
        # 生成未选中图标
        img_unselected = create_icon(icon_type, is_active=False)
        path_unselected = os.path.join(images_dir, f'{filename}.png')
        img_unselected.save(path_unselected, 'PNG')
        print(f"✓ 已生成: {path_unselected}")
        
        # 生成选中图标
        img_selected = create_icon(icon_type, is_active=True)
        path_selected = os.path.join(images_dir, f'{filename}-active.png')
        img_selected.save(path_selected, 'PNG')
        print(f"✓ 已生成: {path_selected}")
    
    print("\n所有图标生成完成！")
    print(f"图标保存在: {os.path.abspath(images_dir)}")

if __name__ == '__main__':
    main()

