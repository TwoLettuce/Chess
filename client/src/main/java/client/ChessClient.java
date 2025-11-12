package client;

import dataaccess.DataAccessException;
import serverfacade.ServerFacade;

import java.util.Objects;
import java.util.Scanner;

import ui.EscapeSequences;

public class ChessClient {
    ServerFacade server;
    String preloginStatus = EscapeSequences.RESET_BG_COLOR + EscapeSequences.SET_TEXT_COLOR_MAGENTA + "[Not logged in]";
    String postloginStatus;
    String username;
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

        System.out.println("Thanks for playing!");

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
                if (!checkArgs(args, "register", 3)){
                    break;
                }
                try {
                    server.register(args);
                } catch (DataAccessException ex){
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + ex.getMessage());
                }
                loggedIn = true;
                username = args[1];
                postloginStatus = EscapeSequences.SET_TEXT_COLOR_MAGENTA + "[Logged in as " + username + "]";
                break;
            case "login":
                if (!checkArgs(args, "login", 2)){
                    break;
                }
                try {
                    server.login(args);
                } catch (DataAccessException ex){
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + ex.getMessage());
                }
                loggedIn = true;
                username = args[1];
                postloginStatus = EscapeSequences.SET_TEXT_COLOR_MAGENTA + "[Logged in as " + username + "]";
                break;
            case "logout":
                server.logout(args);
                loggedIn = false;
                postloginStatus = null;
                break;
            case "clear":
                server.clear(args);
                break;
            case "create":
                server.createGame(args);
                break;
            case "list":
                server.listGames(args);
                break;
            case "join":
                server.joinGame(args);
                break;
            default:
                System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Invalid command.");
                System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "Type 'help' for commands and usages.");
                return "";
        }
        return args[0];
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
                return false;
            }
        }
        try {
            String test = args[numArgsExpected+1];
        } catch (ArrayIndexOutOfBoundsException ex){

        }
        return true;
    }

}
