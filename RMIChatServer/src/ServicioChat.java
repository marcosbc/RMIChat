
import java.rmi.*;

interface ServicioChat extends Remote {
    /*
    void alta(Cliente c) throws RemoteException;
    */
    void send(String msg, Sesion s) throws RemoteException;
    boolean addUsuario(Sesion s) throws RemoteException;
    boolean login(Sesion s) throws RemoteException;
    void logout(Sesion s) throws RemoteException;
    void echo(String msg, Sesion s) throws RemoteException;
}
