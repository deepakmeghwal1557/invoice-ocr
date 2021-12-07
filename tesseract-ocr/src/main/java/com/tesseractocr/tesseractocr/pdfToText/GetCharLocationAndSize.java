package com.tesseractocr.tesseractocr.pdfToText;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;

/**
 * This is an example on how to get the x/y coordinates and size of each character in PDF
 */
public class GetCharLocationAndSize extends PDFTextStripper {

    public GetCharLocationAndSize() throws IOException {
    }

    /**
     * @throws IOException If there is an error parsing the document.
     */
    public static void main (String[] args ) throws IOException {
        PDDocument document = null;
        File file = new File("C:\\Users\\deepak.meghwal\\Documents\\Docs\\OCR\\test1\\src\\invoice.pdf");
        try {
            document = PDDocument.load(file);
//            PDPage page = document.getPage(0);
//            PDPageTree pageList = document.getPages();
//            BufferedImage bufferedImage = null;
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            for (int pageInt = 0; pageInt < document.getNumberOfPages(); ++pageInt)
            {
//                BufferedImage bim = pdfRenderer.renderImageWithDPI(pageInt, 300, ImageType.RGB);
                BufferedImage bim = pdfRenderer.renderImage(pageInt);
//                String fileName = OUTPUT_DIR + "image-" + page + ".png";
//                ImageIOUtil.
//                ImageIOUtil.writeImage(bim, fileName, 300);
            }
            document.close();
            float pageWidth =  document.getPage(0).getMediaBox().getWidth();
            float pageHeight =  document.getPage(0).getMediaBox().getHeight();
            PDFTextStripper stripper = new GetCharLocationAndSize();
            stripper.setSortByPosition( true );
            stripper.setStartPage( 0 );
            stripper.setEndPage( document.getNumberOfPages() );

            Writer dummy = new OutputStreamWriter(new ByteArrayOutputStream());
            stripper.writeText(document, dummy);
        }
        finally {
            if( document != null ) {
                document.close();
            }
        }
    }

    /**
     * Override the default functionality of PDFTextStripper.writeString()
     */
    @Override
    protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
        for (TextPosition text : textPositions) {
            System.out.println(text.getUnicode()+ " [(X=" + text.getXDirAdj() + ",Y=" +
                    text.getYDirAdj() + ") height=" + text.getHeightDir() + " width=" +
                    text.getWidthDirAdj() + "]");
        }
    }
}