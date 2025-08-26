package com.pathotrack.domain.entities;

import com.pathotrack.domain.enums.Sexo;

import java.util.List;

/*
    TODO utilizar essa classe na persistencia se por acaso for integrar com BD
 */
public class Paciente {
    private Long id;
    private String nome;
    private String dataNascimento;
    private int idade;
    private Sexo sexo;
    private String sus;
    private String prontuario;
    private List<Caso> casos;

    public Paciente(Long id, String nome, String dataNascimento, int idade, Sexo sexo, String sus, String prontuario) {
        this.id = id;
        this.nome = nome;
        this.dataNascimento = dataNascimento;
        this.idade = idade;
        this.sexo = sexo;
        this.sus = sus;
        this.prontuario = prontuario;
    }
}
