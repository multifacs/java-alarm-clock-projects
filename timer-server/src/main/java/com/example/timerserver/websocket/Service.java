package com.example.timerserver.websocket;

import com.example.timerserver.logic.Timer;
import com.example.timerserver.models.Event;
import com.example.timerserver.repo.Repo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

@org.springframework.stereotype.Service
public class Service {

    private final SimpMessagingTemplate template;
    private final Timer timer;
    private Repo repository;

    @Autowired
    public Service(SimpMessagingTemplate template, Timer timer, Repo repository) {
        this.template = template;
        this.timer = timer;
        this.repository = repository;
    }

    List<Event> eventList;

    DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime( FormatStyle.MEDIUM )
            .withLocale( Locale.UK )
            .withZone( ZoneId.systemDefault() );

    void sendAll() throws Exception {
        System.out.println("Sending all");
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

    @Scheduled(fixedRate = 1000)
    public void sendTime() throws Exception {
        template.convertAndSend(
            "/topic/messages",
            timer.getCurrentTime().toString()
        );
        System.out.println("Sent time " + timer.getCurrentTime().toString());

        eventList = repository.findAll();

        if (eventList.size() > 0) {
            for (Event event : eventList) {
                if (ChronoUnit.SECONDS.between(timer.getCurrentTime(), event.getTime()) < 1) {
                    System.out.println(event.getId() + " is activated");
                    template.convertAndSend(
                            "/topic/messages",
                            "Occurred " + formatter.format( event.getTime() ).toString()
                    );
                    repository.delete(event);
                    eventList = repository.findAll();
                    sendAll();
                }
            }
        }

        timer.tick();
    }
}
