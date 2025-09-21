package com.pathotrack.services;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.O)
public class IdadeService {
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    private final Clock clock;

    public IdadeService(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /**
     * Formata a idade a partir de uma data de nascimento no formato ISO-8601 (yyyy-MM-dd).
     */
    public String formatAgeYearsMonths(String birthDateIso) {
        LocalDate birthDate = parseIsoDate(birthDateIso);
        LocalDate today = LocalDate.now(clock);

        if (birthDate.isAfter(today)) {
            throw new IllegalArgumentException("Data de nascimento no futuro: " + birthDateIso);
        }

        Period period = Period.between(birthDate, today);
        int years = period.getYears();
        int months = period.getMonths();

        return years + "a " + months + "m";
    }

    public static LocalDate parseIsoDate(String s) {
        try {
            return LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e1) {
            try {
                DateTimeFormatter br = new DateTimeFormatterBuilder()
                        .parseCaseInsensitive()
                        .appendPattern("d/M/uuuu")
                        .toFormatter(new Locale("pt", "BR"));
                return LocalDate.parse(s, br);
            } catch (DateTimeParseException e2) {
                throw new IllegalArgumentException("Formato inválido (use yyyy-MM-dd): " + s, e2);
            }
        }
    }

    public static IdadeService systemDefault() {
        return new IdadeService(Clock.systemDefaultZone());
    }

    public static IdadeService ofZone(String zoneId) {
        return new IdadeService(Clock.system(ZoneId.of(zoneId)));
    }
}