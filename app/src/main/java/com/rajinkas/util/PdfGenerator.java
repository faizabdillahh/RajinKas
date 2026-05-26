package com.rajinkas.util;

import android.content.Context;
import android.os.Environment;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.rajinkas.data.local.entity.AuditLogEntity;
import com.rajinkas.data.local.entity.DuesPaymentEntity;
import com.rajinkas.data.local.entity.StudentEntity;
import com.rajinkas.data.local.entity.TransactionEntity;
import com.rajinkas.data.local.model.DuesPaymentWithConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class PdfGenerator {

    public static String generateTransactionReport(Context context, String className, String period, List<TransactionEntity> transactions) {
        String fileName = "Laporan_Transaksi_" + System.currentTimeMillis() + ".pdf";
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName);

        try {
            PdfWriter writer = new PdfWriter(new FileOutputStream(file));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("LAPORAN KAS KELAS").setBold().setFontSize(18));
            document.add(new Paragraph("Kelas: " + className));
            document.add(new Paragraph("Periode: " + period));
            document.add(new Paragraph("\n"));

            float[] columnWidths = {1, 3, 3, 2, 2};
            Table table = new Table(UnitValue.createPointArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            table.addHeaderCell("No");
            table.addHeaderCell("Tanggal");
            table.addHeaderCell("Keterangan");
            table.addHeaderCell("Tipe");
            table.addHeaderCell("Nominal");

            int no = 1;
            for (TransactionEntity t : transactions) {
                table.addCell(String.valueOf(no++));
                
                String dateStr = t.getCreatedAt();
                if (dateStr != null && dateStr.contains("T")) {
                    dateStr = dateStr.replace("T", " ").substring(0, 16);
                } else {
                    dateStr = t.getTransactionDate();
                }
                table.addCell(dateStr);
                
                table.addCell(t.getDescription());
                table.addCell(t.getType());
                table.addCell(String.valueOf((int)t.getAmount()));
            }

            document.add(table);
            document.close();
            return file.getAbsolutePath();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String generateDuesStatusReport(Context context, String className, List<StudentEntity> students, List<DuesPaymentWithConfig> allPayments) {
        String fileName = "Laporan_Status_Iuran_" + System.currentTimeMillis() + ".pdf";
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName);

        try {
            PdfWriter writer = new PdfWriter(new FileOutputStream(file));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("LAPORAN STATUS IURAN & TUNGGAKAN SISWA").setBold().setFontSize(18));
            document.add(new Paragraph("Kelas: " + className));
            document.add(new Paragraph("Tanggal Cetak: " + DateUtils.nowIso8601().replace("T", " ")));
            document.add(new Paragraph("\n"));

            float[] columnWidths = {1, 3, 3, 3, 2};
            Table table = new Table(UnitValue.createPointArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            table.addHeaderCell("No");
            table.addHeaderCell("Nama Siswa");
            table.addHeaderCell("Lunas");
            table.addHeaderCell("Tunggakan");
            table.addHeaderCell("Total Hutang");

            int no = 1;
            for (StudentEntity s : students) {
                table.addCell(String.valueOf(no++));
                table.addCell(s.getName());
                
                StringBuilder paidDetails = new StringBuilder();
                StringBuilder unpaidDetails = new StringBuilder();
                double totalArrears = 0;

                for (DuesPaymentWithConfig pc : allPayments) {
                    if (pc.payment.getStudentId() == s.getId()) {
                        if ("PAID".equals(pc.payment.getStatus())) {
                            paidDetails.append("✅ ").append(pc.config.getName())
                                       .append(" (").append(pc.payment.getPeriodLabel()).append(")\n");
                        } else {
                            unpaidDetails.append("❌ ").append(pc.config.getName())
                                         .append(" (").append(pc.payment.getPeriodLabel()).append(")\n");
                            totalArrears += pc.config.getAmount();
                        }
                    }
                }

                table.addCell(paidDetails.toString().trim());
                table.addCell(unpaidDetails.toString().trim());
                table.addCell("Rp " + (int)totalArrears);
            }

            document.add(table);
            document.close();
            return file.getAbsolutePath();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String generateAuditLogReport(Context context, String className, List<AuditLogEntity> logs) {
        String fileName = "Laporan_Audit_" + System.currentTimeMillis() + ".pdf";
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName);

        try {
            PdfWriter writer = new PdfWriter(new FileOutputStream(file));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("LAPORAN AUDIT LOG").setBold().setFontSize(18));
            document.add(new Paragraph("Kelas: " + className));
            document.add(new Paragraph("Tanggal Cetak: " + DateUtils.nowIso8601()));
            document.add(new Paragraph("\n"));

            float[] columnWidths = {1, 2, 2, 4};
            Table table = new Table(UnitValue.createPointArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            table.addHeaderCell("No");
            table.addHeaderCell("Waktu");
            table.addHeaderCell("Aksi");
            table.addHeaderCell("Deskripsi");

            int no = 1;
            for (AuditLogEntity l : logs) {
                table.addCell(String.valueOf(no++));
                table.addCell(l.getCreatedAt());
                table.addCell(l.getAction() + " (" + l.getEntityType() + ")");
                table.addCell(l.getDescription());
            }

            document.add(table);
            document.close();
            return file.getAbsolutePath();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
