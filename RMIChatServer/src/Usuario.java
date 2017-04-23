
import java.rmi.*;

class Usuario {
    private String username;
    private String password;
    public Usuario() {
    }
    public Usuario(String username, String password) {
        setUsername(username);
        setPassword(password);
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getUsername() {
        return username;
    }
    public void setPassword(String password) {
        this.password = password;
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
