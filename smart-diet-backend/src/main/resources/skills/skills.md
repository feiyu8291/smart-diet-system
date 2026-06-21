# 智能减肥健康食谱与体重管理系统 - 开发技能与代码规范规范说明 (Skills)

本项目后端严格遵循以下核心设计模式、数据库协作与代码编写规范。在后续的功能迭代与业务开发中，所有开发人员（与 AI
代理）必须贯彻执行这些标准，以保证系统架构的纯净度、高可维护性与安全性。

---

## 1. 统一接口响应规范 (API Results)

所有控制器（Controller）暴露给前端的 HTTP 接口，其返回值必须统一使用 `Result<T>` 泛型类进行包裹，禁止直接返回裸数据或 Map。

### 1.1 状态码定义 (`ResultCode`)

- `200` (SUCCESS)：操作成功。
- `400` (PARAM_ERROR)：客户端传参错误。
- `401` (UNAUTHORIZED)：未登录或 Token 失效。
- `403` (FORBIDDEN)：已登录但无此接口或菜单的访问权限。
- `500` (FAILED)：系统内部繁忙/异常。

### 1.2 规范使用示例

```java
@RestController
@RequestMapping("/api/example")
public class ExampleController {
    
    // 成功返回
    @GetMapping("/get")
    public Result<ExampleVO> getDetail(@RequestParam Long id) {
        ExampleVO vo = exampleService.getDetailById(id);
        return Result.success(vo);
    }
    
    // 失败/异常处理由全局拦截器自动捕获并封装为 Result.failed(code, message)
}
```

---

## 2. PO / DTO / VO 模型流转与职责界定

为避免数据库实体结构直接暴露给前台（防止数据穿透与强耦合），项目中的模型层次关系有着严格的职责定义，并且不能混用：

```text
    [ 前端请求 ] 
        │
        ▼ (PO / Param)
   【 Controller 】 
        │
        ▼ (DTO)
    【 Service 】 ───► [ 业务流转 & 存储处理 ]
        │
        ▼ (Entity / PO)
    【 Mapper 】 ───► [ 数据库 I/O ]
        │
        ▼ (Entity / PO)
    【 Service 】
        │
        ▼ (VO)
   【 Controller 】
        │
        ▼ (VO)
    [ 前端响应 ]
```

### 2.1 模型定义规则

- **PO (Persistent Object) / Entity**：数据库映射实体类（如 `SysUser`, `Dish`）。Controller 接收前端传入的简单请求数据参数时，可直接用其作为参数载体。
- **DTO (Data Transfer Object)**：业务存储、计算与层间流转数据。所有新增/修改等复杂业务接口、配餐和计算服务，传参流转时必须封装为对应的
  DTO（如 `MealPlanSaveDTO`），并在服务层做中转存储。
- **VO (Value Object / View Object)**：视图展示数据。所有 Controller 返回给前端的 `Result<T>`，其内部的泛型 `T` 必须为对应的
  VO 对象（如 `LoginVO`, `DishVO`），VO 中仅包含前端展示需要的字段。

---

## 3. Swagger 注释与 MyBatis 字段映射规范

所有实体类（PO/Entity）和 DTO/VO 在开发时必须配备详尽的说明注释，以便自动生成接口文档与维护 SQL 映射：

### 3.1 注解规范

- **类声明**：在类上使用 `@Schema(description = "...")` 说明该实体/传输对象的业务用途。
- **字段属性**：
    - 在每一个字段属性上，必须使用 `@Schema(description = "...")` 标注具体的中文字段描述与说明。
    - 在 PO 实体类的字段上，必须使用 MyBatis-Plus 的 `@TableField(value = "...")` 显式指定该属性所映射的底层数据库列名（蛇形命名，如
      `real_name`），即使采用驼峰自动转换，也必须声明。
    - 主键 ID 统一标注 `@TableId(value = "...", type = IdType.AUTO)`。

### 3.2 实体类规范代码结构

```java
@Schema(description = "用户信息表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user")
public class SysUser extends BaseEntity {

    @TableId(value = "user_id", type = IdType.AUTO)
    @Schema(description = "用户主键ID")
    private Long userId;

    @TableField(value = "real_name")
    @Schema(description = "真实姓名")
    private String realName;

    @TableField(value = "use_status")
    @Schema(description = "是否禁用 (0-否, 1-是)")
    private Integer useStatus;
}
```

---

## 4. 公共审计字段与自动填充设计 (Auditing)

为维护干净的审计日志，避免在每次增删改查时手动 `setCreateTime` 或 `setUpdateBy`，所有的持久层 PO 实体类必须继承自
`BaseEntity` 公共基类：

### 4.1 `BaseEntity` 结构

- 包含公共的五个核心审计字段：`delFlag` (逻辑删除，配置 `@TableLogic`)，`createBy`，`updateBy`，`createTime`，`updateTime`。

### 4.2 自动填充原理 (`MyMetaObjectHandler`)

- 在字段上配置 `fill = FieldFill.INSERT` 或是 `fill = FieldFill.INSERT_UPDATE`。
- 在 `MyMetaObjectHandler` 中，新增操作时，系统自动提取 `SecurityContextHolder` 中当前在线用户的姓名填充入 `createBy` 和
  `updateBy`，并获取 `LocalDateTime.now()` 填充 `createTime` 与 `updateTime`；更新操作时，自动强制填充 `updateBy` 和
  `updateTime`。

---

## 5. 全局异常拦截机制 (Global Exception Handling)

系统所有的业务错误和不可控异常，统一由 `GlobalExceptionHandler` 捕获拦截，包装为标准 API 格式返回给前端：

- **业务逻辑异常**：在 Service 层遇到非法操作（如“余额不足”、“菜谱冲突”等）时，直接抛出
  `throw BusinessException.withMessage("错误内容")` 或指定 `ResultCode` 的异常。拦截器会自动捕获并返回 `400` 或 `500`
  状态码的 `Result`。
- **系统底层异常**：未捕获的运行时异常（`RuntimeException`, `SQLException` 等）会被拦截并打印 Error 日志，然后返回
  `Result.failed("系统繁忙，请稍后再试")`，以防止服务器内部 SQL 结构或敏感信息泄漏。

---

## 6. Flyway 数据库脚本命名与职责拆分规范

数据库变更统一由 Flyway 控制，脚本文件存放在 `src/main/resources/db/migration/` 中，文件命名遵循职责分离原则：

- **结构定义脚本 (DDL)**：
    - **命名规范**：`V1.0.X__create_[模块/对象名].sql`
    - **职责**：专门用于表的创建 (`CREATE TABLE`)、修改列属性、增加索引等结构变更。该文件中**禁止**包含任何 `INSERT INTO`
      数据插入语句。
- **数据初始化脚本 (DML)**：
    - **命名规范**：`V1.0.X__init_insert_[数据内容说明].sql` 或 `V1.0.X__init_update_[数据说明].sql`
    - **职责**：专门用于插入系统的预置初始静态数据（如初始食材、默认菜系、超级管理员角色、初始菜单资源树关联等）。

---

## 7. 配置文件 YML 多环境拆分规范

项目采用 `application.yml` 多环境组合方案，以在开发与容器化部署中实现无缝切换：

- **`application.yml` (基础公共)**：配置通用的参数（MyBatis-Plus 逻辑删除值、Quartz 的 JDBC 持久化调度数据源参数、Jackson
  序列化），并缺省配置 `spring.profiles.active: dev`。
- **`application-dev.yml` (本地开发)**：面向本地开发人员。数据库的 URL 账号密码、对象存储连接、Redis 均配置为直连本地
  localhost 的固定硬编码参数，方便直接在 IDE 中一键运行。
- **`application-pro.yml` (容器生产)**：面向 Docker / K8s 部署。所有的配置项全部从容器内的环境变量（如 `${DB_HOST}`、
  `${MINIO_ACCESS_KEY}`）中动态注入，方便 Docker Compose 联合拉起时做整体的环境重写。
