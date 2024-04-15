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

@RestController
@CrossOrigin("http://localhost:5173/")
public class GPTController {
    @RequestMapping(value = "/get-test")
    public @ResponseBody ChatGPTResponse test() {
        String prompt = "Generar un examen de 30 preguntas en formato JSON sobre programación orientada a objetos (exclusivamente en java). Puedes utilizar el siguiente formato: [{'pregunta_1': 'pregunta','respuestas':['respuesta 1','respuesta 2','respuesta 3']}]. Damelos sin saltos de linea ni formato, el puro json en una sola linea. OJO, básate en los siguientes temas: Clases, Objetos, Abstracción, Encapsulamiento y polimorfismo, Herencia, Herencia Jerarquica, Casting, Tipos de Polimorfismo, Clases anidadas, Genéricos, Interfaces, Interfaces Anidadas";

        prompt = "{\"model\": \"" + "gpt-3.5-turno" + "\", \"messages\": [{\"role\": \"user\", \"content\": \"" + prompt + "\"}]}";

        return new ChatGPTResponse(chatGPT(prompt));
    }

    @PostMapping(value = "/chat")
    public ChatGPTResponse chat(@RequestParam("prompt") String prompt) {
        return new ChatGPTResponse(chatGPT(prompt));
    }


    public String chatGPT(String body) {
        String url = "https://api.openai.com/v1/chat/completions";
        String apiKey = System.getenv("OPENAI_API_KEY");
        String model = "gpt-3.5-turbo-0125";

        try {
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
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
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

    public String extractMessageFromJSONResponse (String response) {
        JsonObject jsonObject = new Gson().fromJson(response, JsonObject.class);

        return jsonObject.getAsJsonArray("choices")
                .get(0)
                .getAsJsonObject()
                .getAsJsonObject("message")
                .get("content")
                .getAsString();
    }


    static class ChatGPTResponse {
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
