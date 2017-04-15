
import java.rmi.*;

class Usuario {
    private String username;
    private String password;
    public Usuario(String username, String password) {
        this.username = username;
        this.password = password;
    }
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public boolean equals(Usuario u) {
        if (username.equals(u.getUsername()) && password.equals(u.getPassword())) {
            return true;
        }
        return false;
    }
}
