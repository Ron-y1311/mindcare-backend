package com.upc.mindcare.services;

import com.upc.mindcare.dtos.PacienteDTO;
import com.upc.mindcare.dtos.ProfesionalDTO;
import com.upc.mindcare.dtos.UsuarioDTO;
import com.upc.mindcare.entities.*;
import com.upc.mindcare.repositories.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UsuarioService {

    private static final String ROL_ADMIN = "ADMIN";
    private static final String ROL_PACIENTE = "PACIENTE";
    private static final String ROL_PROFESIONAL = "PROFESIONAL";

    @Autowired private UsuarioRepositorio usuarioRepositorio;
    @Autowired private PacienteRepositorio pacienteRepositorio;
    @Autowired private ProfesionalRepositorio profesionalRepositorio;
    @Autowired private RolRepositorio rolRepositorio;
    @Autowired private PasswordResetTokenRepositorio passwordResetTokenRepositorio;
    @Autowired private ModelMapper modelMapper;
    @Autowired private PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioDTO registrarUsuario(UsuarioDTO dto) {
        if (usuarioRepositorio.findByCorreo(dto.getCorreo()).isPresent()) {
            throw new RuntimeException("El correo ya esta registrado");
        }
        if (dto.getRolId() == null) {
            throw new RuntimeException("El rol es obligatorio");
        }

        Rol rol = rolRepositorio.findById(dto.getRolId())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        Usuario usuario = modelMapper.map(dto, Usuario.class);
        usuario.setRol(rol);
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setVerificado(Boolean.FALSE);
        usuario.setActivo(Boolean.TRUE);
        usuario.setFechaRegistro(LocalDateTime.now());
        usuario.setUltimoAcceso(null);

        return modelMapper.map(usuarioRepositorio.save(usuario), UsuarioDTO.class);
    }

    public UsuarioDTO login(String correo, String password) {
        Usuario usuario = usuarioRepositorio.findByCorreoAndActivoTrue(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado o inactivo"));
        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new RuntimeException("Credenciales invalidas");
        }
        usuario.setUltimoAcceso(LocalDateTime.now());
        usuarioRepositorio.save(usuario);
        return modelMapper.map(usuario, UsuarioDTO.class);
    }

    public List<UsuarioDTO> listarUsuarios() {
        return usuarioRepositorio.findAll().stream().map(u -> modelMapper.map(u, UsuarioDTO.class)).toList();
    }

    public List<UsuarioDTO> listarAdministradores() { return listarPorRol(ROL_ADMIN); }
    public List<UsuarioDTO> listarPacientesUsuarios() { return listarPorRol(ROL_PACIENTE); }
    public List<UsuarioDTO> listarProfesionalesUsuarios() { return listarPorRol(ROL_PROFESIONAL); }

    public UsuarioDTO obtenerUsuarioPorId(Long id) {
        return modelMapper.map(buscarUsuario(id), UsuarioDTO.class);
    }

    public UsuarioDTO buscarUsuarioPorCorreo(String correo) {
        Usuario usuario = usuarioRepositorio.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return modelMapper.map(usuario, UsuarioDTO.class);
    }

    @Transactional
    public void eliminarUsuario(Long id) { cambiarEstadoActivo(id, false); }

    @Transactional
    public PacienteDTO registrarPaciente(PacienteDTO dto) {
        Usuario usuario = buscarUsuario(dto.getUsuarioId());
        validarRol(usuario, ROL_PACIENTE);
        if (pacienteRepositorio.existsByUsuario_IdUsuario(dto.getUsuarioId())) {
            throw new RuntimeException("El usuario ya tiene un perfil de paciente");
        }
        Paciente paciente = new Paciente();
        paciente.setUsuario(usuario);
        paciente.setEdad(dto.getEdad());
        paciente.setGenero(dto.getGenero());
        paciente.setFechaNacimiento(dto.getFechaNacimiento());
        paciente.setTelefono(dto.getTelefono());
        paciente.setContactoEmergencia(dto.getContactoEmergencia());
        return modelMapper.map(pacienteRepositorio.save(paciente), PacienteDTO.class);
    }

    @Transactional
    public ProfesionalDTO registrarProfesional(ProfesionalDTO dto) {
        Usuario usuario = buscarUsuario(dto.getUsuarioId());
        validarRol(usuario, ROL_PROFESIONAL);
        if (profesionalRepositorio.existsByUsuario_IdUsuario(dto.getUsuarioId())) {
            throw new RuntimeException("El usuario ya tiene un perfil profesional");
        }
        Profesional profesional = new Profesional();
        profesional.setUsuario(usuario);
        profesional.setEspecialidad(dto.getEspecialidad());
        profesional.setNumeroColegiatura(dto.getNumeroColegiatura());
        profesional.setEtiquetas(dto.getEtiquetas());
        profesional.setAniosExperiencia(dto.getAniosExperiencia());
        profesional.setDescripcionPerfil(dto.getDescripcionPerfil());
        profesional.setDocumentoValidacion(dto.getDocumentoValidacion());
        profesional.setEstadoValidacion("PENDIENTE");
        profesional.setFechaSolicitud(LocalDateTime.now());
        return modelMapper.map(profesionalRepositorio.save(profesional), ProfesionalDTO.class);
    }

    @Transactional
    public String solicitarRecuperacionPassword(String correo) {
        Usuario usuario = usuarioRepositorio.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("El correo no existe"));
        PasswordResetToken reset = new PasswordResetToken();
        reset.setUsuario(usuario);
        reset.setToken(UUID.randomUUID().toString());
        reset.setFechaExpiracion(LocalDateTime.now().plusMinutes(30));
        reset.setUsado(Boolean.FALSE);
        passwordResetTokenRepositorio.save(reset);
        return "Token de recuperacion generado: " + reset.getToken();
    }

    @Transactional
    public String restablecerPassword(String token, String nuevaPassword) {
        PasswordResetToken reset = passwordResetTokenRepositorio.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token de recuperacion invalido"));
        if (Boolean.TRUE.equals(reset.getUsado())) {
            throw new RuntimeException("El token ya fue utilizado");
        }
        if (reset.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("El token expiro");
        }
        Usuario usuario = reset.getUsuario();
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepositorio.save(usuario);
        reset.setUsado(Boolean.TRUE);
        passwordResetTokenRepositorio.save(reset);
        return "Contrasena actualizada correctamente";
    }

    @Transactional
    public String recuperarPassword(String correo, String nuevaPassword) {
        Usuario usuario = usuarioRepositorio.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("El correo no existe"));
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepositorio.save(usuario);
        return "Contrasena actualizada correctamente";
    }

    @Transactional public void activarUsuario(Long id) { cambiarEstadoActivo(id, true); }
    @Transactional public void desactivarUsuario(Long id) { cambiarEstadoActivo(id, false); }

    private List<UsuarioDTO> listarPorRol(String rol) {
        return usuarioRepositorio.findByRol_Nombre(rol).stream().map(u -> modelMapper.map(u, UsuarioDTO.class)).toList();
    }

    private Usuario buscarUsuario(Long id) {
        return usuarioRepositorio.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    private void cambiarEstadoActivo(Long id, boolean activo) {
        Usuario usuario = buscarUsuario(id);
        usuario.setActivo(activo);
        usuarioRepositorio.save(usuario);
    }

    private void validarRol(Usuario usuario, String rolEsperado) {
        if (usuario.getRol() == null || usuario.getRol().getNombre() == null) {
            throw new RuntimeException("El usuario no tiene rol asignado");
        }
        if (!usuario.getRol().getNombre().equalsIgnoreCase(rolEsperado)) {
            throw new RuntimeException("El usuario no tiene rol de " + rolEsperado);
        }
    }
}