package net.campoint.visitx.api.examples;

import org.apache.http.client.utils.URIBuilder;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import net.campoint.visitx.api.examples.socketHelpers.OnlineUpdatesSocket;

import java.net.URI;
import java.net.URISyntaxException;

public class GetOnlineStates {
    public static void main( String[] args ) throws URISyntaxException {
        URI webSocketUri = new URIBuilder("wss://data.campoints.net")
                .setParameter("accessKey", Credentials.AccessKey)
                .build();

        WebSocketClient client = new WebSocketClient();
        OnlineUpdatesSocket socket = new OnlineUpdatesSocket(message -> {
            if(message.isOnlineStateUpdateMessage() && !message.deleted){

                String senderId = message.data.user.ref.key;

                if(message.data.isAvailableForChat()){
                    System.out.printf("Sender %s is available for chat.%n", senderId);
                }
                else {
                    System.out.printf("Sender %s is no longer available for chat.%n", senderId);
                }
            }
        });

        try {
            client.start();
             ClientUpgradeRequest request = new ClientUpgradeRequest();
            client.connect(socket, webSocketUri, request);

            for(;;){}
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            try {
                client.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
