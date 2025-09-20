package com.pathotrack.domain.enums;

import android.content.res.Resources;
import android.view.View;
import androidx.annotation.StringRes;

import com.pathotrack.R;

import java.text.Normalizer;
import java.util.Locale;

public enum Etapa {
    RECEBIMENTO(R.string.etapa_recebimento),
    MACROSCOPIA(R.string.etapa_macroscopia),
    PROCESSAMENTO(R.string.etapa_processamento),
    CORTE_HISTOLOGICO(R.string.etapa_corte_histologico),
    LAUDO(R.string.etapa_laudo),
    FINALIZADO(R.string.etapa_finalizado);

    @StringRes
    private final int labelResId;

    Etapa(@StringRes int labelResId) {
        this.labelResId = labelResId;
    }

    public @StringRes int getLabelResId() {
        return labelResId;
    }
    public String label(View v) {
        return v.getResources().getString(labelResId);
    }

    public static Etapa parse(String raw) {
        if (raw == null) return null;
        String norm = normalizeToEnumName(raw);
        try {
            return Etapa.valueOf(norm);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static String normalizeToEnumName(String s) {
        String n = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");              // remove acentos
        n = n.trim().toUpperCase(Locale.ROOT);
        n = n.replaceAll("[^A-Z0-9]+", "_");            // espaços/hífens -> _
        return n;
    }
}
