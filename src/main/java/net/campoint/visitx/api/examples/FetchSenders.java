package net.campoint.visitx.api.examples;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.campoint.visitx.api.examples.ressources.Sender;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FetchSenders {

    public static void main(String[] args) throws IOException, URISyntaxException {
        CloseableHttpClient client = HttpClients.createDefault();

        Collection<Sender> senders = fetchSenders(client);


        // URL url = new URL("https://meta.visit-x.net/VXREST.svc/json/senders");

        // HttpURLConnection con = (HttpURLConnection) url.openConnection();
        // con.setRequestMethod("GET");
        // con.setDoOutput(true);
        // DataOutputStream out = new DataOutputStream(con.getOutputStream());
        // out.writeBytes("" + accessKey);
        // out.flush();
        // out.close();

        // BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        // String inputLine;
        // StringBuffer content = new StringBuffer();

        // while ((inputLine = in.readLine()) != null) {
        //     content.append(inputLine);
        // }
        // in.close();
        // con.disconnect();

        // System.out.print(content.toString());
        // System.in.read();
    }

    private static Collection<Sender> fetchSenders(CloseableHttpClient client) throws URISyntaxException, IOException {
        int next = 0;
        int chunkSize = 1000;
        boolean isDone = false;
        List<Sender> senders = new ArrayList<>();
        Gson gson = new Gson();

        do {
            URI uri = new URIBuilder("https://meta.visit-x.net/VXREST.svc/json/senders")
                    .setParameter("skip", String.valueOf(next))
                    .setParameter("take", String.valueOf(chunkSize))
                    .setParameter("accessKey", Credentials.AccessKey)
                    .build();

            HttpGet get = new HttpGet(uri);
            CloseableHttpResponse response = client.execute(get);

            HttpEntity entity = response.getEntity();
            String json = EntityUtils.toString(entity);


            Type collectionType = new TypeToken<ArrayList<Object>>() {
            }.getType();
            ArrayList<Sender> currentSenders = gson.fromJson(json, collectionType);
            senders.addAll(currentSenders);

            next += currentSenders.size();
            isDone = currentSenders.size() != chunkSize;
            response.close();
        } while (!isDone);

        return senders;
    }
}
