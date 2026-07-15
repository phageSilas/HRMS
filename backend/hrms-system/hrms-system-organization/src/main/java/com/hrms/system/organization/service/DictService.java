package com.hrms.system.organization.service;

import com.hrms.common.web.PageResult;
import com.hrms.system.organization.dto.DictDataCreateDTO;
import com.hrms.system.organization.dto.DictTypeCreateDTO;
import com.hrms.system.organization.vo.DictDataVO;
import com.hrms.system.organization.vo.DictTypeVO;

import java.util.List;

/**
 * 字典服务接口
 */
public interface DictService {

    /**
     * 分页查询字典类型列表
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    PageResult<DictTypeVO> listDictTypes(Integer pageNum, Integer pageSize);

    /**
     * 创建字典类型
     *
     * @param createDTO 创建字典类型 DTO
     * @return 创建的字典类型 ID
     */
    Long createDictType(DictTypeCreateDTO createDTO);

    /**
     * 按类型编码查询字典数据
     *
     * @param typeCode 字典类型编码
     * @return 字典数据列表
     */
    List<DictDataVO> getDictDataByType(String typeCode);

    /**
     * 创建字典数据
     *
     * @param createDTO 创建字典数据 DTO
     * @return 创建的字典数据 ID
     */
    Long createDictData(DictDataCreateDTO createDTO);

}
