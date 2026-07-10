package com.hrms.system.organization.convert;

import com.hrms.system.organization.dto.PostCreateDTO;
import com.hrms.system.organization.dto.PostUpdateDTO;
import com.hrms.system.organization.entity.PostDO;
import com.hrms.system.organization.vo.PostVO;

import java.util.ArrayList;
import java.util.List;

/**
 * 职位对象转换器。
 */
public class PostConvert {

    /**
     * PostCreateDTO 转 PostDO。
     */
    public static PostDO toDO(PostCreateDTO dto) {
        if (dto == null) {
            return null;
        }
        PostDO entity = new PostDO();
        entity.setPostName(dto.getPostName());
        entity.setPostCode(dto.getPostCode());
        entity.setSortNo(dto.getSortNo());
        entity.setStatus(dto.getStatus());
        entity.setRemark(dto.getRemark());
        return entity;
    }

    /**
     * PostUpdateDTO 转 PostDO。
     */
    public static PostDO toDO(PostUpdateDTO dto) {
        if (dto == null) {
            return null;
        }
        PostDO entity = new PostDO();
        entity.setId(dto.getId());
        entity.setPostName(dto.getPostName());
        entity.setSortNo(dto.getSortNo());
        entity.setStatus(dto.getStatus());
        entity.setRemark(dto.getRemark());
        return entity;
    }

    /**
     * PostDO 转 PostVO。
     */
    public static PostVO toVO(PostDO entity) {
        if (entity == null) {
            return null;
        }
        PostVO vo = new PostVO();
        vo.setId(entity.getId());
        vo.setPostName(entity.getPostName());
        vo.setPostCode(entity.getPostCode());
        vo.setSortNo(entity.getSortNo());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    /**
     * PostDO 列表转 PostVO 列表。
     */
    public static List<PostVO> toVOList(List<PostDO> entities) {
        if (entities == null) {
            return null;
        }
        List<PostVO> vos = new ArrayList<>(entities.size());
        for (PostDO entity : entities) {
            vos.add(toVO(entity));
        }
        return vos;
    }
}