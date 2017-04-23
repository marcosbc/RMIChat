class Logger {
    // Colores para simplificar la lectura de mensajes
    public static final String RESET = "\u001B[0m";
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";
    public static final String GRAY = "\u001B[90m";

    // Estilos de texto
    public static final String BOLD = "\u001B[1m";

    // Mezclas
    public static final String BLACK_BOLD = BOLD + BLACK;
    public static final String RED_BOLD = BOLD + RED;
    public static final String GREEN_BOLD = BOLD + GREEN;
    public static final String YELLOW_BOLD = BOLD + YELLOW;
    public static final String BLUE_BOLD = BOLD + BLUE;
    public static final String PURPLE_BOLD = BOLD + PURPLE;
    public static final String CYAN_BOLD = BOLD + CYAN;
    public static final String WHITE_BOLD = BOLD + WHITE;
    public static final String GRAY_BOLD = BOLD + GRAY;

    // Métodos
    public static void prompt (String msg) {
        System.out.print(GRAY_BOLD + msg + RESET);
    }
    public static void text (String msg) {
        System.out.println(msg);
    }
    public static void success (String msg) {
        text(GREEN + msg + RESET);
    }
    public static void info (String msg) {
        text(GRAY + msg + RESET);
    }
    public static void warn (String msg) {
        text(YELLOW + msg + RESET);
    }
    public static void err (String msg) {
        text(RED + msg + RESET);
    }
    public static void notif (String user, String group, String message) {
        // Los mensajes privados no tienen grupo especificado
        if (group == null || group.equals("")) {
            // Mensaje privado al cliente exclusivamente
            text(GRAY_BOLD + "(privado) " + CYAN_BOLD + user + "> " + RESET + message);
        } else {
            // Mensaje público en un grupo
            text(RED_BOLD + group + " " + CYAN_BOLD + user + "> " + RESET + message);
        }
    }
}
