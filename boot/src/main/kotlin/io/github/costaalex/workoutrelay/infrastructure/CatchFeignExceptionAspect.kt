package io.github.costaalex.workoutrelay.infrastructure

import feign.FeignException
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Aspect
@Component
class CatchFeignExceptionAspect(
    private val exceptionMapper: FeignExceptionMapper,
) {

    private val log =
        LoggerFactory.getLogger(this.javaClass)

    @Around("@annotation(CatchFeignException)")
    @Throws(Throwable::class)
    fun trace(
        joinPoint: ProceedingJoinPoint,
    ): Any? {
        val method =
            (
                joinPoint.signature as MethodSignature
            ).method

        val annotation =
            method.getAnnotation(
                CatchFeignException::class.java
            )

        val platform =
            annotation.platform

        try {
            return joinPoint.proceed()
        } catch (exception: FeignException) {
            val platformException =
                exceptionMapper.map(
                    platform,
                    exception,
                )

            log.warn(
                "External platform request failed. platform={}, code={}, upstreamStatus={}",
                platform.title,
                platformException.code,
                platformException.upstreamStatus,
                exception,
            )

            throw platformException
        }
    }
}