
import java.rmi.*;

interface Cliente extends Remote {
    String nombre=null;
    String contraseņa=null;
    void notificacion(String apodo, String m) throws RemoteException;
}
