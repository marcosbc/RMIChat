
import java.rmi.*;

interface Cliente extends Remote {
    String nombre=null;
    String contraseña=null;
    void notificacion(String apodo, String m) throws RemoteException;
}
