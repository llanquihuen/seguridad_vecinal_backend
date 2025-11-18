package cl.seguridad.vecinal.modelo.dto;

public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private String username;
    private String role;
    private Boolean isAdmin;
    private String sector;
    private Integer userId;
    private Long villaId;
    private String villaNombre;
    private String nombre;
    private String apellido;

    public AuthResponse() {}

    // Constructor original (para compatibilidad)
    public AuthResponse(String accessToken, String refreshToken, String username) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.username = username;
    }

    // Constructor con role e isAdmin (MANTENER para Google y Refresh)
    public AuthResponse(String accessToken, String refreshToken, String username, String role, Boolean isAdmin) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.username = username;
        this.role = role;
        this.isAdmin = isAdmin;
        this.sector = null;
        this.userId = null;
    }

    // Constructor COMPLETO (para login normal con sector y userId)
    public AuthResponse(String accessToken, String refreshToken, String username, String role, Boolean isAdmin, String sector, Integer userId) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.username = username;
        this.role = role;
        this.isAdmin = isAdmin;
        this.sector = sector;
        this.userId = userId;
    }

    // Getters y Setters
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Boolean getIsAdmin() { return isAdmin; }
    public void setIsAdmin(Boolean isAdmin) { this.isAdmin = isAdmin; }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Long getVillaId() { return villaId; }
    public void setVillaId(Long villaId) { this.villaId = villaId; }

    public String getVillaNombre() { return villaNombre; }
    public void setVillaNombre(String villaNombre) { this.villaNombre = villaNombre; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    // ✅ Constructor COMPLETO para el dashboard web (con villa)
    public AuthResponse(String accessToken, String refreshToken, String username,
                        String role, Boolean isAdmin, String sector, Integer userId,
                        Long villaId, String villaNombre, String nombre, String apellido) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.username = username;
        this.role = role;
        this.isAdmin = isAdmin;
        this.sector = sector;
        this.userId = userId;
        this.villaId = villaId;
        this.villaNombre = villaNombre;
        this.nombre = nombre;
        this.apellido = apellido;
    }
}