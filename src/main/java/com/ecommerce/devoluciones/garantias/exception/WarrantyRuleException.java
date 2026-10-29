package com.ecommerce.devoluciones.garantias.exception;

public class WarrantyRuleException extends WarrantyException {

    public WarrantyRuleException(String message) {
        super(WarrantyErrorCode.BUSINESS_RULE_VIOLATION, message);
    }

    public WarrantyRuleException(WarrantyErrorCode code, String message) {
        super(code, message);
    }
}