package br.com.pedrodouglas.gerarexcel.service;

import br.com.pedrodouglas.gerarexcel.model.Nfe;
import br.com.pedrodouglas.gerarexcel.model.Nfse;
import br.com.pedrodouglas.gerarexcel.repository.NfeRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class NotaFiscalService {

    final
    NfeRepository nfeRepository;

    public NotaFiscalService(NfeRepository nfeRepository) {
        this.nfeRepository = nfeRepository;
    }

    public List<Nfe> perseNfeData(String xml) {
        try {
            InputStream inputStream = new ByteArrayInputStream(xml.getBytes("UTF-8"));

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(inputStream));

            NodeList nfseNodes = document.getElementsByTagName("nfeProc");

            List<Nfe> nfseList = new ArrayList<>();
            for (int i = 0; i < nfseNodes.getLength(); i++) {
                Node node = nfseNodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element nfseElement = (Element) node;
                    Nfe nfse = new Nfe();

                    nfse.setNumero(Integer.valueOf(nfseElement.getElementsByTagName("nNF").item(0).getTextContent()));
                    nfse.setValorTotal(Double.parseDouble(nfseElement.getElementsByTagName("vNF").item(0).getTextContent()));
                    nfse.setEmpresa(nfseElement.getElementsByTagName("xNome").item(0).getTextContent());
                    nfse.setCfop(Integer.valueOf(nfseElement.getElementsByTagName("CFOP").item(0).getTextContent()));
                    String dataTexto = nfseElement.getElementsByTagName("dhEmi").item(0).getTextContent();

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy");

                    Date fullDate = dateFormat.parse(dataTexto);
                    String formattedDate = outputFormat.format(fullDate);
                    nfse.setDataNfe(formattedDate);

                    NodeList totalNodes = nfseElement.getElementsByTagName("total");
                    if (totalNodes.getLength() > 0) {
                        Element totalElement = (Element) totalNodes.item(0);

                        NodeList icmsTotNodes = totalElement.getElementsByTagName("ICMSTot");
                        if (icmsTotNodes.getLength() > 0) {
                            Element icmsTotElement = (Element) icmsTotNodes.item(0);

                            // Pega os valores de vBC e vICMS
                            nfse.setBaseCalculo(Double.parseDouble(icmsTotElement.getElementsByTagName("vBC").item(0).getTextContent()));
                            nfse.setValorIcms(Double.parseDouble(icmsTotElement.getElementsByTagName("vICMS").item(0).getTextContent()));
                        }
                    }

                    nfseList.add(nfse);
                }
            }

            return calculoPercentual(nfseList);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<Nfe> calculoPercentual(List<Nfe> nfseList) {
        for (Nfe nfse : nfseList) {
            double valorTotal = nfse.getValorTotal();
            double baseCalculo = nfse.getBaseCalculo();
            double valorIcms = nfse.getValorIcms();

            double percentual = BigDecimal.valueOf((valorTotal - baseCalculo) * 0.19).setScale(2, RoundingMode.HALF_UP).doubleValue();
            nfse.setCalculo(percentual);
        }

        return nfseList;

    }


    public List<Nfse> parseNfseData(String xml) {
        try {

            InputStream inputStream = new ByteArrayInputStream(xml.getBytes("UTF-8"));

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
                    nfse.setSituacao(nfseElement.getElementsByTagName("Codigo").item(0).getTextContent());
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
            double valorTotal = 0;
            double pisNotaFiscal = 0;
            double totalCofinsNota = 0;

            for (Nfse nfse : nfseList) {
                Row row = sheet.createRow(rowNum++);

                if(nfse.getSituacao().equalsIgnoreCase("L040") ||  nfse.getSituacao().equalsIgnoreCase("L050")){
                    nfse.setValor(0);
                    nfse.setPis(0);
                    nfse.setCofins(0);
                    nfse.setIrpj(0);
                    nfse.setCsll(0);
                    nfse.setInss(0);
                    nfse.setIss(0);
                }


                row.createCell(0).setCellValue(nfse.getNumero());
                row.createCell(1).setCellValue(nfse.getValor());
                row.createCell(2).setCellValue(nfse.getPis());
                row.createCell(3).setCellValue(nfse.getCofins());
                row.createCell(4).setCellValue(nfse.getIrpj());
                row.createCell(5).setCellValue(nfse.getCsll());
                row.createCell(6).setCellValue(Objects.isNull(nfse.getIss()) ? 0 : nfse.getIss());
                row.createCell(7).setCellValue(Objects.isNull(nfse.getInss()) ? 0 : nfse.getInss());

                valorTotal += nfse.getValor();
                pisNotaFiscal += nfse.getPis(); // Adiciona o valor de PIS para o total
                totalCofinsNota += nfse.getCofins(); // Adiciona o valor de COFINS para o total
            }

            Row totalRow = sheet.createRow(rowNum);
            totalRow.createCell(0).setCellValue("Totais");
            for (int i = 1; i <= 7; i++) {
                // Calcular e adicionar os totais das colunas 1 a 7
                CellReference startCellRef = new CellReference(1, i, false, false);
                CellReference endCellRef = new CellReference(rowNum - 1, i, false, false);

                String formula = "SUM(" + startCellRef.formatAsString() + ":" + endCellRef.formatAsString() + ")";
                totalRow.createCell(i).setCellFormula(formula);
            }

            int rowNumFinal = rowNum + 2;
            Row pis = sheet.createRow(rowNumFinal);

            double pisTotal = valorTotal * 0.0065;
            pisTotal = Math.round(pisTotal * 100.0) / 100.0;

            pis.createCell(0).setCellValue("pis");
            pis.createCell(1).setCellValue(pisTotal);

            Row pisRetido = sheet.createRow(rowNumFinal + 1);
            pisRetido.createCell(0).setCellValue("pis retido");
            pisRetido.createCell(1).setCellValue(pisNotaFiscal);

            Row pisARecolher = sheet.createRow(rowNumFinal + 2);
            pisARecolher.createCell(0).setCellValue("pis a recolher");
            pisARecolher.createCell(1).setCellValue(pisTotal - pisNotaFiscal);

            Row cofins = sheet.createRow(rowNumFinal + 4);
            double totalConfis = valorTotal * 0.03;
            totalConfis = Math.round(totalConfis * 100.0) / 100.0;
            cofins.createCell(0).setCellValue("cofins");
            cofins.createCell(1).setCellValue(totalConfis);

            Row confisRetido = sheet.createRow(rowNumFinal + 5);
            confisRetido.createCell(0).setCellValue("cofins retido");
            confisRetido.createCell(1).setCellValue(totalCofinsNota);

            Row cofinsARecolher = sheet.createRow(rowNumFinal + 6);
            cofinsARecolher.createCell(0).setCellValue("cofins a recolher");
            cofinsARecolher.createCell(1).setCellValue(totalConfis - totalCofinsNota);



            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }

    public byte[] gerarExcelNfe(List<Nfe> nfseList) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("NFE");

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Nota Fiscal");
            headerRow.createCell(1).setCellValue("Data");
            headerRow.createCell(2).setCellValue("Empresa");
            headerRow.createCell(3).setCellValue("CFOP");
            headerRow.createCell(4).setCellValue("ICMS");
            headerRow.createCell(5).setCellValue("Base de Cálculo ICMS");
            headerRow.createCell(6).setCellValue("Valor Total");
            headerRow.createCell(7).setCellValue("Base de cálculo - Protege");

            int rowNum = 1;
            for (Nfe nfe : nfseList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(nfe.getNumero());
                row.createCell(1).setCellValue(nfe.getDataNfe());
                row.createCell(2).setCellValue(nfe.getEmpresa());
                row.createCell(3).setCellValue(nfe.getCfop());
                row.createCell(4).setCellValue(nfe.getValorIcms());
                row.createCell(5).setCellValue(nfe.getBaseCalculo());
                row.createCell(6).setCellValue(nfe.getValorTotal());
                row.createCell(7).setCellValue(nfe.getCalculo());
            }

            for (int i = 0; i < 6; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            dellAll();
            return outputStream.toByteArray();
        }
    }


    public List<Nfe> getAllNotas() {
        return nfeRepository.findAll();
    }

    public void saveAll(List<Nfe> nfseList) {
        nfeRepository.saveAll(nfseList);
    }

    public void dellAll() {
        nfeRepository.deleteAll();
    }


}