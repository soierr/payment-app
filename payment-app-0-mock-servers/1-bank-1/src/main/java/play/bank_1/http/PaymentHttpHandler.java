package play.bank_1.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

public class PaymentHttpHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        InputStream requestBody = exchange.getRequestBody();

        int result;

        byte[] bytes = new byte[1024];

        StringBuilder request = new StringBuilder();

        result = requestBody.read(bytes, 0, bytes.length);

        while (result != -1) {

            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes, 0, result);

            CharBuffer charBuffer = StandardCharsets.UTF_8.decode(byteBuffer);

            request.append(charBuffer);

            result = requestBody.read(bytes, 0, bytes.length);
        }

        requestBody.close();

        System.out.println("\nReceived request: " + request);

        exchange.sendResponseHeaders(200, 0);
        exchange.getResponseBody().close();

    }
}
