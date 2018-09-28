package net.campoint.visitx.api.examples.socketHelpers;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import io.reactivex.ObservableEmitter;
import net.campoint.visitx.api.examples.ressources.Message;
import net.campoint.visitx.api.examples.ressources.MessageData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

@WebSocket(maxTextMessageSize = 64 * 1024)
public class OnlineUpdatesSocket {
    private static Gson gson = new Gson();
    private final OnlineMessageUpdateHandler msgHandler;

    @SuppressWarnings("unused")
    private Session session;

    public OnlineUpdatesSocket(OnlineMessageUpdateHandler msgHandler) {
        this.msgHandler = msgHandler;
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        System.out.printf("Connection closed: %d - %s%n", statusCode, reason);
        this.session = null;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
    }

    @OnWebSocketMessage
    public void onMessage(String msg) {

        if(msg.isEmpty()) {
            return;
        }

        Message message = gson.fromJson(msg, Message.class);

        this.msgHandler.onOnlineUpdateMessage(message);
    }

}
