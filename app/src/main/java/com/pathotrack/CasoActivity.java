package com.pathotrack;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.pathotrack.enums.Etapa;

public class CasoActivity extends AppCompatActivity {

    EditText editTextDataNascimento, editTextDataSolicitacao, editTextNumeroExame,
            editTextNomePaciente, editTextSus;
    RadioGroup radioGroupSexo;
    MaterialAutoCompleteTextView etapaView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caso);

        editTextNumeroExame = findViewById(R.id.editTextNumeroExame);
        editTextNomePaciente = findViewById(R.id.editTextNomePaciente);
        editTextDataNascimento = findViewById(R.id.editTextData);
        radioGroupSexo = findViewById(R.id.radioGroupSexo);
        editTextSus = findViewById(R.id.editTextSus);
        editTextDataSolicitacao = findViewById(R.id.editTextDate);
        etapaView = findViewById(R.id.etEtapaAtual);

        editTextDataNascimento.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker =
                    MaterialDatePicker.Builder.datePicker()
                            .setTitleText("Selecionar data")
                            .build();

            datePicker.addOnPositiveButtonClickListener(selection ->
                    editTextDataNascimento.setText(datePicker.getHeaderText())
            );

            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
        });


        editTextDataSolicitacao.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker =
                    MaterialDatePicker.Builder.datePicker()
                            .setTitleText("Selecionar data")
                            .build();

            datePicker.addOnPositiveButtonClickListener(selection ->
                    editTextDataSolicitacao.setText(datePicker.getHeaderText())
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

        Etapa etapaSelecionada = null;

        if (!txt.isEmpty()) {
            etapaSelecionada = Etapa.fromLabel(txt);
        }
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

        String etapa = etapaView.getText().toString();

        if (etapa == null || etapa.trim().isEmpty()) {
            Toast.makeText(this,
                    R.string.faltou_entrar_com_a_etapa,
                    Toast.LENGTH_LONG).show();
            etapaView.requestFocus();
            return;
        }

        Toast.makeText(this,
                "Número do exame: " + numeroExame + "\n" +
                        "Nome do paciente: " + nomePaciente + "\n" +
                        "Data de nascimento: " + dataNascimento + "\n" +
                        "Sexo: " + checkedRadioButtonId + "\n" +
                        "Número do SUS: " + numeroSus + "\n" +
                        "Data da requisição: " + dataSolicitacao + "\n" +
                        "Etapa atual: " + etapa,
                Toast.LENGTH_LONG
        ).show();
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