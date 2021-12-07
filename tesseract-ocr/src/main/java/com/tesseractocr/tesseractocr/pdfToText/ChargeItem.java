package com.tesseractocr.tesseractocr.pdfToText;

import lombok.Data;

import java.io.Serializable;

@Data
public class ChargeItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long invoiceChargeId;

    private String chargeCode;

    private String chargeName;

    private String uom;

    private Double unitPrice;

    private String chargeUnit;

    private Double billingAmount;

    private String currencyCode;

    private boolean deleted;

    private String description;

    private Long shipmentChargeId;

    private Long workorderChargeId;

    private String chargeStatus;

    private boolean isInvoiced;

    private String equipmentNumber;

}
