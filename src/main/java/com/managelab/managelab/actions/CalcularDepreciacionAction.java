package com.managelab.managelab.actions;

import java.math.*;
import java.time.*;
import java.util.*;

import org.openxava.actions.*;
import org.openxava.jpa.*;

import com.managelab.managelab.modelo.*;

public class CalcularDepreciacionAction extends ViewBaseAction {

    @Override
    public void execute() throws Exception {
        Long id = (Long) getView().getKeyValues().get("id");

        if (id == null) {
            addError("Debe seleccionar un activo");
            return;
        }

        Activo activo = XPersistence.getManager().find(Activo.class, id);

        if (activo == null) {
            addError("Activo no encontrado");
            return;
        }

        if (activo.getCostoInicial() == null || activo.getVidaUtilAnios() == null
                || activo.getVidaUtilAnios() <= 0 || activo.getFechaAdquisicion() == null) {
            addError("El activo debe tener costo inicial, vida util y fecha de adquisicion");
            return;
        }

        LocalDate adq = activo.getFechaAdquisicion().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate hoy = LocalDate.now();

        long aniosTranscurridos = Period.between(adq, hoy).getYears();
        if (aniosTranscurridos < 0) aniosTranscurridos = 0;
        if (aniosTranscurridos > activo.getVidaUtilAnios()) aniosTranscurridos = activo.getVidaUtilAnios();

        BigDecimal depAnual = activo.getCostoInicial()
                .divide(BigDecimal.valueOf(activo.getVidaUtilAnios()), 2, RoundingMode.HALF_UP);
        BigDecimal depTotal = depAnual.multiply(BigDecimal.valueOf(aniosTranscurridos));
        BigDecimal valorActual = activo.getCostoInicial().subtract(depTotal);

        if (valorActual.compareTo(BigDecimal.ZERO) < 0) valorActual = BigDecimal.ZERO;

        DepreciacionActivo depreciacion = new DepreciacionActivo();
        depreciacion.setActivo(activo);
        depreciacion.setFechaCalculo(new Date());
        depreciacion.setValorCalculado(valorActual);

        XPersistence.getManager().persist(depreciacion);
        XPersistence.commit();

        addMessage("Depreciacion calculada: " + valorActual + " (Activo: " + activo.getNombre() + ")");
        getView().refresh();
    }
}
