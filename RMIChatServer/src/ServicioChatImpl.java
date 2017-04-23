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
    List<Grupo> grupos;
    Map<Cliente, Usuario> sessions;

    File userFile;
    File groupFile;
    ObjectMapper JSONSerializer;

    ServicioChatImpl() throws RemoteException {
        sessions = new HashMap<Cliente, Usuario>();
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

    public boolean joinGroup(String name, Cliente c) throws RemoteException {
        Grupo g = findGroup(name);
        if (g != null) {
            if (g.add(sessions.get(c))) {
                return true;
            }
        }
        return false;
    }

    public boolean leaveGroup(String name, Cliente c) throws RemoteException {
        Grupo g = findGroup(name);
        if (g != null) {
            if (g.remove(sessions.get(c))) {
                return true;
            }
        }
        return false;
    }

    private Grupo findGroup(String g) {
        for (int i = 0; i < grupos.size(); i++) {
            if (grupos.get(i).getName().equals(g)) {
                return grupos.get(i);
            }
        }
        return null;
    }

    public String[] listGroups() throws RemoteException {
        ArrayList<String> g = new ArrayList<String>();
        for (int i = 0; i < grupos.size(); i++) {
            g.add(grupos.get(i).getName());
        }
        return g.toArray(new String[g.size()]);
    }

    public String[] listGroups(Cliente c) throws RemoteException {
        ArrayList<String> g = new ArrayList<String>();
        Usuario u = sessions.get(c);
        // Recorrer cada grupo y comprobar si es miembro
        for (int i = 0; i < grupos.size(); i++) {
            if (grupos.get(i).hasMember(u)) {
                g.add(grupos.get(i).getName());
            }
        }
        return g.toArray(new String[g.size()]);
    }

    public boolean sendMessage(String dest, String msg, Cliente c) throws RemoteException {
        boolean success = false;
        // Validar parametros
        if (dest == null || dest.equals("") || msg == null || msg.equals("") || c == null || dest.length() <= 2) {
            // Los parametros están vacíos o son incorrectos
        }
        // Caso de mensaje privado
        else if (dest.substring(0, 1).equals("@")) {
            // Obtener usuario origen
            Usuario orig = sessions.get(c);
            if (orig != null) {
                // Buscar cliente destino (a partir del nombre de usuario)
                for (Map.Entry<Cliente, Usuario> entry: sessions.entrySet()) {
                    if (dest.equals("@" + entry.getValue().getUsername())) {
                        success = true;
                        entry.getKey().notify(orig.getUsername(), null, msg);
                    }
                }
            }
        }
        else if (dest.substring(0, 1).equals("#")) {
            // Obtener usuario origen
            Usuario orig = sessions.get(c);
            Grupo g = findGroup(dest);
            // Comprobar que el grupo es válido
            // Nos aseguramos que el usuario que envía el mensaje está en el grupo
            if (orig != null && g != null && g.hasMember(orig)) {
                List<Usuario> members = g.getMembers();
                // Proceder al envío del mensaje a cada usuario del grupo
                for (int i = 0; i < members.size(); i++) {
                    // Buscar cliente destino (a partir de nombres de usuario)
                    for (Map.Entry<Cliente, Usuario> entry: sessions.entrySet()) {
                        // Comprobar que coincide con un miembro del grupo
                        // No debe coincidir con el miembro que envía el mensaje
                        if (entry.getValue().equals(members.get(i)) && !entry.getValue().equals(orig)) {
                            success = true;
                            entry.getKey().notify(orig.getUsername(), dest, msg);
                        }
                    }
                }
            }
        }
        return success;
    }

    public boolean login(Cliente c) throws RemoteException {
        Usuario u = new Usuario(c.getUsername(), c.getPassword());
        if (buscarUsuario(u, registrados) == null) {
            // No se encuentra el usuario: No se ha registrado aun
            return false;
        }
        sessions.put(c, u);
        return true;
    }

    public boolean addUsuario(Cliente c) throws RemoteException {
        Usuario u = new Usuario(c.getUsername(), c.getPassword());
        if (buscarUsuario(u, registrados) != null) {
            // Ya esta registrado
            return false;
        }
        registrados.add(u);
        sessions.put(c, u);
        return true;
    }

    public void logout(Cliente c) throws RemoteException {
        sessions.remove(c);
    }

    private Usuario buscarUsuario(Usuario u, List<Usuario> listaUsuarios) {
        for(Usuario iteradorUsuario: listaUsuarios) {
            if (iteradorUsuario.equals(u)) {
                return iteradorUsuario;
            }
        }
        return null;
    }

    private Usuario buscarUsuario(String username, List<Usuario> listaUsuarios) {
        for(Usuario iteradorUsuario: listaUsuarios) {
            if (username.equals("@" + iteradorUsuario.getUsername())) {
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
