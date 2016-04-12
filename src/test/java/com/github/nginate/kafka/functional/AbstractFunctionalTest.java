package com.github.nginate.kafka.functional;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.nginate.kafka.TestProperties;
import com.github.nginate.kafka.docker.DockerContainer;
import com.github.nginate.kafka.docker.DockerWrapper;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.github.nginate.kafka.docker.DockerConfigs.kafkaContainerConfiguration;

@Slf4j
public abstract class AbstractFunctionalTest {
    @Getter
    private DockerContainer kafkaContainer;
    @Getter
    private TestProperties testProperties;
    @Getter
    private ZkClient zkClient;

    @BeforeSuite
    public void initProperties() throws Exception {
        testProperties = new TestProperties();
        populateProperties(testProperties, "application.properties");
    }

    @BeforeClass
    public void initDockerContainer() throws Exception {
        DockerClient dockerClient = DockerClientBuilder.getInstance(testProperties.getDockerUrl()).build();
        kafkaContainer = new DockerWrapper.Builder()
                .dockerClient(dockerClient)
                .containerConfiguration(kafkaContainerConfiguration(testProperties.getKafkaPort()))
                .useSlf4jInfoLogging()
                .build();
        kafkaContainer.pullImage();
        kafkaContainer.create();
        kafkaContainer.start();
        log.info("Kafka started on {}", kafkaContainer.getIp());

        zkClient = new ZkClient(String.format("%s:%d", kafkaContainer.getIp(), testProperties.getZookeeperPort()));
    }

    @AfterClass(alwaysRun = true)
    public void tearDownDockerContainer() throws Exception {
        zkClient.close();
        kafkaContainer.forceStop();
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
