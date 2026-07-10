package com.hrms.system.organization.service;

import com.hrms.system.organization.dto.DictDataCreateDTO;
import com.hrms.system.organization.dto.DictDataQueryDTO;
import com.hrms.system.organization.dto.DictDataUpdateDTO;
import com.hrms.system.organization.vo.DictDataVO;

import java.util.List;

/**
 * 字典数据服务接口。
 */
public interface DictDataService {

    Long create(DictDataCreateDTO dto);

    void update(DictDataUpdateDTO dto);

    void delete(Long id);

    List<DictDataVO> list(DictDataQueryDTO dto);

    DictDataVO getById(Long id);

    List<DictDataVO> getByTypeCode(String typeCode);
}