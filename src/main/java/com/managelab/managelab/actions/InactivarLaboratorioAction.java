package com.managelab.managelab.actions;

import org.openxava.actions.*;
import org.openxava.jpa.*;

import com.managelab.managelab.modelo.*;

public class InactivarLaboratorioAction extends ViewBaseAction {

    @Override
    public void execute() throws Exception {
        Long id = (Long) getView().getKeyValues().get("id");

        if (id == null) {
            addError("Debe seleccionar un laboratorio");
            return;
        }

        Laboratorio laboratorio = XPersistence.getManager().find(Laboratorio.class, id);

        if (laboratorio == null) {
            addError("Laboratorio no encontrado");
            return;
        }

        laboratorio.setEstado(EstadoLaboratorio.INACTIVO);

        XPersistence.getManager().flush();
        XPersistence.commit();

        addMessage("Laboratorio inactivado exitosamente");
        getView().refresh();
    }
}
