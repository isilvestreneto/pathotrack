package com.pathotrack.domain.enums;

public enum Etapa {
    RECEBIMENTO("Recebimento"),
    MACROSCOPIA("Macroscopia"),
    PROCESSAMENTO("Processamento"),
    CORTE_HISTOLOGICO("Corte histológico"),
    LAUDO("Laudo"),
    FINALIZADO("Finalizado");

    private final String label;

    Etapa(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static Etapa fromLabel(String label) {
        for (Etapa e : values()) {
            if (e.getLabel().equals(label)) return e;
        }
        throw new IllegalArgumentException("Label inválido: " + label);
    }
}
