package com.tesseractocr.tesseractocr.pdfToText;

import lombok.Data;

import java.util.List;

@Data
public class FilterCriteria {
    private String methodName;
    private List<String>argsList;

}
