package com.pathotrack.views.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.pathotrack.R;
import com.pathotrack.domain.entities.CasoPacienteDTO;
import com.pathotrack.domain.enums.Etapa;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CasoAdapter extends RecyclerView.Adapter<CasoAdapter.CasoViewHolder> {

    private final List<CasoPacienteDTO> items = new ArrayList<>();
    private OnItemClickListener clickListener;

    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }

    private OnItemLongClickListener longClickListener;

    public void setOnItemLongClickListener(OnItemLongClickListener l) {
        this.longClickListener = l;
    }

    public void setItems(List<CasoPacienteDTO> novos) {
        items.clear();
        if (novos != null) items.addAll(novos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CasoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_caso, parent, false);
        return new CasoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CasoViewHolder h, int position) {
        CasoPacienteDTO c = items.get(position);
        String previsao = fmt(c.getDataEntrega());

        h.tvNumeroExame.setText(nz(c.getNumeroExame()));
        Etapa etapaEnum = null;

        Object et = c.getEtapa();
        if (et instanceof Etapa) {
            etapaEnum = (Etapa) et;
        } else if (et instanceof String) {
            String s = (String) et;
            if (!s.isBlank()) {
                etapaEnum = Etapa.parse(s);
            }
        }

        h.tvPaciente.setText(c.getPacienteId().toString());

        h.valIdade.setText(String.valueOf(c.getIdade()));

        h.valSus.setText(String.valueOf(c.getSus()));

        Character sexo = c.getSexo().toString().toCharArray()[0];
        String sexoLegivel = sexo.toString();
        h.valSexo.setText(sexoLegivel);

        h.valProntuario.setText(c.getProntuario());

        h.tvDatas.setText(previsao);

        if (h.chipEtapa != null) {
            String etapaLabel = (etapaEnum != null)
                    ? etapaEnum.label(h.chipEtapa)
                    : nz(c.getEtapa());

            h.chipEtapa.setText(etapaLabel);

            int bgColor = ContextCompat.getColor(h.chipEtapa.getContext(), etapaColorRes(etapaEnum));
            h.chipEtapa.setChipBackgroundColor(ColorStateList.valueOf(bgColor));

            int txtColor = (etapaEnum == null)
                    ? Color.BLACK
                    : Color.WHITE;
            if (etapaEnum == Etapa.RECEBIMENTO)
                txtColor = Color.BLACK;

            h.chipEtapa.setTextColor(txtColor);
        }

        h.itemView.setOnClickListener(v -> {
            if (clickListener == null) return;
            int pos = h.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            clickListener.onItemClick(items.get(pos));
        });

        h.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                int pos = h.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    longClickListener.onItemLongClick(pos);
                }
            }
            v.showContextMenu();
            return true;
        });
    }

    @ColorRes
    private static int etapaColorRes(Etapa etapa) {
        if (etapa == null) return R.color.etapa_default;

        switch (etapa) {
            case RECEBIMENTO:
                return R.color.duck_yellow;
            case MACROSCOPIA:
                return R.color.macro;
            case PROCESSAMENTO:
                return R.color.etapa_processamento;
            case CORTE_HISTOLOGICO:
                return R.color.duck_green_dark;
            case LAUDO:
                return R.color.etapa_laudo;
            case FINALIZADO:
                return R.color.etapa_concluido;
            default:
                return R.color.etapa_default;
        }
    }

    private static String fmt(Object d) {
        if (d == null) return "";
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (d instanceof LocalDate) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        return ((LocalDate) d).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    }
                } else if (d instanceof Date) {
                    return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format((Date) d);
                } else {
                    String s = d.toString();
                    if (s.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        LocalDate ld = LocalDate.parse(s);
                        return ld.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    }
                    return s;
                }
            }
        } catch (Exception e) {
            return d.toString();
        }
        return "";
    }

    private static String nz(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface OnItemClickListener {
        void onItemClick(CasoPacienteDTO caso);
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        this.clickListener = l;
    }

    static class CasoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumeroExame, tvDatas, tvPaciente, valIdade, valSus, valSexo, valProntuario, tvDataEntrega;
        Chip chipEtapa;

        public CasoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumeroExame = itemView.findViewById(R.id.tvNumeroExame);
            tvDatas = itemView.findViewById(R.id.tvDatas);
            tvPaciente = itemView.findViewById(R.id.tvPaciente);
            chipEtapa = itemView.findViewById(R.id.chipEtapa);
            valIdade = itemView.findViewById(R.id.valIdade);
            valSus = itemView.findViewById(R.id.valSus);
            valSexo = itemView.findViewById(R.id.valSexo);
            valProntuario = itemView.findViewById(R.id.valProntuario);
            tvDataEntrega = itemView.findViewById(R.id.dataEntrega);


            if (tvNumeroExame == null || tvDatas == null || tvPaciente == null) {
                throw new IllegalStateException("IDs do layout do item não encontrados. Revise item_caso.xml");
            }
        }
    }
}
