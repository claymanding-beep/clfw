package com.hg.org.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hg.org.domain.SysOrg;
import com.hg.org.mapper.SysOrgMapper;
import com.hg.org.service.SysOrgService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 机构服务实现。
 */
@Service
public class SysOrgServiceImpl extends ServiceImpl<SysOrgMapper, SysOrg> implements SysOrgService {

    @Override
    public List<SysOrg> selectOrgTree() {
        List<SysOrg> all = list(new LambdaQueryWrapper<SysOrg>()
                .orderByAsc(SysOrg::getParentId).orderByAsc(SysOrg::getOrderNum));
        if (all.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, List<SysOrg>> parentMap = all.stream()
                .collect(Collectors.groupingBy(o -> o.getParentId() == null ? 0L : o.getParentId()));
        Set<Long> idSet = all.stream().map(SysOrg::getId).collect(Collectors.toSet());
        List<SysOrg> roots = new ArrayList<>();
        for (SysOrg org : all) {
            Long parentId = org.getParentId() == null ? 0L : org.getParentId();
            if (parentId == 0L || !idSet.contains(parentId)) {
                roots.add(org);
            }
        }
        roots.forEach(root -> fillChildren(root, parentMap));
        return roots;
    }

    private void fillChildren(SysOrg org, Map<Long, List<SysOrg>> parentMap) {
        List<SysOrg> children = parentMap.get(org.getId());
        if (children != null && !children.isEmpty()) {
            org.setChildren(children);
            children.forEach(child -> fillChildren(child, parentMap));
        } else {
            org.setChildren(new ArrayList<>());
        }
    }
}
