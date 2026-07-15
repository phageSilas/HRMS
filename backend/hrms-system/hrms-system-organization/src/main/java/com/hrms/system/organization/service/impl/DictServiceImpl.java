package com.hrms.system.organization.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import com.hrms.common.web.PageResult;
import com.hrms.system.organization.dto.DictDataCreateDTO;
import com.hrms.system.organization.dto.DictTypeCreateDTO;
import com.hrms.system.organization.entity.DictDataEntity;
import com.hrms.system.organization.entity.DictTypeEntity;
import com.hrms.system.organization.mapper.DictDataMapper;
import com.hrms.system.organization.mapper.DictTypeMapper;
import com.hrms.system.organization.service.DictService;
import com.hrms.system.organization.vo.DictDataVO;
import com.hrms.system.organization.vo.DictTypeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 字典服务实现
 */
@Service
@RequiredArgsConstructor
public class DictServiceImpl implements DictService {

    private final DictTypeMapper dictTypeMapper;
    private final DictDataMapper dictDataMapper;

    @Override
    public PageResult<DictTypeVO> listDictTypes(Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<DictTypeEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(DictTypeEntity::getStatus, 1)
                .orderByAsc(DictTypeEntity::getCreateTime);

        Page<DictTypeEntity> page = new Page<>(pageNum, pageSize);
        IPage<DictTypeEntity> resultPage = dictTypeMapper.selectPage(page, wrapper);

        List<DictTypeVO> records = resultPage.getRecords().stream()
                .map(this::convertTypeToVO)
                .collect(Collectors.toList());

        return PageResult.of(records, resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDictType(DictTypeCreateDTO createDTO) {
        // 校验字典类型编码唯一性
        Long count = dictTypeMapper.selectCount(
                Wrappers.<DictTypeEntity>lambdaQuery()
                        .eq(DictTypeEntity::getDictType, createDTO.getDictType())
        );
        if (count > 0) {
            throw new GlobalException(ErrorCode.DATA_DUPLICATE);
        }

        DictTypeEntity dictType = new DictTypeEntity();
        dictType.setDictName(createDTO.getDictName());
        dictType.setDictType(createDTO.getDictType());
        dictType.setRemark(createDTO.getRemark());
        dictType.setStatus(1);

        dictTypeMapper.insert(dictType);
        return dictType.getId();
    }

    @Override
    public List<DictDataVO> getDictDataByType(String typeCode) {
        // 先检查字典类型是否存在
        DictTypeEntity dictType = dictTypeMapper.selectOne(
                Wrappers.<DictTypeEntity>lambdaQuery()
                        .eq(DictTypeEntity::getDictType, typeCode)
                        .eq(DictTypeEntity::getStatus, 1)
        );

        if (dictType == null) {
            throw new GlobalException(new ErrorCode(40030, "字典类型编码不存在"));
        }

        List<DictDataEntity> dataList = dictDataMapper.selectList(
                Wrappers.<DictDataEntity>lambdaQuery()
                        .eq(DictDataEntity::getDictType, typeCode)
                        .eq(DictDataEntity::getStatus, 1)
                        .orderByAsc(DictDataEntity::getSort)
        );

        return dataList.stream()
                .map(this::convertDataToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDictData(DictDataCreateDTO createDTO) {
        // 校验字典类型是否存在
        DictTypeEntity dictType = dictTypeMapper.selectOne(
                Wrappers.<DictTypeEntity>lambdaQuery()
                        .eq(DictTypeEntity::getDictType, createDTO.getDictType())
                        .eq(DictTypeEntity::getStatus, 1)
        );

        if (dictType == null) {
            throw new GlobalException(new ErrorCode(40030, "字典类型编码不存在"));
        }

        DictDataEntity dictData = new DictDataEntity();
        dictData.setDictType(createDTO.getDictType());
        dictData.setDictLabel(createDTO.getDictLabel());
        dictData.setDictValue(createDTO.getDictValue());
        dictData.setCssClass(createDTO.getCssClass());
        dictData.setSort(createDTO.getSort());
        dictData.setStatus(1);
        dictData.setRemark(createDTO.getRemark());

        dictDataMapper.insert(dictData);
        return dictData.getId();
    }

    /**
     * 转换为 DictTypeVO
     */
    private DictTypeVO convertTypeToVO(DictTypeEntity entity) {
        DictTypeVO vo = new DictTypeVO();
        vo.setId(entity.getId());
        vo.setDictName(entity.getDictName());
        vo.setDictType(entity.getDictType());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    /**
     * 转换为 DictDataVO
     */
    private DictDataVO convertDataToVO(DictDataEntity entity) {
        DictDataVO vo = new DictDataVO();
        vo.setDictType(entity.getDictType());
        vo.setDictLabel(entity.getDictLabel());
        vo.setDictValue(entity.getDictValue());
        vo.setCssClass(entity.getCssClass());
        vo.setSort(entity.getSort());
        vo.setStatus(entity.getStatus());
        return vo;
    }

}
