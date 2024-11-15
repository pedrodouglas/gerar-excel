package br.com.pedrodouglas.gerarexcel.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@EqualsAndHashCode
public class Nfe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer numero;
    private Double valorTotal;
    private Double baseCalculo;
    private Double valorIcms;
    private String empresa;
    private Double calculo;
    private Integer cfop;
    private String dataNfe;


}
