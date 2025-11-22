package cl.seguridad.vecinal.modelo.dto;

public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String email;           // Campo principal
    private String username;        // ✅ NUEVO: Alias para compatibilidad con Android
    private String role;
    private Boolean isAdmin;
    private String sector;
    private Integer userId;
    private Long villaId;
    private String villaNombre;
    private String nombre;
    private String apellido;

    // Constructor vacío
    public AuthResponse() {}

    public AuthResponse(String accessToken, String refreshToken, String username) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.username = username;
    }

    public AuthResponse(String accessToken, String refreshToken, String username, String role, Boolean isAdmin) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.username = username;
        this.role = role;
        this.isAdmin = isAdmin;
    }
    // ✅ Constructor COMPLETO (actualizado)
    public AuthResponse(String accessToken, String refreshToken, String email,
                        String role, Boolean isAdmin, String sector, Integer userId,
                        Long villaId, String villaNombre, String nombre, String apellido) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.email = email;
        this.username = email;      // ✅ NUEVO: Asignar el mismo valor que email
        this.role = role;
        this.isAdmin = isAdmin;
        this.sector = sector;
        this.userId = userId;
        this.villaId = villaId;
        this.villaNombre = villaNombre;
        this.nombre = nombre;
        this.apellido = apellido;
    }


    // Getters y Setters
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        this.username = email;  // ✅ Mantener sincronizado
    }

    // ✅ NUEVO: Getter para username (compatibilidad Android)
    public String getUsername() {
        return username != null ? username : email;  // Fallback a email si username es null
    }

    // ✅ NUEVO: Setter para username
    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Long getVillaId() {
        return villaId;
    }

    public void setVillaId(Long villaId) {
        this.villaId = villaId;
    }

    public String getVillaNombre() {
        return villaNombre;
    }

    public void setVillaNombre(String villaNombre) {
        this.villaNombre = villaNombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }
}