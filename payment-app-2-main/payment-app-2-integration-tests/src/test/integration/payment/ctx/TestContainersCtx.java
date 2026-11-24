package payment.ctx;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestContainersCtx {

    @Bean
    TestContainerNginx containerNginx() {
        return new TestContainerNginx();
    }
}
