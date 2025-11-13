package client;

import datamodel.GameData;
import serverfacade.ServerFacade;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

import ui.ChessBoardUI;
import ui.EscapeSequences;

public class ChessClient {
    ServerFacade server;
    ChessBoardUI drawer = new ChessBoardUI();
    String preloginStatus = EscapeSequences.RESET_BG_COLOR + EscapeSequences.SET_TEXT_COLOR_MAGENTA + "[Not logged in]";
    String postloginStatus;
    String username;
    String authToken = "";
    boolean loggedIn = false;


    public ChessClient(String serverURL){
        server = new ServerFacade(serverURL);
    }

    public void run() {
        System.out.println("♕ 240 Chess Client ♕");
        System.out.println("Type 'help' for commands and usages.");

        Scanner scanner = new Scanner(System.in);
        String result = "";
        while (!Objects.equals(result,"quit")){
            printStatus();
            String input = scanner.nextLine();

            result = parseInput(input);
        }

    }

    private String parseInput(String input) {
        String[] args = input.split(" ");
        switch (args[0]){
            case "quit":
                break;
            case "help":
                help();
                break;
            case "register":
                register(args);
                break;
            case "login":
                login(args);
                break;
            case "logout":
                logout(args);

                break;
            case "clear":
                try {
                    server.clear(args);
                } catch (Exception ex){
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + ex.getMessage());
                }
                break;
            case "create":
                create(args);
                break;
            case "list":
                list(args);
                break;
            case "join":
                server.joinGame(args, authToken);
                break;
            default:
                System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Invalid command.");
                System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "Type 'help' for commands and usages.");
                return "";
        }
        return args[0];
    }

    private void list(String[] args) {
        if (checkArgs(args, "list", 0)){
            return;
        }
        ArrayList<GameData> listOfGames;
        try {
            listOfGames = server.listGames(args, authToken);
        } catch (Exception ex) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + ex.getMessage());
            return;
        }
        int i = 1;
        for (GameData gameData : listOfGames){
            System.out.println(EscapeSequences.SET_BG_COLOR_WHITE + i + ".");
            System.out.println(gameData.getGameName() + "\n");
            drawer.draw(gameData.getGame().getBoard(), false);
            System.out.println("\nWhite player: " + gameData.getWhiteUsername());
            System.out.println("Black player: " + gameData.getBlackUsername() + "\n");
        }
        }

    private void create(String[] args) {
        if (checkArgs(args, "create", 1)){
            return;
        }
        int gameID;
        try {
            gameID = server.createGame(args, authToken);
        } catch (Exception ex){
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + ex.getMessage());
            return;
        }
        System.out.println(EscapeSequences.SET_TEXT_COLOR_MAGENTA + "Game created with ID: " + gameID);
    }

    private void logout(String[] args) {
        if (checkArgs(args, "logout", 0)){
            return;
        } else if (Objects.equals(authToken, "")) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "This command is only available once logged in!");
            return;
        }
        try {
            server.logout(args, authToken);
        } catch (Exception ex) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + ex.getMessage());
        }
        loggedIn = false;
        postloginStatus = null;
        authToken = "";
    }

    private void register(String[] args) {
        if (checkArgs(args, "register", 3)){
            return;
        }
        try {
            authToken = server.register(args).authToken();
        } catch (Exception ex){
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Username already taken!");
        }
        loggedIn = true;
        username = args[1];
        postloginStatus = EscapeSequences.SET_TEXT_COLOR_MAGENTA + "[Logged in as " + username + "]";

    }

    private void login(String[] args) {
        if (checkArgs(args, "login", 2)){
            return;
        }
        try {
            authToken = server.login(args).authToken();
        } catch (Exception ex){
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Invalid credentials!");
            return;
        }
        loggedIn = true;
        username = args[1];
        postloginStatus = EscapeSequences.SET_TEXT_COLOR_MAGENTA + "[Logged in as " + username + "]";

    }

    private void help() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "Commands and usages:");
        String comsAndUsages;
        if (loggedIn) {
            comsAndUsages = """
                    quit - exit the program
                    help - retrieve a list of available commands
                    logout - logout current user
                    list - provides a list of the active chess games
                    create <name> - creates a new chess game with the given name
                    join <number> <white/black> - join the chess game associated with the number it's listed under, and choose the color you will play as.
                    clear - clear the database
                    """;
        } else {
            comsAndUsages = """
                    quit - exit the program
                    help - retrieve a list of available commands
                    register <username> <password> <email> - register with a username, password, and email
                    login <username> <password> - login with a username and password that is already registered
                    clear - clear the database
                    """;
        }
        System.out.println(comsAndUsages);
    }

    private void printStatus() {
        if(!loggedIn) {
            System.out.print(preloginStatus + " >> " + EscapeSequences.SET_TEXT_COLOR_GREEN);
        } else {
            System.out.print(postloginStatus + " >> " + EscapeSequences.SET_TEXT_COLOR_GREEN);
        }
    }

    private boolean checkArgs(String[] args, String command, int numArgsExpected){
        int numArgsFound = 0;
        for (int i = 1; i <= numArgsExpected; i++){
            try {
                String test = args[i];
                numArgsFound++;
            } catch (ArrayIndexOutOfBoundsException ex){
                System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Invalid use of command: " + command);
                System.out.println("Expected " + numArgsExpected + " arguments but received " + numArgsFound);
                System.out.println("Use 'help' for information on correct usage.");
                return true;
            }
        }

        if (args.length > numArgsExpected+1) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Invalid use of command: " + command);
            System.out.println("Expected " + numArgsExpected + " arguments but received " + (args.length-1));
            System.out.println("Use 'help' for information on correct usage.");
            return true;
        }
        return false;
    }

}
