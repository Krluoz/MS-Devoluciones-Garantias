package com.ecommerce.devoluciones.garantias.exception;

public abstract class WarrantyException extends RuntimeException {

    private final WarrantyErrorCode code;

    protected WarrantyException(WarrantyErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public WarrantyErrorCode getCode() {
        return code;
    }
}