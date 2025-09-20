package com.pathotrack.persistency;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.pathotrack.domain.entities.CasoPacienteDTO;

@Database(entities = {CasoPacienteDTO.class}, version = 1, exportSchema = false)
public abstract class CasoPacienteDB extends RoomDatabase {

    public abstract CasoPacienteDAO getCasoPacienteDAO();

    private static CasoPacienteDB INSTANCE;

    public static CasoPacienteDB getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (CasoPacienteDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context,
                                    CasoPacienteDB.class,
                                    "caso_paciente_db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    ;

}
