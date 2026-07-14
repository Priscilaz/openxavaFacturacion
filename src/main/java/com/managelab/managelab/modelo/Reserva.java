package com.managelab.managelab.modelo;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.jpa.*;

import com.managelab.managelab.calculators.*;

@Entity
@View(members = "laboratorio; solicitante; fecha; horaInicio, horaFin; estadoReserva; motivoRechazo")
@Tab(properties = "laboratorio.nombre, solicitante.nombre, fecha, horaInicio, horaFin, estadoReserva")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @Required
    @DescriptionsList(descriptionProperties = "nombre")
    private Laboratorio laboratorio;

    @ManyToOne
    @Required
    @DescriptionsList(descriptionProperties = "nombre")
    private Usuario solicitante;

    @Required
    @Temporal(TemporalType.DATE)
    private Date fecha;

    @Required
    @Stereotype("TIME")
    private String horaInicio;

    @Required
    @Stereotype("TIME")
    private String horaFin;

    /**
     * Required + ReadOnly necesita DefaultValueCalculator para que al dar "New"
     * se cargue el valor en pantalla.
     */
    @Required
    @Enumerated(EnumType.STRING)
    @ReadOnly
    @DefaultValueCalculator(EstadoReservaPendienteCalculator.class)
    private EstadoReserva estadoReserva = EstadoReserva.PENDIENTE;

    @Column(length = 500)
    @Stereotype("MEMO")
    private String motivoRechazo;

    /**
     * Flag para evitar validaciones pesadas cuando se hace merge() desde
     * las acciones de aprobar/rechazar.
     */
    @Transient
    private boolean omitirValidacionDisponibilidad = false;

    /**
     * UN SOLO callback para persist/update.
     */
    @PrePersist
    @PreUpdate
    private void antesDeGuardar() {
        asegurarEstadoInicial();

        // Si viene de Aprobar/Rechazar y ya validamos fuera, no repetimos la query
        if (!omitirValidacionDisponibilidad) {
            validarDisponibilidadInterna();
        }
    }

    private void asegurarEstadoInicial() {
        if (estadoReserva == null) {
            estadoReserva = EstadoReserva.PENDIENTE;
        }
    }

    /**
     * Valida conflicto de horario contra reservas APROBADAS o PENDIENTES
     * del mismo laboratorio y fecha.
     */
    private void validarDisponibilidadInterna() {
        if (laboratorio == null || laboratorio.getId() == null ||
            fecha == null || horaInicio == null || horaFin == null) {
            return;
        }

        int inicio = toMinutesOrFail(horaInicio, "horaInicio");
        int fin = toMinutesOrFail(horaFin, "horaFin");

        if (fin <= inicio) {
            throw new javax.validation.ValidationException(
                "La hora fin debe ser mayor a la hora inicio"
            );
        }

        // Estados que consideramos conflicto: APROBADA y PENDIENTE
        List<EstadoReserva> estadosConflicto =
            Arrays.asList(EstadoReserva.APROBADA, EstadoReserva.PENDIENTE);

        String q =
            "SELECT r FROM Reserva r " +
            "WHERE r.laboratorio.id = :labId " +
            "AND r.fecha = :fecha " +
            "AND r.estadoReserva IN :estados " +
            "AND (:idActual IS NULL OR r.id <> :idActual)";

        EntityManager em = XPersistence.getManager();

        @SuppressWarnings("unchecked")
        List<Reserva> reservasConflicto = em
            .createQuery(q)
            .setFlushMode(FlushModeType.COMMIT)
            .setParameter("labId", laboratorio.getId())
            .setParameter("fecha", fecha)
            .setParameter("estados", estadosConflicto)
            .setParameter("idActual", id)
            .getResultList();

        for (Reserva r : reservasConflicto) {
            if (r.getHoraInicio() == null || r.getHoraFin() == null) continue;

            int rInicio = toMinutesOrFail(r.getHoraInicio(), "horaInicio existente");
            int rFin = toMinutesOrFail(r.getHoraFin(), "horaFin existente");

            boolean traslapa = (rInicio < fin) && (rFin > inicio);
            if (traslapa) {
                throw new javax.validation.ValidationException(
                    "El laboratorio ya tiene una reserva registrada (aprobada o pendiente) en ese horario."
                );
            }
        }

        // Normaliza a HH:mm
        this.horaInicio = formatHHmm(inicio);
        this.horaFin = formatHHmm(fin);
    }

    private int toMinutesOrFail(String h, String campo) {
        try {
            String v = h.trim();
            String[] parts = v.split(":");
            if (parts.length != 2) throw new IllegalArgumentException();
            int hh = Integer.parseInt(parts[0]);
            int mm = Integer.parseInt(parts[1]);
            if (hh < 0 || hh > 23 || mm < 0 || mm > 59) throw new IllegalArgumentException();
            return hh * 60 + mm;
        } catch (Exception ex) {
            throw new javax.validation.ValidationException(
                "Formato invalido en " + campo + ". Use HH:mm (ej: 09:30)"
            );
        }
    }

    private String formatHHmm(int minutes) {
        int hh = minutes / 60;
        int mm = minutes % 60;
        return String.format("%02d:%02d", hh, mm);
    }

    // Getters y Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Laboratorio getLaboratorio() { return laboratorio; }
    public void setLaboratorio(Laboratorio laboratorio) { this.laboratorio = laboratorio; }

    public Usuario getSolicitante() { return solicitante; }
    public void setSolicitante(Usuario solicitante) { this.solicitante = solicitante; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public String getHoraInicio() { return horaInicio; }
    public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }

    public String getHoraFin() { return horaFin; }
    public void setHoraFin(String horaFin) { this.horaFin = horaFin; }

    public EstadoReserva getEstadoReserva() { return estadoReserva; }
    public void setEstadoReserva(EstadoReserva estadoReserva) { this.estadoReserva = estadoReserva; }

    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }

    public boolean isOmitirValidacionDisponibilidad() {
        return omitirValidacionDisponibilidad;
    }

    public void setOmitirValidacionDisponibilidad(boolean omitirValidacionDisponibilidad) {
        this.omitirValidacionDisponibilidad = omitirValidacionDisponibilidad;
    }
}
