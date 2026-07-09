package com.hrms.common.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;

import java.time.LocalDateTime;

/**
 * 公共字段基类。
 *
 * <p>所有业务表的实体类（XxxDO）必须继承此类，
 * 以获得统一的公共字段定义和 MyBatis-Plus 自动填充支持。</p>
 *
 * <p>公共字段包括：</p>
 * <ul>
 *   <li>id - 主键</li>
 *   <li>createBy - 创建人 ID</li>
 *   <li>createTime - 创建时间</li>
 *   <li>updateBy - 更新人 ID</li>
 *   <li>updateTime - 更新时间</li>
 *   <li>isDeleted - 逻辑删除标记</li>
 * </ul>
 */
public abstract class BaseEntity {

    /**
     * 主键 ID。
     */
    private Long id;

    /**
     * 创建人 ID。
     * 新增时由 MyBatis-Plus 自动填充当前用户 ID。
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 创建时间。
     * 新增时由 MyBatis-Plus 自动填充当前时间。
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人 ID。
     * 新增和更新时由 MyBatis-Plus 自动填充当前用户 ID。
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /**
     * 更新时间。
     * 新增和更新时由 MyBatis-Plus 自动填充当前时间。
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记。
     * 0 表示未删除，1 表示已删除。
     */
    @TableLogic
    private Integer isDeleted;

    /**
     * 获取主键 ID。
     *
     * @return 主键 ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置主键 ID。
     *
     * @param id 主键 ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取创建人 ID。
     *
     * @return 创建人 ID
     */
    public Long getCreateBy() {
        return createBy;
    }

    /**
     * 设置创建人 ID。
     *
     * @param createBy 创建人 ID
     */
    public void setCreateBy(Long createBy) {
        this.createBy = createBy;
    }

    /**
     * 获取创建时间。
     *
     * @return 创建时间
     */
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    /**
     * 设置创建时间。
     *
     * @param createTime 创建时间
     */
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    /**
     * 获取更新人 ID。
     *
     * @return 更新人 ID
     */
    public Long getUpdateBy() {
        return updateBy;
    }

    /**
     * 设置更新人 ID。
     *
     * @param updateBy 更新人 ID
     */
    public void setUpdateBy(Long updateBy) {
        this.updateBy = updateBy;
    }

    /**
     * 获取更新时间。
     *
     * @return 更新时间
     */
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    /**
     * 设置更新时间。
     *
     * @param updateTime 更新时间
     */
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * 获取逻辑删除标记。
     *
     * @return 逻辑删除标记（0=未删除，1=已删除）
     */
    public Integer getIsDeleted() {
        return isDeleted;
    }

    /**
     * 设置逻辑删除标记。
     *
     * @param isDeleted 逻辑删除标记
     */
    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }
}
