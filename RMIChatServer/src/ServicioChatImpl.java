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

    public void joinGroup(String group, Cliente c) throws RemoteException {
        // TODO: Implementar
    }

    public void leaveGroup(String group, Cliente c) throws RemoteException {
        // TODO: Implementar
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
