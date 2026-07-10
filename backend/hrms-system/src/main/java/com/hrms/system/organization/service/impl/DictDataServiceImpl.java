package com.hrms.system.organization.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hrms.common.exception.BusinessException;
import com.hrms.common.exception.ErrorCode;
import com.hrms.system.organization.convert.DictConvert;
import com.hrms.system.organization.dto.DictDataCreateDTO;
import com.hrms.system.organization.dto.DictDataQueryDTO;
import com.hrms.system.organization.dto.DictDataUpdateDTO;
import com.hrms.system.organization.entity.DictDataDO;
import com.hrms.system.organization.mapper.DictDataMapper;
import com.hrms.system.organization.service.DictDataService;
import com.hrms.system.organization.vo.DictDataVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 字典数据服务实现类。
 */
@Service
public class DictDataServiceImpl implements DictDataService {

    private final DictDataMapper dictDataMapper;

    public DictDataServiceImpl(DictDataMapper dictDataMapper) {
        this.dictDataMapper = dictDataMapper;
    }

    @Override
    public Long create(DictDataCreateDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getTypeCode())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "字典类型编码不能为空");
        }

        if (!StringUtils.hasText(dto.getDictLabel())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "字典标签不能为空");
        }

        if (!StringUtils.hasText(dto.getDictValue())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "字典值不能为空");
        }

        DictDataDO entity = DictConvert.toDO(dto);
        if (entity.getSortNo() == null) {
            entity.setSortNo(0);
        }
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }

        dictDataMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void update(DictDataUpdateDTO dto) {
        if (dto == null || dto.getId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "字典数据 ID 不能为空");
        }

        DictDataDO entity = dictDataMapper.selectById(dto.getId());
        if (entity == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "字典数据不存在");
        }

        DictDataDO updateEntity = DictConvert.toDO(dto);
        dictDataMapper.updateById(updateEntity);
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "字典数据 ID 不能为空");
        }

        DictDataDO entity = dictDataMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "字典数据不存在");
        }

        dictDataMapper.deleteById(id);
    }

    @Override
    public List<DictDataVO> list(DictDataQueryDTO dto) {
        LambdaQueryWrapper<DictDataDO> wrapper = new LambdaQueryWrapper<>();
        if (dto != null) {
            if (StringUtils.hasText(dto.getTypeCode())) {
                wrapper.eq(DictDataDO::getTypeCode, dto.getTypeCode());
            }
            if (StringUtils.hasText(dto.getDictLabel())) {
                wrapper.like(DictDataDO::getDictLabel, dto.getDictLabel());
            }
            if (dto.getStatus() != null) {
                wrapper.eq(DictDataDO::getStatus, dto.getStatus());
            }
        }
        wrapper.orderByAsc(DictDataDO::getSortNo);

        List<DictDataDO> entities = dictDataMapper.selectList(wrapper);
        return entities.stream()
                .map(DictConvert::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public DictDataVO getById(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "字典数据 ID 不能为空");
        }

        DictDataDO entity = dictDataMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "字典数据不存在");
        }

        return DictConvert.toVO(entity);
    }

    @Override
    public List<DictDataVO> getByTypeCode(String typeCode) {
        if (!StringUtils.hasText(typeCode)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "字典类型编码不能为空");
        }

        List<DictDataDO> entities = dictDataMapper.selectByTypeCode(typeCode);
        return entities.stream()
                .map(DictConvert::toVO)
                .collect(Collectors.toList());
    }
}