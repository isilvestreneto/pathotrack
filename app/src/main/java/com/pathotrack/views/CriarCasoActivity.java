package com.pathotrack.views;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.pathotrack.R;
import com.pathotrack.domain.enums.Etapa;
import com.pathotrack.domain.enums.Sexo;
import com.pathotrack.utils.ChavesCasoPaciente;
import com.pathotrack.utils.UtilsAlert;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CriarCasoActivity extends AppCompatActivity {

    EditText editTextDataNascimento, editTextDataSolicitacao, editTextNumeroExame,
            editTextNomePaciente, editTextSus, editTextDataEntrega;
    MaterialButton titulo;
    RadioGroup radioGroupSexo;
    MaterialAutoCompleteTextView etapaView;
    public static final String KEY_MODO = "MODO";
    public static final int MODO_NOVO = 0;
    public static final int MODO_EDITAR = 1;
    private int modo;
    private long casoId = -1L;
    private long pacienteId = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criar_caso);

        titulo = findViewById(R.id.titulo);
        editTextNumeroExame = findViewById(R.id.editTextNumeroExame);
        editTextNomePaciente = findViewById(R.id.editTextNomePaciente);
        editTextDataNascimento = findViewById(R.id.editTextDataNascimento);
        radioGroupSexo = findViewById(R.id.radioGroupSexo);
        editTextSus = findViewById(R.id.editTextSus);
        editTextDataEntrega = findViewById(R.id.editTextDataEntrega);
        editTextDataSolicitacao = findViewById(R.id.editTextDataSolicitacao);
        etapaView = findViewById(R.id.etEtapaAtual);

        setupEtapaDropdown();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            modo = bundle.getInt(KEY_MODO);

            if (modo == MODO_NOVO) {
                titulo.setText(R.string.novoCaso);
            } else {
                titulo.setText(R.string.editarCaso);

                casoId = bundle.getLong("CASO_ID", -1L);
                pacienteId = bundle.getLong("PACIENTE_ID", -1L);

                setTextIfPresent(bundle, ChavesCasoPaciente.NUMERO_EXAME, editTextNumeroExame);
                setTextIfPresent(bundle, ChavesCasoPaciente.NOME_PACIENTE, editTextNomePaciente);
                setTextIfPresent(bundle, ChavesCasoPaciente.DATA_NASCIMENTO, editTextDataNascimento);
                setTextIfPresent(bundle, ChavesCasoPaciente.NUMERO_SUS, editTextSus);
                setTextIfPresent(bundle, ChavesCasoPaciente.DATA_ENTREGA, editTextDataEntrega);
                setTextIfPresent(bundle, ChavesCasoPaciente.DATA_SOLICITACAO, editTextDataSolicitacao);

                String sexoStr = bundle.getString(ChavesCasoPaciente.SEXO);
                if (sexoStr != null) {
                    selectSexo(radioGroupSexo, sexoStr);
                }

                String etapaRaw = bundle.getString(ChavesCasoPaciente.ETAPA);
                if (etapaRaw != null) {
                    Etapa etapa = parseEtapaFlex(etapaRaw);
                    if (etapa != null) {
                        etapaView.setText(etapa.label(etapaView), false);
                        etapaView.setTag(etapa);
                    } else {
                        etapaView.setText(etapaRaw);
                        etapaView.setTag(null);
                    }
                }
            }
        }

        editTextDataNascimento.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker =
                    MaterialDatePicker.Builder.datePicker()
                            .setTitleText("Selecionar data")
                            .build();

            datePicker.addOnPositiveButtonClickListener((selection) -> {
                        String iso = millisToIso(selection);
                        String display = millisToDisplay(selection);

                        editTextDataNascimento.setTag(iso);
                        editTextDataNascimento.setText(display);
                    }

            );

            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
        });


        editTextDataSolicitacao.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker =
                    MaterialDatePicker.Builder.datePicker()
                            .setTitleText("Selecionar data")
                            .build();

            datePicker.addOnPositiveButtonClickListener(selection ->
                    {
                        String iso = millisToIso(selection);
                        String display = millisToDisplay(selection);
                        editTextDataSolicitacao.setTag(iso);
                        editTextDataSolicitacao.setText(display);
                    }
            );

            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
        });

        editTextDataEntrega.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker =
                    MaterialDatePicker.Builder.datePicker()
                            .setTitleText("Selecionar data")
                            .build();

            datePicker.addOnPositiveButtonClickListener(selection ->
                    {
                        String iso = millisToIso(selection);
                        String display = millisToDisplay(selection);
                        editTextDataEntrega.setTag(iso);
                        editTextDataEntrega.setText(display);
                    }
            );

            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
        });

        String[] itens = new String[Etapa.values().length];
        for (int i = 0; i < Etapa.values().length; i++) {
            itens[i] = getString(Etapa.values()[i].getLabelResId());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                itens
        );

        etapaView.setAdapter(adapter);
        etapaView.setKeyListener(null);
        etapaView.setOnClickListener(v -> etapaView.showDropDown());

        String txt = etapaView.getText() == null ? "" : etapaView.getText().toString().trim();
    }

    private void setupEtapaDropdown() {
        MaterialAutoCompleteTextView etapaDropdown = (MaterialAutoCompleteTextView) etapaView;

        List<String> labels = new ArrayList<>();
        for (Etapa e : Etapa.values()) {
            labels.add(getString(e.getLabelResId()));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                labels
        );
        etapaDropdown.setAdapter(adapter);

        etapaDropdown.setOnItemClickListener((parent, view, position, id) -> {
            Etapa selected = Etapa.values()[position];
            etapaDropdown.setTag(selected);
        });

        if (TextUtils.isEmpty(etapaDropdown.getText())) {
            etapaDropdown.setText(getString(Etapa.RECEBIMENTO.getLabelResId()), /* filter= */ false);
            etapaDropdown.setTag(Etapa.RECEBIMENTO);
        }
    }


    private void setTextIfPresent(Bundle b, String key, TextView view) {
        if (b.containsKey(key)) {
            String v = b.getString(key);
            if (v != null) view.setText(v);
        }
    }

    @Nullable
    private Etapa parseEtapaFlex(String raw) {
        return Etapa.parse(raw);
    }

    private void selectSexo(RadioGroup group, String sexoName) {
        String s = sexoName.trim().toUpperCase();
        int masculineId = getIdIfExists("radioMasculino");
        int feminineId = getIdIfExists("radioFeminino");

        if ("MASCULINO".equals(s)) {
            if (masculineId != 0 && group.findViewById(masculineId) != null) {
                group.check(masculineId);
            } else if (group.getChildCount() > 0) {
                group.check(group.getChildAt(0).getId());
            }
        } else if ("FEMININO".equals(s)) {
            if (feminineId != 0 && group.findViewById(feminineId) != null) {
                group.check(feminineId);
            } else if (group.getChildCount() > 1) {
                group.check(group.getChildAt(1).getId());
            }
        } else {
            group.clearCheck();
        }
    }

    private int getIdIfExists(String name) {
        Context ctx = this;
        return ctx.getResources().getIdentifier(name, "id", ctx.getPackageName());
    }

    private static LocalDate epochMsToLocalDate(long ms) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            long epochDay = Math.floorDiv(ms, 86_400_000L); // 24 * 60 * 60 * 1000
            return LocalDate.ofEpochDay(epochDay);
        }
        return null;
    }

    private static String millisToIso(long ms) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;        // yyyy-MM-dd
            return Objects.requireNonNull(epochMsToLocalDate(ms)).format(ISO);
        }
        return "";
    }

    private static String millisToDisplay(long ms) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter BR = DateTimeFormatter.ofPattern("dd/MM/uuuu");
            return Objects.requireNonNull(epochMsToLocalDate(ms)).format(BR);
        }
        return "";
    }

    public void salvarValores(View view) {
        String numeroExame = editTextNumeroExame.getText().toString();

        if (numeroExame.trim().isEmpty()) {
            UtilsAlert.mostrarAviso(this, R.string.faltou_entrar_com_o_n_mero_do_exame);
            editTextNumeroExame.requestFocus();
            return;
        }

        String nomePaciente = editTextNomePaciente.getText().toString();

        if (nomePaciente.trim().isEmpty()) {
            UtilsAlert.mostrarAviso(this, R.string.faltou_entrar_com_o_nome_do_paciente);
            return;
        }

        String dataNascimento = editTextDataNascimento.getText().toString();

        if (dataNascimento.trim().isEmpty()) {
            UtilsAlert.mostrarAviso(this, R.string.faltou_entrar_com_a_data_de_nascimento);
            editTextDataNascimento.requestFocus();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/uuuu");
                LocalDate nascimento = LocalDate.parse(dataNascimento.trim(), formatter);

                if (nascimento.isAfter(LocalDate.now())) {
                    UtilsAlert.mostrarAviso(this, R.string.data_nascimento_no_futuro);
                    editTextDataNascimento.requestFocus();
                    return;
                }

            } catch (DateTimeParseException e) {
                UtilsAlert.mostrarAviso(this, R.string.data_invalida);
                editTextDataNascimento.requestFocus();
                return;
            }
        }

        int checkedRadioButtonId = radioGroupSexo.getCheckedRadioButtonId();
        if (checkedRadioButtonId == -1) {
            UtilsAlert.mostrarAviso(this, R.string.faltou_selecionar_o_sexo);
            radioGroupSexo.requestFocus();
            return;
        }

        String numeroSus = editTextSus.getText().toString();

        if (numeroSus.trim().isEmpty()) {
            UtilsAlert.mostrarAviso(this, R.string.faltou_entrar_com_o_n_mero_do_sus);
            editTextSus.requestFocus();
            return;
        }

        String dataSolicitacao = editTextDataSolicitacao.getText().toString();

        if (dataSolicitacao.trim().isEmpty()) {
            UtilsAlert.mostrarAviso(this, R.string.faltou_entrar_com_a_data_de_solicita_o);
            editTextDataSolicitacao.requestFocus();
            return;
        }

        String dataEntrega = editTextDataEntrega.getText().toString();

        if (dataEntrega.trim().isEmpty()) {
            UtilsAlert.mostrarAviso(this, R.string.faltou_entrar_com_a_data_de_entrega);
            editTextDataEntrega.requestFocus();
            return;
        }


        Etapa etapaSelecionada = (Etapa) etapaView.getTag();

        if (etapaSelecionada == null) {
            UtilsAlert.mostrarAviso(this, R.string.faltou_entrar_com_a_etapa);
            etapaView.requestFocus();
            return;
        }

        Intent intentResposta = new Intent();
        intentResposta.putExtra(ChavesCasoPaciente.NUMERO_EXAME, numeroExame);
        intentResposta.putExtra(ChavesCasoPaciente.NOME_PACIENTE, nomePaciente);
        intentResposta.putExtra(ChavesCasoPaciente.DATA_NASCIMENTO, dataNascimento);
        intentResposta.putExtra(ChavesCasoPaciente.SEXO, Sexo.fromRadioId(checkedRadioButtonId).name());
        intentResposta.putExtra(ChavesCasoPaciente.NUMERO_SUS, numeroSus);
        intentResposta.putExtra(ChavesCasoPaciente.DATA_SOLICITACAO, dataSolicitacao);
        intentResposta.putExtra(ChavesCasoPaciente.ETAPA, etapaSelecionada.name());
        intentResposta.putExtra(ChavesCasoPaciente.DATA_ENTREGA, dataEntrega);

        intentResposta.putExtra(KEY_MODO, modo);

        if (modo == MODO_EDITAR) {
            intentResposta.putExtra("CASO_ID", casoId);
            intentResposta.putExtra("PACIENTE_ID", pacienteId);
        }

        setResult(RESULT_OK, intentResposta);

        UtilsAlert.mostrarAviso(this, R.string.caso_salvo_com_sucesso);

        finish();
    }

    public void limparCampos(View view) {
        editTextNumeroExame.setText(null);
        editTextNomePaciente.setText(null);
        editTextDataNascimento.setText(null);
        radioGroupSexo.clearCheck();
        editTextSus.setText(null);
        editTextDataSolicitacao.setText(null);
        etapaView.setText(null, false);
        editTextNumeroExame.requestFocus();
        UtilsAlert.mostrarAviso(this, R.string.as_entradas_foram_apagadas);
    }


}