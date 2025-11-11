package serverfacade;

import chess.ChessGame;
import ui.ChessBoardUI;

public class ServerFacade {
    public void clear(){

    }

    public void register(){

    }

    public void login(){

    }

    public void logout(){

    }

    public void listGames(){
        ChessBoardUI drawer = new ChessBoardUI();
        ChessGame game = new ChessGame();
        drawer.draw(game.getBoard(), false);
    }

    public void createGame(){

    }

    public void joinGame(){

    }
}
