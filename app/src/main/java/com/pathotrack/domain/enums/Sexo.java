package com.pathotrack.domain.enums;

import com.pathotrack.R;

public enum Sexo {
    MASCULINO(R.id.radioButtonMasculino),
    FEMININO(R.id.radioButtonFeminino);

    private final int radioId;

    Sexo(int radioId) {
        this.radioId = radioId;
    }

    public int getRadioId() {
        return radioId;
    }

    public static Sexo fromRadioId(int id) {
        for (Sexo sexo : values()) {
            if (sexo.radioId == id) {
                return sexo;
            }
        }
        throw new IllegalArgumentException("Id inválido para Sexo: " + id);
    }
}
