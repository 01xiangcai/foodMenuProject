const fs = require('fs')
const path = require('path')
const PImage = require('pureimage')

const size = 160

const icons = [
  {
    name: 'home',
    files: ['tab-home.png', 'tab-home-active.png'],
    palette: {
      inactive: ['#1c2136', '#0d1120'],
      active: ['#5ee7df', '#b490ca']
    },
    draw: (ctx, color) => {
      const roofTop = size * 0.28
      const baseY = size * 0.68
      ctx.lineWidth = 10
      ctx.lineJoin = 'round'
      ctx.strokeStyle = color
      ctx.beginPath()
      ctx.moveTo(size * 0.22, baseY)
      ctx.lineTo(size * 0.5, roofTop)
      ctx.lineTo(size * 0.78, baseY)
      ctx.stroke()

      ctx.beginPath()
      ctx.moveTo(size * 0.3, baseY)
      ctx.lineTo(size * 0.3, size * 0.82)
      ctx.lineTo(size * 0.7, size * 0.82)
      ctx.lineTo(size * 0.7, baseY)
      ctx.stroke()

      ctx.beginPath()
      ctx.moveTo(size * 0.45, size * 0.82)
      ctx.lineTo(size * 0.45, size * 0.62)
      ctx.lineTo(size * 0.55, size * 0.62)
      ctx.lineTo(size * 0.55, size * 0.82)
      ctx.stroke()
    }
  },
  {
    name: 'menu',
    files: ['tab-menu.png', 'tab-menu-active.png'],
    palette: {
      inactive: ['#1f1d2a', '#0a111f'],
      active: ['#f77062', '#fe5196']
    },
    draw: (ctx, color) => {
      ctx.lineWidth = 10
      ctx.lineJoin = 'round'
      ctx.strokeStyle = color
      // cloche dome
      ctx.beginPath()
      ctx.arc(size * 0.5, size * 0.6, size * 0.25, Math.PI, 0)
      ctx.stroke()

      // base line
      ctx.beginPath()
      ctx.moveTo(size * 0.25, size * 0.65)
      ctx.lineTo(size * 0.75, size * 0.65)
      ctx.stroke()

      // handle
      ctx.beginPath()
      ctx.moveTo(size * 0.5, size * 0.3)
      ctx.lineTo(size * 0.5, size * 0.25)
      ctx.stroke()
      ctx.beginPath()
      ctx.arc(size * 0.5, size * 0.25, size * 0.06, 0, Math.PI)
      ctx.stroke()

      // sparkle
      ctx.lineWidth = 6
      ctx.beginPath()
      ctx.moveTo(size * 0.32, size * 0.35)
      ctx.lineTo(size * 0.28, size * 0.45)
      ctx.lineTo(size * 0.38, size * 0.41)
      ctx.stroke()
    }
  },
  {
    name: 'profile',
    files: ['tab-profile.png', 'tab-profile-active.png'],
    palette: {
      inactive: ['#1e1e2f', '#0f1324'],
      active: ['#7f7fd5', '#86a8e7']
    },
    draw: (ctx, color) => {
      ctx.lineWidth = 10
      ctx.strokeStyle = color
      // head
      ctx.beginPath()
      ctx.arc(size * 0.5, size * 0.38, size * 0.16, 0, Math.PI * 2)
      ctx.stroke()

      // shoulders
      ctx.beginPath()
      ctx.moveTo(size * 0.25, size * 0.78)
      ctx.quadraticCurveTo(size * 0.5, size * 0.6, size * 0.75, size * 0.78)
      ctx.stroke()
    }
  }
]

const lerp = (a, b, t) => Math.round(a + (b - a) * t)

const hexToRgb = (hex) => {
  const parsed = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex)
  return parsed
    ? [
        parseInt(parsed[1], 16),
        parseInt(parsed[2], 16),
        parseInt(parsed[3], 16)
      ]
    : [255, 255, 255]
}

const drawGradient = (ctx, [from, to]) => {
  const start = hexToRgb(from)
  const end = hexToRgb(to)
  for (let y = 0; y < size; y++) {
    const t = y / (size - 1)
    const r = lerp(start[0], end[0], t)
    const g = lerp(start[1], end[1], t)
    const b = lerp(start[2], end[2], t)
    ctx.fillStyle = `rgba(${r},${g},${b},1)`
    ctx.fillRect(0, y, size, 1)
  }
}

const drawHalo = (ctx, color) => {
  ctx.lineWidth = 6
  ctx.strokeStyle = color
  ctx.globalAlpha = 0.35
  ctx.beginPath()
  ctx.arc(size * 0.5, size * 0.5, size * 0.42, 0, Math.PI * 2)
  ctx.stroke()
  ctx.globalAlpha = 1
}

const outputDir = path.resolve(__dirname, '../src/static')

icons.forEach((icon) => {
  const [inactiveFile, activeFile] = icon.files

  ;[
    { state: 'inactive', file: inactiveFile },
    { state: 'active', file: activeFile }
  ].forEach(({ state, file }) => {
    const img = PImage.make(size, size)
    const ctx = img.getContext('2d')

    drawGradient(ctx, icon.palette[state])
    drawHalo(ctx, state === 'active' ? '#ffffff' : '#3a3f5c')

    ctx.strokeStyle = state === 'active' ? '#ffffff' : '#d1d5db'
    icon.draw(ctx, ctx.strokeStyle)

    const filePath = path.join(outputDir, file)
    PImage.encodePNGToStream(img, fs.createWriteStream(filePath)).then(
      () => {
        console.log(`Generated ${file}`)
      },
      (err) => {
        console.error(`Failed to write ${file}:`, err)
      }
    )
  })
})














