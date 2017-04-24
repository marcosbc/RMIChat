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
    // Constantes utilizadas para notificaciones a clientes
    public static final int NOTIFICATION_GROUPMSG = 0;
    public static final int NOTIFICATION_PRIVATEMSG = 1;
    public static final int NOTIFICATION_USERJOIN = 2;
    public static final int NOTIFICATION_USERLEAVE = 3;
    public static final int NOTIFICATION_NOSUCHGROUP = 4;
    public static final int NOTIFICATION_ALREADYMEMBERSHIP= 5;
    public static final int NOTIFICATION_NOMEMBERSHIP = 6;
    public static final int NOTIFICATION_NOSUCHUSER = 7;
    public static final int NOTIFICATION_NOVALIDMSG = 8;
    public static final int NOTIFICATION_GROUPEMPTY = 9;
    public static final int NOTIFICATION_NOMSGTYPE = 10;
    public static final int NOTIFICATION_NOSELFMSG = 11;

    // Listas de usuarios, grupos y asociaciones clientes-usuario
    List<Usuario> registrados;
    List<Grupo> grupos;
    Map<Cliente, Usuario> sessions;

    // Ficheros para persistencia de usuarios y grupos
    File userFile;
    File groupFile;
    ObjectMapper JSONSerializer;

    // Constructor de clase
    ServicioChatImpl() throws RemoteException {
        sessions = new HashMap<Cliente, Usuario>();
        load();
        // Hilo que se asegura que los clientes activos no se han desconectado
        new CheckSessionsThread(sessions, this).start();
        // Hilo que guarda usuarios activos en caso de cierre abrupto del programa
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                save();
            }
        });
    }

    // Cargar usuarios y grupos, durante arranque, para persistencia
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

    // Guardar usuarios y grupos, al final de ejecucion, para persistencia
    private void save() {
        try {
            JSONSerializer.writeValue(userFile, this.registrados);
            JSONSerializer.writeValue(groupFile, this.grupos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Unión a un grupo por un usuario
    public boolean joinGroup(String name, Cliente c) throws RemoteException {
        Grupo g = findGroup(name);
        if (g != null) {
            if (g.add(sessions.get(c))) {
                notifyGroup(NOTIFICATION_USERJOIN, c, name, null);
                return true;
            }
            else {
                // Notificación en caso de que el usuario ya perteneciese al grupo
                notifyPrivate(NOTIFICATION_ALREADYMEMBERSHIP, c, c.getUsername(), null);
            }
        }
        else {
            // Notificación en caso de que el grupo no exista
            notifyPrivate(NOTIFICATION_NOSUCHGROUP, c, c.getUsername(), null);
        }
        return false;
    }

    // Salida de un grupo por un usuario
    public boolean leaveGroup(String name, Cliente c) throws RemoteException {
        Grupo g = findGroup(name);
        if (g != null) {
            // Nota: Solo se notificará si el usuario pertenece al grupo
            notifyGroup(NOTIFICATION_USERLEAVE, c, name, null);
            if (g.remove(sessions.get(c))) {
                return true;
            }
            else {
                // Notificación en caso de que el usuario no perteneciese al grupo
                notifyPrivate(NOTIFICATION_NOMEMBERSHIP, c, c.getUsername(), null);
            }
        } else {
            // Notificación en caso de que el grupo no exista
            notifyPrivate(NOTIFICATION_NOSUCHGROUP, c, c.getUsername(), null);
        }
        return false;
    }

    // Buscar un grupo por su nombre
    private Grupo findGroup(String g) {
        for (int i = 0; i < grupos.size(); i++) {
            if (grupos.get(i).getName().equals(g)) {
                return grupos.get(i);
            }
        }
        return null;
    }

    // Listado de todos los grupos
    public String[] listGroups() throws RemoteException {
        ArrayList<String> g = new ArrayList<String>();
        for (int i = 0; i < grupos.size(); i++) {
            g.add(grupos.get(i).getName());
        }
        return g.toArray(new String[g.size()]);
    }

    // Listado de grupos a los que pertenezca un usuario
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

    // Envío de un mensaje de chat por un usuario, a un destino
    public boolean sendMessage(String dest, String msg, Cliente c) throws RemoteException {
        boolean success = false;
        // Validar parámetros
        if (dest != null && !dest.equals("") && msg != null && !msg.equals("") && c != null && dest.length() > 2) {
            // Caso de mensaje privado
            if (dest.substring(0, 1).equals("@")) {
                if (dest.equals("@" + c.getUsername())) {
                    notifyPrivate(NOTIFICATION_NOSELFMSG, c, c.getUsername(), null);
                }
                else {
                    String username = dest.substring(1, dest.length());
                    success = notifyPrivate(NOTIFICATION_PRIVATEMSG, c, username, msg);
                    if (! success) {
                        // Notificación en caso de que no se haya encontrado el usuario
                        notifyPrivate(NOTIFICATION_NOSUCHUSER, c, c.getUsername(), null);
                    }
                }
            }
            else if (dest.substring(0, 1).equals("#")) {
                Usuario u = sessions.get(c);
                Grupo g = findGroup(dest);
                if (g == null) {
                    // Notificación en el caso de que el grupo no exista
                    notifyPrivate(NOTIFICATION_NOSUCHGROUP, c, c.getUsername(), null);
                }
                else if (!g.hasMember(sessions.get(c))) {
                    // Notificación en el caso de que el usuario no pertenezca al grupo
                    notifyPrivate(NOTIFICATION_NOMEMBERSHIP, c, c.getUsername(), null);
                }
                else {
                    success = notifyGroup(NOTIFICATION_GROUPMSG, c, dest, msg);
                    if (! success) {
                        // Notificación en caso de que el grupo esté vacío
                        notifyPrivate(NOTIFICATION_GROUPEMPTY, c, c.getUsername(), null);
                    }
                }
            }
            else {
                // Notificación en caso de tipo de mensaje no conocido
                notifyPrivate(NOTIFICATION_NOMSGTYPE, c, c.getUsername(), null);
            }
        }
        else {
            // Notificación en el caso de error de validación de parámetros
            notifyPrivate(NOTIFICATION_NOVALIDMSG, c, c.getUsername(), null);
        }
        return success;
    }

    // Notificación a un usuario específico
    private boolean notifyPrivate(int notificationType, Cliente c, String dest, String msg) throws RemoteException {
        boolean success = false;
        Usuario orig = sessions.get(c);
        // Buscar cliente(s) destino (a partir del nombre de usuario)
        if (orig != null) {
            for (Map.Entry<Cliente, Usuario> entry: sessions.entrySet()) {
                // Comprobar que coincide con nombre de usuario
                if (dest.equals(entry.getValue().getUsername())) {
                    success = true;
                    entry.getKey().notify(notificationType, orig.getUsername(), null, msg);
                }
            }
        }
        return success;
    }

    // Notificación a los usuarios de un grupo
    private boolean notifyGroup(int notificationType, Cliente c, String dest, String msg) throws RemoteException {
        boolean success = false;
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
                        entry.getKey().notify(notificationType, orig.getUsername(), dest, msg);
                    }
                }
            }
        }
        return success;
    }

    // Inicio de sesión de un usuario
    public boolean login(Cliente c) throws RemoteException {
        Usuario u = new Usuario(c.getUsername(), c.getPassword());
        if (buscarUsuario(u, registrados) == null) {
            // No se encuentra el usuario: No se ha registrado aun
            return false;
        }
        sessions.put(c, u);
        return true;
    }

    // Creación de cuenta de un usuario
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

    // Cierre de sesión de un usuario
    public void logout(Cliente c) throws RemoteException {
        sessions.remove(c);
    }

    // Buscar un usuario, por clase de tipo Usuario, en una lista de usuarios
    private Usuario buscarUsuario(Usuario u, List<Usuario> listaUsuarios) {
        for(Usuario iteradorUsuario: listaUsuarios) {
            if (iteradorUsuario.equals(u)) {
                return iteradorUsuario;
            }
        }
        return null;
    }

    // Buscar un usuario, por nombre de usuario, en una lista de usuarios
    private Usuario buscarUsuario(String username, List<Usuario> listaUsuarios) {
        for(Usuario iteradorUsuario: listaUsuarios) {
            if (username.equals("@" + iteradorUsuario.getUsername())) {
                return iteradorUsuario;
            }
        }
        return null;
    }

    // Simple método para probar conectividad con el servidor, de parte del cliente
    public boolean ping(Cliente c) throws RemoteException {
        if (sessions.get(c) == null) {
            // In case the user was logged out, ignore requests
            return false;
        }
        return true;
    }
}
