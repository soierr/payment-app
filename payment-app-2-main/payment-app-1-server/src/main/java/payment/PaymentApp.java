package payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class PaymentApp {

    static void main(String[] args) {

        SpringApplication app = new SpringApplicationBuilder(PaymentApp.class)
                .build(args);
        app.run();
    }
}
