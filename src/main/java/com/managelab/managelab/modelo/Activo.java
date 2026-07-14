package com.managelab.managelab.modelo;

import java.math.*;
import java.time.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;

@Entity
@View(members = "codigoInventario; nombre; descripcion; costoInicial; fechaAdquisicion; vidaUtilAnios; laboratorio; categoria; valorActual")
@Tab(properties = "codigoInventario, nombre, laboratorio.nombre, categoria.nombre, costoInicial, fechaAdquisicion")
public class Activo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Required
    @Column(length = 50, unique = true)
    private String codigoInventario;

    @Required
    @Column(length = 100)
    private String nombre;

    @Column(length = 500)
    @Stereotype("MEMO")
    private String descripcion;

    @Required
    @Stereotype("MONEY")
    private BigDecimal costoInicial;

    @Required
    @Temporal(TemporalType.DATE)
    private Date fechaAdquisicion;

    @Required
    private Integer vidaUtilAnios;

    @ManyToOne
    @Required
    @DescriptionsList(descriptionProperties = "nombre")
    private Laboratorio laboratorio;

    @ManyToOne
    @Required
    @DescriptionsList(descriptionProperties = "nombre")
    private Categoria categoria;

    /**
     * Valor actual calculado en Java (depreciacion lineal simple).
     * Se muestra en la vista, pero NO se persiste.
     */
    @Transient
    @Stereotype("MONEY")
    @Depends("costoInicial, vidaUtilAnios, fechaAdquisicion")
    public BigDecimal getValorActual() {
        if (costoInicial == null || vidaUtilAnios == null || vidaUtilAnios <= 0 || fechaAdquisicion == null) {
            return costoInicial;
        }

        LocalDate adq = fechaAdquisicion.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate hoy = LocalDate.now();

        long aniosTranscurridos = Period.between(adq, hoy).getYears();
        if (aniosTranscurridos < 0) aniosTranscurridos = 0;
        if (aniosTranscurridos > vidaUtilAnios) aniosTranscurridos = vidaUtilAnios;

        BigDecimal depAnual = costoInicial.divide(BigDecimal.valueOf(vidaUtilAnios), 2, RoundingMode.HALF_UP);
        BigDecimal depTotal = depAnual.multiply(BigDecimal.valueOf(aniosTranscurridos));
        BigDecimal valor = costoInicial.subtract(depTotal);

        if (valor.compareTo(BigDecimal.ZERO) < 0) return BigDecimal.ZERO;
        return valor;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigoInventario() { return codigoInventario; }
    public void setCodigoInventario(String codigoInventario) { this.codigoInventario = codigoInventario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getCostoInicial() { return costoInicial; }
    public void setCostoInicial(BigDecimal costoInicial) { this.costoInicial = costoInicial; }

    public Date getFechaAdquisicion() { return fechaAdquisicion; }
    public void setFechaAdquisicion(Date fechaAdquisicion) { this.fechaAdquisicion = fechaAdquisicion; }

    public Integer getVidaUtilAnios() { return vidaUtilAnios; }
    public void setVidaUtilAnios(Integer vidaUtilAnios) { this.vidaUtilAnios = vidaUtilAnios; }

    public Laboratorio getLaboratorio() { return laboratorio; }
    public void setLaboratorio(Laboratorio laboratorio) { this.laboratorio = laboratorio; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
}
