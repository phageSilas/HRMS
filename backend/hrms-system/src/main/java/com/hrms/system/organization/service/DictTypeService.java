package com.hrms.system.organization.service;

import com.hrms.system.organization.dto.DictTypeCreateDTO;
import com.hrms.system.organization.dto.DictTypeQueryDTO;
import com.hrms.system.organization.dto.DictTypeUpdateDTO;
import com.hrms.system.organization.vo.DictTypeVO;

import java.util.List;

/**
 * 字典类型服务接口。
 */
public interface DictTypeService {

    Long create(DictTypeCreateDTO dto);

    void update(DictTypeUpdateDTO dto);

    void delete(Long id);

    List<DictTypeVO> list(DictTypeQueryDTO dto);

    DictTypeVO getById(Long id);
}