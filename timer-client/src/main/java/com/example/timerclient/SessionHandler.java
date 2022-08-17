package com.example.timerclient;

import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.Locale;


public class SessionHandler extends StompSessionHandlerAdapter {

    TimeData timeData;
    Instant time;
    EventContainer eventContainer;
    OccurredData occurredData;

    StompSession session;

    DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime( FormatStyle.MEDIUM )
            .withLocale( Locale.UK )
            .withZone( ZoneId.systemDefault() );

    public SessionHandler(TimeData timeData, EventContainer eventContainer, OccurredData occurredData) {
        this.timeData = timeData;
        this.eventContainer = eventContainer;
        this.occurredData = occurredData;
    }

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        System.out.println("New session established : " + session.getSessionId());
        session.subscribe("/topic/messages", this);
        System.out.println("Subscribed to /topic/messages");
        session.send("/app/chat", "Get");
        this.session = session;
    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        System.out.println(exception);
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return String.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        String msg = (String) payload;

        if (msg.contains("Events")) {
            if (msg.length() > 6) {
                eventContainer.setMsg(msg.substring(7));
            } else {
                eventContainer.setMsg("");
            }
        } else if (msg.contains("Occurred")) {
            occurredData.setMsg(msg.substring(9));
        } else {
            time = Instant.parse(msg);
            timeData.setMsg(formatter.format(time));
        }
    }

    @Override
    public void handleTransportError(StompSession session, Throwable exception) {
        System.out.println("Connection dropped");
        // todo: schedule a reconnection attempt
    }

    public void sendMsg(String msg) {
        if (msg.length() == 5) {
            Instant newEvent = Instant.now();

            int minutes = Integer.parseInt(msg.substring(0, 2));
            int seconds = Integer.parseInt(msg.substring(3, 5));

            System.out.println(minutes + " " + seconds);

            newEvent = newEvent.plus(minutes, ChronoUnit.MINUTES);
            newEvent = newEvent.plus(seconds, ChronoUnit.SECONDS);

            session.send("/app/event", newEvent.toString());
            System.out.println("Sending " + newEvent.toString());
            return;
        }

        System.out.println("Wrong format");
    }
}
