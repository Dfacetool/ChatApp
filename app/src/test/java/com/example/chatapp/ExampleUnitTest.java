package com.example.chatapp;

import static com.example.chatapp.ChatGptService.defaultClient;
import static com.example.chatapp.ChatGptService.defaultObjectMapper;
import static com.example.chatapp.ChatGptService.defaultRetrofit;


import com.fasterxml.jackson.databind.ObjectMapper;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;


import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void testGetChatGPTResponse() throws IOException {
        /*String prompt = "Hello, how are you?";
        setUp();
        Call<ChatGPTResponse> call = chatGPTAPI.getChatGPTResponse(new ChatGPTRequest(prompt,150, 0.5, 0.9));
        Response<ChatGPTResponse> response = call.execute();
        if (response.isSuccessful()) {
            ChatGPTResponse chatGPTResponse = response.body();
            Gson gson = new Gson();
            String jsonResponse = gson.toJson(chatGPTResponse);
            System.out.println(jsonResponse);
        } else {
            System.out.println("Error: " + response.errorBody().string());
        }*/
        String token = "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";
        ObjectMapper mapper = defaultObjectMapper();
        Duration timeout = Duration.ofSeconds(60);;
        OkHttpClient client = defaultClient(token, timeout)
                .newBuilder()
                .addInterceptor(new HttpLoggingInterceptor())
                .build();
        /*Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openai.com")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();*/
        Retrofit retrofit = defaultRetrofit(client, mapper)
                .newBuilder()
                //.baseUrl("https://api.openai.com/v1/chat/")
                .build();
        List<ChatMessage> messages = new ArrayList();
        messages.add(new ChatMessage("user","How to find a cat in Kansas?"));
        OpenAiApi api = retrofit.create(OpenAiApi.class);
        ChatGptService service = new ChatGptService(api);
        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                .messages(messages)
                .model("gpt-3.5-turbo")
                //.model("ada")
                .n(5)
                .maxTokens(500)
                .build();
        service.createChatCompletion(completionRequest).getChoices().forEach(System.out::println);
    }
}
