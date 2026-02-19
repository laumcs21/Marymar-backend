package com.marymar.app.persistence.Mapper;

import com.marymar.app.business.DTO.PersonaCreateDTO;
import com.marymar.app.business.DTO.PersonaResponseDTO;
import com.marymar.app.persistence.Entity.Persona;
import com.marymar.app.persistence.Entity.Rol;
import org.springframework.stereotype.Component;

@Component
public class PersonaMapper {

    // =========================
    // CreateDTO -> Entity (usando Builder)
    // =========================
    public Persona toEntity(PersonaCreateDTO dto) {

        if (dto == null) {
            return null;
        }

        Rol rol = null;
        if (dto.getRol() != null) {
            rol = Rol.valueOf(dto.getRol().toUpperCase());
        }

        return new Persona.Builder()
                .numeroIdentificacion(dto.getNumeroIdentificacion())
                .nombre(dto.getNombre())
                .email(dto.getEmail())
                .contrasena(dto.getContrasena())
                .telefono(dto.getTelefono())
                .fechaNacimiento(dto.getFechaNacimiento())
                .rol(rol)
                .direccionEnvio(dto.getDireccionEnvio())
                .salario(dto.getSalario())
                .activo(true)
                .build();
    }

    // =========================
    // Entity -> ResponseDTO
    // =========================
    public PersonaResponseDTO toDTO(Persona persona) {

        if (persona == null) {
            return null;
        }

        PersonaResponseDTO dto = new PersonaResponseDTO();

        dto.setId(persona.getId());
        dto.setNombre(persona.getNombre());
        dto.setEmail(persona.getEmail());
        dto.setTelefono(persona.getTelefono());
        dto.setFechaNacimiento(persona.getFechaNacimiento());

        dto.setRol(persona.getRol());

        dto.setDireccionEnvio(persona.getDireccionEnvio());
        dto.setSalario(persona.getSalario());
        dto.setNumeroIdentificacion(persona.getNumeroIdentificacion());
        dto.setActivo(persona.isActivo());

        // Nuevos campos de auditoría
        dto.setCreatedAt(persona.getCreatedAt());
        dto.setUpdatedAt(persona.getUpdatedAt());

        return dto;
    }

    // =========================
    // Update desde CreateDTO
    // =========================
    public void updateFromDTO(Persona persona, PersonaCreateDTO dto) {

        if (dto.getNombre() != null) {
            persona.setNombre(dto.getNombre());
        }

        if (dto.getEmail() != null) {
            persona.setEmail(dto.getEmail());
        }

        if (dto.getContrasena() != null) {
            persona.setContrasena(dto.getContrasena());
        }

        if (dto.getTelefono() != null) {
            persona.setTelefono(dto.getTelefono());
        }

        if (dto.getDireccionEnvio() != null) {
            persona.setDireccionEnvio(dto.getDireccionEnvio());
        }

        if (dto.getSalario() != null) {
            persona.setSalario(dto.getSalario());
        }

        if (dto.getRol() != null) {
            persona.setRol(Rol.valueOf(dto.getRol().toUpperCase()));
        }
    }
}
