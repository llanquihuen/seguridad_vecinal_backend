package cl.seguridad.vecinal.modelo.dto;

public class LoginRequest {
    private String email;
    private String password;

    public String getEmail() {
        return email;
    }

    // Error estaba aquí - tenías setMail en lugar de setEmail
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}