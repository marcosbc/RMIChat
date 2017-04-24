
import java.rmi.*;

interface ServicioChat extends Remote {
    boolean joinGroup(String group, Cliente c) throws RemoteException;
    boolean leaveGroup(String group, Cliente c) throws RemoteException;
    String[] listGroups() throws RemoteException;
    String[] listGroups(Cliente c) throws RemoteException;
    boolean sendMessage(String dest, String msg, Cliente c) throws RemoteException;
    boolean addUsuario(Cliente c) throws RemoteException;
    boolean login(Cliente c) throws RemoteException;
    void logout(Cliente c) throws RemoteException;
    boolean ping(Cliente c) throws RemoteException;
}
