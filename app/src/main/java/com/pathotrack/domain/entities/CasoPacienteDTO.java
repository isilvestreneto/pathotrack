package com.pathotrack.domain.entities;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.pathotrack.domain.enums.Etapa;
import com.pathotrack.domain.enums.Sexo;
import com.pathotrack.services.IdadeService;

import java.util.UUID;

import javax.annotation.processing.Generated;

@Entity(tableName = "caso_paciente")
public class CasoPacienteDTO {

    @PrimaryKey(autoGenerate = true)
    private Long casoId;

    public void setCasoId(Long casoId) {
        this.casoId = casoId;
    }

    public void setNumeroExame(@NonNull String numeroExame) {
        this.numeroExame = numeroExame;
    }

    public void setDataRequisicao(@NonNull String dataRequisicao) {
        this.dataRequisicao = dataRequisicao;
    }

    public void setDataEntrega(@NonNull String dataEntrega) {
        this.dataEntrega = dataEntrega;
    }

    public void setEtapa(@NonNull Etapa etapa) {
        this.etapa = etapa;
    }

    public void setPacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
    }

    public void setPacienteNome(@NonNull String pacienteNome) {
        this.pacienteNome = pacienteNome;
    }

    public void setDataNascimento(@NonNull String dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public void setIdade(@NonNull String idade) {
        this.idade = idade;
    }

    public void setSexo(@NonNull Sexo sexo) {
        this.sexo = sexo;
    }

    public void setSus(@NonNull String sus) {
        this.sus = sus;
    }

    public void setProntuario(@NonNull String prontuario) {
        this.prontuario = prontuario;
    }

    @NonNull
    private String numeroExame;
    @NonNull
    private String dataRequisicao;
    @NonNull
    private String dataEntrega;
    @NonNull
    private Etapa etapa;

    @ColumnInfo(name = "pacienteId")
    private Long pacienteId;
    @NonNull
    private String pacienteNome;
    @NonNull
    private String dataNascimento;
    @NonNull
    private String idade = "";
    @NonNull
    private Sexo sexo;
    @NonNull
    private String sus;
    @NonNull
    @Generated(value = "org.jetbrains.annotations", date = "2024-06-10T16:20:30.123Z")
    private String prontuario;

    public CasoPacienteDTO() {
    }

    @Ignore
    public CasoPacienteDTO(@NonNull String numeroExame, @NonNull String dataRequisicao,
                           @NonNull String dataEntrega, @NonNull Etapa etapa, Long pacienteId,
                           @NonNull String pacienteNome, @NonNull String dataNascimento,
                           @NonNull Sexo sexo, @NonNull String sus, @NonNull String prontuario) {
        this.numeroExame = numeroExame;
        this.dataRequisicao = dataRequisicao;
        this.dataEntrega = dataEntrega;
        this.etapa = etapa;
        this.pacienteId = (pacienteId == null || pacienteId <= 0) ? gerarPacienteIdCurto() : pacienteId; // ✅ gera só aqui
        this.pacienteNome = pacienteNome;
        this.dataNascimento = dataNascimento;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            this.idade = this.definirIdade(dataNascimento);
        }
        this.sexo = sexo;
        this.sus = sus;
        this.prontuario = (prontuario == null || prontuario.isEmpty()) ? gerarProntuarioAleatorio() : prontuario;
    }

    private static long gerarPacienteIdCurto() {
        long base = System.currentTimeMillis() & 0x3FFFFFFF;
        long aleatorio = (long) (Math.random() * 900_000_000L) + 100_000_000L;
        long id = (base ^ aleatorio) % 900_000_000L + 100_000_000L;
        return id;
    }

    public static String gerarProntuarioAleatorio() {
        return UUID.randomUUID().toString();
    }

    private String definirIdade(String dataNascimento) {
        IdadeService idade = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            idade = IdadeService.systemDefault();
        }
        String result = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            result = idade.formatAgeYearsMonths(dataNascimento);
        }
        return result;
    }

    public Long getCasoId() {
        return casoId;
    }

    public String getNumeroExame() {
        return numeroExame;
    }

    public String getDataRequisicao() {
        return dataRequisicao;
    }

    public String getDataEntrega() {
        return dataEntrega;
    }

    public Etapa getEtapa() {
        return etapa;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public String getPacienteNome() {
        return pacienteNome;
    }

    public String getDataNascimento() {
        return dataNascimento;
    }

    public String getIdade() {
        return idade;
    }

    public Sexo getSexo() {
        return sexo;
    }

    public String getSus() {
        return sus;
    }

    public String getProntuario() {
        return prontuario;
    }


}
