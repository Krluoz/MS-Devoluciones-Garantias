package com.ecommerce.devoluciones.garantias.exception;

public enum WarrantyErrorCode {

    // Específicos de createClaim / getClaimStatus
    USER_NOT_FOUND,
    ORDER_NOT_FOUND,
    PRODUCT_NOT_IN_ORDER,
    CLAIM_NOT_FOUND,
    WARRANTY_EXPIRED,
    QUANTITY_EXCEEDS_AVAILABLE,
    DUPLICATE_PRODUCT_IN_REQUEST,

    // Del handler (errores de formato de la petición)
    VALIDATION_ERROR,
    MALFORMED_REQUEST,
    INVALID_PARAMETER,
    INTERNAL_ERROR,

    // Por defecto cuando alguien lanza la excepción sin código
    RESOURCE_NOT_FOUND,
    BUSINESS_RULE_VIOLATION,
    BAD_REQUEST,

    // Seguridad
    UNAUTHORIZED,
    FORBIDDEN
}
