package ru.inheaven.aida.vk.fx;

import com.google.common.base.Splitter;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.UserAuthResponse;
import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import static javafx.application.Application.launch;

/**
 * @author Anatoly A. Ivanov
 *         Date: 05.02.2017.
 */
public class VkAuthApplication extends Application{
    public static void main(String[] args) throws ClientException, ApiException {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("AIDA-VK");

        WebView webView = new WebView();

        WebEngine webEngine = webView.getEngine();
        webEngine.setJavaScriptEnabled(true);

        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            String location = webEngine.getLocation();

            System.out.println(location);

            if (Worker.State.SUCCEEDED.equals(newValue) && location.contains("code=")) {
                try {
                    vk(location);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        String auth = "https://oauth.vk.com/authorize?client_id=5859270&display=page&redirect_uri=https://oauth.vk.com/blank.html&scope=notify,friends,audio,status,messages,ads,notifications&response_type=code&v=5.62";
        webEngine.load(auth);

        StackPane sp = new StackPane();
        sp.getChildren().add(webView);

        Scene root = new Scene(sp);

        stage.setScene(root);
        stage.show();
    }

    private static void vk(String location) throws MalformedURLException, ClientException, ApiException {
        String query = location.split("#")[1];
        Map<String, String> map = Splitter.on('&').trimResults().withKeyValueSeparator("=").split(query);
        String code = map.get("code");

        System.out.println("code=" + code);

//        TransportClient transportClient = new HttpTransportClient();
//        VkApiClient vk = new VkApiClient(transportClient);
//
//        UserAuthResponse authResponse = vk.oauth()
//                .userAuthorizationCodeFlow(5859270, "rYXQAdTTBO4XlwSg8sjS", "https://oauth.vk.com/blank.html", code)
//                .execute();
//
//        UserActor actor = new UserActor(authResponse.getUserId(), authResponse.getAccessToken());
//        System.out.println(actor);
//
//        com.vk.api.sdk.objects.friends.responses.GetResponse getResponse = vk.friends().get().execute();
//        System.out.println(getResponse);

    }
}
