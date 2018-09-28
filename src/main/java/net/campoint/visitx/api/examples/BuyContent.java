package net.campoint.visitx.api.examples;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.util.Pair;
import net.campoint.visitx.api.examples.ressources.Gallery;
import net.campoint.visitx.api.examples.ressources.Sender;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class BuyContent {
    private static CloseableHttpClient client = HttpClients.createDefault();
    private static Gson gson = new Gson();
    private static DocumentBuilder xmlDocumentBuilder;

    static {
        try {
            xmlDocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, URISyntaxException, SAXException {
        Collection<Sender> demoSenders = fetchDemoSenders();

        Pair<Sender, Gallery> demoSenderWithShopGallery = findFirstDemoSenderWithShopGallery(demoSenders);

        String links = buyGalleryFromSender(demoSenderWithShopGallery.getKey(), demoSenderWithShopGallery.getValue());

        System.out.print(links);
    }

    private static String buyGalleryFromSender(Sender sender, Gallery galleryToBuy) throws URISyntaxException, IOException, SAXException {
        URI buyUri = new URIBuilder("https://visit-x.net/interfaces/content/buy.php")
                .setParameter("cid", galleryToBuy.umaId)
                .setParameter("uip", "10,10,10,10")
                .setParameter("type", "G")
                .build();

        HttpGet buyRequest = new HttpGet(buyUri);
        String authHeaderValue = Base64.getEncoder().encodeToString(String.format("%s:%s", Credentials.ApiUserName, Credentials.ApiPassword).getBytes()) ;
        buyRequest.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + authHeaderValue);

        CloseableHttpResponse buyResponse = client.execute(buyRequest);
        String resp = EntityUtils.toString(buyResponse.getEntity()).replace("\n", "");
        Document buyResponseXml = xmlDocumentBuilder.parse(new InputSource(new StringReader(resp)));
        String buyId = buyResponseXml.getDocumentElement().getFirstChild().getNodeValue().trim();

        URI getLinksUri = new URIBuilder("https://visit-x.net/interfaces/content/getLinks.php")
                .setParameter("buyId", buyId)
                .build();

        HttpGet getLinksRequest = new HttpGet(getLinksUri);
        getLinksRequest.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + authHeaderValue);

        CloseableHttpResponse getLinksResponse = client.execute(getLinksRequest);
        return EntityUtils.toString(getLinksResponse.getEntity()).replace("\n", "");
    }

    private static Pair<Sender, Gallery> findFirstDemoSenderWithShopGallery(Collection<Sender> demoSenders) throws IOException, URISyntaxException {
        for(Sender sender : demoSenders) {
            Optional<Gallery> validGallery = getShoppingGalleriesFor(sender)
                    .stream()
                    .filter(g -> g.hasValidGalleryPrice())
                    .findFirst();

            if(validGallery.isPresent())
            {
                System.out.print(validGallery.get());
                return new Pair<>(sender, validGallery.get());
            }
        }

        return null;
    }

    private static Collection<Gallery> getShoppingGalleriesFor(Sender sender) throws IOException, URISyntaxException {
        URI uri = new URIBuilder("https://meta.visit-x.net/VXREST.svc/json/senders/" + sender.UserId + "/shopGalleries")
                .setParameter("accessKey", Credentials.AccessKey)
                .build();

        HttpGet get = new HttpGet(uri);
        CloseableHttpResponse response = client.execute(get);

        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
            return new ArrayList<>();
        }
        else {
            String json = EntityUtils.toString(response.getEntity());

            Type collectionType = new TypeToken<ArrayList<Gallery>>() {
            }.getType();
            return gson.fromJson(json, collectionType);
        }
    }

    @SuppressWarnings("Duplicates")
    private static Collection<Sender> fetchDemoSenders() throws IOException, URISyntaxException {
        String query = "sender.IsTestLogin == true";

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


            Type collectionType = new TypeToken<ArrayList<Sender>>() {
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
