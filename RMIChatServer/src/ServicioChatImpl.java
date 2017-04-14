import java.util.*;
import java.rmi.*;
import java.rmi.server.*;

class ServicioChatImpl extends UnicastRemoteObject implements ServicioChat {
    List<Cliente> registrados;
    List<Cliente> activos;
    List<Grupo> grupos;

    ServicioChatImpl() throws RemoteException {
        registrados = new LinkedList<Cliente>();
        activos = new LinkedList<Cliente>();
        grupos = new LinkedList<Grupo>();
        load();
    }

    private void load() {
        // Aqui va la carga de los json de los usuarios y grupos registradoss       
    }
    private void save(){
        // Aqui va el guardado de las listas de registrados y grupos a json
    }


    /*
    public void alta(Cliente c) throws RemoteException {
        l.add(c);
    }
    public void baja(Cliente c) throws RemoteException {
        l.remove(l.indexOf(c));
    }
    public void envio(Cliente esc, String apodo, String m)
        throws RemoteException {
        for (Cliente c: l) 
        if (!c.equals(esc))
            c.notificacion(apodo, m);
    }
    */

    public boolean login(Cliente cl) throws RemoteException {
        for(Cliente c : registrados){
            if (c.getUsername().equals(cl.getUsername())){
                if(c.getPassword().equals(cl.getPassword())){
                    activos.add(cl);
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }
    public boolean addCliente(Cliente cl) throws RemoteException {
        for(Cliente c : registrados){
            if (c.getUsername().equals(cl.getUsername())){
                return false;
            }
        }
        registrados.add(cl);
        activos.add(cl);
        return true;
    }
}
