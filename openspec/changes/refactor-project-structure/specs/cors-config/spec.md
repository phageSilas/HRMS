## ADDED Requirements

### 需求:CorsConfig 必须允许前端跨域调用

跨域配置 `CorsConfig` 必须允许前端（React 应用）跨域调用后端接口。

#### 场景:前端跨域调用成功

- **当** 前端应用从 localhost:3000 调用后端 localhost:8080 的接口
- **那么** 浏览器不拦截请求，接口正常返回数据

#### 场景:预检请求通过

- **当** 前端发送 OPTIONS 预检请求
- **那么** 返回 200 状态码，携带正确的 CORS 响应头

### 需求:CorsConfig 必须配置标准 CORS 响应头

CorsConfig 必须配置以下 CORS 响应头：

| 响应头 | 值 |
|--------|------|
| Access-Control-Allow-Origin | *（开发环境）或指定域名（生产环境） |
| Access-Control-Allow-Methods | GET, POST, PUT, DELETE, PATCH, OPTIONS |
| Access-Control-Allow-Headers | Authorization, Content-Type |
| Access-Control-Allow-Credentials | true |

#### 场景:CORS 响应头完整

- **当** 检查 CorsConfig 配置
- **那么** 包含 Access-Control-Allow-Origin、Access-Control-Allow-Methods、Access-Control-Allow-Headers、Access-Control-Allow-Credentials 配置

### 需求:CorsConfig 必须在 hrms-server 中实现

CorsConfig 配置类必须在 hrms-server.config 包中实现，作为启动聚合模块的一部分。

#### 场景:配置类位置正确

- **当** 检查 CorsConfig 文件位置
- **那么** 文件路径为 hrms-server/src/main/java/com/hrms/server/config/CorsConfig.java