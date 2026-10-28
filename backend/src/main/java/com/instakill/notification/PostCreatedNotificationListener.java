package com.instakill.notification;

import com.instakill.post.domain.PostCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Sends a plain-text push notification to a public ntfy.sh topic whenever a post is created.
 * Runs asynchronously after the post transaction commits, so a slow or failing notification
 * service never delays or breaks post creation.
 */
@Component
public class PostCreatedNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(PostCreatedNotificationListener.class);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final boolean enabled;
    private final String url;

    public PostCreatedNotificationListener(
            @Value("${notification.enabled:true}") boolean enabled,
            @Value("${notification.ntfy.base-url:https://ntfy.sh}") String baseUrl,
            @Value("${notification.ntfy.topic:instakill-demo}") String topic) {
        this.enabled = enabled;
        this.url = baseUrl.replaceAll("/+$", "") + "/" + topic;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onPostCreated(PostCreatedEvent event) {
        if (!enabled) {
            return;
        }
        String message = "New post by " + event.username()
                + "\nPost ID: " + event.postId()
                + "\nCreated at: " + event.createdAt();
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("Title", "Instakill: new post")
                .header("Tags", "camera")
                .POST(HttpRequest.BodyPublishers.ofString(message, StandardCharsets.UTF_8))
                .build();
        try {
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() >= 300) {
                log.warn("Notification service returned HTTP {} for post {}", response.statusCode(), event.postId());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("Failed to send notification for post {}: {}", event.postId(), e.toString());
        }
    }
}
