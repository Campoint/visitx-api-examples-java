package net.campoint.visitx.api.examples;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.campoint.visitx.api.examples.ressources.*;
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
import java.util.HashMap;

public class TranslateProfile {
    private static Gson gson = new Gson();

    public static void main(String[] args) throws URISyntaxException, IOException {
        CloseableHttpClient client = HttpClients.createDefault();

        Sender sender = fetchFirstSender(client);

        String languageCode = "DE";
        Profile senderProfile = getSenderProfile(client, sender);

        for(ProfileProperty prop : senderProfile.profileProperties) {
            if(prop.languageCode == null || prop.languageCode.equals(languageCode)){
                String propName = prop.key;
                String propValue = getPropertyValue(client, prop.value, languageCode);

                System.out.println(propName + " : " + propValue);
            }
        }

    }

    private static String getPropertyValue(CloseableHttpClient client, ProfilePropertyValue propertyValue, String languageCode) throws IOException, URISyntaxException {
        String value = propertyValue.value;

        if(propertyValue.languageCode == null && propertyValue.translationKey != null){
            HashMap<String, String> translations = getTranslations(client, languageCode, propertyValue.translationKey);

            if(propertyValue.isMultiValue()){
                String[] values = value.split(",");

                ArrayList<String> translatedValues = new ArrayList<>();
                for(String v : values) {
                    translatedValues.add(translateValue(v, translations));
                }
                return String.join(", ", translatedValues);
            }
            else{
                return translateValue(value, translations);
            }
        }

        return value;
    }

    private static String translateValue(String value, HashMap<String, String> translations) {
        return translations.getOrDefault(value, value);
    }

    private static HashMap<String, String> getTranslations(CloseableHttpClient client, String languageCode, String translationKey) throws URISyntaxException, IOException {
        URI uri = new URIBuilder("https://meta.visit-x.net/VXREST.svc/json/translations/"+  translationKey + "/" + languageCode)
                .setParameter("accessKey", Credentials.AccessKey)
                .build();

        HttpGet get = new HttpGet(uri);
        CloseableHttpResponse response = client.execute(get);

        String json = EntityUtils.toString(response.getEntity());

        Type collectionType = new TypeToken<ArrayList<Translation>>() {
        }.getType();

        HashMap<String, String> translations = new HashMap<>();

        for(Translation t : (ArrayList<Translation>)gson.fromJson(json, collectionType)){
            translations.putIfAbsent(t.index, t.value);
        }

        return translations;
    }

    private static Profile getSenderProfile(CloseableHttpClient client, Sender sender) throws IOException, URISyntaxException {
        System.out.println(sender.UserId);

        URI uri = new URIBuilder("https://meta.visit-x.net/VXREST.svc/json/senders/"+ sender.UserId + "/profile")
                .setParameter("accessKey", Credentials.AccessKey)
                .build();

        HttpGet get = new HttpGet(uri);
        CloseableHttpResponse response = client.execute(get);

        String json = EntityUtils.toString(response.getEntity());

        return gson.fromJson(json, Profile.class);
    }

    private static Sender fetchFirstSender(CloseableHttpClient client) throws URISyntaxException, IOException {

        URI uri = new URIBuilder("https://meta.visit-x.net/VXREST.svc/json/senders")
                .setParameter("skip", "0")
                .setParameter("take", "1")
                .setParameter("accessKey", Credentials.AccessKey)
                .build();

        HttpGet get = new HttpGet(uri);
        CloseableHttpResponse response = client.execute(get);

        HttpEntity entity = response.getEntity();
        String json = EntityUtils.toString(entity);


        Type collectionType = new TypeToken<ArrayList<Sender>>() {
        }.getType();
        ArrayList<Sender> senders = gson.fromJson(json, collectionType);
        return senders.get(0);
    }
}
