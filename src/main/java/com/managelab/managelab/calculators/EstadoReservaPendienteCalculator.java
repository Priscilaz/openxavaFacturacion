package com.managelab.managelab.calculators;

import org.openxava.calculators.*;

import com.managelab.managelab.modelo.*;

public class EstadoReservaPendienteCalculator implements ICalculator {
    private static final long serialVersionUID = 1L;

    @Override
    public Object calculate() throws Exception {
        return EstadoReserva.PENDIENTE;
    }
}
