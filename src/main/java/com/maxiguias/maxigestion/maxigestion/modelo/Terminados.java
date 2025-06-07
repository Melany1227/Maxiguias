package com.maxiguias.maxigestion.maxigestion.modelo;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "TERMINADOS")
@Data
public class Terminados {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_TERMINADO")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ID_PRODUCTO", nullable = false)
    private Producto producto;


}
