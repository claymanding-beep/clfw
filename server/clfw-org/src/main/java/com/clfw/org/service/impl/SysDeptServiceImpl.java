package com.clfw.org.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clfw.org.domain.SysDept;
import com.clfw.org.mapper.SysDeptMapper;
import com.clfw.org.service.SysDeptService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 科室服务实现。
 */
@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {

    @Override
    public List<SysDept> selectByOrgId(Long orgId) {
        return list(new LambdaQueryWrapper<SysDept>()
                .eq(orgId != null, SysDept::getOrgId, orgId)
                .orderByAsc(SysDept::getOrderNum));
    }
}
