import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.Data;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class CrptApi {

    private final String CRPT_URL = "https://ismp.crpt.ru/api/v3/lk/documents/create";
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final TimeUnit timeUnit;
    private final int requestLimit;
    private final Bucket bucket;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = timeUnit;
        this.requestLimit = requestLimit;
        Bandwidth limit = Bandwidth.classic(requestLimit, Refill.intervally(requestLimit,
                Duration.ofSeconds(timeUnit.toSeconds(1))));
        bucket = Bucket.builder()
                .addLimit(limit)
                .build();
    }

    public void sendDocument(Document document, String signature) throws Exception {
        String requestBody = objectMapper.writeValueAsString(document);

        if (!checkSignature(signature)) {
            throw new Exception("signature is not good");
        }
        if (bucket.tryConsume(1)) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(CRPT_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } else {
            Thread.sleep(1000);
        }
    }

    private boolean checkSignature(String signature) {
        //some logic
        return true;
    }

    @Data
    public class Document {
        private Description description;
        @JsonProperty("doc_id")
        private String docId;
        @JsonProperty("doc_status")
        private String doc_Status;
        @JsonProperty("doc_type")
        private String docType;
        private boolean importRequest;
        @JsonProperty("owner_inn")
        private String ownerInn;
        @JsonProperty("participant_inn")
        private String participantInn;
        @JsonProperty("producerInn")
        private String producer_inn;
        @JsonProperty("productionDate")
        private String production_date;
        @JsonProperty("productionType")
        private String production_type;
        private List<Product> products;
        @JsonProperty("reg_date")
        private String regDate;
        @JsonProperty("reg_number")
        private String regNumber;

        @Data
        public class Description {
            private String participantInn;
        }

        @Data
        public class Product {
            @JsonProperty("certificate_document")
            private String certificateDocument;
            @JsonProperty("certificateDocumentDate")
            private String certificate_document_date;
            @JsonProperty("certificate_document_number")
            private String certificateDocumentNumber;
            @JsonProperty("ownerInn")
            private String owner_inn;
            @JsonProperty("producer_inn")
            private String producerInn;
            @JsonProperty("productionDate")
            private String production_date;
            @JsonProperty("tnved_code")
            private String tnvedCode;
            @JsonProperty("uit_code")
            private String uitCode;
            @JsonProperty("uitu_code")
            private String uituCode;
        }
    }

}
