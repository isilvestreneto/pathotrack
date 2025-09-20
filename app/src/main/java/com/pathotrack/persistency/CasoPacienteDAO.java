package com.pathotrack.persistency;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.pathotrack.domain.entities.CasoPacienteDTO;

import java.util.List;

@Dao
public interface CasoPacienteDAO {
    @Insert
    long insert(CasoPacienteDTO casoPacienteDTO);

    @Update
    int update(CasoPacienteDTO casoPacienteDTO);

    @Query("SELECT * FROM caso_paciente WHERE casoId = :casoId")
    CasoPacienteDTO queryForId(long casoId);

    String ORDER_KEY =
            "CASE WHEN length(dataEntrega)=10 " +
                    "THEN substr(dataEntrega,7,4)||substr(dataEntrega,4,2)||substr(dataEntrega,1,2) " +
                    "ELSE NULL END";

    @Query("SELECT COUNT(*) FROM caso_paciente")
    int countAll();

    @Query("SELECT COUNT(*) FROM caso_paciente WHERE etapa LIKE :etapaPrefix || '%'")
    int countByEtapa(String etapaPrefix);

    @Query("SELECT * FROM caso_paciente " +
            "ORDER BY " + ORDER_KEY + " ASC, dataEntrega ASC " +
            "LIMIT :limit OFFSET :offset")
    List<CasoPacienteDTO> findAllPagedOrderByDataEntregaAsc(int limit, int offset);

    @Query("SELECT * FROM caso_paciente " +
            "ORDER BY " + ORDER_KEY + " DESC, dataEntrega DESC " +
            "LIMIT :limit OFFSET :offset")
    List<CasoPacienteDTO> findAllPagedOrderByDataEntregaDesc(int limit, int offset);

    @Query("SELECT * FROM caso_paciente " +
            "WHERE etapa LIKE :etapaPrefix || '%' " +
            "ORDER BY " + ORDER_KEY + " ASC, dataEntrega ASC " +
            "LIMIT :limit OFFSET :offset")
    List<CasoPacienteDTO> findByEtapaPagedOrderByDataEntregaAsc(String etapaPrefix, int limit, int offset);

    @Query("SELECT * FROM caso_paciente " +
            "WHERE etapa LIKE :etapaPrefix || '%' " +
            "ORDER BY " + ORDER_KEY + " DESC, dataEntrega DESC " +
            "LIMIT :limit OFFSET :offset")
    List<CasoPacienteDTO> findByEtapaPagedOrderByDataEntregaDesc(String etapaPrefix, int limit, int offset);

    @Query("DELETE FROM caso_paciente WHERE casoId = :casoId")
    int deleteById(long casoId);

    @Query("SELECT * FROM caso_paciente")
    List<CasoPacienteDTO> findAll();
}
