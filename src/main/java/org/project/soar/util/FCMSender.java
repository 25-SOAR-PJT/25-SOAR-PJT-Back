package org.project.soar.util;

import com.google.firebase.messaging.*;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;


@Component
public class FCMSender {
    public String sendToToken(String token, String title, String body, Map<String, String> data)
            throws FirebaseMessagingException {

        Message.Builder builder = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder().setTitle(title).setBody(body).build());

        if (data != null && !data.isEmpty()) builder.putAllData(data);

        // 안드로이드(알림 채널/TTL 등) 필요 시:
        AndroidConfig android = AndroidConfig.builder()
                .setTtl(Duration.ofHours(1).toMillis())
                .setNotification(AndroidNotification.builder().build())
                .build();
        builder.setAndroidConfig(android);

        // iOS(APNs) 필요 시:
        ApnsConfig apns = ApnsConfig.builder()
                .putHeader("apns-priority", "10")
                .setAps(Aps.builder().setMutableContent(true).build())
                .build();
        builder.setApnsConfig(apns);

        Message message = builder.build();
        return FirebaseMessaging.getInstance().send(message); // messageId 반환
    }

    public String sendToTopic(String topic, String title, String body, Map<String, String> data) throws FirebaseMessagingException {
        Message.Builder builder = Message.builder()
                .setTopic(topic)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build());

        if (data != null && !data.isEmpty()) {
            builder.putAllData(data);
        }

        return FirebaseMessaging.getInstance().send(builder.build());
    }

    public BatchResponse sendMulticast(List<String> tokens, String title, String body, Map<String, String> data)
            throws FirebaseMessagingException {
        MulticastMessage.Builder mb = MulticastMessage.builder()
                .setNotification(Notification.builder().setTitle(title).setBody(body).build());
        if (data != null && !data.isEmpty()) mb.putAllData(data);
        mb.addAllTokens(tokens); // 최대 500개
        return FirebaseMessaging.getInstance().sendEachForMulticast(mb.build());
    }
}
