package com.hrms.common.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.hrms.common.security.SecurityContextHolder;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器。
 *
 * <p>统一处理实体类公共字段的自动填充，包括：</p>
 * <ul>
 *   <li>新增时：自动填充 createBy、createTime、updateBy、updateTime</li>
 *   <li>更新时：自动填充 updateBy、updateTime</li>
 * </ul>
 *
 * <p>此处理器放在 hrms-common.handler 包，作为全局唯一实现，
 * 禁止各业务模块单独实现，避免 Bean 冲突。</p>
 *
 * <p>注意：定时任务或异步线程中调用时，用户 ID 将为 null。</p>
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 新增时自动填充。
     *
     * <p>填充字段：createBy、createTime、updateBy、updateTime</p>
     *
     * @param metaObject 元数据对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        Long userId = SecurityContextHolder.getUserId();
        LocalDateTime now = LocalDateTime.now();

        // 填充创建信息
        this.strictInsertFill(metaObject, "createBy", Long.class, userId);
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);

        // 同时填充更新信息（新增时也是第一次更新）
        this.strictInsertFill(metaObject, "updateBy", Long.class, userId);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
    }

    /**
     * 更新时自动填充。
     *
     * <p>填充字段：updateBy、updateTime</p>
     *
     * @param metaObject 元数据对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        Long userId = SecurityContextHolder.getUserId();
        LocalDateTime now = LocalDateTime.now();

        // 填充更新信息
        this.strictUpdateFill(metaObject, "updateBy", Long.class, userId);
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, now);
    }
}