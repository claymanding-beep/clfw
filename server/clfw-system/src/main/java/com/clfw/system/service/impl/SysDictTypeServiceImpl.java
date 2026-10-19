package com.clfw.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clfw.system.domain.SysDictType;
import com.clfw.system.mapper.SysDictTypeMapper;
import com.clfw.system.service.SysDictDataService;
import com.clfw.system.service.SysDictTypeService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 字典类型服务实现。
 * 字典类型删除或编码变更时，同步失效对应字典数据的 Redis 缓存。
 */
@Service
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements SysDictTypeService {

    private final SysDictDataService dictDataService;

    public SysDictTypeServiceImpl(SysDictDataService dictDataService) {
        this.dictDataService = dictDataService;
    }

    @Override
    public boolean updateById(SysDictType entity) {
        String oldType = entity.getDictType();
        if (!StringUtils.hasText(oldType) && entity.getId() != null) {
            SysDictType old = getById(entity.getId());
            if (old != null) {
                oldType = old.getDictType();
            }
        }
        boolean result = super.updateById(entity);
        dictDataService.clearCache(oldType);
        if (StringUtils.hasText(entity.getDictType())) {
            dictDataService.clearCache(entity.getDictType());
        }
        return result;
    }

    @Override
    public boolean removeByIds(Collection<?> ids) {
        Set<String> types = new HashSet<>();
        if (ids != null) {
            for (Object id : ids) {
                if (id instanceof Serializable) {
                    SysDictType type = getById((Serializable) id);
                    if (type != null && StringUtils.hasText(type.getDictType())) {
                        types.add(type.getDictType());
                    }
                }
            }
        }
        boolean result = super.removeByIds(ids);
        types.forEach(dictDataService::clearCache);
        return result;
    }
}
