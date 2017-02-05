package ru.inheaven.aida.vk.service;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.UserAuthResponse;
import com.vk.api.sdk.objects.messages.responses.GetHistoryResponse;

/**
 * @author Anatoly A. Ivanov
 *         Date: 05.02.2017.
 */
public class LetterService {
    public static void main(String[] args) throws ClientException, ApiException {
        TransportClient transportClient = new HttpTransportClient();
        VkApiClient vk = new VkApiClient(transportClient);

//        UserAuthResponse authResponse = vk.oauth()
//                .userAuthorizationCodeFlow(5859270, "rYXQAdTTBO4XlwSg8sjS", "https://oauth.vk.com/blank.html", "d3b58b46f12a95fa5d")
//                .execute();

        UserActor actor = new UserActor(815096, "d21f84d00033522a8c6ae2f7f264caa34b6f5da8b4ba9659677ea56a7f0d6aa7856b6b5cc6d3d44d190e4");
//        System.out.println(actor);

//        System.out.println(vk.friends().get().userId(815096).execute());

        GetHistoryResponse historyResponse = vk.messages().getHistory(actor).count(200).rev(true).userId("8479387").execute();

        historyResponse.getItems().forEach(m -> System.out.println(m.getId() + " " + m.getBody()));
    }
}
