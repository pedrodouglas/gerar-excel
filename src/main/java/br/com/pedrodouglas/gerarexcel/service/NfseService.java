package br.com.pedrodouglas.gerarexcel.service;

import br.com.pedrodouglas.gerarexcel.model.Nfse;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class NfseService {
    public List<Nfse> parseNfseData(String xml) {
        try {
            // Convertendo a String XML para um InputStream usando ByteArrayInputStream
            InputStream inputStream = new ByteArrayInputStream(xml.getBytes("UTF-8"));

            // Criando o Document a partir do InputStream
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(inputStream));
            NodeList nfseNodes = document.getElementsByTagName("GerarNfseResposta");

            List<Nfse> nfseList = new ArrayList<>();
            for (int i = 0; i < nfseNodes.getLength(); i++) {
                Node node = nfseNodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element nfseElement = (Element) node;
                    Nfse nfse = new Nfse();
                    nfse.setNumero(nfseElement.getElementsByTagName("Numero").item(0).getTextContent());
                    nfse.setValor(Double.parseDouble(nfseElement.getElementsByTagName("ValorServicos").item(0).getTextContent()));
                    nfse.setPis(Double.parseDouble(nfseElement.getElementsByTagName("ValorPis").item(0).getTextContent()));
                    nfse.setCofins(Double.parseDouble(nfseElement.getElementsByTagName("ValorCofins").item(0).getTextContent()));
                    nfse.setIrpj(Double.parseDouble(nfseElement.getElementsByTagName("ValorIr").item(0).getTextContent()));
                    nfse.setCsll(Double.parseDouble(nfseElement.getElementsByTagName("ValorCsll").item(0).getTextContent()));
                    nfse.setIss(Double.parseDouble(nfseElement.getElementsByTagName("ValorIss").item(0).getTextContent()));
                    nfse.setInss(Double.parseDouble(nfseElement.getElementsByTagName("ValorInss").item(0).getTextContent()));
                    nfseList.add(nfse);
                }
            }
            return nfseList;

        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] gerarExcel(List<Nfse> nfseList) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Notas Fiscais");

            // Adicionando títulos às colunas
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Nota Fiscal");
            headerRow.createCell(1).setCellValue("Valor");
            headerRow.createCell(2).setCellValue("PIS");
            headerRow.createCell(3).setCellValue("COFINS");
            headerRow.createCell(4).setCellValue("IRPJ");
            headerRow.createCell(5).setCellValue("CSLL");
            headerRow.createCell(6).setCellValue("ISS");
            headerRow.createCell(7).setCellValue("INSS");

            int rowNum = 1;
            for (Nfse nfse : nfseList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(nfse.getNumero());
                row.createCell(1).setCellValue(nfse.getValor());
                row.createCell(2).setCellValue(nfse.getPis());
                row.createCell(3).setCellValue(nfse.getCofins());
                row.createCell(4).setCellValue(nfse.getIrpj());
                row.createCell(5).setCellValue(nfse.getCsll());
                row.createCell(6).setCellValue(Objects.isNull(nfse.getIss()) ? 0 :nfse.getIss() );
                row.createCell(7).setCellValue(Objects.isNull(nfse.getInss()) ? 0 :nfse.getInss() );
            }


            // Escreva o arquivo Excel em um ByteArrayOutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }

    public byte[] gerarCSV(List<Nfse> nfseList) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             CSVPrinter csvPrinter = new CSVPrinter(new OutputStreamWriter(outputStream), CSVFormat.DEFAULT)) {

            // Adicione cabeçalhos ao CSV
            csvPrinter.printRecord("Nota Fiscal", "Valor", "PIS", "COFINS", "IRPJ", "CSLL","ISS", "INSS");

            // Adicione dados ao CSV
            for (Nfse nfse : nfseList) {
                csvPrinter.printRecord(nfse.getNumero(), nfse.getValor(), nfse.getPis(), nfse.getCofins(), nfse.getIrpj(), nfse.getCsll(), nfse.getInss());
            }

            csvPrinter.flush();
            return outputStream.toByteArray();
        }
    }
}
