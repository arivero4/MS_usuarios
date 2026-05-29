package usuarios.infrastructure.adapter.in.web.dto;

public class LoginResponse {

    private String token;
    private String tipo;
    private Long expiracion;

    public LoginResponse(String token, Long expiracion) {
        this.token = token;
        this.tipo = "Bearer";
        this.expiracion = expiracion;
    }

    public String getToken() { return token; }
    public String getTipo() { return tipo; }
    public Long getExpiracion() { return expiracion; }
}
