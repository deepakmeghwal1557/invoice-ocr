package com.tesseractocr.tesseractocr.pdfToText;

import lombok.Data;

import java.util.List;

@Data
class RectangleDetailsRegion {
    private Integer x;
    private Integer y;
    private Integer width;
    private Integer height;
    private String labelName;
    private String labelCode;
    private List<FilterCriteria> filterCriteriaList;
    private Boolean isActive;
    private String ocrText;
    private String filterText;
}
