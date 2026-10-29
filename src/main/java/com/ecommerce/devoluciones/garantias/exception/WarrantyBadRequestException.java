package com.ecommerce.devoluciones.garantias.exception;

public class WarrantyBadRequestException extends WarrantyException {

    public WarrantyBadRequestException(String message) {
        super(WarrantyErrorCode.BAD_REQUEST, message);
    }

    public WarrantyBadRequestException(WarrantyErrorCode code, String message) {
        super(code, message);
    }
}