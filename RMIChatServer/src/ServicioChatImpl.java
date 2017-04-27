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
    public static final int NOTIFICATION_USERCONNECT = 12;
    public static final int NOTIFICATION_USERDISCONNECT = 13;

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
        Usuario u = sessions.get(c);
        if (u != null) {
            // El usuario debe haber iniciado sesión
            Grupo g = findGroup(name);
            if (g != null) {
                if (g.add(sessions.get(c))) {
                    notifyGroup(NOTIFICATION_USERJOIN, u, name, null);
                    return true;
                }
                else {
                    // Notificación en caso de que el usuario ya perteneciese al grupo
                    notifyPrivate(NOTIFICATION_ALREADYMEMBERSHIP, u, u.getUsername(), null);
                }
            }
            else {
                // Notificación en caso de que el grupo no exista
                notifyPrivate(NOTIFICATION_NOSUCHGROUP, u, u.getUsername(), null);
            }
        }
        return false;
    }

    // Salida de un grupo por un usuario
    public boolean leaveGroup(String name, Cliente c) throws RemoteException {
        Usuario u = sessions.get(c);
        if (u != null) {
            // El usuario debe haber iniciado sesión
            Grupo g = findGroup(name);
            if (g != null) {
                // Nota: Solo se notificará si el usuario pertenece al grupo
                notifyGroup(NOTIFICATION_USERLEAVE, u, name, null);
                if (g.remove(sessions.get(c))) {
                    return true;
                }
                else {
                    // Notificación en caso de que el usuario no perteneciese al grupo
                    notifyPrivate(NOTIFICATION_NOMEMBERSHIP, u, u.getUsername(), null);
                }
            } else {
                // Notificación en caso de que el grupo no exista
                notifyPrivate(NOTIFICATION_NOSUCHGROUP, u, u.getUsername(), null);
            }
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
        String[] result = null;
        Usuario u = sessions.get(c);
        if (u != null) {
            // El usuario debe haber iniciado sesión
            ArrayList<String> g = new ArrayList<String>();
            // Recorrer cada grupo y comprobar si es miembro
            for (int i = 0; i < grupos.size(); i++) {
                if (grupos.get(i).hasMember(u)) {
                    g.add(grupos.get(i).getName());
                }
            }
            result = g.toArray(new String[g.size()]);
        }
        return result;
    }

    // Listado de usuarios en un grupo
    public String[] listConnectedGroupMembers(String name, Cliente c) throws RemoteException {
        return getGroupMembers(name, c, true);
    }

    // Listado de usuarios en un grupo
    public String[] listGroupMembers(String name, Cliente c) throws RemoteException {
        return getGroupMembers(name, c, false);
    }

    // Obtener clases de usuario, para simplificar gestión de notificaciones
    private String[] getGroupMembers(String name, Cliente c, boolean connectedOnly) throws RemoteException {
        String[] result = null;
        Usuario u = sessions.get(c);
        Grupo g = findGroup(name);
        if (u != null) {
            // El usuario debe haber iniciado sesión
            if (g == null) {
                // Notificación en el caso de que no exista un grupo
                notifyPrivate(NOTIFICATION_NOSUCHGROUP, u, u.getUsername(), null);
            }
            else if (!g.hasMember(u)) {
                // Notificación en el caso de que el usuario no pertenezca al grupo
                // Así nos aseguramos que siempre tendremos usuarios con este método
                notifyPrivate(NOTIFICATION_NOMEMBERSHIP, u, u.getUsername(), null);
            }
            else {
                List<Usuario> members = g.getMembers();
                ArrayList<String> usernames = new ArrayList<String>();
                // Recorrer cada grupo y comprobar si es miembro
                for (int i = 0; i < members.size(); i++) {
                    if (!connectedOnly || isUserConnected(members.get(i))) {
                        usernames.add(members.get(i).getUsername());
                    }
                }
                result = usernames.toArray(new String[usernames.size()]);
            }
        }
        return result;
    }

    // Comprobar si un usuario está conectado o no
    private boolean isUserConnected (Usuario u) {
        // Recorrer lista de sesiones
        for (Map.Entry<Cliente, Usuario> entry: sessions.entrySet()) {
            if (u.equals(entry.getValue())) {
                return true;
            }
        }
        return false;
    }

    // Envío de un mensaje de chat por un usuario, a un destino
    public boolean sendMessage(String dest, String msg, Cliente c) throws RemoteException {
        boolean success = false;
        Usuario u = sessions.get(c);
        // Validar parámetros, y asegurarnos de que el usuario ya ha iniciado sesión
        if (u != null && dest != null && !dest.equals("") && msg != null && !msg.equals("") && c != null && dest.length() > 2) {
            // Caso de mensaje privado
            if (dest.substring(0, 1).equals("@")) {
                if (dest.equals("@" + u.getUsername())) {
                    notifyPrivate(NOTIFICATION_NOSELFMSG, u, u.getUsername(), null);
                }
                else {
                    String username = dest.substring(1, dest.length());
                    success = notifyPrivate(NOTIFICATION_PRIVATEMSG, u, username, msg);
                    if (! success) {
                        // Notificación en caso de que no se haya encontrado el usuario
                        notifyPrivate(NOTIFICATION_NOSUCHUSER, u, u.getUsername(), null);
                    }
                }
            }
            else if (dest.substring(0, 1).equals("#")) {
                Grupo g = findGroup(dest);
                if (g == null) {
                    // Notificación en el caso de que el grupo no exista
                    notifyPrivate(NOTIFICATION_NOSUCHGROUP, u, u.getUsername(), null);
                }
                else if (!g.hasMember(sessions.get(c))) {
                    // Notificación en el caso de que el usuario no pertenezca al grupo
                    notifyPrivate(NOTIFICATION_NOMEMBERSHIP, u, u.getUsername(), null);
                }
                else {
                    success = notifyGroup(NOTIFICATION_GROUPMSG, u, dest, msg);
                    if (! success) {
                        // Notificación en caso de que el grupo esté vacío
                        notifyPrivate(NOTIFICATION_GROUPEMPTY, u, u.getUsername(), null);
                    }
                }
            }
            else {
                // Notificación en caso de tipo de mensaje no conocido
                notifyPrivate(NOTIFICATION_NOMSGTYPE, u, u.getUsername(), null);
            }
        }
        else if (u != null) {
            // Notificación en el caso de error de validación de parámetros
            notifyPrivate(NOTIFICATION_NOVALIDMSG, u, u.getUsername(), null);
        }
        return success;
    }

    // Notificación a un usuario específico
    private boolean notifyPrivate(int notificationType, Usuario orig, String dest, String msg) throws RemoteException {
        boolean success = false;
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
    private boolean notifyGroup(int notificationType, Usuario orig, String dest, String msg) throws RemoteException {
        boolean success = false;
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

    // Notificación a los usuarios de todos los grupos donde esté determinado usuario
    private boolean notifyGroupsWithUser(int notificationType, Usuario orig, String msg) throws RemoteException {
        boolean success = false;
        // Comprobar que el usuario es válido
        if (orig != null) {
            // Recorrer grupos, comprobar si el usuario está en cada uno de ellos
            for (int i = 0; i < grupos.size(); i++) {
                List<Usuario> members = grupos.get(i).getMembers();
                for (int j = 0; j < members.size(); j++) {
                    if (orig.equals(members.get(j))) {
                        // Hemos encontrado un grupo en el que está el usuario
                        // Enviamos la notificación al grupo
                        notifyGroup(notificationType, orig, grupos.get(i).getName(), msg);
                        success = true;
                        break;
                    }
                }
            }
        }
        return success;
    }

    // Inicio de sesión de un usuario
    public boolean login(Cliente c) throws RemoteException {
        if (sessions.get(c) != null) {
            // Ya ha iniciado sesión anteriormente
            return false;
        }
        Usuario u = new Usuario(c.getUsername(), c.getPassword());
        if (buscarUsuario(u, registrados) == null) {
            // No se encuentra el usuario: No se ha registrado aun
            return false;
        }
        sessions.put(c, u);
        notifyGroupsWithUser(NOTIFICATION_USERCONNECT, u, null);
        return true;
    }

    // Creación de cuenta de un usuario
    public boolean addUsuario(Cliente c) throws RemoteException {
        if (sessions.get(c) != null) {
            // Ya ha iniciado sesión anteriormente
            return false;
        }
        Usuario u = new Usuario(c.getUsername(), c.getPassword());
        if (buscarUsuario(u.getUsername(), registrados) != null) {
            // Hay un usuario registrado con el mismo nombre de usuario
            return false;
        }
        registrados.add(u);
        sessions.put(c, u);
        return true;
    }

    // Cierre de sesión de un usuario
    public void logout(Cliente c) throws RemoteException {
        Usuario u = sessions.get(c);
        if (u != null) {
            notifyGroupsWithUser(NOTIFICATION_USERDISCONNECT, u, null);
            sessions.remove(c);
        }
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
            if (username.equals(iteradorUsuario.getUsername())) {
                return iteradorUsuario;
            }
        }
        return null;
    }

    // Simple método para probar conectividad con el servidor, de parte del cliente
    public boolean ping(Cliente c) throws RemoteException {
        if (sessions.get(c) == null) {
            // En el caso de que hubiese cerrado sesión, ignorar peticiones
            return false;
        }
        return true;
    }
}
