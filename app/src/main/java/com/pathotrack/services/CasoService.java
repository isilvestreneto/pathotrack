package com.pathotrack.services;

import com.pathotrack.domain.entities.CasoPacienteDTO;
import com.pathotrack.domain.enums.Etapa;
import com.pathotrack.domain.enums.Sexo;

import java.util.ArrayList;
import java.util.List;

public class CasoService {

    private static final List<CasoPacienteDTO> casos = new ArrayList<>();

    public CasoService() {
        // Avaliador/Professor: caso queira ver com a lista vazia, comentar a linha abaixo
        seedMocks();
    }

    public static List<CasoPacienteDTO> buscarCasos() {
        return casos;
    }

    private void seedMocks() {
        casos.clear();

        casos.add(new CasoPacienteDTO(
                1001L, "EX-0001", "20/08/2025", "28/08/2025",
                Etapa.RECEBIMENTO, 501L, "Ana Clara Silva", "15/04/1990",
                Sexo.FEMININO, "12345678901", "P-0001"));

        casos.add(new CasoPacienteDTO(
                1002L, "EX-0002", "21/08/2025", "29/08/2025",
                Etapa.MACROSCOPIA, 502L, "Bruno Ferreira", "09/11/1985",
                Sexo.MASCULINO, "98765432100", "P-0002"));

        casos.add(new CasoPacienteDTO(
                1003L, "EX-0003", "22/08/2025", "30/08/2025",
                Etapa.PROCESSAMENTO, 503L, "Carla Mendes", "30/06/1978",
                Sexo.FEMININO, "11122233344", "P-0003"));

        casos.add(new CasoPacienteDTO(
                1004L, "EX-0004", "23/08/2025", "01/09/2025",
                Etapa.CORTE_HISTOLOGICO, 504L, "Diego Souza", "12/01/2001",
                Sexo.MASCULINO, "55566677788", "P-0004"));

        casos.add(new CasoPacienteDTO(
                1005L, "EX-0005", "24/08/2025", "02/09/2025",
                Etapa.LAUDO, 505L, "Elaine Rocha", "02/02/1995",
                Sexo.FEMININO, "22233344455", "P-0005"));

        casos.add(new CasoPacienteDTO(
                1006L, "EX-0006", "25/08/2025", "02/09/2025",
                Etapa.RECEBIMENTO, 506L, "Fábio Lima", "21/07/1988",
                Sexo.MASCULINO, "33344455566", "P-0006"));

        casos.add(new CasoPacienteDTO(
                1007L, "EX-0007", "26/08/2025", "03/09/2025",
                Etapa.MACROSCOPIA, 507L, "Gabriela Nunes", "05/05/1992",
                Sexo.FEMININO, "44455566677", "P-0007"));

        casos.add(new CasoPacienteDTO(
                1008L, "EX-0008", "27/08/2025", "04/09/2025",
                Etapa.PROCESSAMENTO, 508L, "Henrique Alves", "18/09/1983",
                Sexo.MASCULINO, "66677788899", "P-0008"));
    }
}
