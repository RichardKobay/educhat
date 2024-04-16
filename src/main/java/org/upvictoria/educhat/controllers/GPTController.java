package org.upvictoria.educhat.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

@RestController
@CrossOrigin("http://localhost:5173/")
public class GPTController {

    @PostMapping("/request")
    public ChatGPTResponse request(@RequestBody Map<String, String> requestBody) {
        String prompt = requestBody.get("prompt");
        return new ChatGPTResponse(chatGPT(prompt));
    }

    public String chatGPT(String body) {
        String url = "https://api.openai.com/v1/chat/completions";
        String apiKey = System.getenv("OPENAI_API_KEY");

        try {
            BufferedReader br = getBufferedReader(body, url, apiKey);
            String line;

            StringBuilder response = new StringBuilder();

            while ((line = br.readLine()) != null) {
                response.append(line);
            }

            br.close();

            return extractMessageFromJSONResponse(response.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static BufferedReader getBufferedReader(String body, String url, String apiKey) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setDoOutput(true);

        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
        writer.write(body);
        writer.flush();
        writer.close();

        // Response
        return new BufferedReader(new InputStreamReader(connection.getInputStream()));
    }

    public String extractMessageFromJSONResponse (String response) {
        JsonObject jsonObject = new Gson().fromJson(response, JsonObject.class);

        return jsonObject.getAsJsonArray("choices")
                .get(0)
                .getAsJsonObject()
                .getAsJsonObject("message")
                .get("content")
                .getAsString();
    }


    private class ChatGPTResponse {
        private String response;

        public ChatGPTResponse(String response) {
            this.response = response;
        }

        public String getResponse() {
            return response;
        }

        public void setResponse(String response) {
            this.response = response;
        }

    }
}
