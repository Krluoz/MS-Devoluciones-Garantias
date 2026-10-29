package com.ecommerce.devoluciones.garantias.exception;

public class WarrantyNotFoundException extends WarrantyException {

    public WarrantyNotFoundException(String message) {
        super(WarrantyErrorCode.RESOURCE_NOT_FOUND, message);
    }

    public WarrantyNotFoundException(WarrantyErrorCode code, String message) {
        super(code, message);
    }
}