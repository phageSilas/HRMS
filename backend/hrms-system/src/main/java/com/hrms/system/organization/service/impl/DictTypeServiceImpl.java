package com.hrms.system.organization.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hrms.common.exception.BusinessException;
import com.hrms.common.exception.ErrorCode;
import com.hrms.system.organization.convert.DictConvert;
import com.hrms.system.organization.dto.DictTypeCreateDTO;
import com.hrms.system.organization.dto.DictTypeQueryDTO;
import com.hrms.system.organization.dto.DictTypeUpdateDTO;
import com.hrms.system.organization.entity.DictTypeDO;
import com.hrms.system.organization.mapper.DictTypeMapper;
import com.hrms.system.organization.service.DictTypeService;
import com.hrms.system.organization.vo.DictTypeVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 字典类型服务实现类。
 */
@Service
public class DictTypeServiceImpl implements DictTypeService {

    private final DictTypeMapper dictTypeMapper;

    public DictTypeServiceImpl(DictTypeMapper dictTypeMapper) {
        this.dictTypeMapper = dictTypeMapper;
    }

    @Override
    public Long create(DictTypeCreateDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getTypeCode())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "字典类型编码不能为空");
        }

        DictTypeDO exist = dictTypeMapper.selectByTypeCode(dto.getTypeCode());
        if (exist != null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "字典类型编码已存在");
        }

        DictTypeDO entity = DictConvert.toDO(dto);
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }

        dictTypeMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void update(DictTypeUpdateDTO dto) {
        if (dto == null || dto.getId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "字典类型 ID 不能为空");
        }

        DictTypeDO entity = dictTypeMapper.selectById(dto.getId());
        if (entity == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "字典类型不存在");
        }

        DictTypeDO updateEntity = DictConvert.toDO(dto);
        dictTypeMapper.updateById(updateEntity);
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "字典类型 ID 不能为空");
        }

        DictTypeDO entity = dictTypeMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "字典类型不存在");
        }

        dictTypeMapper.deleteById(id);
    }

    @Override
    public List<DictTypeVO> list(DictTypeQueryDTO dto) {
        LambdaQueryWrapper<DictTypeDO> wrapper = new LambdaQueryWrapper<>();
        if (dto != null) {
            if (StringUtils.hasText(dto.getTypeCode())) {
                wrapper.like(DictTypeDO::getTypeCode, dto.getTypeCode());
            }
            if (StringUtils.hasText(dto.getTypeName())) {
                wrapper.like(DictTypeDO::getTypeName, dto.getTypeName());
            }
            if (dto.getStatus() != null) {
                wrapper.eq(DictTypeDO::getStatus, dto.getStatus());
            }
        }
        wrapper.orderByAsc(DictTypeDO::getTypeCode);

        List<DictTypeDO> entities = dictTypeMapper.selectList(wrapper);
        return entities.stream()
                .map(DictConvert::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public DictTypeVO getById(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "字典类型 ID 不能为空");
        }

        DictTypeDO entity = dictTypeMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "字典类型不存在");
        }

        return DictConvert.toVO(entity);
    }
}