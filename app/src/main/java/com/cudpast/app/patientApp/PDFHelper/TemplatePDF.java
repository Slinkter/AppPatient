package com.cudpast.app.patientApp.PDFHelper;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class TemplatePDF {
    //.Variable
    private Context context;
    private File pdfFile;
    private Document document;
    private PdfWriter pdfWriter;
    private Paragraph paragraph;
    private Font fTitle = new Font(Font.FontFamily.TIMES_ROMAN, 20, Font.BOLD);
    private Font fSubTitle = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD);
    private Font fText = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
    private Font fHighText = new Font(Font.FontFamily.TIMES_ROMAN, 15, Font.BOLD, BaseColor.BLUE);

    //.Constructor
    public TemplatePDF(Context context) {
        this.context = context;
    }

    //.Metodo
    //.1
    public void openDocument() {
        createFile();
        try {
            document = new Document(PageSize.A4);
            pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //.2
    public void closeDocument() {
        document.close();
    }

    //.3
    private void createFile() {
        //Crear una carpeta en el dispositivo
        File foder = new File(Environment.getExternalStorageDirectory().toString(), "PDF");
        if (!foder.exists()) {
            foder.mkdir();
        }
        pdfFile = new File(foder, "TemplatePDF.pdf");
    }

    //.4
    public void addMetada(String title, String subject, String author) {
        document.addTitle(title);
        document.addSubject(subject);
        document.addAuthor(author);
    }

    //.5
    public void addTitles(String title, String subTitle, String date) {
        try {
            paragraph = new Paragraph();

            addChildp(new Paragraph(title, fTitle));
            addChildp(new Paragraph(subTitle, fSubTitle));
            addChildp(new Paragraph("Generado: " + date, fHighText));
            paragraph.setSpacingAfter(20);
            document.add(paragraph);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("openDocument", e.toString());
        }
    }

    //.6
    private void addChildp(Paragraph childParagraph) {
        childParagraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.add(childParagraph);
    }

    //.7
    public void addParagraph(String text) {
        try {
            paragraph = new Paragraph(text, fText);
            paragraph.setSpacingAfter(5);
            paragraph.setSpacingBefore(5);
            document.add(paragraph);
        } catch (Exception e) {
            Log.e("addParagraph", e.toString());
        }
    }

    //.8
    public void addCreateTable(String[] header, ArrayList<String[]> clients) {
        try {
            paragraph = new Paragraph();
            paragraph.setFont(fText);

            PdfPTable pdfPTable = new PdfPTable(header.length);
            pdfPTable.setWidthPercentage(100);

            pdfPTable.setSpacingBefore(10);
            pdfPTable.setSpacingAfter(20);

            PdfPCell pdfPCell;

            int indexC = 0;
            //Encabezados--columna
            while (indexC < header.length) {
                pdfPCell = new PdfPCell(new Phrase(header[indexC++], fSubTitle));
                pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                pdfPCell.setPaddingTop(20);
                pdfPCell.setPaddingBottom(20);

                pdfPCell.setBackgroundColor(BaseColor.GREEN);

                pdfPTable.addCell(pdfPCell);
            }
            //Contenido
            for (int indexR = 0; indexR < clients.size(); indexR++) {
                String[] row = clients.get(indexR);
                for (indexC = 0; indexC < header.length; indexC++) {
                    pdfPCell = new PdfPCell(new Phrase(row[indexC]));
                    pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                    pdfPCell.setPaddingTop(15);

                    pdfPCell.setFixedHeight(40);
                    pdfPTable.addCell(pdfPCell);
                }
            }

            paragraph.add(pdfPTable);
            document.add(paragraph);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //.9
    public void viewPDF() {
        Intent intent = new Intent(context, ViewPDFActivity.class);
        intent.putExtra("path", pdfFile.getAbsolutePath());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

}
