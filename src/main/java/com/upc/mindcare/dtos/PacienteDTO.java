package com.upc.mindcare.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PacienteDTO
{
    private Long pacienteId;

    @Min(value = 0, message = "Edad invÃ¡lida")
    private Integer edad;

    @NotBlank(message = "El gÃ©nero es obligatorio")
    private String genero;

    @NotNull(message = "Fecha obligatoria")
    private LocalDate fechaNacimiento;

    @Pattern(regexp = "^[0-9]{9}$", message = "TelÃ©fono debe tener 9 dÃ­gitos")
    private String telefono;

    @Pattern(regexp = "^[0-9]{9}$", message = "Contacto invÃ¡lido")
    private String contactoEmergencia;

    private Long usuarioId;
}

