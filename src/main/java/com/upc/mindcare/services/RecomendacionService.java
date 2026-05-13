package com.upc.mindcare.services;

import com.upc.mindcare.dtos.RecomendacionDTO;
import com.upc.mindcare.entities.Tracking;
import com.upc.mindcare.repositories.PacienteRepositorio;
import com.upc.mindcare.repositories.TrackingRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecomendacionService {

    @Autowired
    private TrackingRepositorio trackingRepositorio;

    @Autowired
    private PacienteRepositorio pacienteRepositorio;

    public String generarRecomendacionesPersonalizadas(Long pacienteId) {

        validarPacienteExiste(pacienteId);

        Tracking ultimo = obtenerUltimoTracking(pacienteId);

        if (ultimo == null) {
            return "No hay datos suficientes para generar recomendaciones.";
        }

        int intensidad = obtenerIntensidadValida(ultimo);

        if (intensidad <= 3) {
            return "Se recomienda continuar con hábitos saludables y actividades de bienestar emocional.";
        } else if (intensidad <= 6) {
            return "Se recomienda realizar actividades de autocuidado como respiración guiada, descanso adecuado y ejercicio moderado.";
        } else {
            return "Se recomienda agendar una cita con un profesional de salud mental para recibir seguimiento.";
        }
    }

    public String evaluarEstadoEmocional(Long pacienteId) {

        validarPacienteExiste(pacienteId);

        Tracking ultimo = obtenerUltimoTracking(pacienteId);

        if (ultimo == null) {
            return "Estado emocional desconocido.";
        }

        int intensidad = obtenerIntensidadValida(ultimo);

        if (intensidad <= 3) {
            return "Nivel emocional estable.";
        } else if (intensidad <= 6) {
            return "Nivel emocional moderado.";
        } else {
            return "Nivel emocional elevado. Se recomienda seguimiento profesional.";
        }
    }

    public String generarAlertaPreventiva(Long pacienteId) {

        validarPacienteExiste(pacienteId);

        List<Tracking> lista = trackingRepositorio.findByPaciente_PacienteIdOrderByFechaAsc(pacienteId);

        if (lista.size() < 3) {
            return "No hay suficientes datos para generar alerta.";
        }

        int contadorAlto = 0;

        for (int i = lista.size() - 3; i < lista.size(); i++) {
            Tracking tracking = lista.get(i);

            if (tracking.getNumeroIntensidad() != null) {
                int intensidad = tracking.getNumeroIntensidad();

                if (intensidad < 1 || intensidad > 10) {
                    throw new RuntimeException("La intensidad emocional debe estar entre 1 y 10");
                }

                if (intensidad >= 7) {
                    contadorAlto++;
                }
            }
        }

        if (contadorAlto >= 2) {
            return "ALERTA: Se detectan niveles emocionales elevados en registros recientes. Se recomienda seguimiento profesional.";
        }

        return "Sin alertas.";
    }

    public RecomendacionDTO generarResumenRecomendacion(Long pacienteId) {

        validarPacienteExiste(pacienteId);

        Tracking ultimo = obtenerUltimoTracking(pacienteId);

        if (ultimo == null) {
            return new RecomendacionDTO(
                    pacienteId,
                    "Desconocido",
                    "Sin datos suficientes",
                    "No existen registros emocionales suficientes para realizar un análisis personalizado.",
                    List.of(
                            "Registrar el estado emocional diariamente.",
                            "Completar encuestas emocionales para obtener una mejor evaluación.",
                            "Revisar el historial emocional conforme se registren nuevos datos."
                    ),
                    "Registrar estado emocional",
                    "Baja",
                    "No hay suficientes datos para generar alerta."
            );
        }

        int intensidad = obtenerIntensidadValida(ultimo);
        String alerta = generarAlertaPreventiva(pacienteId);

        if (intensidad <= 3) {
            return new RecomendacionDTO(
                    pacienteId,
                    "Estable",
                    "Bienestar emocional adecuado",
                    "El último registro emocional muestra una intensidad baja, lo que indica un estado emocional estable. Se recomienda mantener hábitos saludables y continuar con el seguimiento preventivo.",
                    List.of(
                            "Mantener una rutina de descanso adecuada.",
                            "Continuar con actividades de bienestar emocional.",
                            "Registrar el estado emocional para monitorear el progreso."
                    ),
                    "Mantener seguimiento",
                    "Baja",
                    alerta
            );
        } else if (intensidad <= 6) {
            return new RecomendacionDTO(
                    pacienteId,
                    "Moderado",
                    "Requiere autocuidado",
                    "El último registro emocional muestra una intensidad moderada. Se recomienda aplicar estrategias de autocuidado y continuar registrando el estado emocional para identificar posibles cambios.",
                    List.of(
                            "Realizar ejercicios de respiración guiada.",
                            "Tomar pausas durante el día.",
                            "Registrar una reflexión diaria para identificar factores de estrés."
                    ),
                    "Aplicar autocuidado",
                    "Media",
                    alerta
            );
        } else {
            return new RecomendacionDTO(
                    pacienteId,
                    "Elevado",
                    "Requiere seguimiento profesional",
                    "El último registro emocional muestra una intensidad elevada. Se recomienda recibir orientación profesional y continuar con el seguimiento emocional dentro de la plataforma.",
                    List.of(
                            "Agendar una cita con un profesional de salud mental.",
                            "Evitar sobrecargarse con actividades estresantes.",
                            "Registrar el estado emocional diariamente para monitorear cambios."
                    ),
                    "Agendar cita",
                    "Alta",
                    alerta
            );
        }
    }

    private void validarPacienteExiste(Long pacienteId) {
        pacienteRepositorio.findById(pacienteId)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
    }

    private Tracking obtenerUltimoTracking(Long pacienteId) {
        List<Tracking> lista = trackingRepositorio.findByPaciente_PacienteIdOrderByFechaAsc(pacienteId);

        if (lista.isEmpty()) {
            return null;
        }

        return lista.get(lista.size() - 1);
    }

    private int obtenerIntensidadValida(Tracking tracking) {
        if (tracking.getNumeroIntensidad() == null) {
            throw new RuntimeException("El registro emocional no tiene intensidad asignada");
        }

        int intensidad = tracking.getNumeroIntensidad();

        if (intensidad < 1 || intensidad > 10) {
            throw new RuntimeException("La intensidad emocional debe estar entre 1 y 10");
        }

        return intensidad;
    }
}