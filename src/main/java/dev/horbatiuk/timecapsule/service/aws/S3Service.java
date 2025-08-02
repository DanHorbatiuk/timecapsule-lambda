package dev.horbatiuk.timecapsule.service.aws;

import dev.horbatiuk.timecapsule.exception.AppException;
import dev.horbatiuk.timecapsule.persistence.dto.capsule.CapsuleResponseDTO;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final String bucketName = System.getenv("AWS_BUCKET");
    private final String filesFolder = System.getenv("FILE_FOLDER_NAME");
    private final String dataFolder = System.getenv("DATA_FOLDER_NAME");

    private final String region = System.getenv("AWS_SERVICES_REGION");
    private final String accessKey = System.getenv("S3_ACCESS_KEY");
    private final String secretKey = System.getenv("S3_SECRET_KEY");

    private final S3Client s3Client = S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)
            ))
            .build();

    private String buildKeyForFile(String capsuleId, String filename) {
        return filesFolder != null && !filesFolder.isBlank()
                ? filesFolder + "/" + capsuleId + "/" + filename
                : filename;
    }

    private String buildKeyForData(String capsuleId) {
        return dataFolder != null && !dataFolder.isBlank()
                ? dataFolder + "/" + capsuleId + ".json"
                : capsuleId;
    }

    public void uploadFile(String capsuleId, String filename, InputStream inputStream,
                           long contentLength, String contentType) {
        String key = buildKeyForFile(capsuleId, filename);
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();
        try {
            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, contentLength));
        } catch (AwsServiceException | SdkClientException e){
            System.out.printf(e.getMessage());
            throw new AppException("S3 upload failed for: " + filename, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void deleteFile(String capsuleId, String filename) {
        String key = buildKeyForFile(capsuleId, filename);
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        try {
            s3Client.deleteObject(request);
        } catch (AwsServiceException | SdkClientException e){
            throw new AppException("Delete file " + filename + " failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public byte[] getFile(String capsuleId, String filename) {
        String key = buildKeyForFile(capsuleId, filename);
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        return s3Client.getObjectAsBytes(request).asByteArray();
    }

    public void uploadCapsuleData(
            CapsuleResponseDTO capsule
    ) {
        JSONObject sendingData = new JSONObject();
        try {
            sendingData.put("capsuleId", capsule.getId());
            sendingData.put("title", capsule.getTitle());
            sendingData.put("email", capsule.getEmail());
            sendingData.put("username", capsule.getUsername());
            sendingData.put("description", capsule.getDescription());
            sendingData.put("createdAt", capsule.getCreatedAt());
            sendingData.put("openAt", capsule.getOpenAt());
        } catch (Exception e) {
            throw new AppException("Sending data failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        String key = buildKeyForData(capsule.getId().toString());
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType("application/json")
                .build();
        try {
            byte[] jsonBytes = sendingData.toString().getBytes(StandardCharsets.UTF_8);
            s3Client.putObject(request, RequestBody.fromBytes(jsonBytes));
        } catch (AwsServiceException | SdkClientException e){
            throw new AppException("S3 upload failed for capsule: " + capsule.getId().toString(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}