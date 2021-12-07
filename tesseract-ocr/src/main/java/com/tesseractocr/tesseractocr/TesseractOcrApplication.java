package com.tesseractocr.tesseractocr;

import com.tesseractocr.tesseractocr.pdfToText.TesseractExample;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.tesseractocr")
public class TesseractOcrApplication {

	public static void main(String[] args) {
		SpringApplication.run(TesseractOcrApplication.class, args);
		System.out.println("Application is running");

//		TesseractExample tesseractExample = new TesseractExample();
//		tesseractExample.doOCR();

	}

}
