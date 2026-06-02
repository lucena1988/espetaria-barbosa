package br.com.espetariabarbosa.service;

import br.com.espetariabarbosa.entity.ItemPedido;
import br.com.espetariabarbosa.entity.Pagamento;
import br.com.espetariabarbosa.entity.ParcelaPagamento;
import br.com.espetariabarbosa.entity.Pedido;
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

@Service
public class ReciboPdfService {

    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] gerar(Pedido pedido) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(document);
            writer.header(pedido);
            writer.orderInfo(pedido);
            writer.items(pedido);
            writer.payment(pedido);
            writer.footerMessage();
            writer.close();

            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new RuntimeException("Nao foi possivel gerar o recibo em PDF", exception);
        }
    }

    private String money(BigDecimal value) {
        BigDecimal safeValue = value == null ? BigDecimal.ZERO : value;
        return NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(safeValue);
    }

    private class PdfWriter {
        private static final float MARGIN = 42;
        private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
        private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
        private static final float CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2);

        private final PDDocument document;
        private final PDImageXObject watermark;
        private PDPageContentStream content;
        private float y;

        PdfWriter(PDDocument document) throws IOException {
            this.document = document;
            this.watermark = carregarMarcaDagua(document);
            addPage();
        }

        void header(Pedido pedido) throws IOException {
            drawText("Espetaria Barbosa", MARGIN, y, PDType1Font.HELVETICA_BOLD, 22, 0.93f, 0.18f, 0.12f);
            y -= 24;
            drawText("Recibo do pedido #" + pedido.getId(), MARGIN, y, PDType1Font.HELVETICA_BOLD, 15);
            y -= 18;
            drawText("Gerado em: " + LocalDateTime.now().format(DATA_HORA), MARGIN, y,
                    PDType1Font.HELVETICA, 10, 0.28f, 0.32f, 0.38f);
            y -= 18;
            drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, 0.88f, 0.28f, 0.14f);
            y -= 24;
        }

        void orderInfo(Pedido pedido) throws IOException {
            section("Dados da comanda");
            infoRow("Cliente", pedido.getNomeCliente());
            infoRow("Atendimento", pedido.getTipoAtendimento() == null ? "-" : pedido.getTipoAtendimento().name());
            infoRow("Mesa/Referencia", pedido.getMesa());
            infoRow("Status", pedido.getStatus() == null ? "-" : pedido.getStatus().name());
            infoRow("Criado em", pedido.getCriadoEm() == null ? "-" : pedido.getCriadoEm().format(DATA_HORA));
            y -= 8;
        }

        void items(Pedido pedido) throws IOException {
            section("Itens");
            tableHeader();
            boolean shaded = false;
            for (ItemPedido item : pedido.getItens()) {
                ensureSpace(30);
                if (shaded) {
                    drawFilledRect(MARGIN, y - 19, CONTENT_WIDTH, 22, 0.97f, 0.97f, 0.95f);
                }
                drawText(truncate(item.getNomeProduto(), 34), MARGIN + 8, y - 12, PDType1Font.HELVETICA, 9);
                drawText(String.valueOf(item.getQuantidade()), MARGIN + 278, y - 12, PDType1Font.HELVETICA, 9);
                drawText(money(item.getPrecoUnitario()), MARGIN + 344, y - 12, PDType1Font.HELVETICA, 9);
                drawText(money(item.getSubtotal()), MARGIN + 452, y - 12, PDType1Font.HELVETICA_BOLD, 9);
                y -= 22;
                shaded = !shaded;
            }
            y -= 14;
        }

        void payment(Pedido pedido) throws IOException {
            section("Pagamento");
            Pagamento pagamento = pedido.getPagamento();
            BigDecimal valorOriginal = pagamento == null || pagamento.getValorOriginal() == null
                    ? pedido.getTotal()
                    : pagamento.getValorOriginal();
            BigDecimal desconto = pagamento == null ? BigDecimal.ZERO : pagamento.getDesconto();
            BigDecimal taxaServico = pagamento == null ? BigDecimal.ZERO : pagamento.getTaxaServico();
            BigDecimal valorFinal = pagamento == null || pagamento.getValor() == null ? pedido.getTotal() : pagamento.getValor();

            totalRow("Total dos itens", valorOriginal, false);
            totalRow("Desconto", desconto, false);
            totalRow("Taxa de servico", taxaServico, false);
            totalRow("Total pago", valorFinal, true);

            if (pagamento != null && pagamento.getParcelas() != null && !pagamento.getParcelas().isEmpty()) {
                y -= 8;
                drawText("Formas de pagamento", MARGIN, y, PDType1Font.HELVETICA_BOLD, 10);
                y -= 18;
                for (ParcelaPagamento parcela : pagamento.getParcelas()) {
                    totalRow(label(parcela.getFormaPagamento().name()), parcela.getValor(), false);
                }
            }
        }

        void footerMessage() throws IOException {
            y -= 12;
            drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, 0.82f, 0.82f, 0.82f);
            y -= 22;
            drawText("Obrigado pela preferencia!", MARGIN, y, PDType1Font.HELVETICA_BOLD, 12,
                    0.50f, 0.11f, 0.11f);
        }

        void close() throws IOException {
            if (content != null) {
                content.close();
            }
        }

        private void tableHeader() throws IOException {
            drawFilledRect(MARGIN, y - 20, CONTENT_WIDTH, 22, 0.50f, 0.11f, 0.11f);
            drawText("Produto", MARGIN + 8, y - 13, PDType1Font.HELVETICA_BOLD, 8, 1, 1, 1);
            drawText("Qtd", MARGIN + 278, y - 13, PDType1Font.HELVETICA_BOLD, 8, 1, 1, 1);
            drawText("Unitario", MARGIN + 344, y - 13, PDType1Font.HELVETICA_BOLD, 8, 1, 1, 1);
            drawText("Subtotal", MARGIN + 452, y - 13, PDType1Font.HELVETICA_BOLD, 8, 1, 1, 1);
            y -= 24;
        }

        private void section(String text) throws IOException {
            ensureSpace(44);
            drawText(text, MARGIN, y, PDType1Font.HELVETICA_BOLD, 12);
            y -= 18;
        }

        private void infoRow(String label, String value) throws IOException {
            ensureSpace(22);
            drawText(label + ":", MARGIN, y, PDType1Font.HELVETICA_BOLD, 9);
            drawText(value == null || value.isBlank() ? "-" : value, MARGIN + 110, y, PDType1Font.HELVETICA, 9);
            y -= 17;
        }

        private void totalRow(String label, BigDecimal value, boolean highlight) throws IOException {
            ensureSpace(24);
            if (highlight) {
                drawFilledRect(MARGIN, y - 17, CONTENT_WIDTH, 22, 0.99f, 0.95f, 0.82f);
            }
            drawText(label, MARGIN + 8, y - 10, highlight ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA, 10);
            drawText(money(value), MARGIN + 410, y - 10, PDType1Font.HELVETICA_BOLD, 10);
            y -= 22;
        }

        private void addPage() throws IOException {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            content = new PDPageContentStream(document, page);
            drawWatermark();
            y = PAGE_HEIGHT - MARGIN;
        }

        private void drawWatermark() throws IOException {
            if (watermark == null) {
                return;
            }

            PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();
            graphicsState.setNonStrokingAlphaConstant(0.06f);
            content.setGraphicsStateParameters(graphicsState);

            float width = 340;
            float ratio = (float) watermark.getHeight() / watermark.getWidth();
            float height = width * ratio;
            content.drawImage(watermark, (PAGE_WIDTH - width) / 2, (PAGE_HEIGHT - height) / 2, width, height);

            PDExtendedGraphicsState reset = new PDExtendedGraphicsState();
            reset.setNonStrokingAlphaConstant(1f);
            content.setGraphicsStateParameters(reset);
        }

        private void ensureSpace(float height) throws IOException {
            if (y - height > MARGIN) {
                return;
            }

            content.close();
            addPage();
        }

        private void drawText(String text, float x, float y, PDType1Font font, int size) throws IOException {
            drawText(text, x, y, font, size, 0.08f, 0.10f, 0.16f);
        }

        private void drawText(String text, float x, float y, PDType1Font font, int size,
                              float r, float g, float b) throws IOException {
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

        private void drawFilledRect(float x, float y, float width, float height, float r, float g, float b)
                throws IOException {
            content.setNonStrokingColor(r, g, b);
            content.addRect(x, y, width, height);
            content.fill();
            content.setNonStrokingColor(0, 0, 0);
        }

        private PDImageXObject carregarMarcaDagua(PDDocument document) {
            try {
                var resource = new ClassPathResource("static/images/logo-watermark.png");
                return PDImageXObject.createFromByteArray(document, resource.getInputStream().readAllBytes(),
                        "logo-watermark");
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
                    .replace('\u00A0', ' ');
        }
    }
}
