package com.hg.org.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hg.org.domain.SysOrg;

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
