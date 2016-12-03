package cxc.jex.tracer.logback;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.mail.*;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.boolex.OnMarkerEvaluator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * SMTPAppender replacement, the reason http://jira.qos.ch/browse/LOGBACK-1158
 * 
 * @author gimatdinov
 *
 */
public class EmailAppender extends AppenderBase<ILoggingEvent> {

    private String markers;
    private OnMarkerEvaluator evaluator;

    private String smtpHost;
    private int smtpPort = 25;
    private boolean ssl = false;
    private boolean startTLS = false;

    private ExecutorService executor;

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
        subjectLayout = new PatternLayout();
        subjectLayout.setContext(context);
        subjectLayout.setPattern(subject);
        subjectLayout.start();

        bodyLayout = new PatternLayout();
        bodyLayout.setContext(context);
        bodyLayout.setPattern(body);
        bodyLayout.start();

        evaluator = new OnMarkerEvaluator();
        evaluator.setContext(context);
        for (String item : markers.split(",")) {
            evaluator.addMarker(item.trim());
        }
        evaluator.start();
        executor = Executors.newCachedThreadPool();

        super.start();
    }

    @Override
    public void stop() {
        evaluator.stop();
        bodyLayout.stop();
        subjectLayout.stop();
        executor.shutdown();
        super.stop();
    }

    @Override
    public void append(ILoggingEvent event) {
        try {
            if (evaluator.evaluate(event)) {
                final Email email = new HtmlEmail();
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
                String subjectText = subjectLayout.doLayout(event);
                subjectText = subjectText.length() > 100 ? subjectText.substring(0, 96) + "..." : subjectText;
                email.setSubject(subjectText);
                email.setMsg(bodyLayout.doLayout(event));
                executor.submit(new Runnable() {
                    public void run() {
                        try {
                            email.send();
                        } catch (Exception e) {
                            System.out.println("Appender[" + getName() + "] : " + e.getMessage());
                        }
                    }
                });
            }
        } catch (Exception ex) {
            System.out.println("Appender[" + getName() + "] : " + ex.getMessage());
        }
    }

    public boolean isStartTLS() {
        return startTLS;
    }

    public void setStartTLS(boolean startTLS) {
        this.startTLS = startTLS;
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
