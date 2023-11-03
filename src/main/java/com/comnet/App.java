package com.comnet;


import java.io.*;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.util.Base64;

public class App {
    private static final String SMTP_SERVER = "smtp.naver.com";
    private static final int SMTPS_PORT = 465; // SMTPS 포트

    public static void main(String[] args) throws IOException {
        // SMTP 서버와의 연결 설정
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket socket = (SSLSocket) factory.createSocket(SMTP_SERVER, SMTPS_PORT);

        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        // SMTP 서버의 응답 메시지 출력
        System.out.println(reader.readLine());

        // HELO/EHLO
        sendCommand(writer, reader, "EHLO localhost");

        // 로그인 (AUTH LOGIN)
        sendCommand(writer, reader, "AUTH LOGIN");

        String senderEmail = "발신자 메일";
        String AppPassword = "앱 비밀번호";
        sendCommand(writer, reader, encodeBase64(senderEmail));
        sendCommand(writer, reader, encodeBase64(AppPassword));

        // MAIL FROM, RCPT TO
        sendCommand(writer, reader, "MAIL FROM: <발신자 메일>");
        sendCommand(writer, reader, "RCPT TO: <받는사람 메일>");

        // DATA
        sendCommand(writer, reader, "DATA");
        writer.write("Subject: Test Email\r\n");
        writer.write("From: 발신자 메일\r\n");
        writer.write("To: 받는사람 메일\r\n\r\n");
        writer.write("This is a test email.\r\n.\r\n");
        writer.flush();
        System.out.println(reader.readLine());

        // QUIT
        sendCommand(writer, reader, "QUIT");
        socket.close();
    }

    public static void sendCommand(BufferedWriter writer, BufferedReader reader, String command) throws IOException {
        writer.write(command + "\r\n");
        writer.flush();
        System.out.println("> " + command);
        System.out.println(reader.readLine());
    }

    public static String encodeBase64(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes());
    }
}