
import java.rmi.*;

interface Cliente extends Remote {
    String nombre=null;
    String contrase�a=null;
    void notificacion(String apodo, String m) throws RemoteException;
}
