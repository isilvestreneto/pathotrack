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

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
    private Sexo sexo;
    @NonNull
    private String sus;
    @NonNull
    @Generated(value = "org.jetbrains.annotations", date = "2024-06-10T16:20:30.123Z")
    private String prontuario;

    @Ignore
    private transient String cachedIdade;

    public CasoPacienteDTO() {
    }

    @Ignore
    public CasoPacienteDTO(@NonNull String numeroExame,
                           @NonNull String dataRequisicao,
                           @NonNull String dataEntrega,
                           @NonNull Etapa etapa,
                           Long pacienteId,
                           @NonNull String pacienteNome,
                           @NonNull String dataNascimento,
                           @NonNull Sexo sexo,
                           @NonNull String sus,
                           @NonNull String prontuario) {
        this.numeroExame = numeroExame;
        this.dataRequisicao = dataRequisicao;
        this.dataEntrega = dataEntrega;
        this.etapa = etapa;
        this.pacienteId = (pacienteId == null || pacienteId <= 0) ? gerarPacienteIdCurto() : pacienteId;
        this.pacienteNome = pacienteNome;
        this.dataNascimento = dataNascimento;
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
        final String ALFABETO = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        final SecureRandom RANDOM = new SecureRandom();

        String data;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            data = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        } else {
            data = new SimpleDateFormat("yyyyMM").format(new Date());
        }

        StringBuilder sufixo = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            int index = RANDOM.nextInt(ALFABETO.length());
            sufixo.append(ALFABETO.charAt(index));
        }

        return data + "-" + sufixo;
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

    public Sexo getSexo() {
        return sexo;
    }

    public String getSus() {
        return sus;
    }

    public String getProntuario() {
        return prontuario;
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }

    @Ignore
    public String getIdade() {
        if (cachedIdade != null) return cachedIdade;

        String dn = dataNascimento == null ? "" : dataNascimento.trim();
        if (dn.isEmpty()) return cachedIdade = "";

        try {
            int anos, meses;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LocalDate birth;

                if (dn.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    birth = LocalDate.parse(dn);
                } else {
                    DateTimeFormatter BR =
                            DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale.ROOT);
                    birth = LocalDate.parse(dn, BR);
                }

                LocalDate today = LocalDate.now();
                if (birth.isAfter(today)) return cachedIdade = "";

                Period p = Period.between(birth, today);
                anos = p.getYears();
                meses = p.getMonths();
            } else {
                // Fallback legado
                SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
                SimpleDateFormat br  = new SimpleDateFormat("dd/MM/yyyy", Locale.ROOT);
                iso.setLenient(false); br.setLenient(false);

                Date birthDate = dn.matches("\\d{4}-\\d{2}-\\d{2}") ? iso.parse(dn) : br.parse(dn);
                if (birthDate == null) return cachedIdade = "";

                Calendar cBirth = Calendar.getInstance();
                cBirth.setTime(birthDate);
                Calendar cNow = Calendar.getInstance();

                anos = cNow.get(Calendar.YEAR) - cBirth.get(Calendar.YEAR);
                int m = cNow.get(Calendar.MONTH) - cBirth.get(Calendar.MONTH);
                int d = cNow.get(Calendar.DAY_OF_MONTH) - cBirth.get(Calendar.DAY_OF_MONTH);
                if (d < 0) m--;
                if (m < 0) { anos--; m += 12; }
                meses = m;
            }

            return cachedIdade = (meses > 0) ? (anos + "a " + meses + "m") : (anos + "a");
        } catch (Exception e) {
            return cachedIdade = "";
        }
    }



}
