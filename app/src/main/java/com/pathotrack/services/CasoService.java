package com.pathotrack.services;

import com.pathotrack.domain.entities.CasoPacienteDTO;
import com.pathotrack.domain.enums.Etapa;
import com.pathotrack.domain.enums.Sexo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CasoService {

    private static final List<CasoPacienteDTO> casos = new ArrayList<>();

    public CasoService() {
        // Avaliador/Professor caso queira ver com a lista vazia, comentar linha abaixo
        seedMocks();
    }

    public static List<CasoPacienteDTO> buscarCasos() {
        return casos;
    }

    private void seedMocks() {
        casos.clear();

        Random rnd = new java.util.Random();

        Etapa[] etapas = Etapa.values();

        Sexo[] sexos = Sexo.values();
        casos.add(new CasoPacienteDTO(
                1001L, "EX-0001", "20/08/2025", "28/08/2025",
                etapas[rnd.nextInt(etapas.length)], 501L, "Ana Clara Silva", "15/04/1990",
                sexos[rnd.nextInt(sexos.length)], "12345678901", "P-0001"));

        casos.add(new CasoPacienteDTO(
                1002L, "EX-0002", "22/08/2025", "28/08/2025",
                etapas[rnd.nextInt(etapas.length)], 502L, "Bruno Ferreira", "09/11/1985",
                sexos[rnd.nextInt(sexos.length)], "98765432100", "P-0002"));

        casos.add(new CasoPacienteDTO(
                1003L, "EX-0003", "25/08/2025", "27/08/2025",
                etapas[rnd.nextInt(etapas.length)], 503L, "Carla Mendes", "30/06/1978",
                sexos[rnd.nextInt(sexos.length)], "11122233344", "P-0003"));

        casos.add(new CasoPacienteDTO(
                1004L, "EX-0004", "26/08/2025", "01/09/2025",
                etapas[rnd.nextInt(etapas.length)], 504L, "Diego Souza", "12/01/2001",
                sexos[rnd.nextInt(sexos.length)], "55566677788", "P-0004"));

        casos.add(new CasoPacienteDTO(
                1005L, "EX-0005", "26/08/2025", "28/08/2025",
                etapas[rnd.nextInt(etapas.length)], 505L, "Elaine Rocha", "02/02/1995",
                sexos[rnd.nextInt(sexos.length)], "22233344455", "P-0005"));

        casos.add(new CasoPacienteDTO(
                1006L, "EX-0006", "27/08/2025", "29/08/2025",
                etapas[rnd.nextInt(etapas.length)], 506L, "Fábio Lima", "21/07/1988",
                sexos[rnd.nextInt(sexos.length)], "33344455566", "P-0006"));

        casos.add(new CasoPacienteDTO(
                1007L, "EX-0007", "28/08/2025", "28/08/2025",
                etapas[rnd.nextInt(etapas.length)], 507L, "Gabriela Nunes", "05/05/1992",
                sexos[rnd.nextInt(sexos.length)], "44455566677", "P-0007"));

        casos.add(new CasoPacienteDTO(
                1008L, "EX-0008", "29/08/2025", "03/09/2025",
                etapas[rnd.nextInt(etapas.length)], 508L, "Henrique Alves", "18/09/1983",
                sexos[rnd.nextInt(sexos.length)], "66677788899", "P-0008"));
    }
}
