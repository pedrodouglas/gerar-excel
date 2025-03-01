package br.com.pedrodouglas.gerarexcel.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Getter
@Setter
@Entity
public class Nfse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String numero;
    private double valor;
    private double pis;
    private double cofins;
    private double irpj;
    private double csll;
    private double iss;
    private double inss;
    private String situacao;


}
