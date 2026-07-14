package com.managelab.managelab.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.jpa.*;

import com.managelab.managelab.modelo.*;

public class AprobarReservaAction extends TabBaseAction {

    @Override
    public void execute() throws Exception {

        Map[] keys = getTab().getSelectedKeys();
        if (keys == null || keys.length == 0) {
            addError("Debe seleccionar una reserva (marque el checkbox de la izquierda).");
            return;
        }

        Long id = (Long) keys[0].get("id");
        Reserva reserva = XPersistence.getManager().find(Reserva.class, id);

        if (reserva == null) { addError("Reserva no encontrada."); return; }
        if (reserva.getEstadoReserva() != EstadoReserva.PENDIENTE) {
            addError("Solo se pueden aprobar reservas PENDIENTES.");
            return;
        }

        // Validar traslape SOLO aquí (fuera del callback)
        validarTraslapeAlAprobar(reserva);

        // Aprobar evitando que el callback dispare la query interna
        reserva.setOmitirValidacionDisponibilidad(true);
        reserva.setEstadoReserva(EstadoReserva.APROBADA);

        XPersistence.getManager().merge(reserva);
        XPersistence.commit();

        addMessage("Reserva aprobada exitosamente.");
        getTab().reset();
    }

    private void validarTraslapeAlAprobar(Reserva reserva) {

        String q = "SELECT r FROM Reserva r " +
                "WHERE r.laboratorio.id = :labId " +
                "AND r.fecha = :fecha " +
                "AND r.estadoReserva = :estadoAprobada " +
                "AND r.id <> :idActual";

        @SuppressWarnings("unchecked")
        List<Reserva> aprobadas = XPersistence.getManager()
                .createQuery(q)
                .setParameter("labId", reserva.getLaboratorio().getId())
                .setParameter("fecha", reserva.getFecha())
                .setParameter("estadoAprobada", EstadoReserva.APROBADA)
                .setParameter("idActual", reserva.getId())
                .getResultList();

        int inicio = toMinutesOrFail(reserva.getHoraInicio(), "horaInicio");
        int fin = toMinutesOrFail(reserva.getHoraFin(), "horaFin");

        for (Reserva r : aprobadas) {
            int rInicio = toMinutesOrFail(r.getHoraInicio(), "horaInicio existente");
            int rFin = toMinutesOrFail(r.getHoraFin(), "horaFin existente");
            if (rInicio < fin && rFin > inicio) {
                throw new javax.validation.ValidationException(
                        "No se puede aprobar: ya existe una reserva aprobada que se traslapa en ese horario."
                );
            }
        }
    }

    private int toMinutesOrFail(String h, String campo) {
        try {
            String[] parts = h.trim().split(":");
            int hh = Integer.parseInt(parts[0]);
            int mm = Integer.parseInt(parts[1]);
            if (hh < 0 || hh > 23 || mm < 0 || mm > 59) throw new IllegalArgumentException();
            return hh * 60 + mm;
        } catch (Exception ex) {
            throw new javax.validation.ValidationException("Formato invalido en " + campo + ". Use HH:mm (ej: 09:30)");
        }
    }
}
