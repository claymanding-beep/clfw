package com.hg.org.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hg.org.domain.SysDept;

import java.util.List;

/**
 * 科室服务。
 */
public interface SysDeptService extends IService<SysDept> {

    /**
     * 查询指定机构下的科室列表。
     */
    List<SysDept> selectByOrgId(Long orgId);
}
