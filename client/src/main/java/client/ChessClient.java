package client;

import chess.ChessGame;
import datamodel.GameData;
import serverfacade.GameDataList;
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
    GameDataList gameDataList = new GameDataList();


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
                    authToken = "";
                    gameDataList = null;
                    loggedIn = false;
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
                join(args);
                break;
            default:
                System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Invalid command.");
                System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "Type 'help' for commands and usages.");
                return "";
        }
        return args[0];
    }

    private void join(String[] args) {
        if (checkArgs(args, "join", 2)){
            return;
        }
        if (gameDataList.isEmpty()) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Use 'list' to view potential games first!");
            return;
        }
        args[1] = args[1].toUpperCase();

        if (!args[1].equals("WHITE") && !args[1].equals("BLACK")){
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Expected 'white' or 'black' as 1st argument, but got " + args[1]);
            return;
        }

        boolean joinedAsBlack = !Objects.equals(args[1], "WHITE");
        int gameIndex;
        try {
            gameIndex = Integer.parseInt(args[2]);
        } catch (NumberFormatException ex){
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Expected a number as 2nd argument but got '" + args[2] + "'");
            return;
        }
        if (gameIndex > gameDataList.size()){
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Invalid game");
            return;
        }
        int gameID = gameDataList.get(gameIndex-1).getGameID();
        try {
            server.joinGame(args[1], gameID, authToken);
            System.out.println("joined " + gameDataList.get(Integer.parseInt(args[2])).getGameName() + " as " + args[1]);
        } catch (Exception ex){
//            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Please login first!");
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + ex.getMessage());
            return;
        }
        enterGameRepl(joinedAsBlack, Integer.parseInt(args[2]));
    }

    private void enterGameRepl(boolean joinedAsBlack, int gameID) {
        Scanner scanner = new Scanner(System.in);
        printSadStatus();
        drawer.draw(gameDataList.get(gameID).getGame().getBoard(), joinedAsBlack);
        while (true) {
            String input = scanner.nextLine();
            if (Objects.equals(input, "quit")){
                System.out.println("Returning to main menu . . .");
                break;
            } else if (Objects.equals(input, "help")) {
                System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "Commands and usages: \nquit - exit the game");
            } else {
                System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Sorry! This part of the game hasn't been implemented yet!");
            }
        }
    }

    private void printSadStatus() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + """
                Sorry! The developers haven't gotten this far yet!
                Use 'quit' to return to the main menu.
                Here's the board and the players that have joined so far:""");
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
        gameDataList = (GameDataList) listOfGames;
        if (gameDataList.isEmpty()) {
            System.out.println("There are no active games! Use 'create <gameName>' to create one!");
            return;
        }
        int i = 1;
        for (GameData gameData : listOfGames){
            System.out.println(EscapeSequences.SET_TEXT_COLOR_WHITE + i + ". " + gameData.getGameName());
            drawer.draw(gameData.getGame().getBoard(), false);
            System.out.print(EscapeSequences.SET_TEXT_COLOR_WHITE);
            System.out.println("White player: " + gameData.getWhiteUsername());
            System.out.print(EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY);
            System.out.println("Black player: " + gameData.getBlackUsername() + "\n");
            i++;
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
        System.out.println(EscapeSequences.SET_TEXT_COLOR_MAGENTA + "Game created with name: " + args[1]);
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
                    join <white/black> <number> - join the chess game associated with the number it's listed under, and choose the color you will play as.
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
