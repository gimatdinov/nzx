package cxc.jex.tracer.logback;

import org.apache.commons.mail.*;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.boolex.OnMarkerEvaluator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.boolex.EvaluationException;

public class EmailAppender extends AppenderBase<ILoggingEvent> {

    private String markers;
    private OnMarkerEvaluator evaluator;

    private String smtpHost;
    private int smtpPort = 25;
    private boolean ssl = false;
    private boolean startTLS = false;

    public boolean isStartTLS() {
        return startTLS;
    }

    public void setStartTLS(boolean startTLS) {
        this.startTLS = startTLS;
    }

    private String username;
    private String password;
    private String from;
    private String to;
    private String subject;
    private PatternLayout subjectLayout;
    private String body;
    private PatternLayout bodyLayout;

    @Override
    public void start() {
        evaluator = new OnMarkerEvaluator();
        evaluator.setContext(context);
        for (String item : markers.split(",")) {
            evaluator.addMarker(item.trim());
        }
        evaluator.start();

        subjectLayout = new PatternLayout();
        subjectLayout.setContext(context);
        subjectLayout.setPattern(subject);
        subjectLayout.start();

        bodyLayout = new PatternLayout();
        bodyLayout.setContext(context);
        bodyLayout.setPattern(body);
        bodyLayout.start();

        super.start();
    }

    public void append(ILoggingEvent event) {
        try {
            if (evaluator.evaluate(event)) {
                try {
                    Email email = new SimpleEmail();
                    email.setCharset("UTF-8");
                    email.setHostName(smtpHost);
                    email.setSmtpPort(smtpPort);
                    if (username != null && username.length() > 0) {
                        email.setAuthenticator(new DefaultAuthenticator(username, password));
                    }
                    email.setSSLOnConnect(ssl);
                    email.setStartTLSEnabled(startTLS);
                    email.setFrom(from);
                    for (String item : to.split(",")) {
                        email.addTo(item.trim());
                    }
                    email.setSubject(subjectLayout.doLayout(event));
                    email.setMsg(bodyLayout.doLayout(event));
                    email.send();
                } catch (Exception e) {
                    System.out.println("Appender[" + getName() + "] : " + e.getMessage());
                }
            }
        } catch (EvaluationException ex) {
            System.out.println("Appender[" + getName() + "] : " + ex.getMessage());
        }
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(int smtpPort) {
        this.smtpPort = smtpPort;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getMarkers() {
        return markers;
    }

    public void setMarkers(String markers) {
        this.markers = markers;
    }

}
