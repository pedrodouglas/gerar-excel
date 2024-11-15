package br.com.pedrodouglas.gerarexcel.controller;

import br.com.pedrodouglas.gerarexcel.model.Nfe;
import br.com.pedrodouglas.gerarexcel.service.NotaFiscalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class UploadController {
    @Autowired
    private NotaFiscalService notaFiscalService;

    @GetMapping("/nfse")
    public String showUploadForm() {
        return "nfse";
    }

    @GetMapping("/nfe")
    public String showTeste(Model model) {
        List<Nfe> notasFiscais = notaFiscalService.getAllNotas();
        model.addAttribute("notasFiscais", notasFiscais);
        return "nfe"; // Nome da página Thymeleaf
    }

    @GetMapping("/")
    public String index(Model model) {
        return "index";
    }



}
