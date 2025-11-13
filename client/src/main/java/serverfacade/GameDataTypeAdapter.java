package serverfacade;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import datamodel.GameData;

import java.io.IOException;
import java.util.Objects;

public class GameDataTypeAdapter extends TypeAdapter<GameData> {
    @Override
    public void write(JsonWriter jsonWriter, GameData gameData) throws IOException {
        Gson gson = new Gson();

        gson.getAdapter(GameData.class).write(jsonWriter, gameData);
    }

    @Override
    public GameData read(JsonReader jsonReader) throws IOException {
        int gameID = 0;
        String whiteUsername = null;
        String blackUsername = null;
        String gameName = null;
        String gameAsJson = null;
        ChessGame game = null;

        jsonReader.beginObject();
        String identifier;
        while(jsonReader.hasNext()){
            identifier = jsonReader.nextName();
            switch(identifier){
                case "gameID":
                    gameID = jsonReader.nextInt();
                    break;
                case "whiteUsername":
                    whiteUsername = jsonReader.nextString();
                    break;
                case "blackUsername":
                    blackUsername = jsonReader.nextString();
                    break;
                case "gameName":
                    gameName = jsonReader.nextString();
                    break;
                case "game":
                    game = buildGame(jsonReader);
                    break;
            }
        }

        jsonReader.endObject();

        return new GameData(gameID, whiteUsername, blackUsername, gameName, game);
    }

    private ChessGame buildGame(JsonReader jsonReader) throws IOException {
        ChessGame.TeamColor currentTurn = null;
        ChessBoard board = null;

        jsonReader.beginObject();
        while (jsonReader.hasNext()){
            String identifier = jsonReader.nextName();
            if(Objects.equals(identifier, "currentTurn")){
                currentTurn = ChessGame.TeamColor.valueOf(jsonReader.nextString());
            } else if (Objects.equals(identifier, "board")){
                board = buildBoard(jsonReader);
            }
        }
        jsonReader.endObject();
        return new ChessGame(currentTurn, board);
    }

    private ChessBoard buildBoard(JsonReader jsonReader) throws IOException {
        ChessPiece[][] board = new ChessPiece[8][8];
        jsonReader.beginObject();
        jsonReader.nextName();
        jsonReader.beginArray();
        for (int i = 0; i < 8; i++){
            jsonReader.beginArray();
            for (int j = 0; j < 8; j++){
                if (jsonReader.peek() == JsonToken.NULL){
                    jsonReader.nextNull();
                    board[i][j] = null;
                    continue;
                }
                jsonReader.beginObject();
                ChessPiece.PieceType pieceType = null;
                ChessGame.TeamColor teamColor = null;
                boolean hasMoved = false;
                while (jsonReader.hasNext()){
                    String identifier = jsonReader.nextName();
                    switch (identifier){
                        case "type" -> pieceType = ChessPiece.PieceType.valueOf(jsonReader.nextString());
                        case "pieceColor" -> teamColor = ChessGame.TeamColor.valueOf(jsonReader.nextString());
                        case "hasMoved" -> jsonReader.nextBoolean();
                    }
                }
                jsonReader.endObject();
                board[i][j] = new ChessPiece(teamColor, pieceType, hasMoved);
            }
            jsonReader.endArray();
        }
        jsonReader.endArray();
        jsonReader.endObject();
        return new ChessBoard(board);
    }
}
