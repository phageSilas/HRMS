package com.hrms.business.personnel.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.business.personnel.convert.EntryApplicationConvert;
import com.hrms.business.personnel.dto.EntryApplicationCreateOrUpdateRequestDTO;
import com.hrms.business.personnel.dto.EntryApplicationQueryDTO;
import com.hrms.business.personnel.entity.EntryApplicationEntity;
import com.hrms.business.personnel.enums.ApplicationStatusEnum;
import com.hrms.business.personnel.mapper.EntryApplicationMapper;
import com.hrms.business.personnel.service.EntryApplicationService;
import com.hrms.business.personnel.vo.EntryApplicationPageVO;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import com.hrms.common.web.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 入职申请服务实现
 */
@Service
@RequiredArgsConstructor
public class EntryApplicationServiceImpl implements EntryApplicationService {

    private static final ErrorCode ENTRY_APPLICATION_PHONE_DUPLICATE = new ErrorCode(40045, "手机号已存在入职申请");

    private static final ErrorCode ENTRY_APPLICATION_NOT_FOUND = new ErrorCode(40041, "入职申请不存在");

    private static final ErrorCode ENTRY_APPLICATION_NOT_DRAFT = new ErrorCode(40042, "非草稿状态无法修改");

    private static final int DEFAULT_PAGE_NUM = 1;

    private static final int DEFAULT_PAGE_SIZE = 20;

    private static final int MAX_PAGE_SIZE = 200;

    private final EntryApplicationMapper entryApplicationMapper;

    @Override
    public PageResult<EntryApplicationPageVO> pageEntryApplications(EntryApplicationQueryDTO queryDTO) {
        int pageNum = normalizePageNum(queryDTO.getPageNum());
        int pageSize = normalizePageSize(queryDTO.getPageSize());
        Page<EntryApplicationEntity> page = entryApplicationMapper.selectPage(
                Page.of(pageNum, pageSize),
                buildPageQueryWrapper(queryDTO)
        );
        List<EntryApplicationPageVO> records = page.getRecords().stream()
                .map(EntryApplicationConvert::toPageVO)
                .toList();
        return PageResult.of(records, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public EntryApplicationPageVO createEntryApplication(EntryApplicationCreateOrUpdateRequestDTO requestDTO) {
        checkPhoneAvailable(requestDTO.getPhone(), null);
        EntryApplicationEntity entity = EntryApplicationConvert.toEntity(requestDTO);
        entity.setApprovalStatus(ApplicationStatusEnum.DRAFT.getCode());
        entryApplicationMapper.insert(entity);
        return EntryApplicationConvert.toPageVO(entity);
    }

    @Override
    public void updateEntryApplication(Long id, EntryApplicationCreateOrUpdateRequestDTO requestDTO) {
        EntryApplicationEntity entity = getRequiredEntryApplication(id);
        assertDraft(entity);
        checkPhoneAvailable(requestDTO.getPhone(), id);
        EntryApplicationConvert.fillEntity(entity, requestDTO);
        entryApplicationMapper.updateById(entity);
    }

    /**
     * 查询必定存在的入职申请。
     *
     * @param id 入职申请ID
     * @return 入职申请实体
     */
    private EntryApplicationEntity getRequiredEntryApplication(Long id) {
        EntryApplicationEntity entity = entryApplicationMapper.selectById(id);
        if (entity == null) {
            throw new GlobalException(ENTRY_APPLICATION_NOT_FOUND);
        }
        return entity;
    }

    /**
     * 校验入职申请是否为草稿。
     *
     * @param entity 入职申请实体
     */
    private void assertDraft(EntryApplicationEntity entity) {
        if (entity.getApprovalStatus() == null || entity.getApprovalStatus() != ApplicationStatusEnum.DRAFT.getCode()) {
            throw new GlobalException(ENTRY_APPLICATION_NOT_DRAFT);
        }
    }

    /**
     * 校验入职申请手机号是否可用。
     *
     * @param phone 手机号
     * @param excludeId 排除的入职申请ID
     */
    private void checkPhoneAvailable(String phone, Long excludeId) {
        Long count = entryApplicationMapper.selectCount(new LambdaQueryWrapper<EntryApplicationEntity>()
                .eq(EntryApplicationEntity::getPhone, phone)
                .ne(excludeId != null, EntryApplicationEntity::getId, excludeId));
        if (count != null && count > 0) {
            throw new GlobalException(ENTRY_APPLICATION_PHONE_DUPLICATE);
        }
    }

    /**
     * 构建入职申请分页查询条件。
     *
     * @param queryDTO 入职申请查询参数
     * @return 查询条件
     */
    private LambdaQueryWrapper<EntryApplicationEntity> buildPageQueryWrapper(EntryApplicationQueryDTO queryDTO) {
        LambdaQueryWrapper<EntryApplicationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(StrUtil.isNotBlank(queryDTO.getKeyword()), keywordWrapper -> keywordWrapper
                .like(EntryApplicationEntity::getCandidateName, queryDTO.getKeyword())
                .or()
                .like(EntryApplicationEntity::getPhone, queryDTO.getKeyword()));
        wrapper.eq(queryDTO.getApprovalStatus() != null, EntryApplicationEntity::getApprovalStatus, queryDTO.getApprovalStatus());
        wrapper.eq(queryDTO.getDepartmentId() != null, EntryApplicationEntity::getDeptId, queryDTO.getDepartmentId());
        wrapper.ge(queryDTO.getDateStart() != null, EntryApplicationEntity::getCreateTime,
                queryDTO.getDateStart() == null ? null : LocalDateTime.of(queryDTO.getDateStart(), LocalTime.MIN));
        wrapper.le(queryDTO.getDateEnd() != null, EntryApplicationEntity::getCreateTime,
                queryDTO.getDateEnd() == null ? null : LocalDateTime.of(queryDTO.getDateEnd(), LocalTime.MAX));
        wrapper.orderByDesc(EntryApplicationEntity::getCreateTime);
        return wrapper;
    }

    /**
     * 规范化页码。
     *
     * @param pageNum 页码
     * @return 有效页码
     */
    private int normalizePageNum(Integer pageNum) {
        if (pageNum == null || pageNum < 1) {
            return DEFAULT_PAGE_NUM;
        }
        return pageNum;
    }

    /**
     * 规范化每页条数。
     *
     * @param pageSize 每页条数
     * @return 有效每页条数
     */
    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

}
