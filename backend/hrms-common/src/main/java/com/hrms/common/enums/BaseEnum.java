package com.hrms.common.enums;

import java.io.Serializable;

/**
 * 枚举基类接口
 */
public interface BaseEnum extends Serializable {

    /**
     * 获取编码值
     *
     * @return 编码值
     */
    int getCode();

    /**
     * 获取描述信息
     *
     * @return 描述信息
     */
    String getDesc();

}
