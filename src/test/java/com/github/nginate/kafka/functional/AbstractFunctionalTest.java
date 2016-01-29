package com.github.nginate.kafka.functional;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.nginate.kafka.TestProperties;
import com.github.nginate.kafka.docker.DockerWrapper;
import com.google.common.collect.Maps;
import lombok.Getter;
import org.apache.commons.beanutils.BeanUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import static com.github.nginate.kafka.docker.DockerConfigs.kafkaContainerConfiguration;

public abstract class AbstractFunctionalTest {

    private DockerWrapper kafkaContainer;
    @Getter
    private TestProperties testProperties;

    @BeforeSuite
    public void initProperties() throws Exception {
        testProperties = new TestProperties();
        populateProperties(testProperties, "application.properties");
    }

    @BeforeClass
    public void initDockerContainer() throws Exception {
        DockerClient dockerClient = DockerClientBuilder.getInstance(testProperties.getDockerUrl()).build();
        kafkaContainer = new DockerWrapper(dockerClient, kafkaContainerConfiguration(testProperties.getKafkaPort()));
        kafkaContainer.start();
        Thread.sleep(5000);
    }

    @AfterClass(alwaysRun = true)
    public void tearDownDockerContainer() throws Exception {
        kafkaContainer.purge();
    }

    private void populateProperties(Object bean, String propertiesFileName) throws IOException, IllegalAccessException, InvocationTargetException {
        Properties properties = loadProperties(propertiesFileName);
        BeanUtils.populate(bean, Maps.fromProperties(properties));
    }

    private Properties loadProperties(String fileName) throws IOException {
        Properties properties = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(fileName)) {
            properties.load(in);
        }
        return properties;
    }
}
