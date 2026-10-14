package com.hg.framework.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hg.common.context.UserContext;
import com.hg.common.log.OperLogRecord;
import com.hg.framework.annotation.OperLog;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 操作日志切面：拦截 {@link OperLog} 注解方法，采集请求信息并发布事件。
 */
@Aspect
@Component
public class OperLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperLogAspect.class);
    // 注册 JavaTimeModule，避免入参含 LocalDateTime 时序列化失败退化为对象 toString
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private static final int MAX_LEN = 2000;

    private final ApplicationEventPublisher publisher;

    public OperLogAspect(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Around("@annotation(operLog)")
    public Object around(ProceedingJoinPoint point, OperLog operLog) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = null;
        Exception exception = null;
        try {
            result = point.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            try {
                saveLog(point, operLog, result, exception, System.currentTimeMillis() - start);
            } catch (Exception e) {
                log.warn("记录操作日志失败: {}", e.getMessage());
            }
        }
    }

    private void saveLog(ProceedingJoinPoint point, OperLog operLog, Object result,
                         Exception exception, long costTime) {
        OperLogRecord record = new OperLogRecord();
        record.setModule(operLog.module());
        record.setOperType(operLog.type().name());
        record.setOperBy(UserContext.getUsername());
        record.setCostTime(costTime);
        record.setOperTime(LocalDateTime.now());
        record.setStatus(exception == null ? 1 : 0);
        if (exception != null) {
            record.setErrorMsg(truncate(exception.getMessage()));
        }

        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            record.setRequestMethod(request.getMethod());
            record.setOperUrl(truncate(request.getRequestURI()));
            record.setOperIp(getIp(request));
        }

        if (operLog.saveParam()) {
            record.setOperParam(truncate(toJson(filterArgs(point.getArgs()))));
        }
        if (operLog.saveResult() && result != null) {
            record.setJsonResult(truncate(toJson(result)));
        }

        publisher.publishEvent(record);
    }

    private Object[] filterArgs(Object[] args) {
        if (args == null) {
            return new Object[0];
        }
        return Arrays.stream(args)
                .filter(a -> !(a instanceof MultipartFile)
                        && !(a instanceof HttpServletRequest)
                        && !(a instanceof jakarta.servlet.http.HttpServletResponse))
                .toArray();
    }

    private String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            return Arrays.stream((Object[]) obj).map(String::valueOf).collect(Collectors.joining(","));
        }
    }

    private String truncate(String str) {
        if (str == null) {
            return null;
        }
        return str.length() > MAX_LEN ? str.substring(0, MAX_LEN) : str;
    }

    private String getIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
