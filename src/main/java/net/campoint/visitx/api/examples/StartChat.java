package net.campoint.visitx.api.examples;

import com.google.gson.Gson;
import io.reactivex.Observable;
import net.campoint.visitx.api.examples.ressources.Message;
import net.campoint.visitx.api.examples.socketHelpers.OnlineUpdatesSocket;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;

public class StartChat {
    private static CloseableHttpClient client = HttpClients.createDefault();

    public static void main(String[] args) throws IOException, URISyntaxException {
        String availableChatPartner = getAvailableChatPartner();

        String chatWindow = initiateChat(availableChatPartner);

        System.out.println("Chat Window Element:");
        System.out.println(chatWindow);
    }

    private static String initiateChat(String availableChatPartner) throws URISyntaxException, IOException {
        URI buyUri = new URIBuilder("https://visit-x.net/interfaces/content/start.php")
                .setParameter("userID", availableChatPartner)
                .build();

        HttpGet get = new HttpGet(buyUri);
        String authHeaderValue = Base64.getEncoder().encodeToString(String.format("%s:%s", Credentials.ApiUserName, Credentials.ApiPassword).getBytes()) ;
        get.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + authHeaderValue);

        CloseableHttpResponse buyResponse = client.execute(get);
        return EntityUtils.toString(buyResponse.getEntity());
    }

    private static String getAvailableChatPartner() {
        Observable<Message> updates = Observable.create(emitter -> {
                URI webSocketUri = new URIBuilder("wss://data.campoints.net")
                .setParameter("accessKey", Credentials.AccessKey)
                .build();

                WebSocketClient client = new WebSocketClient();
                OnlineUpdatesSocket socket = new OnlineUpdatesSocket(emitter::onNext);

                client.start();
                ClientUpgradeRequest request = new ClientUpgradeRequest();
                client.connect(socket, webSocketUri, request);
        });

        return updates.firstElement().blockingGet().data.user.ref.key;

    }
}
