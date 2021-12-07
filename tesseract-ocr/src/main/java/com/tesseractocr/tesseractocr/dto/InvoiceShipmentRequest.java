package com.tesseractocr.tesseractocr.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class InvoiceShipmentRequest implements Serializable{

    private static final long serialVersionUID = 1L;
    private long shipmentId ;
    private String shipmentNumber ;
    private String loadType;
    private boolean deleted;

    private long shipmentLegId;

    private String originLocationCode;

    private String originName;

    private String originLocationType;

    private String originAddress1;

    private String originAddress2;

    private String originCity;

    private String originState;

    private String originPostalCode;

    private String originCountry;

    private String originLocationId;

    private String originPlusCode;

    private String destinationLocationCode;

    private String destinationName;

    private String destinationLocationType;

    private String destinationAddress1;

    private String destinationAddress2;

    private String destinationCity;

    private String destinationState;

    private String destinationPostalCode;

    private String destinationCountry;

    private String destinationLocationId;

    private String destinationPlusCode;

    private Date pickupDate;

    private Date deliveryDate;

    private Date shipmentDate;

    private String shipmentType;

    private String polUnlocode;

    private String portOfLoading;

    private String podUnlocode;

    private String portOfDischarge;

    private String mode;

    private String vesselCode;

    private String vesselName;

    private String voyage;

    private Date eta;

    private Date etd;

    private String proNumber;

    private String toNumber;

    private String incoterm;

    private boolean workOrder;

    private boolean createShipment;

    private String workorderNumber;

    private String serviceLevel;

    private List<InvoiceEquipmentRequest> invoiceEquipmentDetails = new ArrayList<>();

    private List<InvoiceReferenceRequest> references = new ArrayList<>();



}

