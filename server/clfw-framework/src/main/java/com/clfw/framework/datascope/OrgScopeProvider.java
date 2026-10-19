package com.clfw.framework.datascope;

import java.util.Set;

/**
 * 机构范围提供者（SPI）。
 *
 * <p>数据权限引擎位于 hg-framework，按依赖方向（framework ← org）不能反向依赖 hg-org，
 * 因此由框架定义此接口、hg-org 提供实现，Spring 在运行时注入。mobile-api 只依赖 framework，
 * 无实现时以 {@code ObjectProvider} 判空降级。</p>
 */
public interface OrgScopeProvider {

    /**
     * 返回指定机构自身及其所有下级机构的 ID 集合（用于「本机构及以下」数据范围）。
     *
     * @param orgId 当前机构 ID
     * @return 自身 + 所有子孙机构 ID；orgId 为空时返回空集合
     */
    Set<Long> selfAndDescendantOrgIds(Long orgId);
}
