package com.trodix.casbinserver.configuration;

import com.trodix.casbinserver.models.AuthorizedUserSubject;

public interface AuthorizedUserSubjectProvider {

    AuthorizedUserSubject getAuthorizedUserSubject();

}
