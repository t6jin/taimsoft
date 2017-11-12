package com.taimsoft.desktopui.util;

import com.taim.dto.*;
import com.taim.dto.basedtos.UserBaseModelDTO;
import com.taim.model.Transaction;
import com.taimsoft.desktopui.reports.Invoice;
import com.taimsoft.desktopui.reports.InvoiceData;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import org.apache.log4j.Logger;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;

/**
 * Created by tjin on 2/5/2016.
 */
public class InvoiceGenerator {
    private static Logger logger = Logger.getLogger(InvoiceGenerator.class);

    private static Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 20);
    private static Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 18);
    private static Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 14,Font.BOLD);
    private static Font tableTitle = new Font(Font.FontFamily.COURIER, 11, Font.BOLD,BaseColor.WHITE);
    private static Font totalFont = new Font(Font.FontFamily.COURIER, 9, Font.BOLD);
    private static Font addressFont = new Font(Font.FontFamily.TIMES_ROMAN, 12);
    private static Font smallText = new Font(Font.FontFamily.TIMES_ROMAN, 8);
    private static Font midText = new Font(Font.FontFamily.TIMES_ROMAN, 12);
    private static Font largeText = new Font(Font.FontFamily.TIMES_ROMAN, 13);
    private static Font tinyBold = new Font(Font.FontFamily.TIMES_ROMAN, 8,Font.BOLD);
    private static final String CHINESE_FONT_LOCATION = "/fonts/Deng.ttf";
    Font chineseFont = new Font(BaseFont.createFont(CHINESE_FONT_LOCATION, BaseFont.IDENTITY_H, BaseFont.EMBEDDED), 9);

    private String destination;
    private Invoice invoice;
    private StringBuilder errorMsg;

    public enum InvoiceType{
        INVOICE("Invoice"), QUOTATION("Quotation"), DELIVERY("Delivery");

        private String type;
        InvoiceType(String type){
            this.type = type;
        }
    }

    public InvoiceGenerator(String destination) throws IOException, DocumentException{
        this.destination = destination;
        this.errorMsg = new StringBuilder();
    }

    public void buildInvoice(TransactionDTO transaction, UserBaseModelDTO customer, UserBaseModelDTO staff, PropertyDTO property) throws Exception {
        invoice = new Invoice(transaction, customer, staff, property);
        createInvoice(invoice);
    }

    public void buildDelivery(TransactionDTO transaction, UserBaseModelDTO customer, UserBaseModelDTO staff, PropertyDTO property) throws Exception{
        invoice = new Invoice(transaction, customer, staff, property);
        createDelivery(invoice);

    }

    public void buildQuotation(TransactionDTO transaction, UserBaseModelDTO customer, UserBaseModelDTO staff, PropertyDTO property) throws Exception {
        invoice = new Invoice(transaction, customer, staff, property);
        createQuotation(invoice);
    }

    public void createDelivery(Invoice invoice) throws Exception {
        InvoiceData invoiceData = new InvoiceData();
        InvoiceData.AdvancedProfileImp basic = invoiceData.createBasicProfileData(invoice);

        //init
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(destination));
        document.open();
        //Create Header;
        createHeader(document, basic, InvoiceType.DELIVERY, invoice.getTransaction());
        // Address seller / buyer
        createPartyAddress(document, basic);
        // line items
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);
        table.setWidths(new int[]{3, 3, 3, 3, 3});
        table.addCell(getCellTitle("Item", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
        table.addCell(getCellTitle("Name", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
        table.addCell(getCellTitle("Size", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
        table.addCell(getCellTitle("Qty(Boxes)", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
        table.addCell(getCellTitle("Remark", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
        int row=0;
        for (TransactionDetailDTO transactionDetail : invoice.getTransaction().getTransactionDetails()) {
            table.addCell(getCellwithBackground(transactionDetail.getProduct().getSku(), Element.ALIGN_LEFT, totalFont, row));
            table.addCell(getCellwithBackground(transactionDetail.getProduct().getDisplayName(), Element.ALIGN_LEFT, totalFont, row));
            table.addCell(getCellwithBackground(String.format("%s*%s*%s", transactionDetail.getProduct().getLength(),
                    transactionDetail.getProduct().getWidth(), transactionDetail.getProduct().getHeight()), Element.ALIGN_LEFT, totalFont, row));
            int boxes = transactionDetail.getPackageInfo().getBox();
            int res = transactionDetail.getPackageInfo().getPieces();
            String displayNum = boxes+" boxes";
            if(res!=0){
                displayNum+=", "+res+" pieces";
            }
            table.addCell(getCellwithBackground(displayNum, Element.ALIGN_LEFT, totalFont, row));
            table.addCell(getCellwithBackground(transactionDetail.getNote(), Element.ALIGN_LEFT, chineseFont, row));
            row++;
        }
        document.add(table);
        //Add PaymentInfo
        createPaymentInfo(document, InvoiceType.DELIVERY);
        //Add Signature
        PdfPTable signTable = new PdfPTable(2);
        signTable.setWidthPercentage(86);
        signTable.addCell(getCellNoWrap("Packing by: _________________", Element.ALIGN_LEFT, largeText));
        signTable.addCell(getCellNoWrap("Customer Sign: ________________", Element.ALIGN_RIGHT, largeText));
        signTable.addCell(getCellNoWrap(" ", Element.ALIGN_CENTER,largeText ));
        signTable.addCell(getCellNoWrap(" ", Element.ALIGN_CENTER, largeText));
        signTable.addCell(getCellNoWrap("Date:         __________________", Element.ALIGN_LEFT, largeText));
        signTable.addCell(getCellNoWrap("Date:            ___________________", Element.ALIGN_RIGHT, largeText));
        if(signTable.getTotalWidth()==0)signTable.setTotalWidth((document.right()-document.left())*signTable.getWidthPercentage()/100f);
        signTable.writeSelectedRows(0, -1, (document.left()+document.right()-signTable.getTotalWidth())/2f, document.bottom() + signTable.getTotalHeight()*3f, writer.getDirectContent());

        //Create CompanyClaims
        createCompanyClaims(document);
        document.close();
        openPDF(this.destination);
    }


    public void createInvoice(Invoice invoice) throws Exception {
        InvoiceData invoiceData = new InvoiceData();
        InvoiceData.AdvancedProfileImp basic = invoiceData.createBasicProfileData(invoice);

        //init
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(destination));
        document.open();
        // header
        createHeader(document, basic, InvoiceType.INVOICE, invoice.getTransaction());
        //gstNum Field
        createGSTNo(document);
        // Address seller / buyer
        createPartyAddress(document, basic);

        // line items
        createItemList(document, InvoiceType.INVOICE);
        //Add PaymentInfo
        createPaymentInfo(document, InvoiceType.INVOICE);
        //Add Company Claims
        createCompanyClaims(document);
        document.close();
        openPDF(this.destination);
    }

    public void createQuotation(Invoice invoice) throws Exception{
        InvoiceData invoiceData = new InvoiceData();
        InvoiceData.AdvancedProfileImp basic = invoiceData.createBasicProfileData(invoice);
        //init
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(destination));
        document.open();
        // header
        createHeader(document, basic, InvoiceType.QUOTATION, invoice.getTransaction());
        //gstNum Field
        createGSTNo(document);
        // Address seller / buyer
        createPartyAddress(document, basic);
        // line items
        createItemList(document, InvoiceType.QUOTATION);
        //Create CompanyClaims
        createCompanyClaims(document);
        document.close();
        openPDF(this.destination);
    }

    public String convertDate(Date d, String newFormat) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(newFormat);
        return sdf.format(d);
    }

    public PdfPCell getPartyAddress(String who, String name, String line1, String line2, String countryID, String postcode, String city, String company, String phone, String email) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.addElement(new Paragraph(who, addressFont));
        cell.addElement(new Paragraph(name, addressFont));
        cell.addElement(new Paragraph(line1, addressFont));
        if(line2 != null){
            cell.addElement(new Paragraph(line2, addressFont));
        }
        cell.addElement(new Paragraph(String.format("%s %s", countryID, city), addressFont));
        cell.addElement(new Paragraph(postcode, addressFont));
        if(company != null){
            cell.addElement(new Paragraph(company, addressFont));
        }
        if(phone != null){
            cell.addElement(new Paragraph("Phone: " + phone, addressFont));
        }
        if(email != null){
            cell.addElement(new Paragraph("Email: " + email, addressFont));
        }
        return cell;
    }

    public PdfPCell getCell(String value, int alignment, Font font) {
        PdfPCell cell = new PdfPCell();
        cell.setUseAscender(true);
        cell.setUseDescender(true);
        Paragraph p = new Paragraph(value, font);
        p.setAlignment(alignment);
        cell.addElement(p);
        return cell;
    }
    public PdfPCell getCellTitle(String value, int alignment, Font font, BaseColor color) {
        PdfPCell cell = new PdfPCell();
        cell.setUseAscender(true);
        cell.setUseDescender(true);
        cell.setBackgroundColor(color);
        Paragraph p = new Paragraph(value, font);
        p.setAlignment(alignment);
        cell.addElement(p);
        return cell;
    }

    public PdfPCell getCellwithBackground(String value, int alignment, Font font, int i) {
        PdfPCell cell = new PdfPCell();
        cell.setUseAscender(true);
        cell.setUseDescender(true);
        if (i%2==0){
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        }
        Paragraph p = new Paragraph(value, font);
        p.setAlignment(alignment);
        cell.addElement(p);
        return cell;
    }

    public PdfPCell getCellNoWrap(String value, int alignment, Font font) {
        PdfPCell cell = new PdfPCell();
        cell.setUseAscender(true);
        cell.setUseDescender(true);
        cell.setBorder(Rectangle.NO_BORDER);
        Paragraph p = new Paragraph(value, font);
        p.setAlignment(alignment);
        cell.addElement(p);
        return cell;
    }

    public PdfPCell getCellNoWrapwithBack(String value, int alignment, Font font, BaseColor color) {
        PdfPCell cell = new PdfPCell();
        cell.setUseAscender(true);
        cell.setUseDescender(true);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setBackgroundColor(color);
        Paragraph p = new Paragraph(value, font);
        p.setAlignment(alignment);
        cell.addElement(p);
        return cell;
    }


    public PdfPCell getCellUnder(String value, int alignment, Font font) {
        PdfPCell cell = new PdfPCell();
        cell.setUseAscender(true);
        cell.setUseDescender(true);
        cell.setBorder(Rectangle.BOTTOM);
        Paragraph p = new Paragraph(value, font);
        p.setAlignment(alignment);
        cell.addElement(p);
        return cell;
    }
    public PdfPCell getCellTop(String value, int alignment, Font font) {
        PdfPCell cell = new PdfPCell();
        cell.setUseAscender(true);
        cell.setUseDescender(true);
        cell.setBorder(Rectangle.TOP);
        Paragraph p = new Paragraph(value, font);
        p.setAlignment(alignment);
        cell.addElement(p);
        return cell;
    }

    public PdfPCell getCellHolder() {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);

        return cell;
    }

    public void getEmptyCellHolder(PdfPTable table, int num){
        int i = 0;
        while(i < num){
            table.addCell(getCellHolder());
            i++;
        }
    }

    private void createHeader(Document document, InvoiceData.AdvancedProfileImp profileImp, InvoiceType type, TransactionDTO transaction){
        Paragraph pCompany = new Paragraph("Milan Building Supply LTD.",catFont);
        pCompany.setAlignment(Element.HEADER);
        String typeString = null;
        String invoiceId = null;

        //Generate invoice type
        if(type.equals(InvoiceType.DELIVERY)){
            typeString = "Pick Up/Delivery Order";
        }else if(type.equals(InvoiceType.QUOTATION)){
            typeString = "Quotation Order";
        }else if(type.equals(InvoiceType.INVOICE)){
            if(this.invoice.getTransaction().getTransactionType().equals(Transaction.TransactionType.INVOICE)){
                typeString = profileImp.getName();
            }else if(this.invoice.getTransaction().getTransactionType().equals(Transaction.TransactionType.RETURN)){
                typeString = "RETURN";
            }
        }

        //Generate invoice Id
        invoiceId = String.format("D/%05d", invoice.getId());
        Paragraph pType = new Paragraph(typeString + " " + invoiceId, subFont);
        pType.setAlignment(Element.ALIGN_RIGHT);
        Paragraph pDate = null;
        try {
            pDate = new Paragraph(convertDate(profileImp.getDateTime(), "MMM dd, yyyy"), smallBold);
        } catch (Exception e) {
            logger.error(e.getMessage() + "\nThe full stack trace is: ", e);
            errorMsg.append(e.getMessage()).append("\n");
        }
        pDate.setAlignment(Element.ALIGN_RIGHT);
        try {
            document.add(pCompany);
            document.add(pType);
            document.add(pDate);
        } catch (DocumentException e) {
            logger.error(e.getMessage() + "\nThe full stack trace is: ", e);
            errorMsg.append(e.getMessage()).append("\n");
        }

    }

    private void createItemList(Document document, InvoiceType type){
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);
        try {
            table.setWidths(new int[]{3, 3, 2, 2, 2, 2, 3});
        } catch (DocumentException e) {
            logger.error(e.getMessage() + "\nThe full stack trace is: ", e);
            errorMsg.append(e.getMessage()).append("\n");
        }
        table.addCell(getCellTitle("Item", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
        table.addCell(getCellTitle("Name", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
        table.addCell(getCellTitle("Size", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
        table.addCell(getCellTitle("Price", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
        table.addCell(getCellTitle("Qty(ft)", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
        table.addCell(getCellTitle("Subtotal", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
        table.addCell(getCellTitle("Remark", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
        int row=0;
        double total=0;
        for (TransactionDetailDTO transactionDetail : invoice.getTransaction().getTransactionDetails()) {
            double tmpSubTotal = Double.valueOf(InvoiceData.format2dec(InvoiceData.round(transactionDetail.getSaleAmount()*100/(100-transactionDetail.getDiscount()))));
            total+=tmpSubTotal;
            table.addCell(getCellwithBackground(transactionDetail.getProduct().getSku(), Element.ALIGN_LEFT, totalFont, row));
            table.addCell(getCellwithBackground(transactionDetail.getProduct().getDisplayName(), Element.ALIGN_LEFT, totalFont, row));
            table.addCell(getCellwithBackground(String.format("%s*%s*%s", transactionDetail.getProduct().getLength(),
                    transactionDetail.getProduct().getWidth(), transactionDetail.getProduct().getHeight()), Element.ALIGN_LEFT, totalFont, row));
            table.addCell(getCellwithBackground(InvoiceData.format2dec(InvoiceData.round(transactionDetail.getProduct().getUnitPrice())), Element.ALIGN_LEFT, totalFont, row));
            table.addCell(getCellwithBackground(String.valueOf(transactionDetail.getQuantity()), Element.ALIGN_LEFT, totalFont, row));
            table.addCell(getCellwithBackground(String.valueOf(tmpSubTotal), Element.ALIGN_LEFT, totalFont, row));
            table.addCell(getCellwithBackground(transactionDetail.getNote(), Element.ALIGN_LEFT, chineseFont, row));
            row++;
        }
        getEmptyCellHolder(table, 5);
        table.addCell(getCellNoWrap("Subtotal:", Element.ALIGN_LEFT, tinyBold));
        table.addCell(getCellNoWrap("$CAD   " + new BigDecimal(total).setScale(2, BigDecimal.ROUND_HALF_EVEN), Element.ALIGN_JUSTIFIED_ALL, smallText));

        double discount =  new BigDecimal(
                invoice.getTotal()-invoice.getTransaction().getGst()-invoice.getTransaction().getPst()-total)
                .setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();

        if(discount>0){
            String msg = "Discount is greater than 0";
            logger.error(msg);
            errorMsg.append(msg).append("\n");
        }
        getEmptyCellHolder(table, 5);
        table.addCell(getCellNoWrap("Discount:", Element.ALIGN_LEFT, tinyBold));
        table.addCell(getCellNoWrap("$CAD   " + new BigDecimal(discount).setScale(2, BigDecimal.ROUND_HALF_EVEN), Element.ALIGN_JUSTIFIED_ALL, smallText));


        getEmptyCellHolder(table, 5);
        table.addCell(getCellNoWrap("GST:", Element.ALIGN_LEFT, tinyBold));
        table.addCell(getCellNoWrap("$CAD   " + (invoice.getTransaction().getGst()), Element.ALIGN_JUSTIFIED_ALL, smallText));

        getEmptyCellHolder(table, 5);
        table.addCell(getCellUnder("PST:", Element.ALIGN_LEFT, tinyBold));
        table.addCell(getCellUnder("$CAD   " + (invoice.getTransaction().getPst()), Element.ALIGN_JUSTIFIED_ALL, smallText));

        getEmptyCellHolder(table, 5);
        table.addCell(getCellTop("Total:", Element.ALIGN_LEFT, totalFont));
        table.addCell(getCellTop("$CAD  " + invoice.getTotal(), Element.ALIGN_JUSTIFIED_ALL, totalFont));

        double paid = 0;
        for (PaymentDTO paymentRecord : invoice.getTransaction().getPayments()){
            paid+=paymentRecord.getPaymentAmount();
        }
        BigDecimal paidRoundEven = new BigDecimal(paid).setScale(2, BigDecimal.ROUND_HALF_EVEN);
        getEmptyCellHolder(table, 5);
        table.addCell(getCellUnder("Paid:", Element.ALIGN_LEFT, totalFont));
        table.addCell(getCell("$CAD  " + paidRoundEven.toString(), Element.ALIGN_JUSTIFIED_ALL, totalFont));

        getEmptyCellHolder(table, 5);
        table.addCell(getCellTop("Total Due:", Element.ALIGN_LEFT, totalFont));
        table.addCell(getCellTop("$CAD  " + new BigDecimal((invoice.getTotal()-paid)).setScale(2, BigDecimal.ROUND_HALF_EVEN), Element.ALIGN_JUSTIFIED_ALL, totalFont));

        try {
            document.add(table);
        } catch (DocumentException e) {
            logger.error(e.getMessage() + "\nThe full stack trace is: ", e);
            errorMsg.append(e.getMessage()).append("\n");
        }
    }

    private void createGSTNo(Document document){
        Paragraph pGst = new Paragraph("GST No. " + invoice.getProperty().getGstNumber(), tinyBold);
        pGst.setAlignment(Element.ALIGN_LEFT);
        try {
            document.add(pGst);
            document.add(new Paragraph());
        } catch (DocumentException e) {
            logger.error(e.getMessage() + "\nThe full stack trace is: ", e);
            errorMsg.append(e.getMessage()).append("\n");
        }
    }

    private void createPartyAddress(Document document, InvoiceData.AdvancedProfileImp basic){
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        PdfPCell buyer;
        PdfPCell seller;
        String buyerWho;
        String sellerWho;
        if(this.invoice.getTransaction().getTransactionType().equals(Transaction.TransactionType.RETURN)){
            buyerWho = "From:";
            sellerWho = "To:";
        }else{
            buyerWho = "To:";
            sellerWho = "From:";
        }
        buyer = getPartyAddress(buyerWho,
                basic.getBuyerName(),
                basic.getBuyerLineOne(),
                basic.getBuyerLineTwo(),
                basic.getBuyerCountryID(),
                basic.getBuyerPostcode(),
                basic.getBuyerCityName(),
                basic.getBuyerCompanyName(),
                basic.getBuyerPhoneNumber(),
                basic.getBuyerEmail());
        seller = getPartyAddress(sellerWho,
                basic.getSellerName(),
                basic.getSellerLineOne(),
                basic.getSellerLineTwo(),
                basic.getSellerCountryID(),
                basic.getSellerPostcode(),
                basic.getSellerCityName(),
                basic.getSellerCompanyName(),
                basic.getSellerPhoneNumber(),
                basic.getSellerEmail());
        if(this.invoice.getTransaction().getTransactionType().equals(Transaction.TransactionType.RETURN)){
            table.addCell(buyer);
            table.addCell(seller);
        }else{
            table.addCell(seller);
            table.addCell(buyer);
        }
        try {
            document.add(table);
        } catch (DocumentException e) {
            logger.error(e.getMessage() + "\nThe full stack trace is: ", e);
            errorMsg.append(e.getMessage()).append("\n");
        }

    }

    private void createPaymentInfo(Document document, InvoiceType type){
        Paragraph p = new Paragraph("Payment Information:",addressFont);
        PdfPTable table = null;
        int row = 0;
        p.setAlignment(Element.ALIGN_LEFT);
        if(type.equals(InvoiceType.DELIVERY)){
            table = new PdfPTable(2);
            table.setWidthPercentage(50);
            table.setHorizontalAlignment(0);
            table.setSpacingBefore(10);
            try {
                table.setWidths(new int[]{5,5});
            } catch (DocumentException e) {
                logger.error(e.getMessage() + "\nThe full stack trace is: ", e);
                errorMsg.append(e.getMessage()).append("\n");
            }
            table.addCell(getCellTitle("Date", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
            table.addCell(getCellTitle("Type", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
            row = 0;
            for (PaymentDTO paymentRecord : invoice.getTransaction().getPayments()){
                table.addCell(getCellwithBackground(paymentRecord.getDateModified().toString(), Element.ALIGN_LEFT, totalFont, row));
                table.addCell(getCellwithBackground(paymentRecord.getPaymentType().toString(), Element.ALIGN_LEFT, totalFont, row));
                row++;
            }
            p.add(table);
        }else if(type.equals(InvoiceType.INVOICE)){
            if(this.invoice.getTransaction().getTransactionType().equals(Transaction.TransactionType.INVOICE)){
                table = new PdfPTable(4);
                table.setWidthPercentage(50);
                table.setHorizontalAlignment(0);
                table.setSpacingBefore(10);
                try {
                    table.setWidths(new int[]{3, 2, 2, 4});
                } catch (DocumentException e) {
                    logger.error(e.getMessage() + "\nThe full stack trace is: ", e);
                    errorMsg.append(e.getMessage()).append("\n");
                }
                table.addCell(getCellTitle("Date", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
                table.addCell(getCellTitle("Amount", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
                table.addCell(getCellTitle("Type", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
                table.addCell(getCellTitle("Deposit", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
                for (PaymentDTO paymentRecord : invoice.getTransaction().getPayments()){
                    table.addCell(getCellwithBackground(paymentRecord.getDateModified().toString(), Element.ALIGN_LEFT, totalFont, row));
                    table.addCell(getCellwithBackground(InvoiceData.format2dec(InvoiceData.round(paymentRecord.getPaymentAmount())), Element.ALIGN_LEFT, totalFont, row));
                    table.addCell(getCellwithBackground(paymentRecord.getPaymentType().toString(), Element.ALIGN_LEFT, totalFont, row));
                    table.addCell(getCellwithBackground(paymentRecord.isDeposit()? "YES" : "NO", Element.ALIGN_LEFT, totalFont, row));
                    row++;
                }
            }else if (this.invoice.getTransaction().getTransactionType().equals(Transaction.TransactionType.RETURN)){
                table = new PdfPTable(3);
                table.setWidthPercentage(50);
                table.setHorizontalAlignment(0);
                table.setSpacingBefore(10);
                try {
                    table.setWidths(new int[]{3, 3, 3});
                } catch (DocumentException e) {
                    logger.error(e.getMessage() + "\nThe full stack trace is: ", e);
                    errorMsg.append(e.getMessage()).append("\n");
                }
                table.addCell(getCellTitle("Date", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
                table.addCell(getCellTitle("Amount", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
                table.addCell(getCellTitle("Type", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
                for (PaymentDTO paymentRecord : invoice.getTransaction().getPayments()){
                    table.addCell(getCellwithBackground(paymentRecord.getDateModified().toString(), Element.ALIGN_LEFT, totalFont, row));
                    table.addCell(getCellwithBackground(InvoiceData.format2dec(InvoiceData.round(paymentRecord.getPaymentAmount())), Element.ALIGN_LEFT, totalFont, row));
                    table.addCell(getCellwithBackground(paymentRecord.getPaymentType().toString(), Element.ALIGN_LEFT, totalFont, row));
                    row++;
                }
            }
            p.add(table);
        }
        try {
            document.add(p);
        } catch (DocumentException e) {
            logger.error(e.getMessage() + "\nThe full stack trace is: ", e);
            errorMsg.append(e.getMessage()).append("\n");
        }
    }

    private void createCompanyClaims(Document document){
        document.newPage();
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);
        table.addCell(getCellNoWrapwithBack("THANK YOU FOR YOUR BUSINESS!", Element.ALIGN_CENTER, addressFont, BaseColor.LIGHT_GRAY));
        try {
            document.add(table);
        } catch (DocumentException e) {
            logger.error(e.getMessage() + "\nThe full stack trace is: ", e);
            errorMsg.append(e.getMessage()).append("\n");
        }

        Paragraph p = new Paragraph("[Disclaimer:]",midText);
        p.setAlignment(Element.ALIGN_LEFT);
        try {
            document.add(p);
        } catch (DocumentException e) {
            logger.error(e.getMessage() + "\nThe full stack trace is: ", e);
            errorMsg.append(e.getMessage()).append("\n");
        }
        p = new Paragraph("1. Payments\n" +
                "-  A 50% deposit is required on all orders.\n" +
                "-  Full payment is required six days prior to delivery or pickup.\n" +
                "-  Credit card payments are accepted in sore and by phone.\n" +
                "-  Milan Tiles does not accept payment upon delivery\n" +
                "\n" +
                "2. Return and Cancellation\n" +
                "-  A refund will be issued for full boxes of current tile returned in good box condition within 2 \n" +
                "months of the date product were received.\n" +
                "-  No returns or refund allowed on discontinued or special order items.\n" +
                "-  Milan Tiles Ltd is under no obligation to accept the cancellation of any special order items.\n" +
                "-  All returns must be in their original packaging and in the condition in which it was received.\n" +
                "\n" +
                "3. Delivery\n" +
                "-  All deliveries should be arranged through the store of purchase.\n" +
                "-  We are unable to provide you with specific delivery times �?only that it will be a morning or \n" +
                "afternoon delivery.\n" +
                "-  Delivery on stocked products will be charge from $50 delivery fee which depends on the location.\n" +
                "-  Special orders will be take more than 6 weeks to delivery.\n" +
                "\n" +
                "4. Pick-Ups\n" +
                "This is generally arranged through our Milan Tile warehouse. You must check in at the reception \n" +
                "desk before goods can be released. Once your goods have left our facility, Milan will not accept \n" +
                "any claims of damage. If someone other than the name on the invoice is picking up yours tiles, \n" +
                "you must notify us in advance.\n"+
                "\n" +
                "5. Third-Party Pick-Ups\n" +
                "Milan will not accept any damage claims once a third-party carrier has deceived the goods. Please \n" +
                "note tile installers are considered a third party.\n" +
                "\n" +
                "6. Installations\n" +
                "Milan will not provide any tile installers for customers. Milan do not have responsibility on any \n" +
                "installation issues.",largeText);
        p.setAlignment(Element.ALIGN_LEFT);
        try {
            document.add(p);
        } catch (DocumentException e) {
            logger.error(e.getMessage() + "\nThe full stack trace is: ", e);
            errorMsg.append(e.getMessage()).append("\n");
        }
    }

    private void openPDF(String path){
        if(this.errorMsg.length() == 0){
            ButtonType openType = new ButtonType("Open Invoice");
            ButtonType cancelType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            Alert alert = new AlertBuilder()
                    .alertType(Alert.AlertType.CONFIRMATION)
                    .alertTitle("Invoice Generation")
                    .alertHeaderText("Invoice Generation Successful!")
                    .alertContentText("Invoice is successfully generated at\n" + path + "\n\nClick Open Invoice to open it")
                    .alertButton(openType, cancelType)
                    .build();

            Optional<ButtonType> result = alert.showAndWait();
            if(result.isPresent() && result.get() == openType){
                if(Desktop.isDesktopSupported()){
                    try {
                        Desktop.getDesktop().open(new File(path));
                    } catch (IOException e) {
                        logger.error(e.getMessage() + "\nThe full stack trace is: ", e);
                        errorMsg.append(e.getMessage()).append("\n");
                    }
                }else{
                    logger.error("AWT Desktop is not supported!");
                    errorMsg.append("AWT Desktop is not supported!").append("\n");
                }
            }else{
                alert.close();
            }
        }else{
            new AlertBuilder()
                    .alertType(Alert.AlertType.ERROR)
                    .alertTitle("Invoice Generation")
                    .alertHeaderText("Invoice generation is failed!")
                    .alertContentText(errorMsg.toString())
                    .build()
                    .showAndWait();
        }
    }

}