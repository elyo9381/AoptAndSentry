package com.ddoel.person.demo.config;

import com.ddoel.person.demo.domain.CommonDto;
import io.sentry.Sentry;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.RequestContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@Component
@Aspect
public class BindingAdvice {

    @Before("execution(* com.ddoel.person.demo.web..*Controller.*(..))")
    public void testBeforeCheck(){
        //전처리만 사용할것 Return 같은 값을 처리할수없다. 왜냐? 함수실행 내부 코드 사용전이므로

        //request 값 처리 못하나요?
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        log.info("주소 : {}",request);


        log.info("");
    }

    @After("execution(* com.ddoel.person.demo.web..*Controller.*(..))")
    public void testAfterCheck(){
        log.info("");

    }


    //@Before
    //@After
    @Around("execution(* com.ddoel.person.demo.web..*Controller.*(..))")
    public Object validCheck(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        String type = proceedingJoinPoint.getSignature().getDeclaringTypeName();
        String method = proceedingJoinPoint.getSignature().getName();

        log.info("type confirm : {}", type);
        log.info("method confirm : {}", method);

        // 아규먼트 리턴
        Object[] args = proceedingJoinPoint.getArgs();

        for (Object arg : args) {
            if(arg instanceof BindingResult){
                BindingResult bindingResult = (BindingResult) arg;


                // 서비스 : 정상적인 화면 -> 사용자요청
                if(bindingResult.hasErrors()){
                    Map<String,String> errorMap = new HashMap<>();

                    for(FieldError error : bindingResult.getFieldErrors()){
                        errorMap.put(error.getField(),error.getDefaultMessage());
                        log.warn(type+"."+method+"()=>필드 : "+error.getField()+", 메시지:"+error.getDefaultMessage());
                        Sentry.captureMessage(type+"."+method+"()=>필드 : "+error.getField()+", 메시지:"+error.getDefaultMessage());
                    }

                    return new CommonDto<>(HttpStatus.BAD_REQUEST.value(),errorMap);
                }
            }
        }

        return proceedingJoinPoint.proceed();// 함수의 시택을 실행하라.
    }
}