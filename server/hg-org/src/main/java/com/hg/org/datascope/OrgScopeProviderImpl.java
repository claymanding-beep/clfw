package com.hg.org.datascope;

import com.hg.framework.datascope.OrgScopeProvider;
import com.hg.org.domain.SysOrg;
import com.hg.org.service.SysOrgService;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 机构范围提供者实现：基于 sys_org 的 parent_id 展开「本机构及以下」。
 *
 * <p>sys_org 不在数据权限受控表白名单内，此处查询不会被数据权限拦截器再次过滤，无递归风险。
 * 机构表数据量小（数十条），当前按需实时计算；若后续机构量级增大，可在此加短 TTL 缓存。</p>
 */
@Component
public class OrgScopeProviderImpl implements OrgScopeProvider {

    private final SysOrgService orgService;

    public OrgScopeProviderImpl(SysOrgService orgService) {
        this.orgService = orgService;
    }

    @Override
    public Set<Long> selfAndDescendantOrgIds(Long orgId) {
        if (orgId == null) {
            return Collections.emptySet();
        }
        List<SysOrg> all = orgService.list();
        // parent_id -> 子机构 id 列表
        Map<Long, List<Long>> childrenMap = all.stream()
                .filter(o -> o.getParentId() != null && o.getId() != null)
                .collect(Collectors.groupingBy(SysOrg::getParentId,
                        Collectors.mapping(SysOrg::getId, Collectors.toList())));

        Set<Long> result = new HashSet<>();
        Deque<Long> stack = new ArrayDeque<>();
        stack.push(orgId);
        while (!stack.isEmpty()) {
            Long cur = stack.pop();
            if (result.add(cur)) {
                List<Long> children = childrenMap.get(cur);
                if (children != null) {
                    children.forEach(stack::push);
                }
            }
        }
        return result;
    }
}
