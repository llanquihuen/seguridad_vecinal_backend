package cl.seguridad.vecinal.service;

import cl.seguridad.vecinal.dao.UsuarioRepository;
import cl.seguridad.vecinal.dao.VillaRepository;
import cl.seguridad.vecinal.modelo.Usuario;
import cl.seguridad.vecinal.modelo.Role;
import cl.seguridad.vecinal.modelo.Villa;
import cl.seguridad.vecinal.modelo.dto.UserCreateRequest;
import cl.seguridad.vecinal.modelo.dto.UserUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private VillaRepository villaRepository;

    // ========== MÉTODOS EXISTENTES ==========

    public Usuario saveUser(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> getUserByRut(String rut) {
        return usuarioRepository.findByRut(rut);
    }

    public List<Usuario> getAllUsers() {
        return usuarioRepository.findAll();
    }

    public boolean checkRut(String rut) {
        return usuarioRepository.existsUsuarioByRut(rut);
    }

    // ========== MÉTODOS CON FILTRO DE VILLA ==========

    // ✅ Obtener usuarios con paginación (con filtro opcional de villa)
    public Page<Usuario> getUsersPaginated(Pageable pageable, Long villaId) {
        if (villaId != null) {
            return usuarioRepository.findByVillaId(villaId, pageable);
        }
        return usuarioRepository.findAll(pageable);
    }

    // Obtener usuario por ID
    public Optional<Usuario> getUserById(Integer id) {
        return usuarioRepository.findById(id);
    }

    // Obtener usuario por email
    public Optional<Usuario> getUserByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    // Verificar si existe usuario por email
    public boolean checkEmail(String email) {
        return usuarioRepository.existsUsuarioByEmail(email);
    }

    // ✅ Crear nuevo usuario desde el dashboard
    public Usuario createUser(UserCreateRequest request) {
        // Validaciones
        if (usuarioRepository.existsUsuarioByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        if (usuarioRepository.existsUsuarioByRut(request.getRut())) {
            throw new RuntimeException("El RUT ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setEmail(request.getEmail());
        usuario.setRut(request.getRut());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setDireccion(request.getDireccion());
        usuario.setLatitud(request.getLatitud());
        usuario.setLongitud(request.getLongitud());
        usuario.setRole(request.getRole() != null ? request.getRole() : Role.VECINO);

        // ✅ ASIGNAR VILLA SI SE PROPORCIONA
        if (request.getVillaId() != null) {
            Villa villa = villaRepository.findById(request.getVillaId())
                    .orElseThrow(() -> new RuntimeException("Villa no encontrada con ID: " + request.getVillaId()));
            usuario.setVilla(villa);
        }

        usuario.setSector(request.getSector());
        usuario.setFechaRegistro(LocalDate.now());
        usuario.setEstadoCuenta(true);
        usuario.setVerificado(request.getRole() == Role.SUPER_ADMIN || request.getRole() == Role.ADMIN_VILLA);

        return usuarioRepository.save(usuario);
    }

    // ✅ Actualizar usuario (con validación de permisos)
    public Usuario updateUser(Integer id, UserUpdateRequest request, Usuario currentUser) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // ✅ VALIDACIÓN: ADMIN_VILLA solo puede editar usuarios de su villa
        if (currentUser.getRole() == Role.ADMIN_VILLA) {
            if (!usuario.getVillaId().equals(currentUser.getVillaId())) {
                throw new RuntimeException("No tienes permisos para editar usuarios de otra villa");
            }
        }

        // Verificar si el email ya existe (excepto el propio usuario)
        if (request.getEmail() != null && !request.getEmail().equals(usuario.getEmail())) {
            if (usuarioRepository.existsUsuarioByEmail(request.getEmail())) {
                throw new RuntimeException("El email ya está registrado");
            }
            usuario.setEmail(request.getEmail());
        }

        // Verificar si el RUT ya existe (excepto el propio usuario)
        if (request.getRut() != null && !request.getRut().equals(usuario.getRut())) {
            if (usuarioRepository.existsUsuarioByRut(request.getRut())) {
                throw new RuntimeException("El RUT ya está registrado");
            }
            usuario.setRut(request.getRut());
        }

        // Actualizar campos
        if (request.getNombre() != null && !request.getNombre().trim().isEmpty()) {
            usuario.setNombre(request.getNombre());
        }
        if (request.getApellido() != null && !request.getApellido().trim().isEmpty()) {
            usuario.setApellido(request.getApellido());
        }
        if (request.getDireccion() != null) {
            usuario.setDireccion(request.getDireccion());
        }
        if (request.getLatitud() != null) {
            usuario.setLatitud(request.getLatitud());
        }
        if (request.getLongitud() != null) {
            usuario.setLongitud(request.getLongitud());
        }
        if (request.getSector() != null) {
            usuario.setSector(request.getSector());
        }

        // ✅ SOLO SUPER_ADMIN PUEDE CAMBIAR ROLES
        if (request.getRole() != null && !request.getRole().equals(usuario.getRole())) {
            if (currentUser.getRole() != Role.SUPER_ADMIN) {
                throw new RuntimeException("Solo SUPER_ADMIN puede cambiar roles de usuario");
            }
            usuario.setRole(request.getRole());
        }

        // Actualizar password solo si se proporciona
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return usuarioRepository.save(usuario);
    }

    // ✅ Toggle verificación (con asignación de sector)
    public Usuario toggleVerification(Integer id, String sector, Usuario currentUser) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // ✅ VALIDACIÓN: ADMIN_VILLA solo puede verificar usuarios de su villa
        if (currentUser.getRole() == Role.ADMIN_VILLA) {
            if (!usuario.getVillaId().equals(currentUser.getVillaId())) {
                throw new RuntimeException("No tienes permisos para verificar usuarios de otra villa");
            }
        }

        usuario.setVerificado(!usuario.isVerificado());

        // Si se está verificando y se proporciona sector, asignarlo
        if (usuario.isVerificado() && sector != null && !sector.trim().isEmpty()) {
            usuario.setSector(sector.trim());
        }

        return usuarioRepository.save(usuario);
    }

    // Toggle estado de cuenta
    public Usuario toggleAccountStatus(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setEstadoCuenta(!usuario.isEstadoCuenta());
        return usuarioRepository.save(usuario);
    }

    // ✅ Cambiar rol (solo SUPER_ADMIN)
    public Usuario changeUserRole(Integer id, Role newRole, Usuario currentUser) {
        if (currentUser.getRole() != Role.SUPER_ADMIN) {
            throw new RuntimeException("Solo SUPER_ADMIN puede cambiar roles");
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setRole(newRole);

        // Si se cambia a ADMIN o SUPER_ADMIN, verificar automáticamente
        if (newRole == Role.SUPER_ADMIN || newRole == Role.ADMIN_VILLA) {
            usuario.setVerificado(true);
        }

        return usuarioRepository.save(usuario);
    }

    // Desactivar usuario (soft delete)
    public void deactivateUser(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setEstadoCuenta(false);
        usuarioRepository.save(usuario);
    }

    // ✅ Eliminar usuario (con validación)
    public void deleteUser(Integer id, Usuario currentUser) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // ✅ ADMIN_VILLA solo puede eliminar usuarios de su villa
        if (currentUser.getRole() == Role.ADMIN_VILLA) {
            if (!usuario.getVillaId().equals(currentUser.getVillaId())) {
                throw new RuntimeException("No tienes permisos para eliminar usuarios de otra villa");
            }
        }

        usuarioRepository.deleteById(id);
    }

    // ✅ Búsqueda con filtro de villa
    public Page<Usuario> searchUsers(String query, Pageable pageable, Long villaId) {
        return usuarioRepository.searchByTextAndVilla(query, villaId, pageable);
    }

    // ✅ Estadísticas por villa
    public UserStats getUserStats(Long villaId) {
        long total = villaId != null ? usuarioRepository.countByVillaId(villaId) : usuarioRepository.count();
        long active = villaId != null ? usuarioRepository.countByVillaIdAndEstadoCuentaTrue(villaId)
                : usuarioRepository.findAll().stream().filter(Usuario::isEstadoCuenta).count();
        long verified = villaId != null ? usuarioRepository.countByVillaIdAndVerificadoTrue(villaId)
                : usuarioRepository.findAll().stream().filter(Usuario::isVerificado).count();
        long admins = villaId != null ?
                usuarioRepository.countByVillaIdAndRole(villaId, Role.ADMIN_VILLA) +
                        usuarioRepository.countByVillaIdAndRole(villaId, Role.SUPER_ADMIN)
                : usuarioRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.ADMIN_VILLA || u.getRole() == Role.SUPER_ADMIN)
                .count();
        long pending = total - verified;

        return new UserStats(total, active, verified, admins, pending);
    }

    // Clase para estadísticas
    public static class UserStats {
        public final long total;
        public final long active;
        public final long verified;
        public final long admins;
        public final long pending;

        public UserStats(long total, long active, long verified, long admins, long pending) {
            this.total = total;
            this.active = active;
            this.verified = verified;
            this.admins = admins;
            this.pending = pending;
        }
    }

    // ✅ Obtener sectores de una villa
    public List<String> getSectoresByVilla(Long villaId) {
        if (villaId == null) {
            return usuarioRepository.findDistinctSectores();
        }
        return usuarioRepository.findDistinctSectoresByVillaId(villaId);
    }

    // Asignar sector a usuario
    public Usuario asignarSector(Integer userId, String sector) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setSector(sector);
        return usuarioRepository.save(usuario);
    }

    // Obtener usuarios por sector con paginación
    public Page<Usuario> getUsersBySector(String sector, Pageable pageable) {
        return usuarioRepository.findBySector(sector, pageable);
    }

    // ✅ Contar usuarios por villa
    public long countAllUsers() {
        return usuarioRepository.count();
    }

    public long countVerifiedUsers() {
        return usuarioRepository.findAll().stream().filter(Usuario::isVerificado).count();
    }
}