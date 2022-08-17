package com.example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.ZoneId;
import javax.swing.*;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

public class TimerClient extends JFrame implements ActionListener {

    private final JButton connect;
    private final JButton close;
    private final JTextArea ta;
    private final JTextField chatField;
    private final JLabel timeLabel;
    private final JLabel eventLabel;
    private WebSocketClient cc;

    public TimerClient(String defaultlocation) {
        super("Timer app");
        Container c = getContentPane();
        GridLayout layout = new GridLayout();
        layout.setColumns(1);
        layout.setRows(6);
        c.setLayout(layout);

        connect = new JButton("Connect");
        connect.addActionListener(this);
        c.add(connect);

        close = new JButton("Close");
        close.addActionListener(this);
        close.setEnabled(false);
        c.add(close);

        //JScrollPane scroll = new JScrollPane();
        ta = new JTextArea();
        //scroll.setViewportView(ta);
        c.add(ta);
        ta.setSize(100,200);

        chatField = new JTextField();
        chatField.setText("");
        chatField.addActionListener(this);
        chatField.setSize(100,50);
        c.add(chatField);

        timeLabel = new JLabel();
        timeLabel.setText("time:time");
        c.add(timeLabel);

        eventLabel = new JLabel();
        eventLabel.setText("event");
        c.add(eventLabel);

        java.awt.Dimension d = new java.awt.Dimension(300, 400);
        setPreferredSize(d);
        setSize(d);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (cc != null) {
                    cc.close();
                }
                dispose();
            }
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == chatField) {
            if (cc != null) {
                cc.send(chatField.getText());
                chatField.setText("");
                chatField.requestFocus();
            }

        } else if (e.getSource() == connect) {
            try {
                // cc = new ChatClient(new URI(uriField.getText()), area, ( Draft ) draft.getSelectedItem() );
                cc = new WebSocketClient(new URI("ws://localhost:8887")) {

                    @Override
                    public void onMessage(String message) {
                        if (message.contains("time")) {
                            Instant time = Instant.parse(message.substring(5));
                            timeLabel.setText(Integer.toString(time.atZone(ZoneId.systemDefault()).getMinute()) + ":" + Integer.toString(time.atZone(ZoneId.systemDefault()).getSecond()));
                        } else if (message.contains("event")) {
                            eventLabel.setText(message.substring(6));
                        } else {
                            ta.setText(message);
                            ta.setCaretPosition(ta.getDocument().getLength());
                        }
                    }

                    @Override
                    public void onOpen(ServerHandshake handshake) {
                        //ta.append("You are connected to ChatServer: " + getURI() + "\n");
                        ta.setCaretPosition(ta.getDocument().getLength());
                    }

                    @Override
                    public void onClose(int code, String reason, boolean remote) {
                        // ta.append(
                                // "You have been disconnected from: " + getURI() + "; Code: " + code + " " + reason
                                    //    + "\n");
                        ta.setCaretPosition(ta.getDocument().getLength());
                        connect.setEnabled(true);
                        close.setEnabled(false);
                    }

                    @Override
                    public void onError(Exception ex) {
                        ta.append("Exception occurred ...\n" + ex + "\n");
                        ta.setCaretPosition(ta.getDocument().getLength());
                        ex.printStackTrace();
                        connect.setEnabled(true);
                        close.setEnabled(false);
                    }
                };

                close.setEnabled(true);
                connect.setEnabled(false);
                cc.connect();
            } catch (URISyntaxException ex) {
                ta.append("not a valid WebSocket URI\n");
            }
        } else if (e.getSource() == close) {
            cc.close();
        }
    }

    public static void main(String[] args) {
        String location = "ws://localhost:8887";
        new TimerClient(location);
    }

}
