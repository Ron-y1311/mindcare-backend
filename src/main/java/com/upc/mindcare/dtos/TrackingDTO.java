package com.upc.mindcare.dtos;

import com.upc.mindcare.entities.EstadoAnimo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TrackingDTO
{
    //Valida propiedades de tracking
    private Long idTracking;
    private Long estadoAnimoId;
    private LocalDateTime fecha;
    private String nota;
    private Integer numeroIntensidad;
    private String reflexionDescripcion;
    private Long pacienteId;
}

