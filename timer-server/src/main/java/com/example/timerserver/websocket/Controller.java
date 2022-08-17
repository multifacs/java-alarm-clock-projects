package com.example.timerserver.websocket;

import com.example.timerserver.models.Event;
import com.example.timerserver.repo.Repo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@org.springframework.stereotype.Controller
public class Controller {

    private Repo repository;
    private final SimpMessagingTemplate template;

    @Autowired
    public Controller(Repo repository, SimpMessagingTemplate template) {
        this.repository = repository;
        this.template = template;
    }

    DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime( FormatStyle.MEDIUM )
            .withLocale( Locale.UK )
            .withZone( ZoneId.systemDefault() );

    void sendAll() throws Exception {
        System.out.println("Sending all");
        List<Event> eventList = repository.findAll();
        String eventListString = "Events ";

        for (Event event : eventList) {
            eventListString += formatter.format( event.getTime() ) + "\n";
        }

        eventListString = eventListString.substring(0, eventListString.length() - 1);

        System.out.println(eventListString);

        template.convertAndSend(
                "/topic/messages",
                eventListString
        );
    }

    @MessageMapping("/chat")
    public void receiveMsg(String message) throws Exception {
        System.out.println("New client");
        sendAll();
    }

    @MessageMapping("/event")
    public void receiveEvent(String message) throws Exception {
        System.out.println("Received event: " + message);
        Event event = new Event(UUID.randomUUID().toString(), Instant.parse(message));
        repository.save(event);
        sendAll();
    }
}
