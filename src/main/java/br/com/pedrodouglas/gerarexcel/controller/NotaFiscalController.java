package br.com.pedrodouglas.gerarexcel.controller;

import br.com.pedrodouglas.gerarexcel.model.Nfe;
import br.com.pedrodouglas.gerarexcel.model.Nfse;
import br.com.pedrodouglas.gerarexcel.service.NotaFiscalService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class NotaFiscalController {
    final
    NotaFiscalService notaFiscalService;

    public NotaFiscalController(NotaFiscalService notaFiscalService) {
        this.notaFiscalService = notaFiscalService;
    }

    @PostMapping("/upload-nfe")
    public ResponseEntity<?> uploadNfe(@RequestParam("fileNotas") MultipartFile[] files) {
        List<Nfe> nfeList = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                if (file.getOriginalFilename().toLowerCase().endsWith(".xml")) {
                    String xmlData = new String(file.getBytes());
                    nfeList.addAll(notaFiscalService.perseNfeData(xmlData));
                    notaFiscalService.saveAll(nfeList);
                }
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar o arquivo: " + file.getOriginalFilename());
            }
        }

        return ResponseEntity.ok(nfeList);
    }

    @PostMapping("/upload-nfse")
    public ResponseEntity<byte[]> uploadNfse(@RequestParam("file") MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename();
            if (fileName != null && fileName.toLowerCase().endsWith(".xml")) {
                String xmlData = new String(file.getBytes());
                List<Nfse> nfseList = notaFiscalService.parseNfseData(xmlData);
                byte[] excelBytes = notaFiscalService.gerarExcel(nfseList);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentDispositionFormData("attachment", "notas_fiscais.xlsx");

                return ResponseEntity.ok().headers(headers).body(excelBytes);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Por favor, escolha um arquivo XML válido.".getBytes());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(("Erro ao processar o arquivo XML: " + e.getMessage()).getBytes());
        }

    }

    @GetMapping("/downloadExcel")
    public ResponseEntity<byte[]> downloadExcel() {
        List<Nfe> notasFiscais = notaFiscalService.getAllNotas();
        byte[] excelBytes;
        try {
            excelBytes = notaFiscalService.gerarExcelNfe(notasFiscais);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "notas_fiscais.xlsx");

        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }




}