
package com.ecommerce.devoluciones.garantias.config;

import org.springframework.security.core.Authentication;

public interface UserIdResolver {

    Long obtenerUserId(Authentication authentication);

}
