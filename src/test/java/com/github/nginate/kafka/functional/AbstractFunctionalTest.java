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
import java.net.URI;
import java.util.Properties;

import static com.github.nginate.commons.lang.NStrings.format;
import static com.github.nginate.kafka.DockerConfigs.kafkaContainerConfiguration;

@Slf4j
@Test(groups = "integration")
public abstract class AbstractFunctionalTest {
    @Getter
    private DockerContainer kafkaContainer;
    @Getter
    private static TestProperties testProperties;
    @Getter
    private ZkClient zkClient;
    @Getter
    private String kafkaHost;

    @BeforeSuite
    public void initProperties() throws Exception {
        testProperties = new TestProperties();
        populateProperties(testProperties, "application.properties");
    }

    @BeforeClass
    public void initDockerContainer() throws Exception {
        DockerClientOptions clientOptions = DockerUtils.defaultClientOptions().withReadTimeout(null);
        String dockerHost = clientOptions.getDockerUri().startsWith("http") ?
                URI.create(clientOptions.getDockerUri()).getHost() :
                "localhost";
        NDockerClient dockerClient = DockerUtils.createClient(clientOptions);

        kafkaHost = dockerHost;

        CreateContainerOptions kafkaContainerConfiguration = kafkaContainerConfiguration(kafkaHost,
                testProperties.getKafkaPort(), testProperties.getZookeeperPort());
        kafkaContainer = DockerUtils.forceCreateContainer(dockerClient, kafkaContainerConfiguration);
        kafkaContainer.start();
        kafkaContainer.awaitStarted();

        log.info("Kafka started on {}", kafkaHost);

        zkClient = new ZkClient(format("{}:{}", dockerHost, testProperties.getZookeeperPort()));
    }

    @AfterClass(alwaysRun = true)
    public void tearDownDockerContainer() throws Exception {
        zkClient.close();
        kafkaContainer.stop();
        kafkaContainer.printLogs();
        kafkaContainer.remove();
    }

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
