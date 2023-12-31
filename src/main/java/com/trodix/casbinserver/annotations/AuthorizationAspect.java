package com.trodix.casbinserver.annotations;

import com.trodix.casbinserver.client.api.v1.EnforcerApi;
import com.trodix.casbinserver.configuration.AuthorizedUserSubjectProvider;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

@Component
@Aspect
public class AuthorizationAspect {

    private final AuthorizedUserSubjectProvider authorizedUserSubjectProvider;

    private final EnforcerApi enforcer;

    public AuthorizationAspect(AuthorizedUserSubjectProvider authorizedUserSubjectProvider, EnforcerApi enforcer) {
        this.authorizedUserSubjectProvider = authorizedUserSubjectProvider;
        this.enforcer = enforcer;
    }

    @Pointcut("@annotation(Authorization)")
    public void authorizationPointcut() {

    }

    @Around("authorizationPointcut()")
    public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature methodSignature = ((MethodSignature) joinPoint.getSignature());
        Authorization authz = methodSignature.getMethod().getAnnotation(Authorization.class);

        Map<String, Object> annotatedParameterValue = getAuthResourceIdAnnotatedParameterValue(methodSignature.getMethod(), joinPoint.getArgs());

        boolean granted = handleAuthorizationForUser(authz);

        if (granted) {
            return joinPoint.proceed();
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to access this resource.");
    }

    private boolean handleAuthorizationForUser(Authorization authz) {
        String userId = authorizedUserSubjectProvider.getAuthorizedUserSubject().getId();
        String resourceType = authz.resourceType().toLowerCase();
        String permissionType = authz.permissionType().toString().toUpperCase();

        return enforcer.enforce(userId, resourceType, permissionType);
    }

    private Map<String, Object> getAuthResourceIdAnnotatedParameterValue(Method method, Object[] args) {
        Map<String, Object> annotatedParameters = new HashMap<>();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Parameter[] parameters = method.getParameters();

        int i = 0;
        for (Annotation[] annotations : parameterAnnotations) {
            Object arg = args[i];
            String name = parameters[i++].getDeclaringExecutable().getName();
            for (Annotation annotation : annotations) {
                if (annotation instanceof AuthResourceId) {
                    annotatedParameters.put(name, arg);
                }
            }
        }
        return annotatedParameters;
    }



}
