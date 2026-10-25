import js from '@eslint/js'
import pluginVue from 'eslint-plugin-vue'
import prettier from 'eslint-plugin-prettier'
import prettierConfig from 'eslint-config-prettier'
import globals from 'globals'

/**
 * ESLint 9 Flat Config
 * 规则基线：ESLint 推荐 + Vue3 基础规范 + Prettier 格式化
 * 使用：npm run lint（自动修复） / npm run lint:check（只检查）
 */
export default [
  { ignores: ['dist/**', 'node_modules/**', 'public/**', 'dev.log', 'dev_err.log'] },
  js.configs.recommended,
  ...pluginVue.configs['flat/essential'],
  prettierConfig,
  {
    files: ['**/*.{js,mjs,vue}'],
    languageOptions: {
      ecmaVersion: 'latest',
      sourceType: 'module',
      globals: {
        ...globals.browser,
        ...globals.node
      }
    },
    plugins: {
      prettier
    },
    rules: {
      // 格式化问题降级为警告，避免阻塞构建；`npm run lint` 可一键修复
      'prettier/prettier': 'warn',
      // 页面组件命名允许单词（如 Login.vue），符合本项目管理端多页面风格
      'vue/multi-word-component-names': 'off',
      'no-unused-vars': 'warn',
      'no-console': 'off',
      'vue/no-v-html': 'off'
    }
  }
]
