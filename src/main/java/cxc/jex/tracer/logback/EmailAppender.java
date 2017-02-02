package cxc.jex.tracer.logback;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

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
                Properties props = new Properties();
                props.put("mail.smtp.host", smtpHost);
                props.put("mail.smtp.socketFactory.port", smtpPort);
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.socketFactory.fallback", "false");
                props.put("mail.smtp.starttls.enable", startTLS);
                props.put("mail.smtp.ssl.trust", "*");
                props.put("mail.smtp.auth", true);
                props.put("mail.smtp.port", smtpPort);
                Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

                final MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(from));
                String[] emails = to.split(",");
                InternetAddress dests[] = new InternetAddress[emails.length];
                for (int i = 0; i < emails.length; i++) {
                    dests[i] = new InternetAddress(emails[i].trim().toLowerCase());
                }
                message.setRecipients(Message.RecipientType.TO, dests);
                String subjectText = subjectLayout.doLayout(event);
                subjectText = subjectText.length() > 100 ? subjectText.substring(0, 96) + "..." : subjectText;
                message.setSubject(subjectText, "UTF-8");
                Multipart mp = new MimeMultipart();
                MimeBodyPart mbp = new MimeBodyPart();
                mbp.setContent(bodyLayout.doLayout(event), "text/html;charset=utf-8");
                mp.addBodyPart(mbp);
                message.setContent(mp);
                message.setSentDate(new java.util.Date());

                executor.submit(new Runnable() {
                    public void run() {
                        try {
                            Transport.send(message);
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
