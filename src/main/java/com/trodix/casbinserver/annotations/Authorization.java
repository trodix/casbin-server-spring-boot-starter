package com.trodix.casbinserver.annotations;

import com.trodix.casbinserver.models.PermissionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Authorization {

    String resourceType();

    PermissionType permissionType();

}
