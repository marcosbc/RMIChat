
import java.rmi.*;

interface ServicioChat extends Remote {
    /*
    void alta(Cliente c) throws RemoteException;
    void baja(Cliente c) throws RemoteException;
    */
    void envio(Sesion s, String apodo, String m) throws RemoteException;
    boolean addUsuario(Sesion s) throws RemoteException;
    boolean login(Sesion s) throws RemoteException;
    void echo(String msg, Sesion s) throws RemoteException;
}
