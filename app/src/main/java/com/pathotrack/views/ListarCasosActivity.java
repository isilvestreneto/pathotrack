package com.pathotrack.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.pathotrack.R;
import com.pathotrack.domain.entities.CasoPacienteDTO;
import com.pathotrack.domain.enums.Etapa;
import com.pathotrack.domain.enums.Sexo;
import com.pathotrack.utils.ChavesCasoPaciente;
import com.pathotrack.views.adapters.CasoAdapter;

import java.util.ArrayList;
import java.util.List;

public class ListarCasosActivity extends AppCompatActivity {

    private RecyclerView rvCasos;
    private CasoAdapter adapter;
    private MaterialButton btnPrev, btnNext;
    private TextView tvPage;

    private static final int PAGE_SIZE = 5;
    private int currentPage = 0;
    private int totalPages = 1;
    private List<CasoPacienteDTO> allCasos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listar_casos);

        rvCasos = findViewById(R.id.rvCasos);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        tvPage = findViewById(R.id.tvPage);

        adapter = new CasoAdapter();
        rvCasos.setLayoutManager(new LinearLayoutManager(this));
        rvCasos.setAdapter(adapter);

        // Busca mockada (depois troca por repositório/HTTP)
        // List<CasoPacienteDTO> result = CasoService.buscarCasos();
        /**
         *
         * TODO: futuramente, quando salvar a entidade em algum lugar, descomentar essa linha
         * TODO: e inserir a busca de onde tiver com os dados persistidos
        */

        // allCasos = (result != null) ? result : java.util.Collections.emptyList();

        // Calcula total de páginas
        totalPages = Math.max(1, (int) Math.ceil(allCasos.size() / (double) PAGE_SIZE));

        updatePage();

        adapter.setOnItemClickListener(caso -> {
            String msg = "Exame: " + safe(caso.getNumeroExame())
                    + "\nPrevisão: " + safe(caso.getDataRequisicao()) + " → " + safe(caso.getDataEntrega())
                    + "\nEtapa: " + safe(caso.getEtapa())
                    + "\nPaciente ID: " + safe(caso.getPacienteId());
            android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_SHORT).show();
        });

        btnPrev.setOnClickListener(v -> {
            if (currentPage > 0) {
                currentPage--;
                updatePage();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
                updatePage();
            }
        });
    }

    private void updatePage() {
        int from = currentPage * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, allCasos.size());
        List<CasoPacienteDTO> pageItems = from < to ? allCasos.subList(from, to) : new ArrayList<>();
        adapter.setItems(new ArrayList<>(pageItems));
        tvPage.setText((currentPage + 1) + " / " + totalPages);

        btnPrev.setEnabled(currentPage > 0);
        btnNext.setEnabled(currentPage < totalPages - 1);
    }

    public void abrirSobre(View view) {
        Intent intentAbertura = new Intent(this, AutoriaActivity.class);
        startActivity(intentAbertura);
    }

    public void abrirAdicionar(View view) {
        launcherNovoCaso.launch(new Intent(this, CriarCasoActivity.class));
    }

    private static String safe(Object o) {
        return o == null ? "-" : String.valueOf(o);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    ActivityResultLauncher<Intent> launcherNovoCaso = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == CriarCasoActivity.RESULT_OK) {
                    Intent intent = result.getData();
                    CasoPacienteDTO novo = montarCasoAPartirDosExtras(intent);
                    allCasos.add(0, novo);
                    totalPages = Math.max(1, (int) Math.ceil(allCasos.size() / (double) PAGE_SIZE));
                    currentPage = 0;
                    updatePage();
                }
            });

    private @Nullable CasoPacienteDTO montarCasoAPartirDosExtras(@NonNull Intent data) {
        String numeroExame = data.getStringExtra(ChavesCasoPaciente.NUMERO_EXAME);
        String nomePaciente = data.getStringExtra(ChavesCasoPaciente.NOME_PACIENTE);
        String dataNascimento = data.getStringExtra(ChavesCasoPaciente.DATA_NASCIMENTO);
        String sexoName = data.getStringExtra(ChavesCasoPaciente.SEXO);
        String numeroSus = data.getStringExtra(ChavesCasoPaciente.NUMERO_SUS);
        String dataSolicitacao = data.getStringExtra(ChavesCasoPaciente.DATA_SOLICITACAO);
        String etapaLabel = data.getStringExtra(ChavesCasoPaciente.ETAPA);
        String dataEntrega = data.getStringExtra(ChavesCasoPaciente.DATA_ENTREGA);

        if (numeroExame == null || nomePaciente == null || dataNascimento == null ||
                sexoName == null || numeroSus == null || dataSolicitacao == null || etapaLabel == null || dataEntrega == null) {
            return null;
        }

        Sexo sexo = Sexo.valueOf(sexoName);
        Etapa etapa = Etapa.fromLabel(etapaLabel);

        long idCaso = System.currentTimeMillis();
        long idPaciente = idCaso + 7;

        return new CasoPacienteDTO(
                idCaso,
                numeroExame,
                dataSolicitacao,
                dataEntrega,
                etapa,
                idPaciente,
                nomePaciente,
                dataNascimento,
                sexo,
                numeroSus,
                numeroSus
        );
    }
}
