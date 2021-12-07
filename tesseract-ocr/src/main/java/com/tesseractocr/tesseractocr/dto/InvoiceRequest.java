package com.tesseractocr.tesseractocr.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class InvoiceRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private long invoiceId;

    private int invoiceStatus;


    private String paymentTerms;

    private String paymentMethod;


    private Date paymentDueDate;

    private Date dateSent;

    private double totalAmount;

    private String currencyCode;

    private String createdBy;

    private String modifiedBy;

    private String additionalNotes;

    private String invoiceType;

    private Integer orgID;

    private String orgCode;

    private String originatorCode;

    private String carrierCode;

    private String sourceType;

    private String invoiceNumber;

    private String invoiceChargeType;

    private String mode;

    private List<InvoicePartyRequest> invoicePartyDetails = new ArrayList<>();

    private List<InvoiceShipmentRequest> shipmentDetails = new ArrayList<>();

}
