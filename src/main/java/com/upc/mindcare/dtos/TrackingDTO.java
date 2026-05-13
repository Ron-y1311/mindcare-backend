package com.upc.mindcare.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TrackingDTO {

    private Long idTracking;

    @NotNull(message = "El paciente es obligatorio")
    private Long pacienteId;

    @NotNull(message = "El estado de ánimo es obligatorio")
    private Long estadoAnimoId;

    @NotNull(message = "La intensidad emocional es obligatoria")
    @Min(value = 1, message = "La intensidad mínima es 1")
    @Max(value = 10, message = "La intensidad máxima es 10")
    private Integer numeroIntensidad;

    @NotBlank(message = "La nota es obligatoria")
    private String nota;

    @NotBlank(message = "La reflexión es obligatoria")
    private String reflexionDescripcion;
}