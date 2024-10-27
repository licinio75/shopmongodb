package shopsqs.demo.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.JoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    // Log antes  asdfasdf  de la ejecución de cualquier método del controlador
    @Before("execution(* shopsqs.demo.controller.*.*(..))")
    public void logBeforeMethod(JoinPoint joinPoint) {
        logger.info("Entrando en el método: {} con los argumentos: {}", joinPoint.getSignature(), joinPoint.getArgs());
    }

    // Log después de que un método del controlador se ejecute correctamente
    @AfterReturning(pointcut = "execution(* shopsqs.demo.controller.*.*(..))", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        logger.info("Método {} ejecutado correctamente. Resultado: {}", joinPoint.getSignature(), result);
    }

    // Log si ocurre una excepción en un método del controlador
    @AfterThrowing(pointcut = "execution(* shopsqs.demo.controller.*.*(..))", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Exception ex) {
        logger.error("Excepción en el método: {}. Detalles: {}", joinPoint.getSignature(), ex.getMessage(), ex);
    }

    // Log para medir el tiempo de ejecución de un método
    @Around("execution(* shopsqs.demo.controller.*.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();  // Ejecuta el método objetivo
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        logger.info("El método {} ejecutado en {} ms", joinPoint.getSignature(), duration);
        return proceed;
    }
}
