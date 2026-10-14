package com.hg.framework.datascope;

import com.hg.common.constant.DataScope;
import com.hg.common.context.UserContext;
import com.hg.common.core.LoginUser;
import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据权限处理器：在 SQL 查询/更新/删除时，按当前登录用户的 dataScope 自动追加过滤条件，
 * 业务代码零侵入。
 *
 * <p>设计要点：</p>
 * <ul>
 *   <li>仅对「数据权限受控表」（含 org_id/dept_id/create_by 的业务表）生效，
 *       通过 {@link #DATA_TABLES} 白名单控制，避免给 sys_menu/sys_dict 等无 org_id 的表拼错列。</li>
 *   <li>超级管理员、未登录、dataScope=全部 时不加任何条件。</li>
 *   <li>条件字符串经 {@link CCJSqlParserUtil#parseCondExpression} 解析为 JSqlParser 表达式，
 *       规避不同版本 InExpression API 差异；数值型 ID 直接拼接，字符串值做单引号转义防注入。</li>
 *   <li>某数据范围所需列在表上不存在时，安全降级（如本科室→本机构）。</li>
 * </ul>
 */
@Component
public class HgDataPermissionHandler implements MultiDataPermissionHandler {

    private static final Logger log = LoggerFactory.getLogger(HgDataPermissionHandler.class);

    /** 数据权限受控表 -> 该表可用的过滤列。新增业务表时在此登记即可。 */
    private static final Map<String, DataColumns> DATA_TABLES = new HashMap<>();

    static {
        // 用户：可按机构 / 科室 / 本人过滤
        DATA_TABLES.put("sys_user", new DataColumns("org_id", "dept_id", "create_by"));
        // 科室：本身即科室，无 dept_id，可按机构 / 本人过滤
        DATA_TABLES.put("sys_dept", new DataColumns("org_id", null, "create_by"));
    }

    private final ObjectProvider<OrgScopeProvider> orgScopeProvider;

    public HgDataPermissionHandler(ObjectProvider<OrgScopeProvider> orgScopeProvider) {
        this.orgScopeProvider = orgScopeProvider;
    }

    @Override
    public Expression getSqlSegment(Table table, Expression where, String mappedStatementId) {
        LoginUser user = UserContext.get();
        // 未登录 / 超管 / 全部数据：不过滤
        if (user == null || user.isSuperAdmin()) {
            return null;
        }
        Integer scope = user.getDataScope();
        if (scope == null || scope == DataScope.ALL) {
            return null;
        }
        if (table == null || table.getName() == null) {
            return null;
        }
        DataColumns cols = DATA_TABLES.get(table.getName().toLowerCase());
        if (cols == null) {
            // 非受控表，放行
            return null;
        }
        String prefix = table.getAlias() != null ? table.getAlias().getName() : table.getName();
        String condition = buildCondition(scope, cols, prefix, user);
        if (condition == null) {
            return null;
        }
        try {
            return CCJSqlParserUtil.parseCondExpression(condition);
        } catch (Exception e) {
            log.warn("数据权限条件解析失败，已忽略该条件: {}", condition, e);
            return null;
        }
    }

    /**
     * 根据数据范围构建 SQL 条件片段（不含 WHERE 关键字）。
     */
    private String buildCondition(int scope, DataColumns cols, String prefix, LoginUser user) {
        switch (scope) {
            case DataScope.ORG_AND_CHILD: {
                // 本机构及以下：优先用机构树展开；无 provider 或无 orgId 时降级为本机构
                Long orgId = user.getOrgId();
                OrgScopeProvider provider = orgScopeProvider.getIfAvailable();
                if (orgId != null && cols.org != null && provider != null) {
                    try {
                        Set<Long> ids = provider.selfAndDescendantOrgIds(orgId);
                        if (ids != null && !ids.isEmpty()) {
                            String idStr = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
                            return prefix + "." + cols.org + " IN (" + idStr + ")";
                        }
                    } catch (Exception e) {
                        log.warn("展开机构树失败，降级为本机构过滤: {}", e.getMessage());
                    }
                }
                return eqOrg(cols, prefix, user);
            }
            case DataScope.ORG:
                return eqOrg(cols, prefix, user);
            case DataScope.DEPT: {
                // 本科室：表无 dept_id 时降级为本机构
                if (cols.dept != null && user.getDeptId() != null) {
                    return prefix + "." + cols.dept + " = " + user.getDeptId();
                }
                return eqOrg(cols, prefix, user);
            }
            case DataScope.SELF:
                // 仅本人：按创建人用户名过滤
                if (cols.user != null && user.getUsername() != null) {
                    return prefix + "." + cols.user + " = '" + escape(user.getUsername()) + "'";
                }
                return null;
            case DataScope.CUSTOM:
                // 自定义数据范围需角色-机构关联表，暂未实现，降级为本机构
                return eqOrg(cols, prefix, user);
            default:
                return null;
        }
    }

    private String eqOrg(DataColumns cols, String prefix, LoginUser user) {
        if (cols.org != null && user.getOrgId() != null) {
            return prefix + "." + cols.org + " = " + user.getOrgId();
        }
        return null;
    }

    /** 单引号转义，防止用户名等字符串值造成 SQL 注入。 */
    private String escape(String value) {
        return value == null ? "" : value.replace("'", "''");
    }

    /** 受控表可用列定义。 */
    private static class DataColumns {
        final String org;
        final String dept;
        final String user;

        DataColumns(String org, String dept, String user) {
            this.org = org;
            this.dept = dept;
            this.user = user;
        }
    }
}
