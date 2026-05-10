package com.example.rest.aspect;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.UUID;

@Component
@Aspect
@Slf4j
public class LoggingAspect {

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void restController() {
    }

    @Around("restController()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {

        long start = System.currentTimeMillis();

        String method = joinPoint.getSignature().toShortString();

        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        Object[] sanitizedArgs = sanitizeArgs(joinPoint.getArgs());

        log.debug(
                "[ENTER] method={} args={}",
                method,
                Arrays.toString(sanitizedArgs)
        );

        try {

            Object result = joinPoint.proceed();

            long executionTime = System.currentTimeMillis() - start;

            log.debug(
                    "[EXIT] method={} result={} time={}ms",
                    method,
                    safeResult(result),
                    executionTime
            );

            return result;

        } catch (Exception ex) {

            long executionTime = System.currentTimeMillis() - start;

            log.error(
                    "[ERROR] method={} time={}ms message={}",
                    method,
                    executionTime,
                    ex.getMessage(),
                    ex
            );

            throw ex;

        } finally {
            MDC.remove("traceId");
        }
    }

    private Object[] sanitizeArgs(Object[] args) {

        return Arrays.stream(args)
                .filter(arg ->
                        !(arg instanceof HttpServletRequest) &&
                                !(arg instanceof HttpServletResponse) &&
                                !(arg instanceof BindingResult)
                )
                .map(this::safeArgument)
                .toArray();
    }

    private Object safeArgument(Object arg) {

        switch (arg) {
            case null -> {
                return "null";
            }
            case MultipartFile file -> {
                return "MultipartFile(name=" + file.getOriginalFilename() + ")";
            }
            case byte[] bytes -> {
                return "byte[" + bytes.length + "]";
            }
            case char[] chars -> {
                return "char[" + chars.length + "]";
            }
            default -> {
            }
        }

        try {
            return arg.toString();
        } catch (Exception ex) {
            return arg.getClass().getSimpleName();
        }
    }

    private Object safeResult(Object result) {

        switch (result) {
            case null -> {
                return "null";
            }
            case Page<?> page -> {
                return String.format(
                        "Page(totalElements=%d, totalPages=%d, size=%d)",
                        page.getTotalElements(),
                        page.getTotalPages(),
                        page.getSize()
                );
            }
            case Iterable<?> iterable -> {

                long count = 0;

                for (Object ignored : iterable) {
                    count++;
                }
                return "Collection(size=" + count + ")";
            }
            default -> {
            }
        }

        if (isPrimitiveLike(result)) {
            return result;
        }
        return result.getClass().getSimpleName();
    }

    private boolean isPrimitiveLike(Object obj) {
        return obj instanceof String ||
                obj instanceof Number ||
                obj instanceof Boolean ||
                obj instanceof Enum<?>;
    }
}