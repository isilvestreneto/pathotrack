package com.pathotrack.views;

import android.content.Context;
import android.content.DialogInterface;
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
import com.pathotrack.persistency.CasoPacienteDB;
import com.pathotrack.utils.ChavesAplicacao;
import com.pathotrack.utils.ChavesCasoPaciente;
import com.pathotrack.utils.UtilsAlert;
import com.pathotrack.views.adapters.CasoAdapter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ListarCasosActivity extends AppCompatActivity {
    private RecyclerView rvCasos;
    private CasoAdapter adapter;
    private MaterialButton btnPrev, btnNext;
    private TextView tvPage, semCasos;
    private View filtersRow, paginationBar;
    private TextInputEditText filtroValor;
    private MaterialAutoCompleteTextView exibicao, ordenarPor;
    private CheckBox checkbox;
    private CasoPacienteDB db;

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

    private static final int PAGE_SIZE = 5;
    private int currentPage = 0;
    private int totalPages = 1;

    private boolean filtrarPorEtapa = false;
    private String etapaPrefix = "";
    private boolean ordenarAsc = false;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listar_casos);

        db = CasoPacienteDB.getInstance(getApplicationContext());

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

        String[] exibicaoOpts = new String[] { getString(R.string.nenhum), getString(R.string.etapa) };

        String[] ordenarOpts = new String[] { getString(R.string.ordenar_antigo_novo),
                getString(R.string.ordenar_novo_antigo) };

        exibicao.setSimpleItems(exibicaoOpts);
        ordenarPor.setSimpleItems(ordenarOpts);

        exibicao.setOnClickListener(v -> exibicao.showDropDown());
        ordenarPor.setOnClickListener(v -> ordenarPor.showDropDown());

        adapter = new CasoAdapter();
        rvCasos.setLayoutManager(new LinearLayoutManager(this));
        rvCasos.setAdapter(adapter);

        lerPreferencias();

        if (sugerirConfiguracao) {
            applyFilterAndSort();
        } else {
            filtrarPorEtapa = false;
            ordenarAsc = false;
            currentPage = 0;
            loadPage();
        }

        adapter.setOnItemLongClickListener(pos -> selectedAdapterPosition = pos);

        btnPrev.setOnClickListener(v -> {
            if (currentPage > 0) {
                currentPage--;
                loadPage();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
                loadPage();
            }
        });

        exibicao.setOnItemClickListener((p, v, pos, id) -> {
            if (isRestoring)
                return;
            applyFilterAndSort();
            savePrefsIfKeep();
        });
        ordenarPor.setOnItemClickListener((p, v, pos, id) -> {
            if (isRestoring)
                return;
            applyFilterAndSort();
            savePrefsIfKeep();
        });

        filtroValor.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                if (isRestoring)
                    return;
                applyFilterAndSort();
                savePrefsIfKeep();
            }
        });

        checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isRestoring)
                return;

            sugerirConfiguracao = isChecked;
            SharedPreferences shared = getSharedPreferences(ChavesAplicacao.PREFERENCIAS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = shared.edit();
            editor.putBoolean(KEY_KEEP, isChecked);

            if (isChecked) {
                String campoToken = getString(R.string.etapa).equalsIgnoreCase(safe(exibicao.getText())) ? T_CAMPO_ETAPA
                        : T_CAMPO_NONE;

                String sortToken = getString(R.string.ordenar_novo_antigo).equalsIgnoreCase(safe(ordenarPor.getText()))
                        ? T_SORT_DESC
                        : T_SORT_ASC;

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
        String valorUI = safe(filtroValor.getText()).trim();
        String ordem = safe(ordenarPor.getText());

        filtrarPorEtapa = getString(R.string.etapa).equalsIgnoreCase(campo) && !valorUI.isEmpty();
        etapaPrefix = filtrarPorEtapa ? etapaToken(valorUI) : "";
        ordenarAsc = getString(R.string.ordenar_antigo_novo).equalsIgnoreCase(ordem);

        currentPage = 0;
        loadPage();
    }

    private void loadPage() {
        int limit = PAGE_SIZE;
        int offset = currentPage * PAGE_SIZE;

        io.execute(() -> {
            int totalAllRows = db.getCasoPacienteDAO().countAll();

            List<CasoPacienteDTO> pageItems;
            int totalRows;

            if (filtrarPorEtapa) {
                totalRows = db.getCasoPacienteDAO().countByEtapa(etapaPrefix);
                pageItems = ordenarAsc
                        ? db.getCasoPacienteDAO().findByEtapaPagedOrderByDataEntregaAsc(etapaPrefix, limit, offset)
                        : db.getCasoPacienteDAO().findByEtapaPagedOrderByDataEntregaDesc(etapaPrefix, limit, offset);
            } else {
                totalRows = totalAllRows;
                pageItems = ordenarAsc
                        ? db.getCasoPacienteDAO().findAllPagedOrderByDataEntregaAsc(limit, offset)
                        : db.getCasoPacienteDAO().findAllPagedOrderByDataEntregaDesc(limit, offset);
            }

            if (pageItems == null)
                pageItems = new ArrayList<>();
            final List<CasoPacienteDTO> items = pageItems;
            final int total = totalRows;
            final int totalAll = totalAllRows;

            runOnUiThread(() -> {
                adapter.setItems(items);
                updateNavUi(items.size(), total, totalAll);
            });
        });
    }

    private String etapaToken(String uiText) {
        String up = uiText == null ? "" : uiText.trim().toUpperCase(java.util.Locale.ROOT);
        for (Etapa e : Etapa.values()) {
            String name = e.name();
            String label = getString(e.getLabelResId()).toUpperCase(java.util.Locale.ROOT);
            if (name.startsWith(up) || label.startsWith(up)) {
                return name.substring(0, Math.min(name.length(), up.length()));
            }
        }
        return up;
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
        checkbox.setVisibility(vazio ? View.GONE : View.VISIBLE);

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
                if (s.isEmpty())
                    return LocalDate.MAX;
                return LocalDate.parse(s, DateTimeFormatter.ofPattern("dd/MM/uuuu"));
            }
        } catch (Exception ignored) {
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            return LocalDate.MAX;
        return null;
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
            checkbox.setVisibility(View.GONE);
        } else {
            semCasos.setVisibility(View.GONE);
            filtersRow.setVisibility(View.VISIBLE);
            paginationBar.setVisibility(View.VISIBLE);
            checkbox.setVisibility(View.VISIBLE);
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

    ActivityResultLauncher<Intent> launcherNovoCaso = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    currentPage = 0;
                    loadPage();
                }
            });

    ActivityResultLauncher<Intent> launcherEditarCaso = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadPage();
                }
            });

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

        CasoPacienteDTO casoPacienteDTO = adapter.getItemAt(posicao);

        if (casoPacienteDTO == null)
            return;

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

    private void excluirDaListaFiltrada(int indexNaPagina) {
        CasoPacienteDTO alvo = adapter.getItemAt(indexNaPagina);
        if (alvo == null)
            return;

        String mensagem = getString(R.string.deseja_apagar) + " " + alvo.getProntuario() + "?";
        DialogInterface.OnClickListener ok = (d, w) -> {
            io.execute(() -> {
                int rows = db.getCasoPacienteDAO().deleteById(alvo.getCasoId());
                if (rows > 0) {
                    int totalAllRows = db.getCasoPacienteDAO().countAll();
                    int totalRows = filtrarPorEtapa
                            ? db.getCasoPacienteDAO().countByEtapa(etapaPrefix)
                            : totalAllRows;

                    int newTotalPages = Math.max(1, (int) Math.ceil(totalRows / (double) PAGE_SIZE));
                    if (currentPage >= newTotalPages)
                        currentPage = Math.max(0, newTotalPages - 1);

                    int limit = PAGE_SIZE;
                    int offset = currentPage * PAGE_SIZE;
                    List<CasoPacienteDTO> pageItems = filtrarPorEtapa
                            ? (ordenarAsc
                                    ? db.getCasoPacienteDAO().findByEtapaPagedOrderByDataEntregaAsc(etapaPrefix, limit,
                                            offset)
                                    : db.getCasoPacienteDAO().findByEtapaPagedOrderByDataEntregaDesc(etapaPrefix, limit,
                                            offset))
                            : (ordenarAsc
                                    ? db.getCasoPacienteDAO().findAllPagedOrderByDataEntregaAsc(limit, offset)
                                    : db.getCasoPacienteDAO().findAllPagedOrderByDataEntregaDesc(limit, offset));
                    if (pageItems == null)
                        pageItems = new ArrayList<>();
                    final List<CasoPacienteDTO> items = pageItems;
                    final int total = totalRows;
                    final int totalAll = totalAllRows;

                    runOnUiThread(() -> {
                        adapter.setItems(items);
                        updateNavUi(items.size(), total, totalAll);
                    });
                } else {
                    runOnUiThread(() -> UtilsAlert.mostrarAviso(this, R.string.houve_um_problema_ao_excluir));
                }
            });
        };
        UtilsAlert.confirmarAcao(this, mensagem, ok, null);
    }

    private void lerPreferencias() {
        boolean prev = isRestoring;
        isRestoring = true;

        try {
            SharedPreferences shared = getSharedPreferences(ChavesAplicacao.PREFERENCIAS, Context.MODE_PRIVATE);
            sugerirConfiguracao = shared.getBoolean(KEY_KEEP, false);
            checkbox.setChecked(sugerirConfiguracao);

            if (sugerirConfiguracao) {
                String campo = shared.getString(KEY_CAMPO, T_CAMPO_NONE);
                String valor = shared.getString(KEY_FILTER_VALUE, "");
                String sort = shared.getString(KEY_SORT, T_SORT_ASC);

                exibicao.setText(T_CAMPO_ETAPA.equals(campo) ? getString(R.string.etapa) : getString(R.string.nenhum),
                        false);

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
        } finally {
            isRestoring = prev;
        }
    }

    private void savePrefsIfKeep() {
        if (!sugerirConfiguracao)
            return;
        SharedPreferences shared = getSharedPreferences(ChavesAplicacao.PREFERENCIAS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();

        String campoToken = T_CAMPO_NONE;
        String campo = safe(exibicao.getText());
        if (getString(R.string.etapa).equalsIgnoreCase(campo))
            campoToken = T_CAMPO_ETAPA;

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

    private int calcTotalPages(int totalRows) {
        return Math.max(1, (int) Math.ceil(totalRows / (double) PAGE_SIZE));
    }

    private void updateNavUi(int pageItemCount, int totalRows, int totalAllRows) {
        totalPages = Math.max(1, (int) Math.ceil(totalRows / (double) PAGE_SIZE));
        currentPage = Math.min(currentPage, totalPages - 1);

        boolean semMatches = (totalRows == 0);
        boolean haDadosNoDB = (totalAllRows > 0);

        if (semMatches && haDadosNoDB) {
            semCasos.setText(getString(R.string.sem_resultados_para_este_filtro));
            semCasos.setVisibility(View.VISIBLE);
        } else if (!haDadosNoDB) {
            semCasos.setText(getString(R.string.sem_casos));
            semCasos.setVisibility(View.VISIBLE);
        } else {
            semCasos.setVisibility(View.GONE);
        }

        filtersRow.setVisibility(View.VISIBLE);
        checkbox.setVisibility(View.VISIBLE);

        if (semMatches) {
            paginationBar.setVisibility(View.GONE);
            tvPage.setText("0 / 1");
            btnPrev.setEnabled(false);
            btnNext.setEnabled(false);
        } else {
            paginationBar.setVisibility(View.VISIBLE);
            tvPage.setText((currentPage + 1) + " / " + totalPages);
            btnPrev.setEnabled(currentPage > 0);
            btnNext.setEnabled(currentPage < totalPages - 1);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadPage();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        io.shutdown();
    }

}
