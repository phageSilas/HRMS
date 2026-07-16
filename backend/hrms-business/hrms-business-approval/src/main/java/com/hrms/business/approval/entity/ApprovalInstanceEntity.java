package com.hrms.business.approval.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审批实例实体
 * <p>
 * 对应数据库表 hr_approval_instance
 * </p>
 */
@Data
@TableName("hr_approval_instance")
public class ApprovalInstanceEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 审批单号
     */
    private String approvalNo;

    /**
     * 审批类型编码
     */
    private String approvalType;

    /**
     * 业务主键ID
     */
    private Long bizId;

    /**
     * 审批标题
     */
    private String title;

    /**
     * 申请人用户ID
     */
    private Long applicantUserId;

    /**
     * 申请人员工ID
     */
    private Long applicantEmployeeId;

    /**
     * 当前节点名称
     */
    private String currentNodeName;

    /**
     * 状态：0-草稿 1-审批中 2-已通过 3-已驳回 4-已撤回
     */
    private Integer approvalStatus;

    /**
     * 表单快照（审批时的业务数据副本）
     */
    private String formJson;

    /**
     * 申请时间
     */
    private LocalDateTime applyTime;

    /**
     * 完成时间
     */
    private LocalDateTime finishTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer isDeleted;

    /**
     * 版本号
     */
    @Version
    private Integer version;
}
