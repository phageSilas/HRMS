## ADDED Requirements

### 需求:hrms-system 必须包含四个子模块目录

hrms-system 模块必须包含 auth、organization、file、log 四个子模块目录。

#### 场景:hrms-system 目录结构完整

- **当** 检查 hrms-system/src/main/java/com/hrms/system 目录
- **那么** 包含 auth、organization、file、log 四个子目录

### 需求:hrms-business 必须包含六个子模块目录

hrms-business 模块必须包含 employee、personnel、attendance、salary、approval、mycenter 六个子模块目录。

#### 场景:hrms-business 目录结构完整

- **当** 检查 hrms-business/src/main/java/com/hrms/business 目录
- **那么** 包含 employee、personnel、attendance、salary、approval、mycenter 六个子目录

### 需求:子模块目录必须包含标准分层结构

每个子模块目录必须包含标准分层子目录：controller、service、service/impl、mapper、entity、dto、vo。部分模块可额外包含 convert、enums、config、handler 等目录。

#### 场景:auth 模块分层结构

- **当** 检查 auth 子模块目录
- **那么** 包含 controller、service、service/impl、mapper、entity、dto、vo、convert、enums 目录

#### 场景:approval 模块分层结构

- **当** 检查 approval 子模块目录
- **那么** 包含 controller、service、service/impl、mapper、entity、dto、vo、convert、enums、handler 目录

#### 场景:mycenter 模块分层结构

- **当** 检查 mycenter 子模块目录
- **那么** 包含 controller、service、service/impl、dto、vo、enums 目录（无 mapper、entity）

### 需求:必须删除现有占位文件

重构前必须删除 hrms-system 和 hrms-business 中的现有占位文件，保持目录结构清晰。

#### 场景:删除 hrms-system 占位文件

- **当** 完成重构
- **那么** hrms-system 目录下的 SystemModuleController.java、SystemModuleService.java、SystemModuleServiceImpl.java 文件已被删除

#### 场景:删除 hrms-business 占位文件

- **当** 完成重构
- **那么** hrms-business 目录下的 BusinessModuleController.java、BusinessModuleService.java、BusinessModuleServiceImpl.java 文件已被删除