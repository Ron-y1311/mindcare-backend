package com.upc.mindcare.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RecomendacionDTO
{
    private Long pacienteId;
    private String nivelEmocional;
    private String estadoGeneral;
    private String analisis;
    private List<String> recomendaciones;
    private String accionSugerida;
    private String prioridad;
    private String alertaPreventiva;
}
