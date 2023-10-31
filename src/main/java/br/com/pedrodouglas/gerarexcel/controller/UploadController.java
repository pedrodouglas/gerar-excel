package br.com.pedrodouglas.gerarexcel.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UploadController {
    @GetMapping("/upload")
    public String showUploadForm() {
        return "upload";
    }
}
