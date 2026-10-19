package com.clfw.framework.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigInteger;

/**
 * Jackson 序列化配置。
 *
 * <p>核心目的：后端主键采用雪花算法生成的 19 位 Long，超出 JavaScript Number 安全整数范围
 * （2^53-1），前端 JSON.parse 会丢失精度，导致按 id 查询详情查不到、编辑报错等问题。
 * 这里将包装类型 {@link Long} 与 {@link BigInteger} 统一序列化为字符串，前端以字符串透传 id，
 * 回传时 Jackson/Spring 能自动将字符串反序列化回 Long，不影响写入。</p>
 *
 * <p>注意：仅注册包装类型 {@code Long.class}，不注册原始类型 {@code long}（Long.TYPE），
 * 因此 {@code PageResult.total} 等原始 long 计数字段仍以数字输出，前端分页无需改动。</p>
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer longToStringCustomizer() {
        return builder -> {
            builder.serializerByType(Long.class, ToStringSerializer.instance);
            builder.serializerByType(BigInteger.class, ToStringSerializer.instance);
        };
    }
}
