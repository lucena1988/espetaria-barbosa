package br.com.espetariabarbosa.service;

import br.com.espetariabarbosa.entity.Pagamento;
import br.com.espetariabarbosa.entity.Pedido;
import br.com.espetariabarbosa.service.DashboardService.ExtratoFinanceiro;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

@Service
public class ExtratoPdfService {

    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter GERADO_EM = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] gerar(ExtratoFinanceiro extrato) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(document);

            writer.header(extrato.resumo().periodoLabel());
            writer.summaryCards(extrato);
            writer.paymentSummary(extrato);
            writer.ordersTable(extrato);
            writer.close();

            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new RuntimeException("Nao foi possivel gerar o extrato em PDF", exception);
        }
    }

    private String money(BigDecimal value) {
        BigDecimal safeValue = value == null ? BigDecimal.ZERO : value;
        return NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(safeValue);
    }

    private class PdfWriter {
        private static final float MARGIN = 42;
        private static final float FOOTER_Y = 30;
        private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
        private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
        private static final float CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2);

        private final PDDocument document;
        private final PDImageXObject watermark;
        private PDPageContentStream content;
        private float y;
        private int pageNumber;

        PdfWriter(PDDocument document) throws IOException {
            this.document = document;
            this.watermark = carregarMarcaDagua(document);
            addPage();
        }

        void header(String periodoLabel) throws IOException {
            drawText("Espetaria Barbosa", MARGIN, y, PDType1Font.HELVETICA_BOLD, 22, 0.93f, 0.18f, 0.12f);
            y -= 24;
            drawText("Extrato financeiro", MARGIN, y, PDType1Font.HELVETICA_BOLD, 15, 0.08f, 0.10f, 0.16f);
            y -= 18;
            drawText("Periodo: " + periodoLabel + "    Gerado em: " + LocalDateTime.now().format(GERADO_EM),
                    MARGIN, y, PDType1Font.HELVETICA, 10, 0.28f, 0.32f, 0.38f);
            y -= 18;
            drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, 0.88f, 0.28f, 0.14f);
            y -= 24;
        }

        void summaryCards(ExtratoFinanceiro extrato) throws IOException {
            section("Resumo financeiro");
            float cardWidth = (CONTENT_WIDTH - 24) / 3;
            float top = y;

            metricCard(MARGIN, top, cardWidth, "Vendas", money(extrato.resumo().vendasDoPeriodo()));
            metricCard(MARGIN + cardWidth + 12, top, cardWidth, "Recebido", money(extrato.totalRecebido()));
            metricCard(MARGIN + (cardWidth + 12) * 2, top, cardWidth, "Pendente", money(extrato.totalPendente()));
            y = top - 70;

            metricCard(MARGIN, y, cardWidth, "Pedidos", String.valueOf(extrato.resumo().pedidosDoPeriodo()));
            metricCard(MARGIN + cardWidth + 12, y, cardWidth, "Ticket medio", money(extrato.resumo().ticketMedio()));
            metricCard(MARGIN + (cardWidth + 12) * 2, y, cardWidth, "Abertos agora", String.valueOf(extrato.resumo().pedidosAbertos()));
            y -= 86;
        }

        void paymentSummary(ExtratoFinanceiro extrato) throws IOException {
            section("Recebimento por forma de pagamento");
            float colWidth = (CONTENT_WIDTH - 12) / 2;
            int index = 0;
            for (Map.Entry<String, BigDecimal> entry : extrato.totaisPorForma().entrySet()) {
                float x = MARGIN + (index % 2) * (colWidth + 12);
                if (index > 0 && index % 2 == 0) {
                    y -= 28;
                }
                paymentRow(x, y, colWidth, entry.getKey(), money(entry.getValue()));
                index++;
            }
            y -= 44;
        }

        void ordersTable(ExtratoFinanceiro extrato) throws IOException {
            section("Pedidos do periodo");

            if (extrato.pedidos().isEmpty()) {
                drawText("Nenhum pedido encontrado para o periodo.", MARGIN, y, PDType1Font.HELVETICA, 10);
                y -= 18;
                return;
            }



            tableHeader();
            boolean shaded = false;
            for (Pedido pedido : extrato.pedidos()) {
                ensureSpace(42);
                if (y < 96) {
                    tableHeader();
                }
                tableRow(pedido, shaded);
                shaded = !shaded;
            }
        }

        void close() throws IOException {
            if (content != null) {
                content.close();
            }
        }

        private void metricCard(float x, float top, float width, String label, String value) throws IOException {
            drawFilledRoundRect(x, top - 52, width, 52, 0.97f, 0.97f, 0.95f);
            drawText(label, x + 12, top - 18, PDType1Font.HELVETICA, 9, 0.38f, 0.42f, 0.48f);
            drawText(value, x + 12, top - 38, PDType1Font.HELVETICA_BOLD, 13, 0.08f, 0.10f, 0.16f);
        }

        private void paymentRow(float x, float y, float width, String forma, String valor) throws IOException {
            drawFilledRoundRect(x, y - 22, width, 24, 0.99f, 0.99f, 0.98f);
            drawText(label(forma), x + 10, y - 14, PDType1Font.HELVETICA_BOLD, 9, 0.08f, 0.10f, 0.16f);
            drawText(valor, x + width - 92, y - 14, PDType1Font.HELVETICA, 9, 0.08f, 0.10f, 0.16f);
        }

        private void tableHeader() throws IOException {
            ensureSpace(54);
            drawFilledRect(MARGIN, y - 20, CONTENT_WIDTH, 22, 0.50f, 0.11f, 0.11f);
            drawText("Pedido", MARGIN + 6, y - 13, PDType1Font.HELVETICA_BOLD, 8, 1, 1, 1);
            drawText("Data", MARGIN + 52, y - 13, PDType1Font.HELVETICA_BOLD, 8, 1, 1, 1);
            drawText("Cliente", MARGIN + 134, y - 13, PDType1Font.HELVETICA_BOLD, 8, 1, 1, 1);
            drawText("Status", MARGIN + 292, y - 13, PDType1Font.HELVETICA_BOLD, 8, 1, 1, 1);
            drawText("Pagamento", MARGIN + 370, y - 13, PDType1Font.HELVETICA_BOLD, 8, 1, 1, 1);
            drawText("Total", MARGIN + 478, y - 13, PDType1Font.HELVETICA_BOLD, 8, 1, 1, 1);
            y -= 24;
        }

        private void tableRow(Pedido pedido, boolean shaded) throws IOException {
            if (shaded) {
                drawFilledRect(MARGIN, y - 19, CONTENT_WIDTH, 22, 0.97f, 0.97f, 0.95f);
            }

            Pagamento pagamento = pedido.getPagamento();
            String criadoEm = pedido.getCriadoEm() == null ? "-" : pedido.getCriadoEm().format(DATA_HORA);
            String pagamentoTexto = pagamento == null ? "PENDENTE" : formasPagamento(pagamento);

            drawText("#" + pedido.getId(), MARGIN + 6, y - 12, PDType1Font.HELVETICA, 8);
            drawText(criadoEm, MARGIN + 52, y - 12, PDType1Font.HELVETICA, 8);
            drawText(truncate(pedido.getNomeCliente(), 24), MARGIN + 134, y - 12, PDType1Font.HELVETICA, 8);
            drawText(pedido.getStatus() == null ? "-" : label(pedido.getStatus().name()), MARGIN + 292, y - 12, PDType1Font.HELVETICA, 8);
            drawText(truncate(pagamentoTexto, 16), MARGIN + 370, y - 12, PDType1Font.HELVETICA, 8);
            drawText(money(valorFinanceiro(pedido)), MARGIN + 478, y - 12, PDType1Font.HELVETICA_BOLD, 8);
            y -= 22;
        }

        private BigDecimal valorFinanceiro(Pedido pedido) {
            if (pedido.getPagamento() != null && pedido.getPagamento().getValor() != null) {
                return pedido.getPagamento().getValor();
            }

            return pedido.getTotal();
        }

        private String formasPagamento(Pagamento pagamento) {
            if (pagamento.getParcelas() != null && !pagamento.getParcelas().isEmpty()) {
                return pagamento.getParcelas().stream()
                        .filter(parcela -> parcela.getFormaPagamento() != null)
                        .map(parcela -> label(parcela.getFormaPagamento().name()))
                        .distinct()
                        .reduce((primeira, segunda) -> primeira + " + " + segunda)
                        .orElse("-");
            }

            if (pagamento.getFormaPagamento() == null) {
                return "-";
            }

            return label(pagamento.getFormaPagamento().name());
        }

        private void section(String text) throws IOException {
            ensureSpace(44);
            drawText(text, MARGIN, y, PDType1Font.HELVETICA_BOLD, 12, 0.08f, 0.10f, 0.16f);
            y -= 18;
        }

        private void addPage() throws IOException {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            pageNumber++;
            content = new PDPageContentStream(document, page);
            drawWatermark();
            y = PAGE_HEIGHT - MARGIN;
            footer();
        }

        private void drawWatermark() throws IOException {
            if (watermark == null) {
                return;
            }

            PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();
            graphicsState.setNonStrokingAlphaConstant(0.07f);
            content.setGraphicsStateParameters(graphicsState);

            float width = 360;
            float ratio = (float) watermark.getHeight() / watermark.getWidth();
            float height = width * ratio;
            content.drawImage(watermark, (PAGE_WIDTH - width) / 2, (PAGE_HEIGHT - height) / 2, width, height);

            PDExtendedGraphicsState reset = new PDExtendedGraphicsState();
            reset.setNonStrokingAlphaConstant(1f);
            content.setGraphicsStateParameters(reset);
        }

        private void footer() throws IOException {
            drawLine(MARGIN, FOOTER_Y + 12, PAGE_WIDTH - MARGIN, FOOTER_Y + 12, 0.82f, 0.82f, 0.82f);
            drawText("Espetaria Barbosa - Extrato financeiro", MARGIN, FOOTER_Y, PDType1Font.HELVETICA, 8, 0.42f, 0.42f, 0.42f);
            drawText("Pagina " + pageNumber, PAGE_WIDTH - MARGIN - 44, FOOTER_Y, PDType1Font.HELVETICA, 8, 0.42f, 0.42f, 0.42f);
        }

        private void ensureSpace(float height) throws IOException {
            if (y - height > FOOTER_Y + 28) {
                return;
            }

            content.close();
            addPage();
        }

        private void drawText(String text, float x, float y, PDType1Font font, int size) throws IOException {
            drawText(text, x, y, font, size, 0.08f, 0.10f, 0.16f);
        }

        private void drawText(String text, float x, float y, PDType1Font font, int size, float r, float g, float b) throws IOException {
            content.beginText();
            content.setNonStrokingColor(r, g, b);
            content.setFont(font, size);
            content.newLineAtOffset(x, y);
            content.showText(sanitize(text));
            content.endText();
            content.setNonStrokingColor(0, 0, 0);
        }

        private void drawLine(float x1, float y1, float x2, float y2, float r, float g, float b) throws IOException {
            content.setStrokingColor(r, g, b);
            content.moveTo(x1, y1);
            content.lineTo(x2, y2);
            content.stroke();
            content.setStrokingColor(0, 0, 0);
        }

        private void drawFilledRect(float x, float y, float width, float height, float r, float g, float b) throws IOException {
            content.setNonStrokingColor(r, g, b);
            content.addRect(x, y, width, height);
            content.fill();
            content.setNonStrokingColor(0, 0, 0);
        }

        private void drawFilledRoundRect(float x, float y, float width, float height, float r, float g, float b) throws IOException {
            drawFilledRect(x, y, width, height, r, g, b);
        }

        private PDImageXObject carregarMarcaDagua(PDDocument document) {
            try {
                var resource = new ClassPathResource("static/images/logo-watermark.png");
                return PDImageXObject.createFromByteArray(document, resource.getInputStream().readAllBytes(), "logo-watermark");
            } catch (IOException exception) {
                return null;
            }
        }

        private String label(String value) {
            if (value == null) {
                return "-";
            }

            return value.replace('_', ' ');
        }

        private String truncate(String text, int max) {
            String sanitized = sanitize(text);
            if (sanitized.length() <= max) {
                return sanitized;
            }
            return sanitized.substring(0, max - 3) + "...";
        }

        private String sanitize(String text) {
            if (text == null) {
                return "";
            }

            String normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "");
            return normalized.replace('\n', ' ')
                    .replace('\r', ' ')
                    .replace('\u00A0', ' ')
                    .replace("–", "-")
                    .replace("—", "-")
                    .replace("ç", "c")
                    .replace("Ç", "C");
        }
    }
}
