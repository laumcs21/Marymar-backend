package com.marymar.app.controller;

import com.marymar.app.business.DTO.PersonaCreateDTO;
import com.marymar.app.business.DTO.PersonaResponseDTO;
import com.marymar.app.business.Service.PersonaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/personas")
@CrossOrigin(origins = "http://localhost:4200")
public class PersonaController {

    private final PersonaService personaService;

    public PersonaController(PersonaService personaService) {
        this.personaService = personaService;
    }

    // =========================
    // ADMIN + MESERO: LISTAR
    // =========================
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('MESERO')")
    public ResponseEntity<List<PersonaResponseDTO>> obtenerTodas() {
        return ResponseEntity.ok(personaService.obtenerTodas());
    }

    // =========================
    // ADMIN + MESERO: OBTENER POR ID
    // =========================
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('MESERO')")
    public ResponseEntity<PersonaResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(personaService.obtenerPorId(id));
    }

    // =========================
    // CLIENTE: OBTENER SU PERFIL (por token)
    // =========================
    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENTE') or hasRole('MESERO') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<PersonaResponseDTO> miPerfil(Principal principal) {
        return ResponseEntity.ok(personaService.obtenerPorEmail(principal.getName()));
    }

    // =========================
    // ADMIN: CREAR
    // =========================
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<PersonaResponseDTO> crear(@RequestBody PersonaCreateDTO dto) {
        PersonaResponseDTO created = personaService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // =========================
    // ADMIN: ACTUALIZAR CUALQUIERA
    // =========================
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<PersonaResponseDTO> actualizar(
            @PathVariable Long id,
            @RequestBody PersonaCreateDTO dto
    ) {
        return ResponseEntity.ok(personaService.actualizar(id, dto));
    }

    // =========================
    // CLIENTE: ACTUALIZAR SU PERFIL (por token)
    // =========================
    @PutMapping("/me")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<PersonaResponseDTO> actualizarMiPerfil(
            Principal principal,
            @RequestBody PersonaCreateDTO dto
    ) {
        PersonaResponseDTO actual = personaService.obtenerPorEmail(principal.getName());
        return ResponseEntity.ok(personaService.actualizar(actual.getId(), dto));
    }

    // =========================
    // ADMIN: ELIMINAR
    // =========================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        personaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // =========================
    // ADMIN: DESACTIVAR
    // =========================
    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        personaService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
