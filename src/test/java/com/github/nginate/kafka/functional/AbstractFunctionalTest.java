package com.github.nginate.kafka.functional;

import com.github.nginate.commons.docker.DockerUtils;
import com.github.nginate.commons.docker.client.DockerClientOptions;
import com.github.nginate.commons.docker.client.NDockerClient;
import com.github.nginate.commons.docker.client.options.CreateContainerOptions;
import com.github.nginate.commons.docker.wrapper.DockerContainer;
import com.github.nginate.kafka.TestProperties;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.nginate.commons.lang.NStrings.format;
import static com.github.nginate.kafka.DockerConfigs.kafkaContainerConfiguration;

@Slf4j
@Test(groups = "integration")
public abstract class AbstractFunctionalTest {

    private static final AtomicInteger KAFKA_PORT_COUNTER = new AtomicInteger(57900);
    private static final AtomicInteger ZOOKEEPER_PORT_COUNTER = new AtomicInteger(58900);

    @Getter
    private DockerContainer kafkaContainer;
    @Getter
    private static TestProperties testProperties;
    @Getter
    private ZkClient zkClient;
    @Getter
    private String kafkaHost;
    @Getter
    private int kafkaPort;
    @Getter
    private int zookeeperPort;

    @BeforeSuite
    public void initProperties() throws Exception {
        testProperties = new TestProperties();
        populateProperties(testProperties, "test.properties");
    }

    @BeforeClass
    public void initDockerContainer() throws Exception {
        DockerClientOptions clientOptions = DockerUtils.defaultClientOptions().withReadTimeout(null);
        String dockerHost = clientOptions.getDockerUri().startsWith("http") ?
                InetAddress.getByName(URI.create(clientOptions.getDockerUri()).getHost()).getHostAddress() :
                "localhost";
        NDockerClient dockerClient = DockerUtils.createClient(clientOptions);

        kafkaHost = dockerHost;
        kafkaPort = KAFKA_PORT_COUNTER.getAndIncrement();
        zookeeperPort = ZOOKEEPER_PORT_COUNTER.getAndIncrement();

        CreateContainerOptions kafkaContainerConfiguration =
                kafkaContainerConfiguration(getKafkaBrokerVersion(), kafkaHost, kafkaPort, zookeeperPort);
        kafkaContainer = DockerUtils.forceCreateContainer(dockerClient, kafkaContainerConfiguration);
        kafkaContainer.start();
        kafkaContainer.awaitStarted();

        log.info("Kafka started on {}:{}", kafkaHost, kafkaPort);

        zkClient = new ZkClient(format("{}:{}", dockerHost, zookeeperPort));
    }

    @AfterClass(alwaysRun = true)
    public void tearDownDockerContainer() throws Exception {
        zkClient.close();
        kafkaContainer.stop();
        kafkaContainer.printLogs();
        kafkaContainer.remove();
    }

    protected abstract String getKafkaBrokerVersion();

    private void populateProperties(Object bean, String propertiesFileName) throws Exception {
        Properties properties = loadProperties(propertiesFileName);
        BeanUtils.populate(bean, Maps.fromProperties(properties));
    }

    private Properties loadProperties(String fileName) throws IOException {
        Properties properties = new Properties();
        try (InputStream in = FileUtils.openInputStream(FileUtils.getFile("src","test", "resources", fileName))) {
            properties.load(in);
        }
        return properties;
    }
}
