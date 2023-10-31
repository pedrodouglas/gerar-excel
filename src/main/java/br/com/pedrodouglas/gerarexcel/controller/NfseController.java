package br.com.pedrodouglas.gerarexcel.controller;

        import br.com.pedrodouglas.gerarexcel.model.Nfse;
        import br.com.pedrodouglas.gerarexcel.service.NfseService;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.http.HttpHeaders;
        import org.springframework.http.HttpStatus;
        import org.springframework.http.MediaType;
        import org.springframework.http.ResponseEntity;
        import org.springframework.ui.Model;
        import org.springframework.web.bind.annotation.*;
        import org.springframework.web.multipart.MultipartFile;
        import org.w3c.dom.Document;
        import org.w3c.dom.NodeList;

        import javax.servlet.http.HttpServletResponse;
        import javax.xml.parsers.DocumentBuilder;
        import javax.xml.parsers.DocumentBuilderFactory;
        import java.io.InputStream;
        import java.nio.charset.StandardCharsets;
        import java.util.List;

@RestController
@RequestMapping("/api")
public class NfseController {
    @Autowired
    NfseService nfseService;

    @PostMapping("/gerar-csv")
    public ResponseEntity<byte[]> gerarExcel(@RequestBody String xmlData, HttpServletResponse response) {

        try {
            // Parse XML and get nfseList
            List<Nfse> nfseList = nfseService.parseNfseData(xmlData);

            // Lógica para gerar o arquivo CSV com nfseList
            byte[] csvBytes = nfseService.gerarCSV(nfseList);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "notas_fiscais.csv");

            // Retorna o arquivo CSV no corpo da resposta com o cabeçalho Content-Disposition configurado
            return ResponseEntity.ok().headers(headers).body(csvBytes);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar Excel", e);
        }
    }

    @PostMapping("/upload-xml")
    public ResponseEntity<byte[]> handleFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename();
            if (fileName != null && fileName.toLowerCase().endsWith(".xml")) {
                String xmlData = new String(file.getBytes());
                List<Nfse> nfseList = nfseService.parseNfseData(xmlData);
                byte[] csvBytes = nfseService.gerarCSV(nfseList);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentDispositionFormData("attachment", "notas_fiscais.csv");

                return ResponseEntity.ok().headers(headers).body(csvBytes);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Por favor, escolha um arquivo XML válido.".getBytes());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(("Erro ao processar o arquivo XML: " + e.getMessage()).getBytes());
        }

    }
}