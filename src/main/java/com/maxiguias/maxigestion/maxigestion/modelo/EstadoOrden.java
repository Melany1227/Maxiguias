package com.maxiguias.maxigestion.maxigestion.modelo;

public enum EstadoOrden {
    PENDIENTE("Pendiente", "warning"),
    EN_PROCESO("En Proceso", "primary"),
    FINALIZADA("Finalizada", "success"),
    CANCELADA("Cancelada", "danger"),
    FACTURADA("Facturada", "info");
    // nuevo o recibido - PROGRESO - facturado - finalizado
    private final String descripcion;
    private final String colorBootstrap;

    EstadoOrden(String descripcion, String colorBootstrap) {
        this.descripcion = descripcion;
        this.colorBootstrap = colorBootstrap;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getColorBootstrap() {
        return colorBootstrap;
    }

    public String getCssClass() {
        return "badge bg-" + colorBootstrap + " text-dark";
    }
}