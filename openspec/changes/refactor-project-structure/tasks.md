## 1. 删除现有占位文件

- [ ] 1.1 删除 hrms-system 现有占位文件（controller、service、convert、domain、dto、entity、enums、mapper、vo 目录）
- [ ] 1.2 删除 hrms-business 现有占位文件（controller、service、convert、dto、entity、mapper、vo 目录及 approval、attendance、config、process、profile、salary 目录）

## 2. 创建 hrms-common 基础设施

- [ ] 2.1 创建 BaseEntity.java（com.hrms.common.entity.BaseEntity），包含 id、createBy、createTime、updateBy、updateTime、isDeleted 字段
- [ ] 2.2 创建 UserContext.java（com.hrms.common.security.UserContext），包含 userId、deptId、roleIds 字段
- [ ] 2.3 创建 SecurityContextHolder.java（com.hrms.common.security.SecurityContextHolder），实现 getUserId()、getDeptId()、getRoleIds()、setContext()、clear() 方法
- [ ] 2.4 创建 MyMetaObjectHandler.java（com.hrms.common.handler.MyMetaObjectHandler），实现 insertFill() 和 updateFill() 方法
- [ ] 2.5 创建 MybatisPlusConfig.java（com.hrms.common.config.MybatisPlusConfig），配置逻辑删除和分页插件

## 3. 创建 hrms-system 子模块目录结构

- [ ] 3.1 创建 auth 子模块目录（controller、service、service/impl、mapper、entity、dto、vo、convert、enums）
- [ ] 3.2 创建 organization 子模块目录（controller、service、service/impl、mapper、entity、dto、vo、convert、enums）
- [ ] 3.3 创建 file 子模块目录（controller、service、service/impl、mapper、entity、dto、vo、convert）
- [ ] 3.4 创建 log 子模块目录（controller、service、service/impl、mapper、entity、dto、vo、enums）

## 4. 创建 hrms-business 子模块目录结构

- [ ] 4.1 创建 employee 子模块目录（controller、service、service/impl、mapper、entity、dto、vo、convert、enums）
- [ ] 4.2 创建 personnel 子模块目录（controller、service、service/impl、mapper、entity、dto、vo、convert、enums）
- [ ] 4.3 创建 attendance 子模块目录（controller、service、service/impl、mapper、entity、dto、vo、convert、enums）
- [ ] 4.4 创建 salary 子模块目录（controller、service、service/impl、mapper、entity、dto、vo、convert、enums）
- [ ] 4.5 创建 approval 子模块目录（controller、service、service/impl、mapper、entity、dto、vo、convert、enums、handler）
- [ ] 4.6 创建 mycenter 子模块目录（controller、service、service/impl、dto、vo、enums）

## 5. 创建 hrms-server 配置

- [ ] 5.1 创建 CorsConfig.java（com.hrms.server.config.CorsConfig），配置跨域响应头

## 6. 验证地基搭建

- [ ] 6.1 验证项目能正常启动（mvn clean install）
- [ ] 6.2 验证 MyBatis-Plus 自动填充生效（插入数据时 create_time 有值）
- [ ] 6.3 验证逻辑删除生效（deleteById 执行 UPDATE 而非 DELETE）
- [ ] 6.4 验证 SecurityContextHolder 可用（能获取当前用户 ID）
- [ ] 6.5 验证 CORS 配置生效（前端可跨域调用后端接口）