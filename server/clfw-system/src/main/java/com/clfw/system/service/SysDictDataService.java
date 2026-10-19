package com.clfw.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.clfw.system.domain.SysDictData;

import java.util.List;

/**
 * 字典数据服务。
 */
public interface SysDictDataService extends IService<SysDictData> {

    /**
     * 根据字典类型编码查询启用的字典值列表（前端下拉数据源）。
     */
    List<SysDictData> selectByDictType(String dictType);

    /**
     * 清除指定字典类型的缓存；dictType 为空时清除全部字典缓存。
     */
    void clearCache(String dictType);
}
