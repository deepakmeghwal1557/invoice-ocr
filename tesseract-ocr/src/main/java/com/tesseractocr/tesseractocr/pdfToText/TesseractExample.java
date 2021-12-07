package com.tesseractocr.tesseractocr.pdfToText;

import com.google.gson.stream.JsonReader;
import com.tesseractocr.tesseractocr.dto.*;
import lombok.Data;
import net.sourceforge.tess4j.*;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.Word;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;

public class TesseractExample {

    private static String sourceFolderPath ="C:\\Users\\deepak.meghwal\\Documents\\Docs\\OCR\\test1\\src\\";
    private static String destinationFolderPath="C:\\Users\\deepak.meghwal\\Documents\\Docs\\OCR\\test1\\dst\\";

    public static void main (String[] args )  {
        List<String> textFiles = new ArrayList<String>();
        File dir = new File(sourceFolderPath);
        for (File file : dir.listFiles()) {
            try {
                runOCROnFile(file);
                System.out.println(file.getName()+" Done");
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
        }

        System.out.println("All Files are done");
    }

    public static void runOCROnFile(File file) throws TesseractException{
//        File file = new File(sourceFolderPath +"invoice.pdf");

        ITesseract instance = new Tesseract();  // JNA Interface Mapping
        instance.setDatapath("tessdata"); // path to tessdata directory


        PredictionConfig predictionConfig = jsonTOPredictionConfig();

        TableItem tableItem = new TableItem();
        countShipmentItems(instance, file,tableItem);

        for(RectangleDetailsRegion prediction : predictionConfig.getPredictions()){
            if(prediction.getIsActive()){
                Rectangle rectangle = new Rectangle(prediction.getX(), prediction.getY(), prediction.getWidth(), prediction.getHeight());
                String result = instance.doOCR(file, rectangle);
                prediction.setOcrText(result);
                applyFilterCriteria(prediction);
            }
        }

        Date currDate = new Date();
        Gson gson = new Gson();
        StringBuffer sb = new StringBuffer();
        sb.append("predictionConfig=>");
        sb.append(gson.toJson(predictionConfig));

        System.out.println(predictionConfig);

        InvoiceRequest invoiceRequest = new InvoiceRequest();
        mapperPredictionConfigToInvoiceRequest(predictionConfig, invoiceRequest);
        mapperTableItemsToInvoiceRequest(tableItem, invoiceRequest);
        System.out.println("invoiceRequest=>"+invoiceRequest);


        sb.append(" \n invoiceRequest=>");
        sb.append(gson.toJson(invoiceRequest));

        try {
            FileWriter resultFile = new FileWriter(destinationFolderPath+"ORC_JSON"+currDate.getTime()+".txt");
            resultFile.write(sb.toString());
            resultFile.close();
        } catch (Exception e) {
            System.err.println("Getting Error while saving the file = "+e.getMessage());
        }
    }



    public static PredictionConfig jsonTOPredictionConfig(){
        PredictionConfig predictionConfig = null;
        Gson gson = new Gson();
        try {
            JsonReader jsonReader = new JsonReader(new FileReader("./src/main/resources/config/BlumeInvoice3.json"));
            predictionConfig = gson.fromJson(jsonReader, PredictionConfig.class);
        }catch (Exception e){
            System.out.println("Getting exception while reading json file");
        }

        return predictionConfig;
    }

    public static void applyFilterCriteria(RectangleDetailsRegion prediction){
        String result = prediction.getOcrText();
        if(StringUtils.isNotBlank(result)){

            for(FilterCriteria currFilter : prediction.getFilterCriteriaList()){
                if(currFilter.getMethodName().equalsIgnoreCase("replaceAll")){
                    result = result.replaceAll(currFilter.getArgsList().get(0),currFilter.getArgsList().get(1));
                }else if(currFilter.getMethodName().equalsIgnoreCase("substringAfter")){
                    result = StringUtils.substringAfter(result, currFilter.getArgsList().get(0));
                }else if(currFilter.getMethodName().equalsIgnoreCase("trim")){
                    result = result.trim();
                }else if(currFilter.getMethodName().equalsIgnoreCase("substringBetween")){
                    result = StringUtils.substringBetween(result, currFilter.getArgsList().get(0), currFilter.getArgsList().get(1));
                }
            }

            prediction.setFilterText(result);

        }

    }

    public static HashMap<String, RectangleDetailsRegion> createMap(PredictionConfig predictionConfig){
            HashMap<String, RectangleDetailsRegion>map = new HashMap<>();

        predictionConfig.getPredictions().stream().forEach(currItem ->{
            map.put(currItem.getLabelCode(), currItem);
        });

        return map;

    }


    public static void mapperPredictionConfigToInvoiceRequest(PredictionConfig predictionConfig, InvoiceRequest invoiceRequest){
        HashMap<String, RectangleDetailsRegion>map = createMap(predictionConfig);

        if(map.containsKey("invoiceNumber") && StringUtils.isNotBlank(map.get("invoiceNumber").getFilterText())){
            invoiceRequest.setInvoiceNumber(map.get("invoiceNumber").getFilterText());
        }

        if(map.containsKey("dateSent") && StringUtils.isNotBlank(map.get("dateSent").getFilterText())){
            SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yy");
                    //new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            try {
                Date dateSent = format.parse(map.get("dateSent").getFilterText());
                invoiceRequest.setDateSent(dateSent);
            } catch (ParseException e) {
                invoiceRequest.setDateSent(new Date());
                System.out.println(e.getMessage());
            }
        }

        List<InvoicePartyRequest>invoicePartyRequestList = new ArrayList<>();
        InvoicePartyRequest billToParty = new InvoicePartyRequest();
        InvoicePartyRequest remitToParty = new InvoicePartyRequest();

        if(map.containsKey("billToParty") && StringUtils.isNotBlank(map.get("billToParty").getFilterText())){
            billToParty.setName(map.get("billToParty").getFilterText());
            billToParty.setPartyType("BillTo");
        }
        if(map.containsKey("remitToParty") && StringUtils.isNotBlank(map.get("remitToParty").getFilterText())){
            remitToParty.setName(map.get("remitToParty").getFilterText());
            remitToParty.setPartyType("RemitTo");
        }

        invoicePartyRequestList.add(billToParty);
        invoicePartyRequestList.add(remitToParty);
        invoiceRequest.setInvoicePartyDetails(invoicePartyRequestList);


        if(map.containsKey("mode") && StringUtils.isNotBlank(map.get("mode").getFilterText())){
            invoiceRequest.setMode(map.get("mode").getFilterText());
        }

        if(map.containsKey("invoiceType") && StringUtils.isNotBlank(map.get("invoiceType").getFilterText())){
            invoiceRequest.setInvoiceType(map.get("invoiceType").getFilterText());
        }


        List<InvoiceShipmentRequest> invoiceShipmentRequestList = new ArrayList<>();
//        List<InvoiceEquipmentRequest>invoiceEquipmentRequestList = new ArrayList<>();
//        List<InvoiceChargesRequest>invoiceChargesRequestList = new ArrayList<>();

        invoiceRequest.setShipmentDetails(invoiceShipmentRequestList);
        InvoiceShipmentRequest invoiceShipmentRequest = new InvoiceShipmentRequest();
        invoiceShipmentRequestList.add(invoiceShipmentRequest);
//        invoiceShipmentRequest.setInvoiceEquipmentDetails(invoiceEquipmentRequestList);

//        InvoiceEquipmentRequest invoiceEquipmentRequest = new InvoiceEquipmentRequest();
//        invoiceEquipmentRequestList.add(invoiceEquipmentRequest);
//        invoiceEquipmentRequest.setInvoiceChargeDetails(invoiceChargesRequestList);
//
//        InvoiceChargesRequest invoiceChargesRequest = new InvoiceChargesRequest();
//        invoiceChargesRequestList.add(invoiceChargesRequest);



        if(map.containsKey("shipmentNumber") && StringUtils.isNotBlank(map.get("shipmentNumber").getFilterText())){
            invoiceShipmentRequest.setShipmentNumber(map.get("shipmentNumber").getFilterText());
        }

//        if(map.containsKey("equipmentNumber") && StringUtils.isNotBlank(map.get("equipmentNumber").getFilterText())){
//            invoiceEquipmentRequest.setEquipmentNumber(map.get("equipmentNumber").getFilterText());
//        }
//
//        if(map.containsKey("chargeCode") && StringUtils.isNotBlank(map.get("chargeCode").getFilterText())){
//            invoiceChargesRequest.setChargeCode(map.get("chargeCode").getFilterText());
//        }
//        if(map.containsKey("chargeName") && StringUtils.isNotBlank(map.get("chargeName").getFilterText())){
//            invoiceChargesRequest.setChargeName(map.get("chargeName").getFilterText());
//        }
//        if(map.containsKey("billingAmount") && StringUtils.isNotBlank(map.get("billingAmount").getFilterText())){
//            String amountCurrency = map.get("billingAmount").getFilterText();
//            if(amountCurrency.length()>=3){
//                String amountStr = amountCurrency.substring(0,amountCurrency.length()-3);
//                String currency = amountCurrency.substring(amountCurrency.length()-3);
//                Double amount = new Double(0);
//                try {
//                    amount = Double.valueOf(amountStr);
//                }catch (Exception e){
//
//                }
//                invoiceChargesRequest.setBillingAmount(amount);
//                invoiceChargesRequest.setCurrencyCode(currency);
//            }
//
//        }
//        if(map.containsKey("description") && StringUtils.isNotBlank(map.get("description").getFilterText())){
//            invoiceChargesRequest.setDescription(map.get("description").getFilterText());
//        }

        if(map.containsKey("totalAmount") && StringUtils.isNotBlank(map.get("totalAmount").getFilterText())){
            String amountCurrency = map.get("totalAmount").getFilterText();
            if(amountCurrency.length()>=3){
                String amountStr = amountCurrency.substring(0,amountCurrency.length()-3);
                String currency = amountCurrency.substring(amountCurrency.length()-3);
                Double amount = new Double(0);
                try {
                    amount = Double.valueOf(amountStr);
                }catch (Exception e){

                }
                invoiceRequest.setTotalAmount(Double.valueOf(amount));
                invoiceRequest.setCurrencyCode(currency);
            }
        }
    }

    public static void mapperTableItemsToInvoiceRequest(TableItem tableItem, InvoiceRequest invoiceRequest){
        int noOfItems = tableItem.getNoOfChargeInEachShipment();
        HashMap<String,List<InvoiceChargesRequest>>equipmentMap = new HashMap<>();

        List<String>equipmentNoList =  tableItem.getChargeMap().get("equipmentNumber");
        List<String>chargeCodeList  =  tableItem.getChargeMap().get("chargeCode");
        List<String>chargeNameList =  tableItem.getChargeMap().get("chargeName");
        List<String>totalChargeList =  tableItem.getChargeMap().get("billingAmount");
        List<String>commentList     =  tableItem.getChargeMap().get("description");

        List<ChargeItem>chargeItemList = new ArrayList<>();

        for(int i=0; i<noOfItems; i++){
            ChargeItem chargeItem = new ChargeItem();
            if(equipmentNoList.size()>i){ chargeItem.setEquipmentNumber(equipmentNoList.get(i)); }
            if(chargeCodeList.size()>i){ chargeItem.setChargeCode(chargeCodeList.get(i)); }
            if(chargeNameList.size()>i){ chargeItem.setChargeName(chargeNameList.get(i)); }
            if(commentList.size()>i){ chargeItem.setDescription(commentList.get(i)); }
            if(totalChargeList.size()>i){
                String amountCurrency = totalChargeList.get(i);
                if(amountCurrency.length()>=3){
                    String amountStr = amountCurrency.substring(0,amountCurrency.length()-3);
                    String currency = amountCurrency.substring(amountCurrency.length()-3);
                    Double amount = new Double(0);
                    try {
                        amount = Double.valueOf(amountStr);
                    }catch (Exception e){

                    }
                    chargeItem.setBillingAmount(amount);
                    chargeItem.setCurrencyCode(currency);
                }
            }

            chargeItemList.add(chargeItem);
        }

        List<InvoiceEquipmentRequest>invoiceEquipmentRequestList = new ArrayList<>();
        for(ChargeItem chargeItem : chargeItemList){
            if(!equipmentMap.containsKey(chargeItem.getEquipmentNumber())){
                equipmentMap.put(chargeItem.getEquipmentNumber(), new ArrayList<InvoiceChargesRequest>());
            }
            InvoiceChargesRequest invoiceChargesRequest = new InvoiceChargesRequest();
            invoiceChargesRequest.setChargeCode(chargeItem.getChargeCode());
            invoiceChargesRequest.setChargeName(chargeItem.getChargeName());
            invoiceChargesRequest.setBillingAmount(chargeItem.getBillingAmount());
            invoiceChargesRequest.setCurrencyCode(chargeItem.getCurrencyCode());
            invoiceChargesRequest.setDescription(chargeItem.getDescription());

            equipmentMap.get(chargeItem.getEquipmentNumber()).add(invoiceChargesRequest);

        }

        for(Map.Entry<String, List<InvoiceChargesRequest>> entry : equipmentMap.entrySet()){
            InvoiceEquipmentRequest invoiceEquipmentRequest = new InvoiceEquipmentRequest();
            invoiceEquipmentRequest.setEquipmentNumber(entry.getKey());
            invoiceEquipmentRequest.setInvoiceChargeDetails(entry.getValue());
            invoiceEquipmentRequestList.add(invoiceEquipmentRequest);
        }

        if(invoiceRequest.getShipmentDetails().size()==0){
            InvoiceShipmentRequest invoiceShipmentRequest = new InvoiceShipmentRequest();
            List<InvoiceShipmentRequest> invoiceShipmentRequestList = new ArrayList<>();
            invoiceShipmentRequestList.add(invoiceShipmentRequest);
            invoiceRequest.setShipmentDetails(invoiceShipmentRequestList);
        }

        invoiceRequest.getShipmentDetails().get(0).setInvoiceEquipmentDetails(invoiceEquipmentRequestList);

    }

    public static void countShipmentItems(ITesseract instance, File file, TableItem tableItem){
        try {
            Rectangle equipmentNoRect = new Rectangle(0, 1000, 500, 2000);
            Rectangle chargeCodeRect = new Rectangle(500, 1000, 500, 2000);
            Rectangle chargeNameRect = new Rectangle(1000, 1000, 350, 2000);
            Rectangle totalChargeRect = new Rectangle(1700, 1000, 300, 2000);
            Rectangle commentRect = new Rectangle(2100, 1000, 500, 300);


            String equipmentNoResult = instance.doOCR(file, equipmentNoRect);
            String chargeCodeResult = instance.doOCR(file, chargeCodeRect);
            String chargeNameResult = instance.doOCR(file, chargeNameRect);
            String totalChargeResult = instance.doOCR(file, totalChargeRect);
            String commentResult = instance.doOCR(file, commentRect);

            equipmentNoResult =  StringUtils.substringAfter(equipmentNoResult, "Equipment#");
            chargeCodeResult  =  StringUtils.substringAfter(chargeCodeResult, "Code");
            chargeNameResult  =  StringUtils.substringAfter(chargeNameResult, "Name");
            totalChargeResult =  StringUtils.substringAfter(totalChargeResult, "Charge");
            commentResult     =  StringUtils.substringAfter(commentResult, "Comments");

            String[] equipmentNoArrLines = Arrays.stream(equipmentNoResult.split("\n")).filter(str -> StringUtils.isNotBlank(str)).toArray(String[]::new);
            String[] chargeCodeArrLines  = Arrays.stream(chargeCodeResult.split("\n")).filter(str -> StringUtils.isNotBlank(str)).toArray(String[]::new);
            String[] chargeNameArrLines = Arrays.stream(chargeNameResult.split("\n")).filter(str -> StringUtils.isNotBlank(str)).toArray(String[]::new);
            String[] totalChargeArrLines = Arrays.stream(totalChargeResult.split("\n")).filter(str -> StringUtils.isNotBlank(str)).toArray(String[]::new);
            String[] commentArrLines = Arrays.stream(commentResult.split("\n")).filter(str -> StringUtils.isNotBlank(str)).toArray(String[]::new);

            int noOfItemInShipment = Math.min(chargeCodeArrLines.length, totalChargeArrLines.length);

            HashMap<String, List<String>>map = new HashMap<>();
            map.put("equipmentNumber", new ArrayList<>());
            map.put("chargeCode", new ArrayList<>());
            map.put("chargeName", new ArrayList<>());
            map.put("billingAmount", new ArrayList<>());
            map.put("description", new ArrayList<>());

            for(int i=0; i<Math.min(noOfItemInShipment, equipmentNoArrLines.length); i++){
                map.get("equipmentNumber").add(equipmentNoArrLines[i]);
            }
            for(int i=0; i<Math.min(noOfItemInShipment, chargeCodeArrLines.length); i++){
                map.get("chargeCode").add(chargeCodeArrLines[i]);
            }
            for(int i=0; i<Math.min(noOfItemInShipment, chargeNameArrLines.length); i++){
                map.get("chargeName").add(chargeNameArrLines[i]);
            }
            for(int i=0; i<Math.min(noOfItemInShipment, totalChargeArrLines.length); i++){
                map.get("billingAmount").add(totalChargeArrLines[i]);
            }
            for(int i=0; i<Math.min(noOfItemInShipment, commentArrLines.length); i++){
                map.get("description").add(commentArrLines[i]);
            }

            tableItem.setChargeMap(map);
            tableItem.setNoOfChargeInEachShipment(noOfItemInShipment);

        } catch (Exception e) {
            System.err.println("Getting Error = "+e.getMessage());
        }
    }

//    public static void countShipmentItems(ITesseract instance, File file, List<Integer>chargeItemsInEachShipment){
//        try {
//            Rectangle rectangle = new Rectangle(500, 1000, 500, 2000);
//            String result = instance.doOCR(file, rectangle);
//
//            result = result.replaceAll(" ", "");
//            result = StringUtils.substringAfter(result, "ChargeCode");
//
//            Rectangle rectangle2 = new Rectangle(1700, 1000, 300, 2000);
//            String result2 = instance.doOCR(file, rectangle2);
//            result2 = result2.replaceAll(" ", "");
//            result2 = StringUtils.substringAfter(result2, "TotalCharge");
//            System.out.println("result => "+result2);
//
//            String[] chargeCodeArr =  result.split("ChargeCode");
//            String[] totalChargeArr =  result2.split("TotalCharge");
//
//            int noOfShipment = Math.min(chargeCodeArr.length, totalChargeArr.length);
//            Integer[] noOfItemInEachShipment = new Integer[noOfShipment];
//
//            for(int i=0; i<noOfShipment; i++){
//                String[] chargeCodeArrLines  = Arrays.stream(chargeCodeArr[i].split("\n")).filter(str -> StringUtils.isNotBlank(str)).toArray(String[]::new);
//                String[] totalChargeArrLines = Arrays.stream(totalChargeArr[i].split("\n")).filter(str -> StringUtils.isNotBlank(str)).toArray(String[]::new);
//                noOfItemInEachShipment[i] = Math.min(chargeCodeArrLines.length, totalChargeArrLines.length);
//            }
//
//            chargeItemsInEachShipment.addAll(Arrays.asList(noOfItemInEachShipment));
//
//        } catch (Exception e) {
//            System.err.println("Getting Error = "+e.getMessage());
//        }
//    }

}



//    public static void main (String[] args ) throws TesseractException {
//
//        File file = new File(sourceFolderPath +"invoice.pdf");
//
//        ITesseract instance = new Tesseract();  // JNA Interface Mapping
//        instance.setDatapath("tessdata"); // path to tessdata directory
//
//        PredictionConfig predictionConfig = jsonTOPredictionConfig();
////        System.out.println(predictionConfig);
//
//        for(RectangleDetailsRegion prediction : predictionConfig.getPredictions()){
//            if(prediction.getIsActive()){
//                Rectangle rectangle = new Rectangle(prediction.getX(), prediction.getY(), prediction.getWidth(), prediction.getHeight());
//                String result = instance.doOCR(file, rectangle);
//                prediction.setOcrText(result);
//                applyFilterCriteria(prediction);
////                System.out.println(prediction.toString());
//            }
//        }
//
//        Date currDate = new Date();
//        Gson gson = new Gson();
//        try {
//            FileWriter resultFile = new FileWriter(destinationFolderPath+"ORC_JSON"+currDate.getTime()+".txt");
//            resultFile.write(gson.toJson(predictionConfig));
//            resultFile.close();
//        } catch (Exception e) {
//            System.err.println("Getting Error while saving the file = "+e.getMessage());
//        }
//
//        System.out.println(predictionConfig);
//
////        try {
////            Rectangle rectangle = new Rectangle(1700, 1200, 800, 1000);
////            String result = instance.doOCR(file, rectangle);
////            System.out.println("result => "+result);
////            String s1 = result.replaceAll("\n"," ");
////            String s2 = StringUtils.substringAfter(s1, "Total").trim();
////            System.out.println("s2="+s2);
////
//////            result = result.replaceAll("\n"," ");
//////            result = StringUtils.substringAfter(result, "Total").trim();
////
////
//////            String arg1="\n", arg2=" ";
//////            String methodName = "replaceAll";
//////            String obj = new String();
//////
//////            Method method = obj.getClass().getMethod(methodName, String.class, String.class);
//////            result = (String) method.invoke(result, arg1, arg2);
//////
//////            arg1="Total";
//////            methodName = "substringAfter";
//////            StringUtils obj2 = new StringUtils();
//////
//////            method = obj2.getClass().getMethod(methodName, String.class, String.class);
//////            result = (String) method.invoke(result, result, arg1);
//////
//////            methodName = "trim";
//////
//////            method = obj.getClass().getMethod(methodName);
//////            result = (String) method.invoke(result);
////
////
////            String arg1="\n", arg2=" ";
////            String methodName = "replaceAll";
////            BlumeStringUtil blumeStringUtil = new BlumeStringUtil();
////            Method method = blumeStringUtil.getClass().getMethod(methodName, ...);
////
////
////
////            System.out.println("result="+result);
////
////        } catch (Exception e) {
////            System.err.println("Getting Error = "+e.getMessage());
////        }
////
////        try {
////            Rectangle rectangle = new Rectangle(1400, 100, 950, 200);
////            String result = instance.doOCR(file, rectangle);
////            System.out.println("result => "+result);
////
////            String s1 = result.replaceAll("Invoice#| |\n","");
////            System.out.println("s1="+s1);
////
////        } catch (Exception e) {
////            System.err.println("Getting Error = "+e.getMessage());
////        }
//
//
//
//
////        try{
//////            GetCharLocationAndSize getCharLocationAndSize = new GetCharLocationAndSize();
//////            getCharLocationAndSize.findPositions(imageFile);
////        }catch (Exception e){
////            System.err.println("Getting Error = "+e.getMessage());
////        }
//
//
//
////
//////        Rectangle rectangle = new Rectangle(0,0, 595, 841);
////        PDDocument document = null;
////        List<Word>allWordList = new ArrayList<>();
////        List<Rectangle>allWordBoundingBox = new ArrayList<>();
////        try {
////            document = PDDocument.load(file);
////            String result = instance.doOCR(file);
////            PDFRenderer pdfRenderer = new PDFRenderer(document);
////            for (int pageInt = 0; pageInt < document.getNumberOfPages(); ++pageInt)
////            {
////                BufferedImage bim = pdfRenderer.renderImage(pageInt);
////                List<Word>wordList = instance.getWords(bim,0);
////                allWordList.addAll(wordList);
////            }
////
////            for(Word word : allWordList){
////                Rectangle currRectangle =  word.getBoundingBox();
////                allWordBoundingBox.add(currRectangle);
////            }
////
////
////            System.out.println("result => "+result);
////            Date currDate = new Date();
////            FileWriter resultFile = new FileWriter(destinationFolderPath+"ORC_"+currDate.getTime()+".txt");
////            resultFile.write(result);
////            resultFile.close();
////        } catch (Exception e) {
////            System.err.println("Getting Error = "+e.getMessage());
////        }
//
//        //////////==================>>>>>>>>>>>>>>>>>>>>>>
//
//
//
//
////        try {
////            String result = instance.doOCR(file);
////            System.out.println("result => "+result);
////            Date currDate = new Date();
////            FileWriter resultFile = new FileWriter(destinationFolderPath+"ORC_"+currDate.getTime()+".txt");
////            resultFile.write(result);
////            resultFile.close();
////        } catch (Exception e) {
////            System.err.println("Getting Error = "+e.getMessage());
////        }
//    }
