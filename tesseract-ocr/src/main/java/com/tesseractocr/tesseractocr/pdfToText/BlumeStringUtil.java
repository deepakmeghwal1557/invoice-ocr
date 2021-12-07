package com.tesseractocr.tesseractocr.pdfToText;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@NoArgsConstructor
public class BlumeStringUtil {

    public String replaceAll(String str, String regex, String replacement){
        return str.replaceAll(regex, replacement);
    }

    public String substringAfter(String str, String separator){
        return StringUtils.substringAfter(str, separator);
    }

    public String trim(String str){
        return str.trim();
    }
}
