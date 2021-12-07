package com.tesseractocr.tesseractocr.pdfToText;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PredictionConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    private String invoiceName;
    List<RectangleDetailsRegion>predictions;
    List<RectangleDetailsRegion>tablePredictions;
}
