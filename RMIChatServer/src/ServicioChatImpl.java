import java.util.*;
import java.rmi.*;
import java.rmi.server.*;

class ServicioChatImpl extends UnicastRemoteObject implements ServicioChat {
    List<Usuario> registrados;
    List<Sesion> activos;
    List<Grupo> grupos;

    ServicioChatImpl() throws RemoteException {
        registrados = new LinkedList<Usuario>();
        activos = new LinkedList<Sesion>();
        grupos = new LinkedList<Grupo>();
        load();
    }

    private void load() {
        // Aqui va la carga de los json de los usuarios y grupos registrados
    }

    private void save(){
        // Aqui va el guardado de las listas de registrados y grupos a json
    }

    /*
    public void alta(Cliente c) throws RemoteException {
        l.add(c);
    }

    */

    public void send(String msg, Sesion s) throws RemoteException {
        for (Sesion iterador: activos)
            if (!iterador.equals(s))
                // TODO: Soportar apodo
                iterador.notify(s.getUsername(), msg);
    }

    public boolean login(Sesion s) throws RemoteException {
        Usuario u = new Usuario(s.getUsername(), s.getPassword());
        if (buscarUsuario(u, registrados) == null) {
            // No se encuentra el usuario: No se ha registrado aun
            return false;
        }
        activos.add(s);
        return true;
    }

    public boolean addUsuario(Sesion s) throws RemoteException {
        Usuario u = new Usuario(s.getUsername(), s.getPassword());
        if (buscarUsuario(u, registrados) != null) {
            // Ya esta registrado
            return false;
        }
        registrados.add(u);
        activos.add(s);
        return true;
    }

    public void logout(Sesion s) throws RemoteException {
        activos.remove(activos.indexOf(s));
    }

    private Usuario buscarUsuario(Usuario u, List<Usuario> listaUsuarios) {
        for(Usuario iteradorUsuario: listaUsuarios) {
            if (iteradorUsuario.equals(u)) {
                return iteradorUsuario;
            }
        }
        return null;
    }

    // Simple eco, para probar conexion entre cliente y servidor
    public void echo(String msg, Sesion s) throws RemoteException {
        System.out.println(msg);
        s.echo(msg);
    }
}
