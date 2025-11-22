const dishDetails = {
  'tomato-beef': {
    id: 'tomato-beef',
    name: '番茄牛腩锅',
    badge: '暖胃',
    price: 138,
    flavor: '番茄微酸 · 牛腩软糯',
    time: '45 分钟',
    energy: '高蛋白 520 kcal',
    summary: '妈妈的拿手菜，汤底浓稠，入口暖暖的甜。',
    description:
      '提前炖好的牛腩配上番茄与胡萝卜，汤底浓稠，入口带着暖暖的甜。配上手擀面或米饭都很百搭。',
    tips: ['加一小勺黄冰糖提鲜', '出锅前撒入紫苏叶清香'],
    photo:
      'https://images.unsplash.com/photo-1482049016688-2d3e1b311543?auto=format&fit=crop&w=1200&q=80',
    comments: [
      {
        author: '妈妈',
        time: '今天 17:35',
        content: '孩子放学冷了就想喝这锅汤，一直是家的味道。',
        expanded: true,
        replies: [
          { author: '可可', time: '17:40', content: '我最爱喝汤！能再加点玉米吗？' },
          { author: '爸爸', time: '17:42', content: '今晚我负责切菜，保证出锅准时。' }
        ]
      },
      {
        author: '奶奶',
        time: '昨天 19:10',
        content: '记得牛腩先焯水，汤才清亮。',
        expanded: false,
        replies: [{ author: '妈妈', time: '昨天 19:28', content: '收到，下次按奶奶的方式来。' }]
      }
    ]
  },
  'spicy-tofu': {
    id: 'spicy-tofu',
    name: '麻婆豆腐',
    badge: '轻辣',
    price: 118,
    flavor: '花椒香 · 嫩豆腐',
    time: '25 分钟',
    energy: '低脂 260 kcal',
    summary: '精选内酯豆腐配自制牛肉末，适合喜欢一点点辣的家人。',
    description:
      '豆腐先焯水去豆腥，再与自制牛肉末、花椒油、豆瓣酱翻炒，最后撒上葱花即成。配米饭超满足。',
    tips: ['关火前沿锅边淋一圈花椒油', '增加少量黑豆豉提升层次'],
    photo:
      'https://images.unsplash.com/photo-1478145046317-39f10e56b5e9?auto=format&fit=crop&w=1200&q=80',
    comments: [
      {
        author: '爸爸',
        time: '18:02',
        content: '今晚想吃这道，调味别太咸。',
        expanded: false,
        replies: [{ author: '妈妈', time: '18:05', content: 'OK，改成家常轻辣版。' }]
      }
    ]
  },
  'lemon-fish': {
    id: 'lemon-fish',
    name: '柠檬烤鱼',
    badge: '低油',
    price: 168,
    flavor: '柠檬清香',
    time: '40 分钟',
    energy: '高蛋白 330 kcal',
    summary: '无油烟烤箱菜，鱼皮酥香，柠檬汁锁住鲜嫩。',
    description:
      '整条鲈鱼处理干净，塞满香草与柠檬，包裹锡纸烤制，最后淋上柠檬汁和橄榄油，口感清爽。',
    tips: ['柠檬片夹在鱼身当中更入味', '烤前刷一层橄榄油保持水润'],
    photo:
      'https://images.unsplash.com/photo-1485921325833-c519f76c4927?auto=format&fit=crop&w=1200&q=80',
    comments: []
  },
  'pumpkin-cheese': {
    id: 'pumpkin-cheese',
    name: '芝士焗南瓜',
    badge: '甜蜜',
    price: 98,
    flavor: '奶香甜',
    time: '30 分钟',
    energy: '快乐 300 kcal',
    summary: '孩子秒空的快乐甜品，表面烤出金黄拉丝。',
    description:
      '南瓜蒸熟捣泥，加入淡奶油、马苏里拉，放入容器覆盖芝士烤至金黄。可做成星球造型，孩子更爱。',
    tips: ['撒上一点肉桂粉更像西餐甜点'],
    photo:
      'https://images.unsplash.com/photo-1504674900247-0877df9cc836?auto=format&fit=crop&w=1200&q=80',
    comments: [
      {
        author: '可可',
        time: '上周',
        content: '能不能做成星球造型？',
        expanded: false,
        replies: [{ author: '妈妈', time: '上周', content: '小宇航员的需求已记录～' }]
      }
    ]
  }
};

module.exports = {
  dishDetails
};

