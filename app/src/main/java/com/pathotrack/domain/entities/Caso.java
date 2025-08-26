package com.pathotrack.domain.entities;

import com.pathotrack.domain.enums.Etapa;

/*
    TODO utilizar essa classe na persistencia se por acaso for integrar com BD
 */

public class Caso {

    private Long id;
    private String dataRequisicao;
    private String dataEntrega;
    private String numeroExame;
    private Etapa etapaAtual;
    private Long pacienteId;

    public Caso(Long id, String dataRequisicao, String dataEntrega, String numeroExame, Etapa etapaAtual, Long pacienteId) {
        this.id = id;
        this.dataRequisicao = dataRequisicao;
        this.dataEntrega = dataEntrega;
        this.numeroExame = numeroExame;
        this.etapaAtual = etapaAtual;
        this.pacienteId = pacienteId;
    }

    public Long getId() {
        return id;
    }

    public String getDataRequisicao() {
        return dataRequisicao;
    }

    public String getDataEntrega() {
        return dataEntrega;
    }

    public String getNumeroExame() {
        return numeroExame;
    }

    public Etapa getEtapaAtual() {
        return etapaAtual;
    }

    public Long getPacienteId() {
        return pacienteId;
    }
}
