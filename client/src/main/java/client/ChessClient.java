package client;

import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import datamodels.GameData;
import serverfacade.GameDataList;
import serverfacade.ServerFacade;
import java.util.Objects;
import java.util.Scanner;

import ui.ChessBoardUI;
import ui.EscapeSequences;
import websocket.ServerMessageHandler;
import websocket.WebSocketFacade;
import websocket.messages.ErrorMessage;
import websocket.messages.GameMessage;
import websocket.messages.ServerMessage;

public class ChessClient implements ServerMessageHandler {
    ServerFacade server;
    WebSocketFacade webSocket;
    ChessBoardUI drawer = new ChessBoardUI();
    String preloginStatus = EscapeSequences.RESET_BG_COLOR + EscapeSequences.SET_TEXT_COLOR_MAGENTA + "[Not logged in]";
    String postloginStatus;
    String playingStatus;
    String username;
    String authToken = "";
    boolean loggedIn = false;
    boolean isBlack;
    int currentGameIndex;
    GameDataList gameDataList = new GameDataList();


    public ChessClient(String serverURL){
        server = new ServerFacade(serverURL);
        webSocket = new WebSocketFacade(serverURL, this);
    }

    public void run() {
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
                if (checkArgs(args, "quit", 0)){ return "notQuit"; }
                if (loggedIn) { logout(args);}
                break;
            case "help":
                checkArgs(args, "help", 0);
                help();
                break;
            case "register":
                if (loggedIn) {
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Please logout first!");
                    return "notQuit";
                }
                register(args); break;
            case "login":
                if (loggedIn) {
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Please logout first!");
                    return "notQuit";
                }
                login(args); break;
            case "logout":
                if (!loggedIn) {
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Please login first!");
                    return "notQuit";
                }
                logout(args);
                break;
            case "clear":
                if (checkArgs(args, "clear", 0)){
                    return "notQuit";
                }
                try {
                    server.clear(args);
                    authToken = "";
                    gameDataList = null;
                    loggedIn = false;
                } catch (Exception ex){
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + ex.getMessage());
                }
                System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Database cleared. I hope you're happy.");
                break;
            case "create":
                if (!loggedIn) {
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Please login first!");
                    return "notQuit";
                }
                create(args);
                break;
            case "list":
                if (!loggedIn) {
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Please login first!");
                    return "notQuit";
                }
                list(args);
                break;
            case "join":
                if (!loggedIn) {
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Please login first!");
                    return "notQuit";
                }
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
        if (gameDataList == null || gameDataList.isEmpty()) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Use 'list' to view potential games first!");
            return;
        }

        int gameIndex;
        try {
            gameIndex = Integer.parseInt(args[2])-1;
        } catch (NumberFormatException ex){
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Expected a number as 2nd argument but got '" + args[2] + "'");
            return;
        }
        if (gameIndex > gameDataList.size()-1){
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Invalid game");
            return;
        }
        int gameID = gameDataList.get(gameIndex).getGameID();
        args[1] = args[1].toUpperCase();
        boolean joinedAsBlack = !Objects.equals(args[1], "WHITE");

        if (args[1].equals("OBSERVER")) {
            webSocket.connectToGame(authToken, gameID, args[1]);
            currentGameIndex = gameIndex;
            observerRepl(gameDataList.get(gameIndex));
        } else if (!args[1].equals("WHITE") && !args[1].equals("BLACK")){
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Expected 'white', 'black', or 'observer' as 1st argument, but got " + args[1]);
            return;
        }
        try {
            server.joinGame(args[1], gameID, authToken);
            System.out.println("joined " + gameDataList.get(gameIndex).getGameName() + " as " + args[1]);
            webSocket.connectToGame(authToken, gameID, args[1]);
        } catch (Exception ex){
            if (Objects.equals(ex.getMessage(), "unexpected status: 401")){
                System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Please login first!");
            } else if (Objects.equals(ex.getMessage(), "unexpected status: 403")){
                System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "That color is already taken!");
            }
            return;
        }
        isBlack = joinedAsBlack;
        playingStatus = "[Playing in game '" + gameDataList.get(gameIndex).getGameName() + "' as " + args[1] + "]";
        enterGameRepl(joinedAsBlack, gameIndex);
    }

    private void printPlayStatus(){
        System.out.print(EscapeSequences.SET_TEXT_COLOR_MAGENTA + playingStatus+ " >> ");
    }

    private void observerRepl(GameData gameData) {
        playingStatus = "[Observing]";
        Scanner scanner = new Scanner(System.in);
        System.out.println("Now observing: " + gameData.getGameName());
        drawer.draw(gameData.getGame().getBoard(), false);
        System.out.println(EscapeSequences.SET_TEXT_COLOR_WHITE + "White player: " + gameData.getWhiteUsername());
        System.out.println(EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY + "Black player: " + gameData.getBlackUsername());
        label:
        while (true) {
            printPlayStatus();
            String input = scanner.nextLine();
            switch (input) {
                case "quit":
                    System.out.println("Returning to main menu . . .");
                    break label;
                case "help":
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "Commands and usages: \n" +
                            "quit - return to menu\nredraw - redraw the board");
                    break;
                case "redraw":
                    drawer.draw(gameData.getGame().getBoard(), false);
                    break;
                case null:
                default:
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Invalid command");
                    break;
            }
        }
    }

    private void enterGameRepl(boolean isBlack, int gameIndex) {
        GameData gameData = gameDataList.get(gameIndex);
        if (isBlack){
            gameData.setBlackUsername(username);
        } else {
            gameData.setWhiteUsername(username);
        }
        Scanner scanner = new Scanner(System.in);
        drawer.draw(gameDataList.get(gameIndex).getGame().getBoard(), isBlack);
        System.out.print(EscapeSequences.SET_TEXT_COLOR_WHITE);
        System.out.println("White player: " + gameData.getWhiteUsername());
        System.out.print(EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY);
        System.out.println("Black player: " + gameData.getBlackUsername());

        while (true) {
            printPlayStatus();
            String[] command = scanner.nextLine().split(" ");
            command[0] = command[0].toLowerCase();
            switch (command[0]){
                case "help":
                    checkArgs(command, command[0], 0);
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + """
                        Commands and usages:
                        help - display commands
                        highlight <position> - highlights legal moves the piece at "position" can make
                        redraw - redraw the board
                        move <position1> <position2> - move the piece at position1 to position2, (i.e. [move e4 e5], [move a1 c1])
                        Note: To castle, use the start and end position of the king, not the rook.
                        Note: When moving a pawn to the final rank, include the piece you want the pawn to be promoted to after the ending position
                        (i.e. move g7 g8 queen) You must choose a promotion piece in this scenario, or the move is invalid.
                        resign - concede defeat
                        leave - exit the game
                        """);
                    break;
                case "highlight", "h":
                    if (checkArgs(command, "highlight", 1)){break;}
                    ChessPosition position = parsePosition(command[1]);
                    try {
                        drawer.highlight(gameData.getGame(), isBlack, position);
                    } catch (NullPointerException ex) {
                        System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "There's no piece there!");
                    }
                    break;
                case "redraw":
                    if (checkArgs(command, command[0], 0)){break;}
                    try {gameDataList = server.listGames(command, authToken);} catch (Exception e) {}
                    gameData = gameDataList.get(gameIndex);
                    drawer.draw(gameData.getGame().getBoard(), isBlack);
                    break;
                case "move":
                    if (checkArgs(command, command[0], 2)){
                        if (checkArgs(command, command[0], 3)){
                            break;
                        }
                    }
                    ChessPiece.PieceType promotionPiece;
                    try {
                        promotionPiece = convertToPromotionPiece(command[3]);
                    } catch (ArrayIndexOutOfBoundsException ex){
                        promotionPiece = null;
                    } catch (InvalidMoveException ex) {
                        System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Invalid promotion piece!");
                        break;
                    }
                    ChessPosition startPos = parsePosition(command[1]);
                    ChessPosition endPos = parsePosition(command[2]);
                    ChessMove move = new ChessMove(startPos, endPos, promotionPiece);
                    webSocket.makeMove(authToken, gameData.getGameID(), move);
                    break;
                case "resign":
                    checkArgs(command, command[0], 0);
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "\nAre you sure? To confirm, type 'yes'");
                    printPlayStatus();
                    String confirm = scanner.nextLine();
                    if (confirm.trim().equalsIgnoreCase("yes")){webSocket.resign(authToken, gameData.getGameID());}
                    else {System.out.println(EscapeSequences.SET_TEXT_COLOR_WHITE + "\nResign aborted");}
                    break;
                case "leave":
                    checkArgs(command, command[0], 0);
                    webSocket.leaveGame(authToken, gameData.getGameID());
                    System.out.println("Returning to main menu . . .");
                    break;
                default:
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Invalid command. Use 'help' for available commands.");
                    break;
            }
            if (Objects.equals(command[0], "leave")){ break; }
        }
    }

    private ChessPiece.PieceType convertToPromotionPiece(String s) throws InvalidMoveException {
        s = s.toUpperCase();
        switch (s) {
            case "QUEEN" -> {return ChessPiece.PieceType.QUEEN;}
            case "BISHOP" -> {return ChessPiece.PieceType.BISHOP;}
            case "KNIGHT" -> {return ChessPiece.PieceType.KNIGHT;}
            case "ROOK" -> {return ChessPiece.PieceType.ROOK;}
            default -> throw new InvalidMoveException();
        }
    }

    private void list(String[] args) {
        if (checkArgs(args, "list", 0)){
            return;
        }
        GameDataList listOfGames;
        try {
            listOfGames = server.listGames(args, authToken);
        } catch (Exception ex) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + ex.getMessage());
            return;
        }
        gameDataList = listOfGames;
        if (gameDataList.isEmpty()) {
            System.out.println("There are no active games! Use 'create <gameName>' to create one!");
            return;
        }
        int i = 1;
        for (GameData gameData : gameDataList){
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
            return;
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
            loggedIn = true;
            username = args[1];
            postloginStatus = EscapeSequences.SET_TEXT_COLOR_MAGENTA + "[Logged in as " + username + "]";
        } catch (Exception ex){
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Invalid credentials!");
        }


    }

    private void help() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "Commands and usages:");
        String commandsAndUsages;
        if (loggedIn) {
            commandsAndUsages = """
                    quit - exit the program
                    help - retrieve a list of available commands
                    logout - logout current user
                    list - provides a list of the active chess games
                    create <name> - creates a new chess game with the given name
                    join <white/black/observer> <number> - join as a player or observer, use number from list
                    clear - clear the database
                   \s""";
        } else {
            commandsAndUsages = """
                    quit - exit the program
                    help - retrieve a list of available commands
                    register <username> <password> <email> - register with a username, password, and email
                    login <username> <password> - login with a username and password that is already registered
                    clear - clear the database""";
        }
        System.out.println(commandsAndUsages);
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

    private ChessPosition parsePosition(String positionAsString){
        char colChar = positionAsString.charAt(0);
        int row = positionAsString.charAt(1) - '0';
        int col;
        switch (colChar){
            case 'a' -> col = 1; case 'b' -> col = 2; case 'c' -> col = 3;
            case 'd' -> col = 4; case 'e' -> col = 5; case 'f' -> col = 6;
            case 'g' -> col = 7; case 'h' -> col = 8; default -> col = -1;
        }
        return new ChessPosition(row, col);
    }

    public void notify(String message) {
        Gson g = new Gson();
        var serverMessage = g.fromJson(message, ServerMessage.class);
        switch (serverMessage.getServerMessageType()) {
            case LOAD_GAME -> {
                GameMessage gameMessage = g.fromJson(message, GameMessage.class);
                gameDataList.get(currentGameIndex).getGame().setBoard(gameMessage.getGame().getBoard());
                System.out.println();
                drawer.draw(gameMessage.getGame().getBoard(), isBlack);
            }
            case NOTIFICATION ->
                    System.out.println("\n" + EscapeSequences.SET_TEXT_COLOR_WHITE + serverMessage.getMessage());
            case ERROR ->
                    System.out.println("\n" + EscapeSequences.SET_TEXT_COLOR_RED + g.fromJson(message, ErrorMessage.class).getErrorMessage());
        }
        printPlayStatus();
    }
}
