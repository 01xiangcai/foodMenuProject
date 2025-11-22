const fs = require('fs');
const path = require('path');

// 检查是否安装了 canvas
let canvas, Image, ImageData;
try {
  const canvasModule = require('canvas');
  canvas = canvasModule.createCanvas;
  Image = canvasModule.Image;
  ImageData = canvasModule.ImageData;
} catch (e) {
  console.log('未安装 canvas 库，使用备用方案...');
  console.log('请运行: npm install canvas');
  console.log('或者使用在线工具生成图标');
  process.exit(1);
}

// 图标尺寸
const SIZE = 81;

// 颜色定义
const COLOR_UNSELECTED = { r: 139, g: 143, b: 163 }; // #8b8fa3
const COLOR_SELECTED = { r: 20, g: 184, b: 255 };    // #14b8ff
const COLOR_BG = { r: 10, g: 17, b: 32 };            // #0a1120

function createIcon(iconType, isActive = false) {
  const img = canvas(SIZE, SIZE);
  const ctx = img.getContext('2d');
  
  // 设置背景透明
  ctx.clearRect(0, 0, SIZE, SIZE);
  
  // 主颜色
  const mainColor = isActive ? COLOR_SELECTED : COLOR_UNSELECTED;
  const colorStr = `rgb(${mainColor.r}, ${mainColor.g}, ${mainColor.b})`;
  
  ctx.fillStyle = colorStr;
  ctx.strokeStyle = colorStr;
  
  const center = SIZE / 2;
  const radius = SIZE / 3;
  
  if (iconType === 'home') {
    // 首页图标：房子
    ctx.beginPath();
    // 屋顶
    ctx.moveTo(center, center - radius);
    ctx.lineTo(center - radius * 0.8, center - radius * 0.3);
    ctx.lineTo(center + radius * 0.8, center - radius * 0.3);
    ctx.closePath();
    ctx.fill();
    
    // 房子主体
    ctx.fillRect(
      center - radius * 0.6,
      center - radius * 0.3,
      radius * 1.2,
      radius * 1.1
    );
    
    // 门
    ctx.fillStyle = `rgb(${COLOR_BG.r}, ${COLOR_BG.g}, ${COLOR_BG.b})`;
    ctx.fillRect(
      center - radius * 0.15,
      center + radius * 0.2,
      radius * 0.3,
      radius * 0.6
    );
    
  } else if (iconType === 'menu') {
    // 点餐图标：菜单/列表
    const lineWidth = 4;
    const lineSpacing = 8;
    const startY = center - radius * 0.6;
    
    ctx.fillStyle = colorStr;
    for (let i = 0; i < 3; i++) {
      const y = startY + i * lineSpacing;
      ctx.fillRect(
        center - radius * 0.7,
        y,
        radius * 1.4,
        lineWidth
      );
    }
    
    // 添加小圆点表示菜品
    const dotY = center + radius * 0.3;
    const dotRadius = 3;
    for (const xOffset of [-radius * 0.4, 0, radius * 0.4]) {
      ctx.beginPath();
      ctx.arc(center + xOffset, dotY, dotRadius, 0, Math.PI * 2);
      ctx.fill();
    }
    
  } else if (iconType === 'profile') {
    // 我的图标：用户头像
    ctx.fillStyle = colorStr;
    
    // 头部（圆形）
    const headRadius = radius * 0.4;
    const headCenterY = center - radius * 0.2;
    ctx.beginPath();
    ctx.arc(center, headCenterY, headRadius, 0, Math.PI * 2);
    ctx.fill();
    
    // 身体（梯形）
    ctx.beginPath();
    ctx.moveTo(center - radius * 0.5, headCenterY + headRadius);
    ctx.lineTo(center + radius * 0.5, headCenterY + headRadius);
    ctx.lineTo(center + radius * 0.4, center + radius * 0.7);
    ctx.lineTo(center - radius * 0.4, center + radius * 0.7);
    ctx.closePath();
    ctx.fill();
  }
  
  // 如果选中状态，添加发光效果
  if (isActive) {
    const glowCanvas = canvas(SIZE, SIZE);
    const glowCtx = glowCanvas.getContext('2d');
    glowCtx.clearRect(0, 0, SIZE, SIZE);
    
    for (let i = 0; i < 3; i++) {
      const glowRadius = radius + 3 + i * 2;
      const alpha = (30 - i * 10) / 255;
      const glowColor = `rgba(${COLOR_SELECTED.r}, ${COLOR_SELECTED.g}, ${COLOR_SELECTED.b}, ${alpha})`;
      
      glowCtx.fillStyle = glowColor;
      glowCtx.beginPath();
      glowCtx.arc(center, center, glowRadius, 0, Math.PI * 2);
      glowCtx.fill();
    }
    
    // 合并光晕效果
    ctx.globalCompositeOperation = 'screen';
    ctx.drawImage(glowCanvas, 0, 0);
    ctx.globalCompositeOperation = 'source-over';
  }
  
  return img;
}

function main() {
  const imagesDir = path.join(__dirname, 'images');
  
  // 确保 images 目录存在
  if (!fs.existsSync(imagesDir)) {
    fs.mkdirSync(imagesDir, { recursive: true });
  }
  
  const icons = [
    { type: 'home', filename: 'tab-home' },
    { type: 'menu', filename: 'tab-menu' },
    { type: 'profile', filename: 'tab-profile' }
  ];
  
  console.log('开始生成图标...\n');
  
  icons.forEach(({ type, filename }) => {
    // 生成未选中图标
    const imgUnselected = createIcon(type, false);
    const bufferUnselected = imgUnselected.toBuffer('image/png');
    const pathUnselected = path.join(imagesDir, `${filename}.png`);
    fs.writeFileSync(pathUnselected, bufferUnselected);
    console.log(`✓ 已生成: ${pathUnselected}`);
    
    // 生成选中图标
    const imgSelected = createIcon(type, true);
    const bufferSelected = imgSelected.toBuffer('image/png');
    const pathSelected = path.join(imagesDir, `${filename}-active.png`);
    fs.writeFileSync(pathSelected, bufferSelected);
    console.log(`✓ 已生成: ${pathSelected}`);
  });
  
  console.log(`\n所有图标生成完成！`);
  console.log(`图标保存在: ${path.resolve(imagesDir)}`);
}

main();

