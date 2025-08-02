package dev.horbatiuk.timecapsule.service.aws;

import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.horbatiuk.timecapsule.exception.AppException;
import dev.horbatiuk.timecapsule.service.renderer.EmailTemplateRenderer;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;
import software.amazon.awssdk.services.ses.model.Destination;

import java.nio.charset.StandardCharsets;

@Slf4j
@NoArgsConstructor
public class SendCapsuleEmailHandler implements RequestHandler<Map<String, Object>, String> {

    private final String filesFolder = System.getenv("FILE_FOLDER_NAME");
    private final String dataFolder = System.getenv("DATA_FOLDER_NAME");

    private final String region = System.getenv("AWS_SERVICES_REGION");
    private final String bucketName = System.getenv("S3_BUCKET");
    private final String senderEmail = System.getenv("MAIL_ADDRESS");
    private final String accessKey = System.getenv("S3_ACCESS_KEY");
    private final String secretKey = System.getenv("S3_SECRET_KEY");

    private final S3Client s3Client = S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)))
            .build();
    private final SesClient sesClient = SesClient.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)
            ))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String handleRequest(Map<String, Object> event, Context context) {
        try {
            String capsuleId = (String) event.get("capsuleId");
            String key = dataFolder + "/" + capsuleId + ".json";
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            String json = s3Client.getObjectAsBytes(request)
                    .asString(StandardCharsets.UTF_8);
            CapsuleEmailMetadata metadata = objectMapper.readValue(json, CapsuleEmailMetadata.class);
            sendEmail(metadata, getPresignedUrls(capsuleId));
            return "Email sent to " + metadata.email();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException("Handler error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void sendEmail(CapsuleEmailMetadata metadata, List<URL> presignedUrls) {
        Destination destination = Destination.builder()
                .toAddresses(metadata.email())
                .build();
        EmailTemplateRenderer renderer = new EmailTemplateRenderer();
        String htmlBody = renderer.renderEmail(metadata, presignedUrls);
        Message message = Message.builder()
                .subject(Content.builder().data("⏳ Your Time Capsule is Open!").charset("UTF-8").build())
                .body(Body.builder()
                        .html(Content.builder()
                                .data(htmlBody)
                                .charset("UTF-8")
                                .build())
                        .build())
                .build();
        SendEmailRequest request = SendEmailRequest.builder()
                .destination(destination)
                .message(message)
                .source(senderEmail)
                .build();
        sesClient.sendEmail(request);
    }

    public List<URL> getPresignedUrls(String capsuleId) {
        S3Presigner presigner = S3Presigner.create();
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(filesFolder + "/"  + capsuleId + "/")
                .build();
        ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
        List<URL> urls = new ArrayList<>();
        for (S3Object s3Object : listResponse.contents()) {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Object.key())
                    .build();
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .getObjectRequest(getObjectRequest)
                    .signatureDuration(Duration.ofDays(7))
                    .build();
            URL url = presigner.presignGetObject(presignRequest).url();
            urls.add(url);
        }
        return urls;
    }

    public record CapsuleEmailMetadata(
            String capsuleId,
            String email,
            String username,
            String title,
            String description,
            String createdAt,
            String openAt
    ) {}
}

