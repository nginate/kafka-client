package com.github.nginate.kafka.docker;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.LogConfig;
import com.github.dockerjava.api.model.RestartPolicy;
import lombok.experimental.UtilityClass;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Optional;
import java.util.function.BiPredicate;

@UtilityClass
public final class DockerConfigs {
    private static final int ZOOKEEPER_PORT = 2181;

    public static ContainerConfig kafkaContainerConfiguration(Integer kafkaPort) throws SocketException {
        return ContainerConfig.builder()
                .image("nginate/kafka-docker-bundle:0.8.2")
                .name("kafka-bundle")
                .exposedPort(ExposedPort.tcp(ZOOKEEPER_PORT))
                .exposedPort(ExposedPort.tcp(kafkaPort))
                .oneToOnePortBindings(ZOOKEEPER_PORT, kafkaPort)
                .env("ADVERTISED_PORT", kafkaPort.toString())
                .env("ADVERTISED_HOST", getHostIp())
                .env("KAFKA_HEAP_OPTS", "-Xmx256M -Xms128M")
                .logConfig(new LogConfig(LogConfig.LoggingType.DEFAULT))
                .restartPolicy(RestartPolicy.alwaysRestart())
                .networkMode("host")
                .build();
    }

    private static String getHostIp() throws SocketException {
        return filterInterfaceIp((inetAddress, networkInterface) -> !inetAddress.isLoopbackAddress()
                && inetAddress instanceof Inet4Address && !networkInterface.getName().contains("docker"))
                .orElseThrow(() -> new RuntimeException("Public IP not found for current host"));
    }

    private String getDockerGatewayIp() throws SocketException {
        return filterInterfaceIp((inetAddress, networkInterface) -> !inetAddress.isLoopbackAddress()
                && inetAddress instanceof Inet4Address && networkInterface.getName().contains("docker"))
                .orElseThrow(() -> new RuntimeException("Docker gateway IP not found for current host"));
    }

    private static Optional<String> filterInterfaceIp(BiPredicate<InetAddress, NetworkInterface> filter) throws SocketException {
        Enumeration<NetworkInterface> networkInterfaces2 = NetworkInterface.getNetworkInterfaces();
        while(networkInterfaces2.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces2.nextElement();
            Enumeration<InetAddress> interfaceAddresses = networkInterface.getInetAddresses();
            while(interfaceAddresses.hasMoreElements()) {
                InetAddress current =  interfaceAddresses.nextElement();
                if (filter.test(current, networkInterface)) {
                    return Optional.of(current.getHostAddress());
                }
            }
        }
        return Optional.empty();
    }
}
