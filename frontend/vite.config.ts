import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  
  // 路径别名配置
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  
  // 开发服务器配置
  server: {
    port: 3000,
    host: '0.0.0.0', // 允许外部访问
    open: false, // 不自动打开浏览器
    // API 代理配置（根据需要启用）
    proxy: {
      '/api': {
        target: process.env.VITE_PROXY_TARGET || 'http://localhost:8080',
        changeOrigin: true,
        // 如果不需要重写路径，注释掉下面这行
        // rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  },
  
  // 构建优化配置
  build: {
    outDir: 'dist',
    sourcemap: false, // 生产环境不生成 sourcemap
    // 代码分割优化
    rollupOptions: {
      output: {
        manualChunks: {
          // 将 React 相关库单独打包
          'react-vendor': ['react', 'react-dom'],
          // 将 Ant Design 单独打包
          'antd-vendor': ['antd', '@ant-design/icons'],
          // 将 Markdown 相关库单独打包
          'markdown-vendor': [
            'react-markdown',
            'remark-gfm',
            'remark-math',
            'rehype-katex',
            'rehype-highlight',
          ],
        },
      },
    },
    // 调整 chunk 大小警告的限值
    chunkSizeWarningLimit: 1000,
    // 压缩配置
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true, // 生产环境移除 console
        drop_debugger: true,
      },
    },
  },
  
  // 预构建优化
  optimizeDeps: {
    include: [
      'react',
      'react-dom',
      'antd',
      '@ant-design/icons',
      'react-markdown',
      'highlight.js',
      'katex',
    ],
  },
})