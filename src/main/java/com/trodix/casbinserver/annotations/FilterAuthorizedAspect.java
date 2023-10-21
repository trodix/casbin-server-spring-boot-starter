package com.trodix.casbinserver.annotations;

import com.trodix.casbinserver.client.api.v1.EnforcerApi;
import com.trodix.casbinserver.configuration.AuthorizedUserSubjectProvider;
import com.trodix.casbinserver.models.PermissionType;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;

@Component
@Aspect
public class FilterAuthorizedAspect {

    private static final Logger log = LoggerFactory.getLogger(FilterAuthorizedAspect.class);

    private final AuthorizedUserSubjectProvider authorizedUserSubjectProvider;

    private final EnforcerApi enforcer;

    public FilterAuthorizedAspect(AuthorizedUserSubjectProvider authorizedUserSubjectProvider, EnforcerApi enforcer) {
        this.authorizedUserSubjectProvider = authorizedUserSubjectProvider;
        this.enforcer = enforcer;
    }

    @Pointcut("@annotation(FilterAuthorized)")
    public void filterAuthorizedPointcut() {

    }

    @Around("filterAuthorizedPointcut()")
    public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        Object object = joinPoint.proceed();

        if (object instanceof List) {
            return filterList(joinPoint, object);
        }

        // TODO handle single object
        throw new IllegalArgumentException("Type " + object.getClass() + " is not supported for annotation " + FilterAuthorized.class);
    }

    /**
     * Handle return type List
     * @param joinPoint
     * @return The filtered list
     * @throws Throwable
     */
    public Object filterList(ProceedingJoinPoint joinPoint, Object object) throws Throwable {
        List<Object> returnObjects = (List<Object>) object;

        if (returnObjects.isEmpty()) {
            return returnObjects;
        }

        MethodSignature methodSignature = ((MethodSignature) joinPoint.getSignature());
        FilterAuthorized authz = methodSignature.getMethod().getAnnotation(FilterAuthorized.class);

        String resourceType = authz.resourceType();
        PermissionType permissionType = authz.permissionType();

        Class returnType = methodSignature.getReturnType();
        Class supported = List.class;
        if (!returnType.equals(supported)) {
            throw new IllegalAccessException("Return type must be of type " + List.class.getName() + ", found " + returnType.getName());
        }

        Set<Object> filtered = new HashSet<>();

        for (Object o : returnObjects) {
            String resourceId = findResourceIdFromAnnotatedField(o)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No field annotated with " + FilterResourceId.class + " found for class " + o.getClass()));

            String userId = authorizedUserSubjectProvider.getAuthorizedUserSubject().getId();

            boolean isGranted = enforcer.enforce(userId, resourceType, permissionType.toString());

            if (isGranted) {
                log.debug("Authorization granted for permission {} on resource {}", permissionType, resourceType);
                filtered.add(o);
            } else {
                log.debug("Authorization denied for permission {} on resource {}", permissionType, resourceType);
            }
        }

        return filtered.stream().toList();
    }

    private Optional<String> findResourceIdFromAnnotatedField(Object object) {
        try {
            Field field = Arrays.stream(object.getClass().getDeclaredFields())
                    .filter(f -> f.isAnnotationPresent(FilterResourceId.class))
                    .findFirst()
                    .orElseThrow();

            field.setAccessible(true);
            String value = String.valueOf(field.get(object));
            field.setAccessible(false);

            return Optional.of(value);
        } catch (Exception e) {
            log.error("An error occurred while trying to get resource id field on object " + object.getClass(), e);
        }
        return Optional.empty();
    }

}
