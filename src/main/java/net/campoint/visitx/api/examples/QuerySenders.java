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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class QuerySenders {
    private static Gson gson = new Gson();
    private static CloseableHttpClient client = HttpClients.createDefault();

    public static void main(String[] args) throws IOException, URISyntaxException {
        Collection<Sender> maleSenders = querySenders("profile.gender == \"M\"");
        System.out.printf("There are %d male senders%n", maleSenders.size());

        Collection<Sender> femaleSendersFromAschaffenburg = querySenders("profile.gender == \"F\" && profile.city.Contains(\"Aschaffenburg\")");
        System.out.printf("There are %d female senders from Aschaffenburg%n", femaleSendersFromAschaffenburg.size());

        Collection<Sender> femaleSendersWithShortHair = querySenders("profile.gender == \"F\" && profile.hair_length1 == 1");
        System.out.printf("There are %d female senders with short hair%n", femaleSendersWithShortHair.size());

        Collection<Sender> slimOrAthleticFemales = querySenders("profile.gender == \"F\" && (profile.figure1 == 1 || profile.figure1 == 2)");
        System.out.printf("There are %d slim or athletic female senders%n", slimOrAthleticFemales.size());

        LocalDateTime twentyYearsAgo = LocalDateTime.now().minusYears(20);
        Collection<Sender> age20OrYounger = querySenders(String.format("profile.birthday1 > new DateTime(%d, %d, %d)", twentyYearsAgo.getYear(), twentyYearsAgo.getMonthValue(), twentyYearsAgo.getDayOfMonth()));
        System.out.printf("There are %d senders younger than 20 years%n", age20OrYounger.size());

    }

    private static Collection<Sender> querySenders(String query) throws URISyntaxException, IOException {
        int next = 0;
        int chunkSize = 1000;
        boolean isDone = false;
        List<Sender> senders = new ArrayList<>();

        do {
            URI uri = new URIBuilder("https://meta.visit-x.net/VXREST.svc/json/senders")
                    .setParameter("skip", String.valueOf(next))
                    .setParameter("take", String.valueOf(chunkSize))
                    .setParameter("query", query)
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
