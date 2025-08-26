package com.pathotrack.domain.entities;

import android.os.Build;

import com.pathotrack.domain.enums.Etapa;
import com.pathotrack.domain.enums.Sexo;
import com.pathotrack.services.IdadeService;

public class CasoPacienteDTO {
    private final Long casoId;
    private final String numeroExame;
    private final String dataRequisicao;
    private final String dataEntrega;
    private final Etapa etapa;

    private final Long pacienteId;
    private final String pacienteNome;
    private final String dataNascimento;
    private String idade = new String();
    private final Sexo sexo;
    private final String sus;
    private final String prontuario;

    public CasoPacienteDTO(Long casoId, String numeroExame, String dataRequisicao, String dataEntrega, Etapa etapa,
                           Long pacienteId, String pacienteNome, String dataNascimento, Sexo sexo,
                           String sus, String prontuario) {
        this.casoId = casoId;
        this.numeroExame = numeroExame;
        this.dataRequisicao = dataRequisicao;
        this.dataEntrega = dataEntrega;
        this.etapa = etapa;
        this.pacienteId = pacienteId;
        this.pacienteNome = pacienteNome;
        this.dataNascimento = dataNascimento;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.idade = this.definirIdade(dataNascimento);
        }
        this.sexo = sexo;
        this.sus = sus;
        this.prontuario = prontuario;
    }

    private String definirIdade(String dataNascimento) {
        IdadeService idade = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            idade = IdadeService.systemDefault();
        }
        String result = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            result = idade.formatAgeYearsMonths(dataNascimento);
        }
        return result;
    }

    public Long getCasoId() {
        return casoId;
    }

    public String getNumeroExame() {
        return numeroExame;
    }

    public String getDataRequisicao() {
        return dataRequisicao;
    }

    public String getDataEntrega() {
        return dataEntrega;
    }

    public Etapa getEtapa() {
        return etapa;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public String getPacienteNome() {
        return pacienteNome;
    }

    public String getDataNascimento() {
        return dataNascimento;
    }

    public String getIdade() {
        return idade;
    }

    public Sexo getSexo() {
        return sexo;
    }

    public String getSus() {
        return sus;
    }

    public String getProntuario() {
        return prontuario;
    }
}
