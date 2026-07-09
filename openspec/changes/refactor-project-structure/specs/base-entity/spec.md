## ADDED Requirements

### 需求:所有业务实体必须继承 BaseEntity

所有业务模块的数据库实体类（XxxDO）必须继承 `com.hrms.common.entity.BaseEntity` 基类，以获得统一的公共字段定义。

#### 场景:创建员工实体

- **当** 开发者在 employee 模块创建 EmployeeDO 实体类
- **那么** 该类必须继承 BaseEntity，自动获得 id、createBy、createTime、updateBy、updateTime、isDeleted 字段

#### 场景:创建系统实体

- **当** 开发者在 auth 模块创建 SysUserDO 实体类
- **那么** 该类必须继承 BaseEntity，自动获得公共字段

### 需求:BaseEntity 必须包含标准公共字段

BaseEntity 基类必须包含以下公共字段，所有字段使用驼峰命名：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| createBy | Long | 创建人 ID |
| createTime | LocalDateTime | 创建时间 |
| updateBy | Long | 更新人 ID |
| updateTime | LocalDateTime | 更新时间 |
| isDeleted | Integer | 逻辑删除标记（0=未删除，1=已删除） |

#### 场景:BaseEntity 字段完整性

- **当** 查阅 BaseEntity 类定义
- **那么** 必须包含 id、createBy、createTime、updateBy、updateTime、isDeleted 六个字段

### 需求:实体类必须配合 MyBatis-Plus 自动填充注解

实体类的公共字段必须使用 `@TableField(fill = FieldFill.INSERT)` 或 `@TableField(fill = FieldFill.INSERT_UPDATE)` 注解，以支持 MyBatis-Plus 自动填充。

#### 场景:新增时自动填充

- **当** 调用 mapper.insert() 保存实体
- **那么** createBy、createTime、updateBy、updateTime 字段自动填充当前用户和时间

#### 场景:更新时自动填充

- **当** 调用 mapper.updateById() 更新实体
- **那么** updateBy、updateTime 字段自动填充当前用户和时间