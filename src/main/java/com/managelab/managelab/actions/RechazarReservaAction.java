package com.managelab.managelab.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.jpa.*;

import com.managelab.managelab.modelo.*;

public class RechazarReservaAction extends TabBaseAction {

    @Override
    public void execute() throws Exception {

        Map[] keys = getTab().getSelectedKeys();

        if (keys == null || keys.length == 0) {
            addError("Debe seleccionar una reserva (marque el checkbox de la izquierda).");
            return;
        }

        Long id = (Long) keys[0].get("id");

        if (id == null) {
            addError("No se pudo obtener el id de la reserva seleccionada.");
            return;
        }

        Reserva reserva = XPersistence.getManager().find(Reserva.class, id);

        if (reserva == null) {
            addError("Reserva no encontrada.");
            return;
        }

        if (reserva.getEstadoReserva() != EstadoReserva.PENDIENTE) {
            addError("Solo se pueden rechazar reservas PENDIENTES.");
            return;
        }

        String motivo = reserva.getMotivoRechazo();
        if (motivo == null || motivo.trim().isEmpty()) {
            addError("Para rechazar: abra la reserva, escriba el motivo de rechazo, guarde y luego rechace.");
            return;
        }

        // Evitar que el callback vuelva a ejecutar la query de disponibilidad
        reserva.setOmitirValidacionDisponibilidad(true);

        reserva.setEstadoReserva(EstadoReserva.RECHAZADA);
        XPersistence.getManager().merge(reserva);
        XPersistence.commit();

        addMessage("Reserva rechazada.");
        getTab().reset(); // refresca listado
    }
}
