## ADDED Requirements

### 需求:MyMetaObjectHandler 必须在 hrms-common 中统一实现

MyBatis-Plus 自动填充处理器 `MyMetaObjectHandler` 必须在 hrms-common.handler 包中统一实现，禁止各业务模块单独实现。

#### 场景:禁止多实现

- **当** 检查项目代码
- **那么** 整个项目只有一个 MyMetaObjectHandler 实现类，位于 hrms-common.handler 包

### 需求:MyMetaObjectHandler 必须自动填充公共字段

MyMetaObjectHandler 必须实现 insertFill 和 updateFill 方法，自动填充公共字段：

| 字段 | 填充时机 | 填充值来源 |
|------|----------|------------|
| createBy | INSERT | SecurityContextHolder.getUserId() |
| createTime | INSERT | LocalDateTime.now() |
| updateBy | INSERT/UPDATE | SecurityContextHolder.getUserId() |
| updateTime | INSERT/UPDATE | LocalDateTime.now() |

#### 场景:新增时自动填充创建信息

- **当** 调用 mapper.insert() 保存实体
- **那么** createBy 字段自动填充当前用户 ID，createTime 字段自动填充当前时间

#### 场景:更新时自动填充更新信息

- **当** 调用 mapper.updateById() 更新实体
- **那么** updateBy 字段自动填充当前用户 ID，updateTime 字段自动填充当前时间

### 需求:MybatisPlusConfig 必须配置逻辑删除

MyBatis-Plus 配置类 `MybatisPlusConfig` 必须配置逻辑删除，将 `is_deleted` 字段作为逻辑删除标记。

#### 场景:调用 deleteById 执行逻辑删除

- **当** 调用 mapper.deleteById(id)
- **那么** 执行 UPDATE 语句设置 is_deleted = 1，不执行物理删除

#### 场景:查询时自动过滤已删除记录

- **当** 调用 mapper.selectList() 查询列表
- **那么** 自动添加 WHERE is_deleted = 0 条件，过滤已删除记录

### 需求:MybatisPlusConfig 必须配置分页插件

MybatisPlusConfig 必须配置分页插件，支持分页查询。

#### 场景:分页查询

- **当** 调用 mapper.selectPage(page, queryWrapper)
- **那么** 返回 Page 对象，包含分页数据和总记录数