package com.managelab.managelab.modelo;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.*;
import org.openxava.annotations.*;

/**
 * Entidad Activo - CU 3: Gestion de Activos
 * El codigoInventario debe ser unico en todo el sistema.
 */
@Entity
@View(members = "codigoInventario; nombre; descripcion; costoInicial; fechaAdquisicion; vidaUtilAnios; laboratorio; categoria; valorActual")
@Tab(properties = "codigoInventario, nombre, laboratorio.nombre, categoria.nombre, costoInicial, fechaAdquisicion")
public class Activo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //cambioPrueba
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
     * Calculo dinamico del valor actual (depreciacion lineal)
     * CU 9: Calculo de Depreciacion
     */
    @Stereotype("MONEY")
    @Calculation("costoInicial - (costoInicial / vidaUtilAnios * YEAR(CURRENT_DATE) - YEAR(fechaAdquisicion))")
    public BigDecimal getValorActual() {
        if (costoInicial == null || vidaUtilAnios == null || vidaUtilAnios == 0 || fechaAdquisicion == null) {
            return costoInicial;
        }

        long aniosTranscurridos = (new Date().getTime() - fechaAdquisicion.getTime()) / (1000L * 60 * 60 * 24 * 365);
        if (aniosTranscurridos < 0)
            aniosTranscurridos = 0;
        if (aniosTranscurridos > vidaUtilAnios)
            aniosTranscurridos = vidaUtilAnios;

        BigDecimal depreciacionAnual = costoInicial.divide(new BigDecimal(vidaUtilAnios), 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal depreciacionTotal = depreciacionAnual.multiply(new BigDecimal(aniosTranscurridos));

        return costoInicial.subtract(depreciacionTotal);
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoInventario() {
        return codigoInventario;
    }

    public void setCodigoInventario(String codigoInventario) {
        this.codigoInventario = codigoInventario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getCostoInicial() {
        return costoInicial;
    }

    public void setCostoInicial(BigDecimal costoInicial) {
        this.costoInicial = costoInicial;
    }

    public Date getFechaAdquisicion() {
        return fechaAdquisicion;
    }

    public void setFechaAdquisicion(Date fechaAdquisicion) {
        this.fechaAdquisicion = fechaAdquisicion;
    }

    public Integer getVidaUtilAnios() {
        return vidaUtilAnios;
    }

    public void setVidaUtilAnios(Integer vidaUtilAnios) {
        this.vidaUtilAnios = vidaUtilAnios;
    }

    public Laboratorio getLaboratorio() {
        return laboratorio;
    }

    public void setLaboratorio(Laboratorio laboratorio) {
        this.laboratorio = laboratorio;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }
}
