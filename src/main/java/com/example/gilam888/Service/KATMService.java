//package com.example.gilam888.Service;
//
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//
//public class KATMService {
//    private static final String KATM_API_URL = "https://infokredit.uz";
//    private static final String AUTH_TOKEN = "Bearer SIZNING_API_TOKENINGIZ";
//
//    public static String checkCustomerScoring(String pinfl, String passportSeries, String passportNumber) {
//        try {
//            // 1. JSON Request Body tayyorlash
//            JSONObject requestBody = new JSONObject();
//            requestBody.put("pinfl", pinfl);
//            requestBody.put("passport_series", passportSeries);
//            requestBody.put("passport_number", passportNumber);
//
//            // 2. HTTP Request yaratish
//            HttpClient client = HttpClient.newHttpClient();
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create(KATM_API_URL))
//                    .header("Content-Type", "application/json")
//                    .header("Authorization", AUTH_TOKEN)
//                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
//                    .build();
//
//            // 3. So'rovni yuborish va javobni olish
//            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//
//            return response.body();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "{\"status\": \"error\", \"message\": \"" + e.getMessage() + "\"}";
//        }
//    }
//}
