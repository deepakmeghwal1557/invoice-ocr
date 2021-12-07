package com.tesseractocr.tesseractocr.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class InvoiceReferenceRequest {
    private String referenceCode;
    private String referenceValue;
    public String getReferenceCode() {
        return referenceCode;
    }
    public void setReferenceCode(String referenceCode) {
        this.referenceCode = referenceCode;
    }
    public String getReferenceValue() {
        return referenceValue;
    }
    public void setReferenceValue(String referenceValue) {
        this.referenceValue = referenceValue;
    }

}
