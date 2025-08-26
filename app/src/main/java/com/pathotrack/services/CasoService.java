package com.pathotrack.services;

import com.pathotrack.domain.entities.CasoPacienteDTO;
import com.pathotrack.domain.enums.Sexo;

import java.util.ArrayList;
import java.util.List;

public class CasoService {

    private List<CasoPacienteDTO> casos = new ArrayList<>();

    public List<CasoPacienteDTO> buscarCasos() {
        return casos;
    }

    private static class PacienteInfo {
        Long id;
        String nome;
        String dataNascimento;
        Sexo sexo;
        String sus;
        String prontuario;

        PacienteInfo(Long id, String nome, String dataNascimento, Sexo sexo, String sus, String prontuario) {
            this.id = id;
            this.nome = nome;
            this.dataNascimento = dataNascimento;
            this.sexo = sexo;
            this.sus = sus;
            this.prontuario = prontuario;
        }
    }
}
