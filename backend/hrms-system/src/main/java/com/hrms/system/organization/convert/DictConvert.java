package com.hrms.system.organization.convert;

import com.hrms.system.organization.dto.DictTypeCreateDTO;
import com.hrms.system.organization.dto.DictTypeUpdateDTO;
import com.hrms.system.organization.dto.DictDataCreateDTO;
import com.hrms.system.organization.dto.DictDataUpdateDTO;
import com.hrms.system.organization.entity.DictTypeDO;
import com.hrms.system.organization.entity.DictDataDO;
import com.hrms.system.organization.vo.DictTypeVO;
import com.hrms.system.organization.vo.DictDataVO;

import java.util.ArrayList;
import java.util.List;

/**
 * 字典对象转换器。
 */
public class DictConvert {

    // ===== DictType 转换 =====

    public static DictTypeDO toDO(DictTypeCreateDTO dto) {
        if (dto == null) return null;
        DictTypeDO entity = new DictTypeDO();
        entity.setTypeCode(dto.getTypeCode());
        entity.setTypeName(dto.getTypeName());
        entity.setStatus(dto.getStatus());
        entity.setRemark(dto.getRemark());
        return entity;
    }

    public static DictTypeDO toDO(DictTypeUpdateDTO dto) {
        if (dto == null) return null;
        DictTypeDO entity = new DictTypeDO();
        entity.setId(dto.getId());
        entity.setTypeName(dto.getTypeName());
        entity.setStatus(dto.getStatus());
        entity.setRemark(dto.getRemark());
        return entity;
    }

    public static DictTypeVO toVO(DictTypeDO entity) {
        if (entity == null) return null;
        DictTypeVO vo = new DictTypeVO();
        vo.setId(entity.getId());
        vo.setTypeCode(entity.getTypeCode());
        vo.setTypeName(entity.getTypeName());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    public static List<DictTypeVO> toVOList(List<DictTypeDO> entities) {
        if (entities == null) return null;
        List<DictTypeVO> vos = new ArrayList<>(entities.size());
        for (DictTypeDO entity : entities) {
            vos.add(toVO(entity));
        }
        return vos;
    }

    // ===== DictData 转换 =====

    public static DictDataDO toDO(DictDataCreateDTO dto) {
        if (dto == null) return null;
        DictDataDO entity = new DictDataDO();
        entity.setTypeCode(dto.getTypeCode());
        entity.setDictLabel(dto.getDictLabel());
        entity.setDictValue(dto.getDictValue());
        entity.setSortNo(dto.getSortNo());
        entity.setStatus(dto.getStatus());
        entity.setRemark(dto.getRemark());
        return entity;
    }

    public static DictDataDO toDO(DictDataUpdateDTO dto) {
        if (dto == null) return null;
        DictDataDO entity = new DictDataDO();
        entity.setId(dto.getId());
        entity.setDictLabel(dto.getDictLabel());
        entity.setDictValue(dto.getDictValue());
        entity.setSortNo(dto.getSortNo());
        entity.setStatus(dto.getStatus());
        entity.setRemark(dto.getRemark());
        return entity;
    }

    public static DictDataVO toVO(DictDataDO entity) {
        if (entity == null) return null;
        DictDataVO vo = new DictDataVO();
        vo.setId(entity.getId());
        vo.setTypeCode(entity.getTypeCode());
        vo.setDictLabel(entity.getDictLabel());
        vo.setDictValue(entity.getDictValue());
        vo.setSortNo(entity.getSortNo());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    public static List<DictDataVO> toDataVOList(List<DictDataDO> entities) {
        if (entities == null) return null;
        List<DictDataVO> vos = new ArrayList<>(entities.size());
        for (DictDataDO entity : entities) {
            vos.add(toVO(entity));
        }
        return vos;
    }
}