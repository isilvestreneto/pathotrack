package com.pathotrack.views;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.pathotrack.R;
import com.pathotrack.domain.entities.CasoPacienteDTO;
import com.pathotrack.domain.enums.Etapa;
import com.pathotrack.domain.enums.Sexo;
import com.pathotrack.services.CasoService;
import com.pathotrack.utils.ChavesAplicacao;
import com.pathotrack.utils.ChavesCasoPaciente;
import com.pathotrack.views.adapters.CasoAdapter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// TODO Exiba um AlertDialog para confirmar a ação do usuário antes de excluir dados persistidos;


public class ListarCasosActivity extends AppCompatActivity {
    private final CasoService casoService;

    private RecyclerView rvCasos;
    private CasoAdapter adapter;
    private MaterialButton btnPrev, btnNext;
    private TextView tvPage, semCasos;
    private View filtersRow, paginationBar;
    private TextInputEditText filtroValor;
    private MaterialAutoCompleteTextView exibicao, ordenarPor;
    private CheckBox checkbox;

    private static final int PAGE_SIZE = 5;
    private int currentPage = 0;
    private int totalPages = 1;
    private List<CasoPacienteDTO> allCasos = new ArrayList<>();
    private List<CasoPacienteDTO> filteredCasos = new ArrayList<>();
    private int selectedAdapterPosition = RecyclerView.NO_POSITION;
    private static final String T_CAMPO_NONE = "NONE";
    private static final String T_CAMPO_ETAPA = "ETAPA";
    private static final String T_SORT_ASC = "ASC";
    private static final String T_SORT_DESC = "DESC";

    private static final String KEY_KEEP = "keep_config";
    private static final String KEY_FILTER_VALUE = "filter_value";
    private static final String KEY_CAMPO = "campo_token";
    private static final String KEY_SORT = "sort_token";
    private boolean sugerirConfiguracao = false;
    private boolean isRestoring = false;

    public ListarCasosActivity() {
        this.casoService = new CasoService();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listar_casos);

        rvCasos = findViewById(R.id.rvCasos);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        tvPage = findViewById(R.id.tvPage);
        filtersRow = findViewById(R.id.filtersRow);
        paginationBar = findViewById(R.id.paginationBar);
        semCasos = findViewById(R.id.semCasos);
        filtroValor = findViewById(R.id.filtroValor);
        exibicao = findViewById(R.id.exibicao);
        ordenarPor = findViewById(R.id.ordenarPor);
        checkbox = findViewById(R.id.checkbox);

        String[] exibicaoOpts = new String[]{
                getString(R.string.nenhum),
                getString(R.string.etapa)
        };

        String[] ordenarOpts = new String[]{
                getString(R.string.ordenar_antigo_novo),
                getString(R.string.ordenar_novo_antigo)
        };


        exibicao.setSimpleItems(exibicaoOpts);
        ordenarPor.setSimpleItems(ordenarOpts);

        exibicao.setOnClickListener(v -> exibicao.showDropDown());
        ordenarPor.setOnClickListener(v -> ordenarPor.showDropDown());

        adapter = new CasoAdapter();
        rvCasos.setLayoutManager(new LinearLayoutManager(this));
        rvCasos.setAdapter(adapter);

        List<CasoPacienteDTO> result = CasoService.buscarCasos();

        /**
         * TODO: futuramente, quando salvar a entidade em algum lugar, descomentar essa linha
         * TODO: e inserir a busca de onde tiver com os dados persistidos
         */
        allCasos = (result != null) ? result : java.util.Collections.emptyList();
        filteredCasos = new ArrayList<>(allCasos);

        totalPages = Math.max(1, (int) Math.ceil(allCasos.size() / (double) PAGE_SIZE));

        isRestoring = true;
        lerPreferencias();
        isRestoring = false;

        if (sugerirConfiguracao) {
            applyFilterAndSort();
        } else {
            updatePage();
        }

        adapter.setOnItemLongClickListener(pos -> selectedAdapterPosition = pos);

        btnPrev.setOnClickListener(v -> {
            if (currentPage > 0) {
                currentPage--;
                updatePageFromFiltered();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
                updatePageFromFiltered();
            }
        });
        exibicao.setOnItemClickListener((p, v, pos, id) -> {
            if (isRestoring) return;
            applyFilterAndSort();
            savePrefsIfKeep();
        });
        ordenarPor.setOnItemClickListener((p, v, pos, id) -> {
            if (isRestoring) return;
            applyFilterAndSort();
            savePrefsIfKeep();
        });

        filtroValor.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                if (isRestoring) return;
                applyFilterAndSort();
                savePrefsIfKeep();
            }
        });

        checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isRestoring) return;

            sugerirConfiguracao = isChecked;
            SharedPreferences shared = getSharedPreferences(ChavesAplicacao.PREFERENCIAS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = shared.edit();
            editor.putBoolean(KEY_KEEP, isChecked);

            if (isChecked) {
                String campoToken = getString(R.string.etapa).equalsIgnoreCase(safe(exibicao.getText()))
                        ? T_CAMPO_ETAPA : T_CAMPO_NONE;

                String sortToken = getString(R.string.ordenar_novo_antigo).equalsIgnoreCase(safe(ordenarPor.getText()))
                        ? T_SORT_DESC : T_SORT_ASC;

                editor.putString(KEY_CAMPO, campoToken);
                editor.putString(KEY_FILTER_VALUE, safe(filtroValor.getText()));
                editor.putString(KEY_SORT, sortToken);
            } else {
                editor.remove(KEY_CAMPO);
                editor.remove(KEY_FILTER_VALUE);
                editor.remove(KEY_SORT);
            }
            editor.apply();
        });

        registerForContextMenu(rvCasos);
    }

    private void applyFilterAndSort() {
        String campo = safe(exibicao.getText());
        String valor = safe(filtroValor.getText());
        String ordem = safe(ordenarPor.getText());

        // 1) FILTRO
        List<CasoPacienteDTO> base = new ArrayList<>(allCasos);

        List<CasoPacienteDTO> filtrados = new ArrayList<>();
        for (CasoPacienteDTO c : base) {
            if (matchesFiltro(c, campo, valor)) {
                filtrados.add(c);
            }
        }

        // 2) ORDENAR
        filtrados.sort(getComparator(ordem));

        // 3) ATUALIZAR ESTADO/PAGINAÇÃO
        filteredCasos = filtrados;
        currentPage = 0;
        totalPages = Math.max(1, (int) Math.ceil(filteredCasos.size() / (double) PAGE_SIZE));
        updatePageFromFiltered();
    }

    private void updatePageFromFiltered() {
        int size = filteredCasos.size();
        totalPages = Math.max(1, (int) Math.ceil(size / (double) PAGE_SIZE));
        currentPage = Math.min(currentPage, totalPages - 1);
        renderCurrentPage();
    }

    private void renderCurrentPage() {
        int from = currentPage * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, filteredCasos.size());
        List<CasoPacienteDTO> pageItems = (from < to) ? filteredCasos.subList(from, to) : new ArrayList<>();
        adapter.setItems(new ArrayList<>(pageItems));

        boolean vazio = allCasos.isEmpty();
        semCasos.setVisibility(vazio ? View.VISIBLE : View.GONE);
        filtersRow.setVisibility(vazio ? View.GONE : View.VISIBLE);
        paginationBar.setVisibility(vazio ? View.GONE : View.VISIBLE);

        tvPage.setText((currentPage + 1) + " / " + totalPages);
        btnPrev.setEnabled(currentPage > 0);
        btnNext.setEnabled(currentPage < totalPages - 1);
    }

    private Comparator<CasoPacienteDTO> getComparator(String ordem) {
        if (getString(R.string.ordenar_antigo_novo).equalsIgnoreCase(ordem)) {
            return java.util.Comparator.comparing(this::parseDateOrMax);
        } else if (getString(R.string.ordenar_novo_antigo).equalsIgnoreCase(ordem)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return (a, b) -> parseDateOrMax(b).compareTo(parseDateOrMax(a));
            }
        }
        return (a, b) -> 0;
    }

    private LocalDate parseDateOrMax(CasoPacienteDTO c) {
        String s = safe(c.getDataEntrega()).trim();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (s.isEmpty()) return LocalDate.MAX;
                return LocalDate.parse(s, DateTimeFormatter.ofPattern("dd/MM/uuuu"));
            }
        } catch (Exception ignored) {
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) return LocalDate.MAX;
        return null;
    }

    private boolean matchesFiltro(CasoPacienteDTO c, String campo, String valor) {
        if (valor.isEmpty() || getString(R.string.nenhum).equalsIgnoreCase(campo)) return true;

        if (getString(R.string.etapa).equalsIgnoreCase(campo)) {
            String alvo = etapaDisplay(c);
            String q = normalize(valor);
            return normalize(alvo).startsWith(q);
        }

        return true;
    }

    private static String normalize(String s) {
        if (s == null) return "";
        String n = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return n.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private String etapaDisplay(CasoPacienteDTO c) {
        Object et = c.getEtapa();
        if (et instanceof Etapa) {
            return getString(((Etapa) et).getLabelResId());
        }
        if (et instanceof String) {
            Etapa e = Etapa.parse((String) et);
            if (e != null) return getString(e.getLabelResId());
            return (String) et;
        }
        return "";
    }

    public abstract class SimpleTextWatcher implements android.text.TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(android.text.Editable s) {
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar_menu, menu);
        return true;
    }

    private void updatePage() {
        int from = currentPage * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, allCasos.size());
        List<CasoPacienteDTO> pageItems = from < to ? allCasos.subList(from, to) : new ArrayList<>();
        adapter.setItems(new ArrayList<>(pageItems));

        if (allCasos.isEmpty()) {
            filtersRow.setVisibility(View.GONE);
            paginationBar.setVisibility(View.GONE);
        } else {
            semCasos.setVisibility(View.GONE);
            filtersRow.setVisibility(View.VISIBLE);
            paginationBar.setVisibility(View.VISIBLE);
            tvPage.setText((currentPage + 1) + " / " + totalPages);
            btnPrev.setEnabled(currentPage > 0);
            btnNext.setEnabled(currentPage < totalPages - 1);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add) {
            abrirAdicionar(null);
            return true;
        } else if (id == R.id.action_about) {
            abrirSobre(null);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void abrirSobre(View view) {
        Intent intentAbertura = new Intent(this, AutoriaActivity.class);
        startActivity(intentAbertura);
    }

    public void abrirAdicionar(View view) {
        Intent intent = new Intent(this, CriarCasoActivity.class);
        intent.putExtra(CriarCasoActivity.KEY_MODO, CriarCasoActivity.MODO_NOVO);
        launcherNovoCaso.launch(intent);
    }

    private static String safe(Object o) {
        return (o == null) ? "" : String.valueOf(o);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    ActivityResultLauncher<Intent> launcherNovoCaso = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == CriarCasoActivity.RESULT_OK) {
            Intent intent = result.getData();
            CasoPacienteDTO novo = montarCasoAPartirDosExtras(intent);
            allCasos.add(0, novo);
            totalPages = Math.max(1, (int) Math.ceil(allCasos.size() / (double) PAGE_SIZE));
            currentPage = 0;
            updatePage();
        }
    });

    ActivityResultLauncher<Intent> launcherEditarCaso = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == CriarCasoActivity.RESULT_OK) {
            Intent intent = result.getData();
            CasoPacienteDTO atualizado = montarCasoAPartirDosExtras(intent);
            if (atualizado == null) return;
            substituirCasoPorId(atualizado.getCasoId(), atualizado);
        }
    });

    private void substituirCasoPorId(Long casoId, CasoPacienteDTO atualizado) {
        for (int i = 0; i < allCasos.size(); i++) {
            Long id = allCasos.get(i).getCasoId();
            if (id != null && id.longValue() == casoId) {
                allCasos.set(i, atualizado);
                break;
            }
        }

        for (int i = 0; i < filteredCasos.size(); i++) {
            Long id = filteredCasos.get(i).getCasoId();
            if (id != null && id.longValue() == casoId) {
                filteredCasos.set(i, atualizado);
                break;
            }
        }

        renderCurrentPage();
    }

    private @Nullable CasoPacienteDTO montarCasoAPartirDosExtras(@NonNull Intent data) {
        String numeroExame = data.getStringExtra(ChavesCasoPaciente.NUMERO_EXAME);
        String nomePaciente = data.getStringExtra(ChavesCasoPaciente.NOME_PACIENTE);
        String dataNascimento = data.getStringExtra(ChavesCasoPaciente.DATA_NASCIMENTO);
        String sexoName = data.getStringExtra(ChavesCasoPaciente.SEXO);
        String numeroSus = data.getStringExtra(ChavesCasoPaciente.NUMERO_SUS);
        String dataSolicitacao = data.getStringExtra(ChavesCasoPaciente.DATA_SOLICITACAO);
        String etapaLabel = data.getStringExtra(ChavesCasoPaciente.ETAPA);
        String dataEntrega = data.getStringExtra(ChavesCasoPaciente.DATA_ENTREGA);
        int modo = data.getIntExtra(CriarCasoActivity.KEY_MODO, CriarCasoActivity.MODO_NOVO);


        if (numeroExame == null || nomePaciente == null || dataNascimento == null ||
                sexoName == null || numeroSus == null || dataSolicitacao == null ||
                etapaLabel == null || dataEntrega == null) {
            return null;
        }

        Sexo sexo = Sexo.valueOf(sexoName);
        Etapa etapa = Etapa.parse(etapaLabel);
        if (etapa == null) {
            etapa = Etapa.parse(etapaLabel);
            if (etapa == null) {
                throw new IllegalArgumentException("Etapa inválida: " + etapaLabel);
            }
        }

        long idCaso = data.getLongExtra(ChavesCasoPaciente.CASO_ID, -1L);
        long idPaciente = data.getLongExtra(ChavesCasoPaciente.PACIENTE_ID, -1L);


        if (modo == CriarCasoActivity.MODO_NOVO || idCaso == -1L) {
            idCaso = System.currentTimeMillis();
            idPaciente = idCaso + 7;
        }

        return new CasoPacienteDTO(idCaso, numeroExame, dataSolicitacao, dataEntrega, etapa,
                idPaciente, nomePaciente, dataNascimento, sexo, numeroSus, numeroSus);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        getMenuInflater().inflate(R.menu.casos_item_selecionado, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (selectedAdapterPosition == RecyclerView.NO_POSITION)
            return super.onContextItemSelected(item);

        int absolute = currentPage * PAGE_SIZE + selectedAdapterPosition;

        if (id == R.id.menuItemEditar) {
            editarItem(absolute);
            return true;

        } else if (id == R.id.menuItemExcluir) {
            excluirDaListaFiltrada(absolute);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void editarItem(int posicao) {
        selectedAdapterPosition = posicao;

        CasoPacienteDTO casoPacienteDTO = allCasos.get(selectedAdapterPosition);

        Intent intent = new Intent(this, CriarCasoActivity.class);

        intent.putExtra(ChavesCasoPaciente.CASO_ID, casoPacienteDTO.getCasoId());
        intent.putExtra(ChavesCasoPaciente.PACIENTE_ID, casoPacienteDTO.getPacienteId());
        intent.putExtra(CriarCasoActivity.KEY_MODO, CriarCasoActivity.MODO_EDITAR);
        intent.putExtra(ChavesCasoPaciente.NUMERO_EXAME, casoPacienteDTO.getNumeroExame());
        intent.putExtra(ChavesCasoPaciente.NOME_PACIENTE, casoPacienteDTO.getPacienteNome());
        intent.putExtra(ChavesCasoPaciente.DATA_NASCIMENTO, casoPacienteDTO.getDataNascimento());
        intent.putExtra(ChavesCasoPaciente.SEXO, Sexo.fromRadioId(casoPacienteDTO.getSexo().getRadioId()).name());
        intent.putExtra(ChavesCasoPaciente.NUMERO_SUS, casoPacienteDTO.getSus());
        intent.putExtra(ChavesCasoPaciente.DATA_SOLICITACAO, casoPacienteDTO.getDataRequisicao());
        intent.putExtra(ChavesCasoPaciente.ETAPA, casoPacienteDTO.getEtapa().name());
        intent.putExtra(ChavesCasoPaciente.DATA_ENTREGA, casoPacienteDTO.getDataEntrega());
        launcherEditarCaso.launch(intent);
    }

    private void excluirDaListaFiltrada(int absoluteIndex) {
        if (absoluteIndex < 0 || absoluteIndex >= filteredCasos.size()) return;

        CasoPacienteDTO alvo = filteredCasos.get(absoluteIndex);

        filteredCasos.remove(absoluteIndex);

        allCasos.remove(alvo);

        totalPages = Math.max(1, (int) Math.ceil(filteredCasos.size() / (double) PAGE_SIZE));
        if (currentPage >= totalPages) currentPage = totalPages - 1;

        renderCurrentPage();
    }

    private void lerPreferencias() {
        SharedPreferences shared = getSharedPreferences(ChavesAplicacao.PREFERENCIAS, Context.MODE_PRIVATE);
        sugerirConfiguracao = shared.getBoolean(KEY_KEEP, false);
        checkbox.setChecked(sugerirConfiguracao);

        if (sugerirConfiguracao) {
            String campo = shared.getString(KEY_CAMPO, T_CAMPO_NONE);
            String valor = shared.getString(KEY_FILTER_VALUE, "");
            String sort = shared.getString(KEY_SORT, T_SORT_ASC);

            exibicao.setText(
                    T_CAMPO_ETAPA.equals(campo) ? getString(R.string.etapa) : getString(R.string.nenhum),
                    false
            );

            filtroValor.setText(valor);

            if (T_SORT_ASC.equals(sort)) {
                ordenarPor.setText(getString(R.string.ordenar_antigo_novo), false);
            } else {
                ordenarPor.setText(getString(R.string.ordenar_novo_antigo), false);
            }
        } else {
            exibicao.setText(getString(R.string.nenhum), false);
            filtroValor.setText("");
            ordenarPor.setText(getString(R.string.ordenar_novo_antigo), false);
        }
    }

    private void savePrefsIfKeep() {
        if (!sugerirConfiguracao) return;
        SharedPreferences shared = getSharedPreferences(ChavesAplicacao.PREFERENCIAS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();

        String campoToken = T_CAMPO_NONE;
        String campo = safe(exibicao.getText());
        if (getString(R.string.etapa).equalsIgnoreCase(campo)) campoToken = T_CAMPO_ETAPA;

        String sortToken = T_SORT_ASC;
        String ordem = safe(ordenarPor.getText());
        if (getString(R.string.ordenar_novo_antigo).equalsIgnoreCase(ordem))
            sortToken = T_SORT_DESC;

        editor.putBoolean(KEY_KEEP, true);
        editor.putString(KEY_CAMPO, campoToken);
        editor.putString(KEY_FILTER_VALUE, safe(filtroValor.getText()));
        editor.putString(KEY_SORT, sortToken);
        editor.apply();
    }
}
