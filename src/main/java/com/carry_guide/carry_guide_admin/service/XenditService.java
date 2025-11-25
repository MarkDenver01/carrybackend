package com.carry_guide.carry_guide_admin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class XenditService {
    @Value("${xendit.secretkey}")
    private String secretKey;

    @Value("${xendit.baseurl}")
    private String xenditBaseUrl;

    @Value("${xendit.success.redirect-url}")
    private String successRedirect;

    @Value("${xendit.failed.redirect-url}")
    private String failedRedirect;


    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public JsonNode createGCashPayment(String userId, int amount) throws Exception {
        String referenceId = "cashin-" + userId + "-" + UUID.randomUUID();
        String url = xenditBaseUrl + "/ewallets/charges";

        // Request JSON body
        String json = """
        {
          "reference_id": "%s",
          "currency": "PHP",
          "amount": %d,
          "checkout_method": "ONE_TIME_PAYMENT",
          "channel_code": "PH_GCASH",
          "channel_properties": {
            "success_redirect_url": "%s",
            "failure_redirect_url": "%s"
          },
          "metadata": {
            "user_id": "%s"
          }
        }
        """.formatted(
                referenceId,
                amount,
                successRedirect,
                failedRedirect,
                userId
        );

        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        String credentials = Credentials.basic(secretKey, "");

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", credentials)
                .build();

        Response response = client.newCall(request).execute();

        String responseBody = response.body().string();
        System.out.println("XENDIT CREATE PAYMENT RESPONSE: " + responseBody);

        return mapper.readTree(responseBody);
    }
}
