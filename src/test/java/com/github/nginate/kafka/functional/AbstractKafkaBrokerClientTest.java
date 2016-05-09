package com.github.nginate.kafka.functional;

import com.github.nginate.kafka.core.KafkaBrokerClient;
import kafka.utils.ZkUtils;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.github.nginate.commons.lang.await.Await.waitUntil;

@Slf4j
public abstract class AbstractKafkaBrokerClientTest extends AbstractFunctionalTest {

    protected KafkaBrokerClient client;

    @BeforeClass(dependsOnMethods = "initDockerContainer")
    public void prepareClient() throws Exception {
        client = new KafkaBrokerClient(getKafkaHost(), getTestProperties().getKafkaPort());

        waitUntil(10000, 1000, () -> {
            try {
                client.connect();
                return true;
            } catch (Exception e) {
                log.warn("Could not connect : {}", e.getMessage());
                return false;
            }
        });

        log.info("Connected");

        ZkUtils.setupCommonPaths(getZkClient());

        // waiting for broker registration in container
        waitUntil(10000, 1000, () -> {
            try {
                return !getZkClient().getChildren("/brokers/ids").isEmpty();
            } catch (Exception e) {
                log.warn("Could not retrieve broker list : {}", e.getMessage());
                return false;
            }
        });
    }

    @AfterClass(alwaysRun = true)
    public void tearDownClient() throws Exception {
        client.close();
    }

    protected <T> T await(CompletableFuture<T> completableFuture)
            throws InterruptedException, ExecutionException, TimeoutException {
        return await(completableFuture, getTestProperties().getClientTimeout());
    }

    protected <T> T await(CompletableFuture<T> completableFuture, int timeout)
            throws InterruptedException, ExecutionException, TimeoutException {
        return completableFuture.get(timeout, TimeUnit.MILLISECONDS);
    }
}
