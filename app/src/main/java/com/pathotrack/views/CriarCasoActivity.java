package com.pathotrack.views;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.pathotrack.R;
import com.pathotrack.domain.enums.Etapa;
import com.pathotrack.domain.enums.Sexo;
import com.pathotrack.utils.ChavesCasoPaciente;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CriarCasoActivity extends AppCompatActivity {

    EditText editTextDataNascimento, editTextDataSolicitacao, editTextNumeroExame,
            editTextNomePaciente, editTextSus, editTextDataEntrega;
    RadioGroup radioGroupSexo;
    MaterialAutoCompleteTextView etapaView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criar_caso);

        editTextNumeroExame = findViewById(R.id.editTextNumeroExame);
        editTextNomePaciente = findViewById(R.id.editTextNomePaciente);
        editTextDataNascimento = findViewById(R.id.editTextDataNascimento);
        radioGroupSexo = findViewById(R.id.radioGroupSexo);
        editTextSus = findViewById(R.id.editTextSus);
        editTextDataEntrega = findViewById(R.id.editTextDataEntrega);
        editTextDataSolicitacao = findViewById(R.id.editTextDataSolicitacao);
        etapaView = findViewById(R.id.etEtapaAtual);

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
            itens[i] = Etapa.values()[i].getLabel();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                itens
        );
        etapaView.setAdapter(adapter);
        etapaView.setText(Etapa.RECEBIMENTO.getLabel(), false);
        etapaView.setKeyListener(null);
        etapaView.setOnClickListener(v -> etapaView.showDropDown());

        String txt = etapaView.getText() == null ? "" : etapaView.getText().toString().trim();
    }

    private static LocalDate epochMsToLocalDate(long ms) {
        // Converte ms -> dias desde 1970-01-01 (UTC) sem passar por timezones
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            long epochDay = Math.floorDiv(ms, 86_400_000L); // 24 * 60 * 60 * 1000
            return LocalDate.ofEpochDay(epochDay);
        }
        return null;
    }

    private static String millisToIso(long ms) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;        // yyyy-MM-dd
            return epochMsToLocalDate(ms).format(ISO);
        }
        return "";
    }

    private static String millisToDisplay(long ms) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter BR = DateTimeFormatter.ofPattern("dd/MM/uuuu");
            return epochMsToLocalDate(ms).format(BR);
        }
        return "";
    }

    public void salvarValores(View view) {
        String numeroExame = editTextNumeroExame.getText().toString();

        if (numeroExame == null || numeroExame.trim().isEmpty()) {
            Toast.makeText(this,
                    R.string.faltou_entrar_com_o_n_mero_do_exame,
                    Toast.LENGTH_LONG).show();
            editTextNumeroExame.requestFocus();
            return;
        }

        String nomePaciente = editTextNomePaciente.getText().toString();

        if (nomePaciente == null || nomePaciente.trim().isEmpty()) {
            Toast.makeText(this,
                    R.string.faltou_entrar_com_o_nome_do_paciente,
                    Toast.LENGTH_LONG).show();
            editTextNomePaciente.requestFocus();
            return;
        }

        String dataNascimento = editTextDataNascimento.getText().toString();

        if (dataNascimento == null || dataNascimento.trim().isEmpty()) {
            Toast.makeText(this,
                    R.string.faltou_entrar_com_a_data_de_nascimento,
                    Toast.LENGTH_LONG).show();
            editTextDataNascimento.requestFocus();
            return;
        }

        int checkedRadioButtonId = radioGroupSexo.getCheckedRadioButtonId();
        if (checkedRadioButtonId == -1) {
            Toast.makeText(this,
                    R.string.faltou_selecionar_o_sexo,
                    Toast.LENGTH_LONG).show();
            radioGroupSexo.requestFocus();
            return;
        }

        String numeroSus = editTextSus.getText().toString();

        if (numeroSus == null || numeroSus.trim().isEmpty()) {
            Toast.makeText(this,
                    R.string.faltou_entrar_com_o_n_mero_do_sus,
                    Toast.LENGTH_LONG).show();
            editTextSus.requestFocus();
            return;
        }

        String dataSolicitacao = editTextDataSolicitacao.getText().toString();

        if (dataSolicitacao == null || dataSolicitacao.trim().isEmpty()) {
            Toast.makeText(this,
                    R.string.faltou_entrar_com_a_data_de_solicita_o,
                    Toast.LENGTH_LONG).show();
            editTextDataSolicitacao.requestFocus();
            return;
        }

        String dataEntrega = editTextDataEntrega.getText().toString();

        if (dataEntrega == null || dataEntrega.trim().isEmpty()) {
            Toast.makeText(this,
                    R.string.faltou_entrar_com_a_data_de_entrega,
                    Toast.LENGTH_LONG).show();
            editTextDataEntrega.requestFocus();
            return;
        }


        String etapa = etapaView.getText().toString();

        if (etapa == null || etapa.trim().isEmpty()) {
            Toast.makeText(this,
                    R.string.faltou_entrar_com_a_etapa,
                    Toast.LENGTH_LONG).show();
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
        intentResposta.putExtra(ChavesCasoPaciente.ETAPA, etapa);
        intentResposta.putExtra(ChavesCasoPaciente.DATA_ENTREGA, dataEntrega);
        setResult(RESULT_OK, intentResposta);

        Toast.makeText(this,
                "Dados salvos com sucesso!",
                Toast.LENGTH_LONG
        ).show();

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
        Toast.makeText(this,
                R.string.as_entradas_foram_apagadas,
                Toast.LENGTH_LONG).show();
    }


}