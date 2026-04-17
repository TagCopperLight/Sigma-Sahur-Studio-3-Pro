package com.sigma.sahur.studio.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.sigma.sahur.studio.exception.BusinessException;
import com.sigma.sahur.studio.exception.ResourceNotFoundException;
import com.sigma.sahur.studio.model.Seance;
import com.sigma.sahur.studio.model.enums.StatutSeance;
import com.sigma.sahur.studio.repository.SeanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Service de génération de factures PDF.
 * Utilise la bibliothèque iText 8 pour produire des documents PDF formatés
 * pour les séances dont le statut est TERMINEE.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FactureService {

    private final SeanceRepository seanceRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Génère la facture PDF pour une séance terminée.
     * Le document inclut les coordonnées du client, le détail de la séance et le montant TTC.
     *
     * @param seanceId identifiant de la séance
     * @return contenu du fichier PDF sous forme de tableau d'octets
     * @throws ResourceNotFoundException si aucune séance ne correspond à cet identifiant
     * @throws BusinessException         si la séance n'est pas dans le statut TERMINEE
     * @throws IOException               en cas d'erreur lors de la génération du PDF
     */
    public byte[] genererFacture(Long seanceId) throws IOException {
        Seance seance = seanceRepository.findById(seanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Séance introuvable avec l'id " + seanceId));

        if (seance.getStatut() != StatutSeance.TERMINEE) {
            throw new BusinessException("La facture n'est disponible que pour les séances TERMINÉES");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document doc = new Document(pdfDoc, PageSize.A4);
        doc.setMargins(50, 50, 50, 50);

        PdfFont bold = PdfFontFactory.createFont("Helvetica-Bold");
        PdfFont regular = PdfFontFactory.createFont("Helvetica");

        // ── En-tête studio ──────────────────────────────────────────────────
        doc.add(new Paragraph("SIGMA SAHUR STUDIO 3 PRO")
                .setFont(bold)
                .setFontSize(20)
                .setFontColor(ColorConstants.DARK_GRAY)
                .setTextAlignment(TextAlignment.CENTER));

        doc.add(new Paragraph("Studio de Photographie Professionnel")
                .setFont(regular)
                .setFontSize(10)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER));

        doc.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine())
                .setMarginTop(10).setMarginBottom(20));

        // ── Titre FACTURE ────────────────────────────────────────────────────
        doc.add(new Paragraph("FACTURE N° " + seance.getId())
                .setFont(bold)
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));

        // ── Coordonnées client ───────────────────────────────────────────────
        doc.add(new Paragraph("INFORMATIONS CLIENT")
                .setFont(bold)
                .setFontSize(12)
                .setFontColor(ColorConstants.DARK_GRAY)
                .setMarginBottom(5));

        Table clientTable = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                .setWidth(UnitValue.createPercentValue(60))
                .setMarginBottom(20);

        addRow(clientTable, "Nom", seance.getClient().getNomComplet(), bold, regular);
        addRow(clientTable, "Adresse", seance.getClient().getAdresse(), bold, regular);
        addRow(clientTable, "Email", seance.getClient().getEmail(), bold, regular);
        doc.add(clientTable);

        // ── Détail de la séance ──────────────────────────────────────────────
        doc.add(new Paragraph("DÉTAIL DE LA SÉANCE")
                .setFont(bold)
                .setFontSize(12)
                .setFontColor(ColorConstants.DARK_GRAY)
                .setMarginBottom(5));

        Table seanceTable = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                .setWidth(UnitValue.createPercentValue(60))
                .setMarginBottom(20);

        addRow(seanceTable, "Date", seance.getDateSeance().format(DATE_FMT), bold, regular);
        addRow(seanceTable, "Heure", seance.getHeureDebut().format(TIME_FMT) +
                " – " + seance.getHeureFin().format(TIME_FMT), bold, regular);
        addRow(seanceTable, "Durée", seance.getDureeMinutes() + " minutes", bold, regular);
        addRow(seanceTable, "Lieu", seance.getLieu(), bold, regular);
        addRow(seanceTable, "Type", formatType(seance.getTypeSeance().name()), bold, regular);
        addRow(seanceTable, "Photographe", seance.getPhotographe().getNom(), bold, regular);
        doc.add(seanceTable);

        // ── Montant ──────────────────────────────────────────────────────────
        doc.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine())
                .setMarginBottom(10));

        doc.add(new Paragraph("MONTANT TOTAL TTC : " + seance.getPrix() + " €")
                .setFont(bold)
                .setFontSize(14)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(30));

        // ── Pied de page ─────────────────────────────────────────────────────
        doc.add(new Paragraph("Merci pour votre confiance.")
                .setFont(regular)
                .setFontSize(10)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER));

        doc.close();
        return baos.toByteArray();
    }

    private void addRow(Table table, String label, String value, PdfFont bold, PdfFont regular) {
        table.addCell(new Cell().add(new Paragraph(label).setFont(bold).setFontSize(10))
                .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f)));
        table.addCell(new Cell().add(new Paragraph(value).setFont(regular).setFontSize(10))
                .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f)));
    }

    private String formatType(String type) {
        return switch (type) {
            case "REPORTAGE" -> "Reportage";
            case "MARIAGE_ET_FETES" -> "Mariage et fêtes";
            case "FAMILLE" -> "Famille";
            default -> type;
        };
    }
}
