import java.util.*;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.rmi.*;
import java.rmi.server.*;

class ServicioChatImpl extends UnicastRemoteObject implements ServicioChat {
    List<Usuario> registrados;
    List<Cliente> activos;
    List<Grupo> grupos;

    File userFile;
    File groupFile;
    ObjectMapper JSONSerializer;

    ServicioChatImpl() throws RemoteException {
        activos = new LinkedList<Cliente>();
        load();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                save();
            }
        });
    }

    private void load() {
        userFile = new File ("./usuarios.json");
        groupFile = new File ("./grupos.json");
        JSONSerializer = new ObjectMapper();

        try {
            if (userFile.exists() && (userFile.length() != 0)) {
                registrados = JSONSerializer.readValue(userFile, new TypeReference<List<Usuario>>() {});
            } else {
                userFile.createNewFile();
                registrados = new ArrayList<Usuario>();
            }

            if (groupFile.exists() && (groupFile.length() != 0)) {
                grupos = JSONSerializer.readValue(groupFile, new TypeReference<List<Grupo>>() {});
            } else {
                groupFile.createNewFile();
                grupos = new ArrayList<Grupo>();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void save() {
        try {
            JSONSerializer.writeValue(userFile, this.registrados);
            JSONSerializer.writeValue(groupFile, this.grupos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean joinGroup(String g, Cliente c) throws RemoteException {
        if (findGroup(g)) {
            c.joinGroup(g);
            // TODO: Implementar logica de suscripcion
            return true;
        }
        return false;
    }

    public boolean leaveGroup(String g, Cliente c) throws RemoteException {
        if (findGroup(g)) {
            c.leaveGroup(g);
            // TODO: Implementar logica de suscripcion
            return true;
        }
        return false;
    }

    private boolean findGroup(String g) {
        for (int i = 0; i < grupos.size(); i++) {
            if (grupos.get(i).getName().equals(g)) {
                return true;
            }
        }
        return false;
    }

    public String[] listGroups() throws RemoteException {
        ArrayList<String> g = new ArrayList<String>();
        for (int i = 0; i < grupos.size(); i++) {
            g.add(grupos.get(i).getName());
        }
        return g.toArray(new String[g.size()]);
    }

    public void sendMessage(String dest, String msg, Cliente c) throws RemoteException {
        for (Cliente iterador: activos)
            if (!iterador.equals(c))
                // TODO: Soportar apodo
                iterador.notify(c.getUsername(), msg);
    }

    public boolean login(Cliente c) throws RemoteException {
        Usuario u = new Usuario(c.getUsername(), c.getPassword());
        if (buscarUsuario(u, registrados) == null) {
            // No se encuentra el usuario: No se ha registrado aun
            return false;
        }
        activos.add(c);
        return true;
    }

    public boolean addUsuario(Cliente c) throws RemoteException {
        Usuario u = new Usuario(c.getUsername(), c.getPassword());
        if (buscarUsuario(u, registrados) != null) {
            // Ya esta registrado
            return false;
        }
        registrados.add(u);
        activos.add(c);
        return true;
    }

    public void logout(Cliente c) throws RemoteException {
        activos.remove(activos.indexOf(c));
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
    public void echo(String msg, Cliente c) throws RemoteException {
        System.out.println(msg);
        c.echo(msg);
    }
}
