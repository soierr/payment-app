package payment.ctx;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.Capability;
import com.github.dockerjava.api.model.Network;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Optional;

public class TestContainerNginx {

    private String containerIp;
    private GenericContainer<?> nginxContainer;
    private DockerClient dockerClient;

    @PostConstruct
    void createContainer() {

        nginxContainer = new GenericContainer<>(DockerImageName.parse("play-nginx:1.29.5"));

        dockerClient = DockerClientFactory.instance().client();

        nginxContainer.withExposedPorts(8080, 8082, 8084)
                .withReuse(false)
                .withCreateContainerCmdModifier(this::createCmdModifier)
                .withFileSystemBind("src/test/resources/nginx/nginx0-default.conf",
                        "/etc/nginx/conf.d/default.conf", BindMode.READ_ONLY)
                .withFileSystemBind("src/test/resources/nginx/nginx1.conf",
                        "/etc/nginx/nginx.conf", BindMode.READ_ONLY);

        List<Network> networks = dockerClient.listNetworksCmd()
                .withNameFilter("payment-network")
                .exec();

        boolean networkExist = networks.stream()
                .anyMatch(n -> "payment-network".equals(n.getName()));

        if (!networkExist) {
            nginxContainer.withNetwork(org.testcontainers.containers.Network.builder()
                    .createNetworkCmdModifier(cmd -> cmd.withName("payment-network"))
                    .build());
        }

        nginxContainer.setNetwork(new org.testcontainers.containers.Network() {
            @Override
            public String getId() {
                return "payment-network";
            }

            @Override
            public void close() {
                System.out.println("Network closed");
            }
        });

        nginxContainer.start();

        containerIp = nginxContainer.getCurrentContainerInfo().getNetworkSettings()
                .getNetworks().entrySet().stream()
                .filter(e -> "payment-network".equals(e.getKey()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("payment-network is not found"))
                .getValue()
                .getIpAddress();

    }

    private void createCmdModifier(CreateContainerCmd createContainerCmd) {

        Optional.of(createContainerCmd)
                .map(hf -> hf.withName("play-nginx"))
                .map(CreateContainerCmd::getHostConfig)
                .map(hc -> hc.withCapAdd(Capability.NET_ADMIN))
                .orElseThrow(() -> new RuntimeException("Couldn't create container"));
    }

    @PreDestroy
    void destroyContainer() throws Exception {
        dockerClient.close();
        nginxContainer.close();
    }

    public String getIp() {
        return containerIp;
    }
}
