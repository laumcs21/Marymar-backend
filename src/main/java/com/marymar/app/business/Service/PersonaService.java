package com.marymar.app.business.Service;

import com.marymar.app.business.DTO.PersonaCreateDTO;
import com.marymar.app.business.DTO.PersonaResponseDTO;
import com.marymar.app.persistence.Entity.Persona;

import java.util.List;

public interface PersonaService {

    PersonaResponseDTO crear(PersonaCreateDTO dto);

    PersonaResponseDTO obtenerPorId(Long id);

    PersonaResponseDTO obtenerPorEmail(String email);

    List<PersonaResponseDTO> obtenerTodas();

    PersonaResponseDTO actualizar(Long id, PersonaCreateDTO dto);

    void desactivar(Long id);

    void eliminar(Long id);

    Persona buscarOCrearUsuarioGoogle(String email, String nombre);

}
