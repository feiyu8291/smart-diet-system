/**
 * 全局统一配置
 */

// 接口请求基准路径，若未在环境变量中配置，则兜底回退至本地端口 8000
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8000';
