package com.tesseractocr.tesseractocr.dto;

import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class InvoicePartyRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private long invoicePartyId;

    private String partyType;

    private Long orgId;

    private String name;

    private String department;

    private String address1;

    private String address2;

    private String city;

    private String state;

    private String zip;

    private String country;

    private String orgCode;

    private Integer dunsNumber;

    private long invoiceContactId;


    private String contactName;

    private String contactEmail;

    private String contactFaxNumber;

    private String contactPhoneNumber;
}