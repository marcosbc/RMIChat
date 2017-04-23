import java.util.*;
import java.rmi.*;
import java.rmi.server.*;

class ClienteChat {
    // Constantes del programa
    private static final int MAX_INTENTOS = 3;

    // Programa principal "main"
    static public void main (String args[]) {
        boolean salirPrograma = false;
        Scanner input = new Scanner(System.in);
        Cliente c = null;
        ServicioChat srv = null;
        if (args.length != 2) {
            Logger.err("Uso: ClienteChat hostregistro numPuertoRegistro");
            return;
        }
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
            srv = (ServicioChat) Naming.lookup("//" + args[0] + ":" + args[1] + "/Chat");
            // Realizar el login del usuario, mediante parametros por linea de comandos
            c = login(srv, input);
        }
        catch (RemoteException e) {
            Logger.err("Error de comunicacion: " + e.toString());
        }
        catch (Exception e) {
            Logger.err("Excepcion en ClienteChat:");
            e.printStackTrace();
        }
        // En caso de login incorrecto, salir con error
        if (c == null) {
            System.exit(1);
        }
        // Ejecucion del programa: Imprimir ayuda y procesar comandos
        printHelp();
        while (!salirPrograma && input.hasNextLine()) {
            String line = input.nextLine().trim();
            String words[] = line.split(" ");
            try {
                // Caso de linea vacia: Mostrar mensaje de error
                if (line == null || line.equals("")) {
                    Logger.warn("Debe introducir un comando válido para el funcionamiento del programa");
                }
                // Caso "/exit"; salir del programa
                else if (line.equalsIgnoreCase("/exit")) {
                    // Salida del programa
                    salirPrograma = true;
                }
                // Caso "/all"; Mostrar lista de grupos existentes
                else if (line.equalsIgnoreCase("/groups")) {
                    // Obtener lista de grupos del servidor
                    String groups[] = srv.listGroups();
                    if (groups != null && groups.length > 0) {
                        Logger.info("Lista de grupos disponibles: " + String.join(", ", groups) + ".");
                    }
                    else {
                        Logger.info("No hay grupos disponibles a los que unirse.");
                    }
                }
                // Caso "/show"; Mostrar lista de grupos existentes
                else if (line.equalsIgnoreCase("/show")) {
                    String currentGroups[] = srv.listGroups(c);
                    if (currentGroups != null && currentGroups.length > 0) {
                        Logger.info("Lista de grupos a los que te has unido: " + String.join(", ", currentGroups) + ".");
                    }
                    else {
                        Logger.info("No te has unido a ningún grupo.");
                    }
                }
                // Caso "/j"; unión a un grupo
                else if (words[0].equalsIgnoreCase("/j") && words.length == 2) {
                    if (!srv.joinGroup(words[1], c)) {
                        Logger.warn("No se ha podido realizar la unión al grupo \"" + words[1] + "\".");
                    }
                }
                // Caso "/l"; unión a un grupo
                else if (words[0].equalsIgnoreCase("/l") && words.length == 2) {
                    if (!srv.leaveGroup(words[1], c)) {
                        Logger.warn("No se ha podido realizar la salida del grupo \"" + words[1] + "\".");
                    }
                }
                // Caso de mensaje público (@usuario) o privado (#grupo)
                else if ((line.substring(0, 1).equals("@") || line.substring(0, 1).equals("#")) && words.length >= 2) {
                    String msg[] = Arrays.copyOfRange(words, 1, words.length);
                    // Mensaje hacia el usuario/grupo en cuestión
                    if (!srv.sendMessage(words[0], String.join(" ", msg), c)) {
                        Logger.warn("No se ha podido mandar el mensaje anterior.");
                    }
                }
                else {
                    printHelp();
                }
            }
            catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        // Realizar logout y salida del programa
        try {
            srv.logout(c);
        }
        catch (RemoteException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    private static void printHelp() {
        // Comando invalido
        Logger.text(
            "Lista de comandos validos:\n\n" +
            "    " + Logger.GRAY_BOLD + "/groups\n" + Logger.RESET +
            "    Mostrar lista de grupos existentes.\n\n" +
            "    " + Logger.GRAY_BOLD + "/show\n" + Logger.RESET +
            "    Mostrar lista de grupos a los que te has unido.\n\n" +
            "    " + Logger.GRAY_BOLD + "/j #GRUPO\n" + Logger.RESET +
            "    Union a un grupo de chat.\n\n" +
            "    " + Logger.GRAY_BOLD + "/l #GRUPO\n" + Logger.RESET +
            "    Salida de un grupo de chat.\n\n" +
            "    " + Logger.GRAY_BOLD + "#GRUPO MENSAJE\n" + Logger.RESET +
            "    Envio de un nuevo mensaje público al grupo #GRUPO.\n" +
            "    Por ejemplo: #general Hola a todos!\n\n" +
            "    " + Logger.GRAY_BOLD + "@USUARIO MENSAJE\n" + Logger.RESET +
            "    Envio de un nuevo mensaje privado a USUARIO.\n" +
            "    Por ejemplo: @fulanito Hola!\n\n" +
            "    " + Logger.GRAY_BOLD + "/exit\n" + Logger.RESET +
            "    Salir de la aplicación.\n"
        );
    }

    private static Cliente login(ServicioChat srv, Scanner input) {
        boolean finLogin = false;
        int intentos = 0;
        String username = null;
        String password = null;
        Cliente c = null;

        Logger.text(
            Logger.GREEN_BOLD +
            "\n*** Bienvenido a ClienteChat para el proyecto de SDySW ***\n" +
            Logger.RESET +
            "\nPor favor, indique su nombre de usuario o escriba " +
            Logger.GRAY_BOLD + "/nuevo" + Logger.RESET + " para crear uno" +
            "\nEn cualquier momento, escriba " +
            Logger.GRAY_BOLD + "/exit" + Logger.RESET + " para salir del programa\n"
        );

        // Obtener nombre de usuario
        Logger.prompt("Comando: ");
        while (!finLogin && input.hasNextLine()) {
            String line = input.nextLine().trim();
            String words[] = line.split(" ");
            // Caso de linea vacia: Mostrar mensaje de error
            if (line == null || line.equals("")) {
                Logger.warn("Debe introducir un comando válido para el funcionamiento del programa");
            }
            // Caso de mas de una palabra: Mostrar mensaje de error
            else if (words.length != 1) {
                Logger.warn("\nEl nombre de usuario ha de estar formado por una sola palabra");
            }
            // Caso "/exit"; salir del programa
            else if (line.equalsIgnoreCase("/exit")){
                finLogin = true;
            }
            // Caso "/nuevo"; registro de usuario con el servidor
            else if (line.equalsIgnoreCase("/nuevo")){
                // Si se ha creado el usuario correctamente, finalizar el login
                if ((c = crearUsuario(srv, input)) != null) {
                    finLogin = true;
                }
            }
            // Validacion; los nombres no pueden empezar por "/" (reservado para comandos)
            else if (line.substring(0, 1).equals("/")) {
                Logger.warn("\nEl nombre de usuario no puede empezar por \"/\", es un carácter reservado para comandos\n");
            }
            // Todo bien; pedimos contraseña
            else {
                username = line;
                Logger.prompt("Contraseña: ");
                password = System.console().readPassword().toString();
                try {
                    // Instanciar el cliente y realizar login
                    c = new ClienteImpl(username, password);
                    if (srv.login(c)) {
                        Logger.success("\nLas credenciales son correctas. Ha iniciado sesion correctamente.\n");
                        finLogin = true;
                    } else {
                        c = null;
                        // Hay un limite de intentos de login
                        if (intentos < MAX_INTENTOS) {
                            Logger.warn(
                                "\nLas credenciales no son correctas. " +
                                "Lleva " + (++intentos) + " de " + MAX_INTENTOS + " intentos.\n"
                            );
                        } else {
                            Logger.err("\n*** Demasiados intentos, el programa se cerrarrá ***");
                            // Se producira una salida del programa
                            break;
                        }
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                    // Evitar problemas posteriores y salir del programa
                    System.exit(1);
                }
            }
            if (!finLogin) {
                Logger.prompt("Comando: ");
            }
        }

        // Si es nulo, no se ha realizado el inicio de sesion correctamente
        return c;
    }

    private static Cliente crearUsuario(ServicioChat srv, Scanner input) {
        boolean finCrear = false;
        String username = null;
        String password = null;
        Cliente c = null;

        Logger.warn("\nSe está procediendo a la creación de un nuevo usuario\n");

        // Obtener nombre de usuario
        Logger.prompt("Nombre de usuario: ");
        while (!finCrear && input.hasNextLine()) {
            String line = input.nextLine().trim();
            String words[] = line.split(" ");
            // Caso de linea vacia: Mostrar mensaje de error
            if (line == null || line.equals("")) {
                Logger.warn("Debe introducir un nombre de usuario no vacío");
            }
            // Caso "/exit"; salir del programa
            else if (line.equalsIgnoreCase("/exit")) {
                finCrear = true;
            }
            // Caso de mas de una palabra: Mostrar mensaje de error
            else if (words.length != 1) {
                Logger.warn("\nEl nombre de usuario ha de estar formado por una sola palabra");
            }
            // Validacion; los nombres no pueden empezar por "/" (reservado para comandos)
            else if (line.substring(0, 1).equals("/")) {
                Logger.warn("\nEl nombre de usuario no puede empezar por /, es un carácter reservado para comandos");
            }
            // Todo bien; pedimos contraseña
            else {
                username = line;
                Logger.prompt("Contraseña: ");
                password = System.console().readPassword().toString();
                try {
                    // Instanciar el cliente y realizar login
                    c = new ClienteImpl(username, password);
                    if (srv.addUsuario(c)) {
                        Logger.success("\n*** Usuario registrado correctamente ***\n");
                        finCrear = true;
                    } else {
                        c = null;
                        Logger.warn("El usuario ya existe. Por favor, inténtelo de nuevo o introduzca /exit para salir.");
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                    // Evitar problemas posteriores y salir del programa
                    System.exit(1);
                }
            }
            if (!finCrear) {
                Logger.prompt("Nombre de usuario: ");
            }
        }

        // Si es nulo, no se ha creado el usuario correctamente
        return c;
    }
}
