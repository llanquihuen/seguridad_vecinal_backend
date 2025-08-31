package cl.seguridad.vecinal.service;

import cl.seguridad.vecinal.dao.UsuarioRepository;
import cl.seguridad.vecinal.modelo.Usuario;
import cl.seguridad.vecinal.modelo.Role;
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

@Service
@Transactional
public class UserService {

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ========== MÉTODOS EXISTENTES (mantenemos tu código actual) ==========

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

    // ========== NUEVOS MÉTODOS PARA EL CRUD DEL DASHBOARD ==========

    // Obtener usuarios con paginación
    public Page<Usuario> getUsersPaginated(Pageable pageable) {
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

    // Crear nuevo usuario desde el dashboard
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
        usuario.setRole(request.getRole() != null ? request.getRole() : Role.USER);
        usuario.setFechaRegistro(LocalDate.now());
        usuario.setEstadoCuenta(true);
        usuario.setVerificado(request.getRole() == Role.ADMIN ? true : false); // Los admin se verifican automáticamente

        return usuarioRepository.save(usuario);
    }

    // Actualizar usuario
    public Usuario updateUser(Integer id, UserUpdateRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

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

        // Actualizar campos solo si vienen en el request
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
        if (request.getRole() != null) {
            usuario.setRole(request.getRole());
        }

        // Solo actualizar password si se proporciona una nueva
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return usuarioRepository.save(usuario);
    }

    // Cambiar estado de verificación
    public Usuario toggleVerification(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setVerificado(!usuario.isVerificado());
        return usuarioRepository.save(usuario);
    }

    // Cambiar rol de usuario
    public Usuario changeUserRole(Integer id, Role newRole) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setRole(newRole);
        return usuarioRepository.save(usuario);
    }

    // Cambiar estado de cuenta
    public Usuario toggleAccountStatus(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setEstadoCuenta(!usuario.isEstadoCuenta());
        return usuarioRepository.save(usuario);
    }

    // Desactivar usuario (soft delete)
    public void deactivateUser(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setEstadoCuenta(false);
        usuarioRepository.save(usuario);
    }

    // Eliminar usuario permanentemente
    public void deleteUser(Integer id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado");
        }
        usuarioRepository.deleteById(id);
    }

    // Obtener estadísticas de usuarios para el dashboard
    public UserStats getUserStats() {
        List<Usuario> allUsers = usuarioRepository.findAll();

        long totalUsers = allUsers.size();
        long activeUsers = allUsers.stream().filter(Usuario::isEstadoCuenta).count();
        long verifiedUsers = allUsers.stream().filter(Usuario::isVerificado).count();
        long adminUsers = allUsers.stream().filter(u -> u.getRole() == Role.ADMIN).count();
        long pendingUsers = totalUsers - verifiedUsers;

        return new UserStats(totalUsers, activeUsers, verifiedUsers, adminUsers, pendingUsers);
    }

    // Clase interna para estadísticas
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

        // Getters para usar desde el controller
        public long getTotal() { return total; }
        public long getActive() { return active; }
        public long getVerified() { return verified; }
        public long getAdmins() { return admins; }
        public long getPending() { return pending; }
    }
}