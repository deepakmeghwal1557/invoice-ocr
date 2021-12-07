package com.tesseractocr.tesseractocr.CoordinatesExtracterFromPdf;

import java.io.IOException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.FilteredTextRenderListener;
import com.itextpdf.text.pdf.parser.LocationTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.RegionTextRenderFilter;
import com.itextpdf.text.pdf.parser.RenderFilter;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;

/**
 * Créer par Malek Boubakri le 03/06/2015 à 15:45.
 */

public class ExtractPageContentArea {
    //
    public void parsePdf(float x,float y,float width,float height,String pdf) throws IOException {
        PdfReader reader = new PdfReader(pdf);
        Rectangle rect = new Rectangle(x, y, width, height);
        RenderFilter filter = new RegionTextRenderFilter(rect);
        TextExtractionStrategy strategy;
        for (int i = 1; i <= reader.getNumberOfPages(); i++) {
            strategy = new FilteredTextRenderListener(new LocationTextExtractionStrategy(), filter);
            System.out.println(PdfTextExtractor.getTextFromPage(reader, i, strategy));
        }
        reader.close();
    }
}
