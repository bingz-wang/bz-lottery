import {defineConfig, loadEnv} from 'vite'
import vue from '@vitejs/plugin-vue'

function createGatewayProxy(target) {
  return {
    target,
    changeOrigin: true
  }
}

export default defineConfig(({mode}) => {
  const env = loadEnv(mode, process.cwd(), '')
  const devPort = Number(env.VITE_DEV_PORT || 9510)
  const previewPort = Number(env.VITE_PREVIEW_PORT || 9511)
  const gatewayTarget = env.VITE_GATEWAY_TARGET || 'http://localhost:9008'
  const allowAllHosts = (env.VITE_ALLOWED_HOSTS || '*').trim() === '*'
  const allowedHosts = allowAllHosts
      ? true
      : env.VITE_ALLOWED_HOSTS
          .split(',')
          .map((item) => item.trim())
          .filter(Boolean)
  const publicHost = (env.VITE_PUBLIC_HOST || '').trim()
  const publicProtocol = (env.VITE_PUBLIC_PROTOCOL || 'https').trim().toLowerCase()
  const hmrConfig = publicHost
      ? {
        host: publicHost,
        protocol: publicProtocol === 'https' ? 'wss' : 'ws',
        clientPort: publicProtocol === 'https' ? 443 : 80
      }
      : undefined

  return {
    plugins: [vue()],
    server: {
      host: '0.0.0.0',
      allowedHosts,
      port: devPort,
      strictPort: true,
      hmr: hmrConfig,
      proxy: {
        '/lottery-user': createGatewayProxy(gatewayTarget),
        '/lottery-activity': createGatewayProxy(gatewayTarget),
        '/lottery-ai': createGatewayProxy(gatewayTarget),
        '/lottery-lottery': createGatewayProxy(gatewayTarget),
        '/lottery-award': createGatewayProxy(gatewayTarget),
        '/lottery-pay': createGatewayProxy(gatewayTarget),
        '/lottery-workflow': createGatewayProxy(gatewayTarget),
        '/lottery-file': createGatewayProxy(gatewayTarget),
        '/lottery-monitor': createGatewayProxy(gatewayTarget),
        '/swagger-ui': createGatewayProxy(gatewayTarget),
        '/swagger-ui.html': createGatewayProxy(gatewayTarget),
        '/v3/api-docs': createGatewayProxy(gatewayTarget),
        '/actuator': createGatewayProxy(gatewayTarget)
      }
    },
    preview: {
      host: '0.0.0.0',
      allowedHosts,
      port: previewPort,
      strictPort: true
    }
  }
})
