package websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

    ConcurrentHashMap<Session, Session> connections = new ConcurrentHashMap<>();


    public void add(Session session){
        connections.put(session, session);
    }

    public void remove(Session session){
        connections.remove(session);
    }

    public void broadcastMessage(ServerMessage serverMessage, Collection<Session> excludedSessions) throws IOException {
        for (Session session : connections.values()){
            if (!excludedSessions.contains(session) && session.isOpen()){
                session.getRemote().sendString(serverMessage.getMessage());
            }
        }
    }

    //send message to a single user
    public void sendMessage(ServerMessage message, Session session) throws IOException {
        if (session.isOpen()){
            session.getRemote().sendString(new Gson().toJson(message));
        }
    }
}
