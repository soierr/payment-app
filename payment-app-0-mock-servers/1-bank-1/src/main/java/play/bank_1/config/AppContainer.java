package play.bank_1.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import play.bank_1.http.HttpServerService;
import play.bank_1.http.PaymentHttpHandler;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AppContainer {

    private final MutablePicoContainer mutablePicoContainer = new DefaultPicoContainer();

    private void configureContainer() {

        mutablePicoContainer.addComponent(PaymentHttpHandler.class);
        mutablePicoContainer.addComponent(HttpServerService.class);
        mutablePicoContainer.start();
    }

    private void startHttp() {
        mutablePicoContainer.getComponent(HttpServerService.class).start();
    }

    public static void start() {
        AppContainer appContainer = new AppContainer();
        appContainer.configureContainer();
        IO.println("Dependency container configured and started");
        appContainer.startHttp();
    }
}
