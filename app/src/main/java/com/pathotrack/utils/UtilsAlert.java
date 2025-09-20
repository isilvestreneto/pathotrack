package com.pathotrack.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.pathotrack.R;

public class UtilsAlert {

    private UtilsAlert() { }

    public static void mostrarAviso(Context contexto,
                                    String mensagem,
                                    DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(contexto);
        builder.setTitle(R.string.alerta);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setMessage(mensagem);
        builder.setNeutralButton("OK", listener);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    public static void mostrarAviso(Context contexto,
                                    int mensagem,
                                    DialogInterface.OnClickListener listener) {
        mostrarAviso(contexto, contexto.getString(mensagem), listener);
    }

    public static void mostrarAviso(Context contexto,
                                    int mensagem) {
        mostrarAviso(contexto, contexto.getString(mensagem), null);
    }

    public static void confirmarAcao(Context contexto,
                                     String mensagem,
                                     DialogInterface.OnClickListener listenerSim,
                                     DialogInterface.OnClickListener listenerNao) {
        AlertDialog.Builder builder = new AlertDialog.Builder(contexto);
        builder.setTitle(R.string.confirmacao);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setMessage(mensagem);
        builder.setPositiveButton(R.string.sim, listenerSim);
        builder.setNegativeButton(R.string.nao, listenerNao);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public static void confirmarAcao(Context contexto,
                                     int idMensagem,
                                     DialogInterface.OnClickListener listenerSim,
                                     DialogInterface.OnClickListener listenerNao) {
       confirmarAcao(contexto, contexto.getColor(idMensagem), listenerSim, listenerNao);
    }

}