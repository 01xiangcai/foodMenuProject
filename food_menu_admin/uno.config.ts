import {
  defineConfig,
  presetAttributify,
  presetIcons,
  presetUno,
  transformerDirectives
} from 'unocss';

export default defineConfig({
  presets: [
    presetUno(),
    presetAttributify(),
    presetIcons({
      scale: 1.2,
      extraProperties: {
        display: 'inline-block'
      }
    })
  ],
  theme: {
    colors: {
      primary: '#14b8ff',
      secondary: '#7c3aed',
      accent: '#00ffb3'
    }
  },
  transformers: [transformerDirectives()]
});

