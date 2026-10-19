package com.clfw.common.core;

import lombok.Data;

/**
 * 分页查询基础参数，业务查询 DTO 可继承此类。
 */
@Data
public class PageQuery {

    /** 页码，从 1 开始 */
    private Integer pageNum = 1;
    /** 每页条数 */
    private Integer pageSize = 20;
    /** 排序字段 */
    private String orderBy;
    /** 是否升序 */
    private boolean asc = true;

    public Integer getPageNum() {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    public Integer getPageSize() {
        if (pageSize == null || pageSize < 1) {
            return 20;
        }
        // 单页最大 100 条，防止恶意大分页
        return pageSize > 100 ? 100 : pageSize;
    }
}
