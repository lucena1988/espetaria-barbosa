package br.com.espetariabarbosa.controller;

import br.com.espetariabarbosa.entity.Mesa;
import br.com.espetariabarbosa.enums.StatusMesa;
import br.com.espetariabarbosa.service.MesaService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mesas")
public class MesaController {

    private final MesaService mesaService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("mesas", mesaService.listarAtivas());
        model.addAttribute("mesa", new Mesa());
        model.addAttribute("statusMesas", StatusMesa.values());
        return "mesas/lista";
    }

    @PostMapping
    public String salvar(@ModelAttribute Mesa mesa) {
        mesaService.salvar(mesa);
        return "redirect:/mesas";
    }

    @PostMapping("/{id}/status")
    public String alterarStatus(@PathVariable Long id, @RequestParam StatusMesa status) {
        mesaService.alterarStatus(id, status);
        return "redirect:/mesas";
    }

    @GetMapping(value = "/{id}/qrcode", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> qrCode(@PathVariable Long id) throws WriterException, IOException {
        mesaService.buscarPorId(id);
        String urlCardapio = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/cardapio/mesa/")
                .path(id.toString())
                .toUriString();

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(urlCardapio, BarcodeFormat.QR_CODE, 260, 260);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(outputStream.toByteArray());
    }
}
