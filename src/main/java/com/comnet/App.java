package com.comnet;


import java.io.*;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.util.Base64;
import java.nio.file.Files;

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
        String appPassword = "앱 비밀번호";
        String receiverEmail = "수신자 메일";
        sendCommand(writer, reader, encodeBase64(senderEmail));
        sendCommand(writer, reader, encodeBase64(appPassword));

        // MAIL FROM, RCPT TO
        sendCommand(writer, reader, "MAIL FROM: <" + senderEmail + ">");
        sendCommand(writer, reader, "RCPT TO: <" + receiverEmail + ".com>");

        // DATA
        sendCommand(writer, reader, "DATA");
        String boundary = "===" + System.currentTimeMillis() + "===";

        // MIME 헤더 작성
        writer.write("MIME-Version: 1.0\r\n");
        writer.write("Content-Type: multipart/mixed; boundary=\"" + boundary + "\"\r\n");

        writer.write("Subject: 제목\r\n");
        writer.write("From: " + senderEmail + "\r\n");
        writer.write("To: " + receiverEmail + "\r\n\r\n");

        // 이메일 본문 작성
        writer.write("--" + boundary + "\r\n");
        writer.write("Content-Type: text/plain; charset=UTF-8\r\n\r\n");
        writer.write("This is a test email with attachment.\r\n\r\n");

        // 첨부 파일 처리
        File file = new File("./atth.jpeg"); // 첨부할 파일 경로
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        String encodedFile = Base64.getEncoder().encodeToString(fileBytes);
        writer.write("--" + boundary + "\r\n");
        writer.write("Content-Type: application/octet-stream; name=\"" + file.getName() + "\"\r\n");
        writer.write("Content-Transfer-Encoding: base64\r\n");
        writer.write("Content-Disposition: attachment; filename=\"" + file.getName() + "\"\r\n\r\n");
        writer.write(encodedFile);
        writer.write("\r\n");

        // MIME 종료
        writer.write("--" + boundary + "--\r\n");

        // 이메일 종료
        writer.write(".\r\n");
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