package com.lhstack.stream.redis.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置客户端信息
 * @author lhstack
 * @date 2021/9/7
 * @class RedisStreamBindingProperties
 * @since 1.8
 */
@ConfigurationProperties("spring.cloud.stream.redis.binder")
public class RedisStreamClientProperties {

    private String host;

    private Integer port;

    private Integer database;

    private String password;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getDatabase() {
        return database;
    }

    public void setDatabase(Integer database) {
        this.database = database;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "RedisStreamBindingProperties{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", database=" + database +
                ", password='" + password + '\'' +
                '}';
    }
}
