package com.furniterental.modelo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "alquileres")
public class Alquiler {
    @Id
    private String id;
    private String usuarioId;
    private String usuarioNombre;
    private String usuarioCorreo;
    private List<ItemCarrito> items;
    private Double total;
    private String estado; // PENDIENTE, CONFIRMADO, EN_CURSO, COMPLETADO, CANCELADO
    private LocalDateTime fechaAlquiler;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String direccionEntrega;
    private String notasAdicionales;
    
    // Campos para negociación de transporte
    private Double costoTransporteUsuario;      // Costo propuesto por el usuario
    private Double costoTransporteAdmin;        // Costo propuesto por el admin
    private Double costoTransporteAceptado;     // Costo finalmente aceptado
    private String estadoTransporte;            // PENDIENTE, PROPUESTA_USUARIO, PROPUESTA_ADMIN, ACEPTADO, RECHAZADO
    private String quienPropuso;                // USUARIO o ADMIN (último en proponer)
    private LocalDateTime fechaPropuestaTransporte;
    
    // Campos para sistema de pagos (50% al confirmar, 50% al completar)
    private Double montoPagoParcial;            // 50% del total a pagar al confirmar
    private Double montoSaldoPendiente;         // 50% restante a pagar al completar
    private LocalDateTime fechaPagoParcial;     // Fecha del pago del 50% inicial
    private LocalDateTime fechaPagoFinal;       // Fecha del pago del 50% final
    private String estadoPagoParcial;           // PENDIENTE, PAGADO
    private String estadoPagoFinal;             // PENDIENTE, PAGADO
    private Boolean pagoContraEntrega;          // true si el usuario eligió pagar contra entrega

    public Alquiler() {
        this.fechaAlquiler = LocalDateTime.now();
        this.estado = "PENDIENTE";
        // No inicializar estadoTransporte aquí - se establece según la elección del usuario
        this.costoTransporteAceptado = 0.0;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }

    public String getUsuarioCorreo() {
        return usuarioCorreo;
    }

    public void setUsuarioCorreo(String usuarioCorreo) {
        this.usuarioCorreo = usuarioCorreo;
    }

    public List<ItemCarrito> getItems() {
        return items;
    }

    public void setItems(List<ItemCarrito> items) {
        this.items = items;
        calcularTotal();
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaAlquiler() {
        return fechaAlquiler;
    }

    public void setFechaAlquiler(LocalDateTime fechaAlquiler) {
        this.fechaAlquiler = fechaAlquiler;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getDireccionEntrega() {
        return direccionEntrega;
    }

    public void setDireccionEntrega(String direccionEntrega) {
        this.direccionEntrega = direccionEntrega;
    }

    public String getNotasAdicionales() {
        return notasAdicionales;
    }

    public void setNotasAdicionales(String notasAdicionales) {
        this.notasAdicionales = notasAdicionales;
    }

    public Double getCostoTransporteUsuario() {
        return costoTransporteUsuario;
    }

    public void setCostoTransporteUsuario(Double costoTransporteUsuario) {
        this.costoTransporteUsuario = costoTransporteUsuario;
    }

    public Double getCostoTransporteAdmin() {
        return costoTransporteAdmin;
    }

    public void setCostoTransporteAdmin(Double costoTransporteAdmin) {
        this.costoTransporteAdmin = costoTransporteAdmin;
    }

    public Double getCostoTransporteAceptado() {
        return costoTransporteAceptado;
    }

    public void setCostoTransporteAceptado(Double costoTransporteAceptado) {
        this.costoTransporteAceptado = costoTransporteAceptado;
    }

    public String getEstadoTransporte() {
        return estadoTransporte;
    }

    public void setEstadoTransporte(String estadoTransporte) {
        this.estadoTransporte = estadoTransporte;
    }

    public String getQuienPropuso() {
        return quienPropuso;
    }

    public void setQuienPropuso(String quienPropuso) {
        this.quienPropuso = quienPropuso;
    }

    public LocalDateTime getFechaPropuestaTransporte() {
        return fechaPropuestaTransporte;
    }

    public void setFechaPropuestaTransporte(LocalDateTime fechaPropuestaTransporte) {
        this.fechaPropuestaTransporte = fechaPropuestaTransporte;
    }

    /**
     * Calcula el total del alquiler incluyendo el costo de transporte aceptado
     */
    private void calcularTotal() {
        double subtotal = 0.0;
        if (items != null && !items.isEmpty()) {
            subtotal = items.stream()
                .mapToDouble(ItemCarrito::getSubtotal)
                .sum();
        }
        
        // Agregar costo de transporte si fue aceptado
        if (costoTransporteAceptado != null && costoTransporteAceptado > 0) {
            this.total = subtotal + costoTransporteAceptado;
        } else {
            this.total = subtotal;
        }
    }
    
    /**
     * Recalcula el total cuando se acepta un costo de transporte
     */
    public void recalcularTotalConTransporte() {
        calcularTotal();
    }
    
    // Getters y Setters para campos de pago
    public Double getMontoPagoParcial() {
        return montoPagoParcial;
    }
    
    public void setMontoPagoParcial(Double montoPagoParcial) {
        this.montoPagoParcial = montoPagoParcial;
    }
    
    public Double getMontoSaldoPendiente() {
        return montoSaldoPendiente;
    }
    
    public void setMontoSaldoPendiente(Double montoSaldoPendiente) {
        this.montoSaldoPendiente = montoSaldoPendiente;
    }
    
    public LocalDateTime getFechaPagoParcial() {
        return fechaPagoParcial;
    }
    
    public void setFechaPagoParcial(LocalDateTime fechaPagoParcial) {
        this.fechaPagoParcial = fechaPagoParcial;
    }
    
    public LocalDateTime getFechaPagoFinal() {
        return fechaPagoFinal;
    }
    
    public void setFechaPagoFinal(LocalDateTime fechaPagoFinal) {
        this.fechaPagoFinal = fechaPagoFinal;
    }
    
    public String getEstadoPagoParcial() {
        return estadoPagoParcial;
    }
    
    public void setEstadoPagoParcial(String estadoPagoParcial) {
        this.estadoPagoParcial = estadoPagoParcial;
    }
    
    public String getEstadoPagoFinal() {
        return estadoPagoFinal;
    }
    
    public void setEstadoPagoFinal(String estadoPagoFinal) {
        this.estadoPagoFinal = estadoPagoFinal;
    }
    
    /**
     * Calcula los montos de pago (50% parcial y 50% saldo pendiente)
     * Se debe llamar cuando el alquiler cambia a estado CONFIRMADO
     * Si es pago contra entrega, no se marcan como pendientes
     */
    public void calcularMontosPago() {
        if (this.total != null && this.total > 0) {
            this.montoPagoParcial = this.total * 0.5;
            this.montoSaldoPendiente = this.total * 0.5;
            
            // Solo marcar como pendiente si NO es pago contra entrega
            if (!Boolean.TRUE.equals(this.pagoContraEntrega)) {
                this.estadoPagoParcial = "PENDIENTE";
                this.estadoPagoFinal = "PENDIENTE";
            } else {
                // Para pago contra entrega, marcar como N/A o null
                this.estadoPagoParcial = null;
                this.estadoPagoFinal = null;
            }
        }
    }
    
    /**
     * Marca el pago parcial como pagado
     */
    public void marcarPagoParcialPagado() {
        this.estadoPagoParcial = "PAGADO";
        this.fechaPagoParcial = LocalDateTime.now();
    }
    
    /**
     * Marca el pago final como pagado
     */
    public void marcarPagoFinalPagado() {
        this.estadoPagoFinal = "PAGADO";
        this.fechaPagoFinal = LocalDateTime.now();
    }
    
    public Boolean getPagoContraEntrega() {
        return pagoContraEntrega;
    }
    
    public void setPagoContraEntrega(Boolean pagoContraEntrega) {
        this.pagoContraEntrega = pagoContraEntrega;
    }
}
