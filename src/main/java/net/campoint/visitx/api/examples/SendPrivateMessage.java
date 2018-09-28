package net.campoint.visitx.api.examples;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.campoint.visitx.api.examples.ressources.Sender;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class SendPrivateMessage {
    private static Gson gson = new Gson();

    public static void main(String[] args) throws URISyntaxException, IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        Sender testAccount = fetchTestAccountForPrivateMessaging(client);

        sendPrivateMessage(client, testAccount);
    }

    private static void sendPrivateMessage(CloseableHttpClient client, Sender testAccount) throws URISyntaxException, IOException {
        URI uri = new URIBuilder("https://www.visit-x.net/smif/contentpartner/sendmail")
                .build();

        HttpPost post = new HttpPost(uri);
        String authHeaderValue = Base64.getEncoder().encodeToString(String.format("%s:%s", Credentials.ApiUserName, Credentials.ApiPassword).getBytes()) ;
        post.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + authHeaderValue);

        List<NameValuePair> form = new ArrayList<>();
        form.add(new BasicNameValuePair("guestId", String.valueOf(Credentials.ApiUserName.hashCode())));
        form.add(new BasicNameValuePair("guest", Credentials.ApiUserName));
        form.add(new BasicNameValuePair("host", testAccount.UserId));
        form.add(new BasicNameValuePair("text", "JAVA API Sample Message"));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form);

        post.setEntity(entity);

        CloseableHttpResponse response = client.execute(post);
        String messageId = EntityUtils.toString(response.getEntity());

        System.out.printf("Successfully send a private message with id %s to sender %s.%n", messageId, testAccount.SenderName);
    }

    private static Sender fetchTestAccountForPrivateMessaging(CloseableHttpClient client) throws IOException, URISyntaxException {
        URI uri = new URIBuilder("https://meta.visit-x.net/VXREST.svc/json/senders")
                .setParameter("skip", "0")
                .setParameter("take", "1")
                .setParameter("query", "sender.IsTestLogin == true && sender.SenderName == \"Froschhueter\"")
                .setParameter("accessKey", Credentials.AccessKey)
                .build();

        HttpGet get = new HttpGet(uri);

        CloseableHttpResponse response = client.execute(get);
        String json = EntityUtils.toString(response.getEntity());

        Type collectionType = new TypeToken<ArrayList<Sender>>() {
        }.getType();

        return ((ArrayList<Sender>)gson.fromJson(json, collectionType)).get(0);
    }
}
