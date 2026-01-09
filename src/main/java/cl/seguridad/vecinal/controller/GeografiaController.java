package cl.seguridad.vecinal.controller;

import cl.seguridad.vecinal.dao.CiudadRepository;
import cl.seguridad.vecinal.dao.ComunaRepository;
import cl.seguridad.vecinal.dao.VillaRepository;
import cl.seguridad.vecinal.modelo.Ciudad;
import cl.seguridad.vecinal.modelo.Comuna;
import cl.seguridad.vecinal.modelo.Villa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/geografia")
public class GeografiaController {

    @Autowired
    private CiudadRepository ciudadRepository;

    @Autowired
    private ComunaRepository comunaRepository;

    @Autowired
    private VillaRepository villaRepository;

    // ========== CIUDADES ==========

    @GetMapping("/ciudades")
    public ResponseEntity<Map<String, Object>> getAllCiudades() {
        try {
            List<Ciudad> ciudades = ciudadRepository.findByActivoTrue();

            List<Map<String, Object>> ciudadesDto = ciudades.stream().map(ciudad -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", ciudad.getId());
                dto.put("nombre", ciudad.getNombre());
                dto.put("region", ciudad.getRegion());
                dto.put("pais", ciudad.getPais());
                return dto;
            }).collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("ciudades", ciudadesDto);
            response.put("total", ciudadesDto.size());
            response.put("status", "success");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Error al obtener ciudades: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // ========== COMUNAS ==========

    @GetMapping("/comunas")
    public ResponseEntity<Map<String, Object>> getAllComunas(
            @RequestParam(required = false) Long ciudadId) {
        try {
            List<Comuna> comunas;

            if (ciudadId != null) {
                comunas = comunaRepository.findActiveByCiudadId(ciudadId);
            } else {
                comunas = comunaRepository.findByActivoTrue();
            }

            List<Map<String, Object>> comunasDto = comunas.stream().map(comuna -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", comuna.getId());
                dto.put("nombre", comuna.getNombre());
                dto.put("ciudadId", comuna.getCiudad().getId());
                dto.put("ciudadNombre", comuna.getCiudadNombre());
                dto.put("codigoPostal", comuna.getCodigoPostal());
                return dto;
            }).collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("comunas", comunasDto);
            response.put("total", comunasDto.size());
            response.put("status", "success");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Error al obtener comunas: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @GetMapping("/comunas/{id}")
    public ResponseEntity<Map<String, Object>> getComunaById(@PathVariable Long id) {
        try {
            Comuna comuna = comunaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Comuna no encontrada"));

            Map<String, Object> dto = new HashMap<>();
            dto.put("id", comuna.getId());
            dto.put("nombre", comuna.getNombre());
            dto.put("ciudadId", comuna.getCiudad().getId());
            dto.put("ciudadNombre", comuna.getCiudadNombre());
            dto.put("codigoPostal", comuna.getCodigoPostal());

            Map<String, Object> response = new HashMap<>();
            response.put("comuna", dto);
            response.put("status", "success");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    // ========== VILLAS ==========

    @GetMapping("/villas")
    public ResponseEntity<Map<String, Object>> getAllVillas(
            @RequestParam(required = false) Long comunaId) {
        try {
            List<Villa> villas;

            if (comunaId != null) {
                villas = villaRepository.findActiveByComunaId(comunaId);
            } else {
                villas = villaRepository.findByActivoTrue();
            }

            List<Map<String, Object>> villasDto = villas.stream().map(villa -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", villa.getId());
                dto.put("nombre", villa.getNombre());
                dto.put("direccion", villa.getDireccion());
                dto.put("comunaId", villa.getComuna().getId());
                dto.put("comunaNombre", villa.getComunaNombre());
                dto.put("ciudadNombre", villa.getCiudadNombre());
                dto.put("sectores", villa.getSectoresList());
                dto.put("telefonoContacto", villa.getTelefonoContacto());
                dto.put("emailContacto", villa.getEmailContacto());
                return dto;
            }).collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("villas", villasDto);
            response.put("total", villasDto.size());
            response.put("status", "success");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Error al obtener villas: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @GetMapping("/villas/{id}")
    public ResponseEntity<Map<String, Object>> getVillaById(@PathVariable Long id) {
        try {
            Villa villa = villaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Villa no encontrada"));

            Map<String, Object> dto = new HashMap<>();
            dto.put("id", villa.getId());
            dto.put("nombre", villa.getNombre());
            dto.put("direccion", villa.getDireccion());
            dto.put("comunaId", villa.getComuna().getId());
            dto.put("comunaNombre", villa.getComunaNombre());
            dto.put("ciudadNombre", villa.getCiudadNombre());
            dto.put("sectores", villa.getSectoresList());
            dto.put("telefonoContacto", villa.getTelefonoContacto());
            dto.put("emailContacto", villa.getEmailContacto());
            dto.put("codigoPostal", villa.getCodigoPostal());

            Map<String, Object> response = new HashMap<>();
            response.put("villa", dto);
            response.put("status", "success");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    // ✅ OBTENER SECTORES DE UNA VILLA
    @GetMapping("/villas/{id}/sectores")
    public ResponseEntity<Map<String, Object>> getSectoresByVilla(@PathVariable Long id) {
        try {
            Villa villa = villaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Villa no encontrada"));

            List<String> sectores = villa.getSectoresList();

            Map<String, Object> response = new HashMap<>();
            response.put("villaId", villa.getId());
            response.put("villaNombre", villa.getNombre());
            response.put("sectores", sectores);
            response.put("total", sectores.size());
            response.put("status", "success");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    // ========== JERARQUÍA COMPLETA ==========

    @GetMapping("/jerarquia")
    public ResponseEntity<Map<String, Object>> getJerarquiaCompleta() {
        try {
            List<Ciudad> ciudades = ciudadRepository.findByActivoTrue();

            List<Map<String, Object>> jerarquia = ciudades.stream().map(ciudad -> {
                Map<String, Object> ciudadDto = new HashMap<>();
                ciudadDto.put("id", ciudad.getId());
                ciudadDto.put("nombre", ciudad.getNombre());
                ciudadDto.put("region", ciudad.getRegion());

                List<Comuna> comunas = comunaRepository.findActiveByCiudadId(ciudad.getId());
                List<Map<String, Object>> comunasDto = comunas.stream().map(comuna -> {
                    Map<String, Object> comunaDto = new HashMap<>();
                    comunaDto.put("id", comuna.getId());
                    comunaDto.put("nombre", comuna.getNombre());

                    List<Villa> villas = villaRepository.findActiveByComunaId(comuna.getId());
                    List<Map<String, Object>> villasDto = villas.stream().map(villa -> {
                        Map<String, Object> villaDto = new HashMap<>();
                        villaDto.put("id", villa.getId());
                        villaDto.put("nombre", villa.getNombre());
                        villaDto.put("sectores", villa.getSectoresList());
                        return villaDto;
                    }).collect(Collectors.toList());

                    comunaDto.put("villas", villasDto);
                    return comunaDto;
                }).collect(Collectors.toList());

                ciudadDto.put("comunas", comunasDto);
                return ciudadDto;
            }).collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("jerarquia", jerarquia);
            response.put("status", "success");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Error al obtener jerarquía: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    // En GeografiaController.java

    @GetMapping("/registro")
    public ResponseEntity<Map<String, Object>> getDataForRegistro() {
        try {
            // Obtener todo lo necesario para el registro en una sola llamada
            List<Ciudad> ciudades = ciudadRepository.findByActivoTrue();

            Map<String, Object> registroData = new HashMap<>();
            registroData.put("ciudades", ciudades.stream().map(ciudad -> {
                Map<String, Object> ciudadDto = new HashMap<>();
                ciudadDto.put("id", ciudad.getId());
                ciudadDto.put("nombre", ciudad.getNombre());
                return ciudadDto;
            }).collect(Collectors.toList()));

            registroData.put("status", "success");
            registroData.put("message", "Seleccione una ciudad para continuar");

            return ResponseEntity.ok(registroData);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Error al obtener datos de registro: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}