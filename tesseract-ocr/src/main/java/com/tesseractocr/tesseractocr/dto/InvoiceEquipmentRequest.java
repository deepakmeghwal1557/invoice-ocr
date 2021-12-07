package com.tesseractocr.tesseractocr.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class InvoiceEquipmentRequest {
    private Long invoiceEquipmentId;
    private String equipmentNumber;
    private String equipmentType ;
    private Double volume;
    private String volumeUom;
    private Double weight;
    private String weightQualifier;
    private String weightUom;
    private String sealNumber;
    private String hazmat;
    private String overweight;
    private String comments;
    private Double pieces;
    private Double height;
    private Double width;
    private Double length;
    private String dimensionUom;
    private String packageType;
    private Integer packages;
    private String customsCommodityCode;
    private boolean deleted;
    private List<InvoiceChargesRequest> invoiceChargeDetails = new ArrayList<>();
}