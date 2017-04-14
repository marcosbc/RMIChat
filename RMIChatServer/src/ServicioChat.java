
import java.rmi.*;

interface ServicioChat extends Remote {
    /*void alta(Cliente c) throws RemoteException;
    void baja(Cliente c) throws RemoteException;
    void envio(Cliente e, String apodo, String m) throws RemoteException;*/
    boolean addCliente(Cliente c) throws RemoteException;
    boolean login(Cliente c) throws RemoteException;
}
