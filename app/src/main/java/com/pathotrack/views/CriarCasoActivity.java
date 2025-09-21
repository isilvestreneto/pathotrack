package com.pathotrack.views;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.pathotrack.R;
import com.pathotrack.domain.entities.CasoPacienteDTO;
import com.pathotrack.domain.enums.Etapa;
import com.pathotrack.domain.enums.Sexo;
import com.pathotrack.persistency.CasoPacienteDB;
import com.pathotrack.utils.UtilsAlert;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CriarCasoActivity extends AppCompatActivity {

    EditText editTextDataNascimento, editTextDataSolicitacao, editTextNumeroExame,
            editTextNomePaciente, editTextSus, editTextDataEntrega;
    MaterialButton titulo;
    RadioGroup radioGroupSexo;
    MaterialAutoCompleteTextView etapaView;
    CasoPacienteDB db;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    CasoPacienteDTO casoOrginal;
    public static final String KEY_MODO = "MODO";
    public static final int MODO_NOVO = 0;
    public static final int MODO_EDITAR = 1;
    private int modo;
    private long casoId = -1L;
    private long pacienteId = -1L;
    private Long pacienteIdParaSalvar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criar_caso);

        db = CasoPacienteDB.getInstance(getApplicationContext());

        titulo = findViewById(R.id.titulo);
        editTextNumeroExame = findViewById(R.id.editTextNumeroExame);
        editTextNomePaciente = findViewById(R.id.editTextNomePaciente);
        editTextDataNascimento = findViewById(R.id.editTextDataNascimento);
        radioGroupSexo = findViewById(R.id.radioGroupSexo);
        editTextSus = findViewById(R.id.editTextSus);
        editTextDataEntrega = findViewById(R.id.editTextDataEntrega);
        editTextDataSolicitacao = findViewById(R.id.editTextDataSolicitacao);
        etapaView = findViewById(R.id.etEtapaAtual);

        MaterialButton btnSalvar = findViewById(R.id.buttonSalvar);
        btnSalvar.setOnClickListener(this::salvarValores);

        setupEtapaDropdown();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            casoId = bundle.getLong("CASO_ID", -1L);
            pacienteId = bundle.getLong("PACIENTE_ID", -1L);
            modo = bundle.getInt(KEY_MODO, MODO_NOVO);
        } else {
            modo = MODO_NOVO;
        }

        pacienteIdParaSalvar = (pacienteId != -1L) ? pacienteId : null;

        if (modo == MODO_EDITAR && (pacienteIdParaSalvar == null) && casoOrginal != null) {
            titulo.setText(R.string.editarCaso);
            pacienteIdParaSalvar = casoOrginal.getPacienteId();
        }

        if (modo == MODO_NOVO) {
            titulo.setText(R.string.novoCaso);
            pacienteIdParaSalvar = (pacienteId != -1L) ? pacienteId : null;
        } else {
            titulo.setText(R.string.editarCaso);

            io.execute(() -> {
                CasoPacienteDTO dto = db.getCasoPacienteDAO().queryForId(casoId);
                runOnUiThread(() -> {
                    casoOrginal = dto;
                    if (casoOrginal != null) {
                        setTextIfPresent(casoOrginal.getNumeroExame(), editTextNumeroExame);
                        setTextIfPresent(casoOrginal.getPacienteNome(), editTextNomePaciente);
                        setTextIfPresent(casoOrginal.getSus(), editTextSus);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            bindIsoToField(editTextDataNascimento, casoOrginal.getDataNascimento());
                            bindIsoToField(editTextDataEntrega, casoOrginal.getDataEntrega());
                            bindIsoToField(editTextDataSolicitacao, casoOrginal.getDataRequisicao());
                        } else {
                            setTextIfPresent(casoOrginal.getDataNascimento(), editTextDataNascimento);
                            setTextIfPresent(casoOrginal.getDataEntrega(), editTextDataEntrega);
                            setTextIfPresent(casoOrginal.getDataRequisicao(), editTextDataSolicitacao);
                        }

                        String sexoStr = casoOrginal.getSexo().name();
                        selectSexo(radioGroupSexo, sexoStr);

                        Etapa etapa = parseEtapaFlex(String.valueOf(casoOrginal.getEtapa()));
                        if (etapa != null) {
                            etapaView.setText(etapa.label(etapaView), false);
                            etapaView.setTag(etapa);
                        } else {
                            etapaView.setText(String.valueOf(casoOrginal.getEtapa()), false);
                            etapaView.setTag(null);
                        }

                        if (pacienteIdParaSalvar == null) {
                            pacienteIdParaSalvar = casoOrginal.getPacienteId();
                        }
                    }
                });
            });
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
            etapaDropdown.setText(getString(Etapa.RECEBIMENTO.getLabelResId()), false);
            etapaDropdown.setTag(Etapa.RECEBIMENTO);
        }
    }


    private void setTextIfPresent(String key, TextView view) {
        view.setText(key);
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

        Etapa etapaSelecionada = (Etapa) etapaView.getTag();
        if (etapaSelecionada == null) {
            UtilsAlert.mostrarAviso(this, R.string.faltou_entrar_com_a_etapa);
            etapaView.requestFocus();
            return;
        }

        String dnText = textOf(editTextDataNascimento);
        String dsText = textOf(editTextDataSolicitacao);
        String deText = textOf(editTextDataEntrega);

        String dnTagIso = tagIsoOf(editTextDataNascimento);
        String dsTagIso = tagIsoOf(editTextDataSolicitacao);
        String deTagIso = tagIsoOf(editTextDataEntrega);

        String dataNascimentoToSave;
        String dataSolicitacaoToSave;
        String dataEntregaToSave;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate birth = readLocalDateFromField(editTextDataNascimento);
            if (birth == null) {
                UtilsAlert.mostrarAviso(this, R.string.data_invalida);
                editTextDataNascimento.requestFocus();
                return;
            }
            if (birth.isAfter(LocalDate.now())) {
                UtilsAlert.mostrarAviso(this, R.string.data_nascimento_no_futuro);
                editTextDataNascimento.requestFocus();
                return;
            }
            dataNascimentoToSave = birth.toString(); // ISO yyyy-MM-dd

            LocalDate req = readLocalDateFromField(editTextDataSolicitacao);
            if (req == null) {
                UtilsAlert.mostrarAviso(this, R.string.faltou_entrar_com_a_data_de_solicita_o);
                editTextDataSolicitacao.requestFocus();
                return;
            }
            dataSolicitacaoToSave = req.toString();

            LocalDate ent = readLocalDateFromField(editTextDataEntrega);
            if (ent == null) {
                UtilsAlert.mostrarAviso(this, R.string.faltou_entrar_com_a_data_de_entrega);
                editTextDataEntrega.requestFocus();
                return;
            }
            dataEntregaToSave = ent.toString();

        } else {
            try {
                java.text.SimpleDateFormat BR = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.ROOT);
                BR.setLenient(false);

                java.util.Date dn = !dnTagIso.isEmpty()
                        ? new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ROOT).parse(dnTagIso)
                        : BR.parse(dnText);
                if (dn == null) throw new IllegalArgumentException("dn null");
                dataNascimentoToSave = toIso(dn);

                java.util.Date ds = !dsTagIso.isEmpty()
                        ? new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ROOT).parse(dsTagIso)
                        : BR.parse(dsText);
                if (ds == null) throw new IllegalArgumentException("ds null");
                dataSolicitacaoToSave = toIso(ds);

                java.util.Date de = !deTagIso.isEmpty()
                        ? new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ROOT).parse(deTagIso)
                        : BR.parse(deText);
                if (de == null) throw new IllegalArgumentException("de null");
                dataEntregaToSave = toIso(de);

            } catch (Exception e) {
                UtilsAlert.mostrarAviso(this, R.string.data_invalida);
                return;
            }
        }

        final CasoPacienteDTO dto = new CasoPacienteDTO(
                numeroExame.trim(),
                dataSolicitacaoToSave,
                dataEntregaToSave,
                etapaSelecionada,
                pacienteIdParaSalvar,
                nomePaciente.trim(),
                dataNascimentoToSave,
                Sexo.fromRadioId(checkedRadioButtonId),
                numeroSus.trim(),
                ""
        );

        final boolean isEdicao = (modo == MODO_EDITAR && casoOrginal != null && casoOrginal.getCasoId() != null);

        io.execute(() -> {
            try {
                long resultId;
                if (isEdicao) {
                    dto.setCasoId(casoOrginal.getCasoId());
                    int rows = db.getCasoPacienteDAO().update(dto);
                    runOnUiThread(() -> {
                        if (rows > 0) {
                            setResult(RESULT_OK);
                            UtilsAlert.mostrarAviso(this, R.string.caso_salvo_com_sucesso);
                            finish();
                        } else {
                            UtilsAlert.mostrarAviso(this, R.string.houve_um_problema_ao_salvar_o_caso);
                        }
                    });
                } else {
                    resultId = db.getCasoPacienteDAO().insert(dto);
                    dto.setCasoId(resultId);
                    runOnUiThread(() -> {
                        if (resultId > 0) {
                            setResult(RESULT_OK);
                            UtilsAlert.mostrarAviso(this, R.string.caso_salvo_com_sucesso);
                            finish();
                        } else {
                            UtilsAlert.mostrarAviso(this, R.string.houve_um_problema_ao_salvar_o_caso);
                        }
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() ->
                        UtilsAlert.mostrarAviso(this, R.string.houve_um_problema_ao_salvar_o_caso));
            }
        });
    }

    private static String textOf(EditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private static String tagIsoOf(EditText et) {
        Object tag = et.getTag();
        return tag == null ? "" : String.valueOf(tag).trim();
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private static LocalDate parseLocalDateFlexible(String display, String isoMaybe) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            Locale L = java.util.Locale.ROOT;
            DateTimeFormatter BR = null;
            BR = DateTimeFormatter.ofPattern("dd/MM/uuuu", L);

            if (!isoMaybe.isEmpty()) {
                try {
                    return java.time.LocalDate.parse(isoMaybe);
                } catch (Exception ignored) {
                }
            }
            if (!display.isEmpty()) {
                try {
                    return java.time.LocalDate.parse(display, BR);
                } catch (Exception ignored) {
                }
            }
            return null;
        }
        return null;

    }

    private static String toIso(java.util.Date d) {
        java.text.SimpleDateFormat out = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ROOT);
        return out.format(d);
    }

    public void limparCampos(View view) {
        editTextNumeroExame.setText(null);
        editTextNomePaciente.setText(null);
        editTextDataNascimento.setText(null);
        radioGroupSexo.clearCheck();
        editTextSus.setText(null);
        editTextDataSolicitacao.setText(null);
        etapaView.setText(null, false);
        editTextDataNascimento.setTag(null);
        editTextDataSolicitacao.setTag(null);
        editTextDataEntrega.setTag(null);
        editTextNumeroExame.requestFocus();
        UtilsAlert.mostrarAviso(this, R.string.as_entradas_foram_apagadas);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private static String isoToDisplay(String iso) {
        DateTimeFormatter BR = DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale.ROOT);
        return LocalDate.parse(iso).format(BR);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private static void bindIsoToField(EditText et, String iso) {
        if (iso == null || iso.trim().isEmpty()) {
            et.setTag("");
            et.setText("");
            return;
        }
        et.setTag(iso.trim());                 // guarda ISO
        et.setText(isoToDisplay(iso.trim()));  // mostra BR
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Nullable
    private static LocalDate readLocalDateFromField(EditText et) {
        String isoMaybe = tagIsoOf(et);
        String display = textOf(et);

        if (!isoMaybe.isEmpty()) {
            try {
                return LocalDate.parse(isoMaybe);
            } catch (Exception ignored) {
            }
        }
        if (!display.isEmpty()) {
            try {
                DateTimeFormatter BR = DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale.ROOT);
                return LocalDate.parse(display, BR);
            } catch (Exception ignored) {
            }
            try {
                return LocalDate.parse(display);
            } catch (Exception ignored) {
            }
        }
        return null;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        io.shutdown();
    }


}