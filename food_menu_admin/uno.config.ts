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
      primary: 'var(--primary-color)',
      'primary-hover': 'var(--primary-color-hover)',
      'primary-pressed': 'var(--primary-color-pressed)',
      'primary-suppl': 'var(--primary-color-suppl)',
      bg: {
        body: 'var(--bg-body)',
        card: 'var(--bg-card)',
        sidebar: 'var(--bg-sidebar)'
      },
      text: {
        primary: 'var(--text-primary)',
        secondary: 'var(--text-secondary)'
      }
    }
  },
  shortcuts: {
    'flex-center': 'flex justify-center items-center',
    'flex-between': 'flex justify-between items-center',
    'card-base': 'bg-bg-card rounded-xl shadow-sm transition-all duration-300',
    'card-hover': 'hover:shadow-lg hover:-translate-y-1',
    'glass-panel': 'glass rounded-xl',
    'btn-primary': 'bg-primary text-white hover:bg-primary-hover active:bg-primary-pressed transition-colors duration-200 rounded-lg px-4 py-2',
    'input-base': 'bg-transparent border border-gray-200 dark:border-gray-700 rounded-lg px-4 py-2 focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none transition-all duration-200'
  },
  transformers: [transformerDirectives()]
});
