# ============================================================
# 星光国际影城 CMS 前端镜像
# 构建：docker build -t cms-web:latest ./frontend
# 三端（用户/员工/管理）共用一个工程，Nginx 托管 dist + 反代 /api
# ============================================================

# ---------- 阶段 1：Node 构建 ----------
FROM node:20-alpine AS builder
WORKDIR /build

COPY package*.json ./
RUN npm ci --registry=https://registry.npmmirror.com || npm install --registry=https://registry.npmmirror.com

COPY . .
RUN npm run build

# ---------- 阶段 2：Nginx 托管 ----------
FROM nginx:1.25-alpine
COPY nginx-default.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /build/dist /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD wget -qO- http://localhost/healthz || exit 1

CMD ["nginx", "-g", "daemon off;"]
