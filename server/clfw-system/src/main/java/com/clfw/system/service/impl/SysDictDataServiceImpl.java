package com.clfw.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clfw.common.constant.CacheConstants;
import com.clfw.common.constant.CommonConstants;
import com.clfw.system.domain.SysDictData;
import com.clfw.system.mapper.SysDictDataMapper;
import com.clfw.system.service.SysDictDataService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 字典数据服务实现。
 * 字典读多写少，按字典类型编码缓存到 Redis（key: sys:dict:{dictType}），写操作时精准失效。
 */
@Service
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictData> implements SysDictDataService {

    /** 字典缓存有效期（兜底，写操作会主动失效） */
    private static final Duration DICT_CACHE_TTL = Duration.ofHours(12);

    private final RedisTemplate<String, Object> redisTemplate;

    public SysDictDataServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SysDictData> selectByDictType(String dictType) {
        String key = CacheConstants.DICT_CACHE_KEY + dictType;
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof List) {
            return (List<SysDictData>) cached;
        }
        List<SysDictData> list = list(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictType, dictType)
                .eq(SysDictData::getStatus, CommonConstants.STATUS_ENABLE)
                .orderByAsc(SysDictData::getDictSort));
        redisTemplate.opsForValue().set(key, list, DICT_CACHE_TTL);
        return list;
    }

    @Override
    public void clearCache(String dictType) {
        if (StringUtils.hasText(dictType)) {
            redisTemplate.delete(CacheConstants.DICT_CACHE_KEY + dictType);
        } else {
            // 清除全部字典缓存
            Set<String> keys = redisTemplate.keys(CacheConstants.DICT_CACHE_KEY + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        }
    }

    @Override
    public boolean save(SysDictData entity) {
        boolean result = super.save(entity);
        clearCache(entity.getDictType());
        return result;
    }

    @Override
    public boolean updateById(SysDictData entity) {
        // 表单可能未携带 dictType，先取旧值以便失效原类型缓存
        String oldType = entity.getDictType();
        if (!StringUtils.hasText(oldType) && entity.getId() != null) {
            SysDictData old = getById(entity.getId());
            if (old != null) {
                oldType = old.getDictType();
            }
        }
        boolean result = super.updateById(entity);
        clearCache(oldType);
        if (StringUtils.hasText(entity.getDictType())) {
            clearCache(entity.getDictType());
        }
        return result;
    }

    @Override
    public boolean removeByIds(Collection<?> ids) {
        // 删除前收集受影响的字典类型
        Set<String> types = new HashSet<>();
        if (ids != null) {
            for (Object id : ids) {
                if (id instanceof Serializable) {
                    SysDictData data = getById((Serializable) id);
                    if (data != null && StringUtils.hasText(data.getDictType())) {
                        types.add(data.getDictType());
                    }
                }
            }
        }
        boolean result = super.removeByIds(ids);
        types.forEach(this::clearCache);
        return result;
    }
}
