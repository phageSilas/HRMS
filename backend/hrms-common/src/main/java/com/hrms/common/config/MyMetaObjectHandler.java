package com.hrms.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.hrms.common.security.SecurityContextHolder;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 元对象处理器（自动填充）
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        Long currentUserId = getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();

        // 创建人
        this.strictInsertFill(metaObject, "createBy", Long.class, currentUserId);
        // 创建时间
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        // 更新人
        this.strictUpdateFill(metaObject, "updateBy", Long.class, currentUserId);
        // 更新时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, now);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        Long currentUserId = getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();

        // 更新人
        this.strictUpdateFill(metaObject, "updateBy", Long.class, currentUserId);
        // 更新时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, now);
    }

    /**
     * 获取当前用户ID
     *
     * @return 用户ID
     */
    private Long getCurrentUserId() {
        try {
            return SecurityContextHolder.getUserId();
        } catch (Exception e) {
            // 如果不在请求上下文中，返回 null，由数据库默认值处理
            return null;
        }
    }

}
