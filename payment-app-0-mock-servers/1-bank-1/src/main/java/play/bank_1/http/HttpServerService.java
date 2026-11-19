package play.bank_1.http;

import com.sun.net.httpserver.HttpServer;
import lombok.RequiredArgsConstructor;
import play.bank_1.exception.HttpServiceException;

import java.io.IOException;
import java.net.InetSocketAddress;

@RequiredArgsConstructor
public class HttpServerService {

    private final PaymentHttpHandler paymentHttpHandler;

    private HttpServer httpServer;

    public void start() {

        try {

            httpServer = HttpServer.create(new InetSocketAddress(8080), 0);
            httpServer.createContext("/payment", paymentHttpHandler);
            httpServer.start();

        } catch (IOException e) {
            throw new HttpServiceException("Failed to start http server");
        }
    }

    public void stop() {
        httpServer.stop(2);
    }
}
