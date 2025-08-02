package dev.horbatiuk.timecapsule.service.aws;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.scheduler.SchedulerClient;
import software.amazon.awssdk.services.scheduler.model.CreateScheduleRequest;
import software.amazon.awssdk.services.scheduler.model.FlexibleTimeWindow;
import software.amazon.awssdk.services.scheduler.model.Target;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventBridgeScheduledService {

    private final String lambdaArn = System.getenv("LAMBDA_ARN") ;
    private final String schedulerRoleArn = System.getenv("SCHEDULER_ROLE_ARN");

    private final String region = System.getenv("AWS_SERVICES_REGION");
    private final String accessKey = System.getenv("S3_ACCESS_KEY");
    private final String secretKey = System.getenv("S3_SECRET_KEY");

    private final SchedulerClient schedulerClient = SchedulerClient.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)
            ))
            .build();;


    public void scheduleLambdaTrigger(UUID capsuleId, Instant openAt) {
        String scheduleName = capsuleId.toString();

        OffsetDateTime odt = openAt.atOffset(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String scheduleExpression = "at(" + odt.format(formatter) + ")";

        CreateScheduleRequest scheduleRequest = CreateScheduleRequest.builder()
                .name(scheduleName)
                .scheduleExpression(scheduleExpression)
                .flexibleTimeWindow(FlexibleTimeWindow.builder().mode("OFF").build())
                .target(Target.builder()
                        .arn(lambdaArn)
                        .roleArn(schedulerRoleArn)
                        .input("{\"capsuleId\": \"" + capsuleId + "\"}")
                        .build())
                .build();

        schedulerClient.createSchedule(scheduleRequest);
    }

}
