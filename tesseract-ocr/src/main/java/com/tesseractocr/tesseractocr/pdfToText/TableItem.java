package com.tesseractocr.tesseractocr.pdfToText;

import lombok.Data;

import java.util.HashMap;
import java.util.List;

@Data
public class TableItem {
    private Integer noOfChargeInEachShipment;
    private HashMap<String, List<String>>chargeMap;
}
