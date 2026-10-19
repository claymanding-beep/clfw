package com.clfw.org.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.clfw.org.domain.SysOrg;

import java.util.List;

/**
 * 机构服务。
 */
public interface SysOrgService extends IService<SysOrg> {

    /**
     * 查询机构树。
     */
    List<SysOrg> selectOrgTree();
}
