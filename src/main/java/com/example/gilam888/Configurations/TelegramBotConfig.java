package com.example.gilam888.Configurations;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

// O'zbekistonda ba'zi provayderlar api.telegram.org'ga trafikni sekinlashtiradi/cheklaydi,
// shu sababli SocketTimeoutException chiqishi mumkin. telegram.bot.proxy.enabled=true qilinsa,
// bot so'rovlari SOCKS5/HTTP proxy orqali yuboriladi (masalan xorijdagi VPS'dagi proxy).
@Configuration
public class TelegramBotConfig {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.proxy.enabled:false}")
    private boolean proxyEnabled;

    @Value("${telegram.bot.proxy.type:SOCKS}")
    private String proxyType; // SOCKS yoki HTTP

    @Value("${telegram.bot.proxy.host:}")
    private String proxyHost;

    @Value("${telegram.bot.proxy.port:0}")
    private int proxyPort;

    @Value("${telegram.bot.proxy.username:}")
    private String proxyUsername;

    @Value("${telegram.bot.proxy.password:}")
    private String proxyPassword;

    @Bean
    public TelegramClient telegramClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS);

        if (proxyEnabled && !proxyHost.isBlank() && proxyPort > 0) {
            Proxy.Type type = "HTTP".equalsIgnoreCase(proxyType) ? Proxy.Type.HTTP : Proxy.Type.SOCKS;
            Proxy proxy = new Proxy(type, new InetSocketAddress(proxyHost, proxyPort));
            builder.proxy(proxy);

            if (!proxyUsername.isBlank()) {
                if (type == Proxy.Type.HTTP) {
                    builder.proxyAuthenticator((route, response) -> response.request().newBuilder()
                            .header("Proxy-Authorization", Credentials.basic(proxyUsername, proxyPassword))
                            .build());
                } else {
                    // SOCKS5 autentifikatsiyasi JVM darajasida sozlanadi
                    Authenticator.setDefault(new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            if (getRequestingHost().equalsIgnoreCase(proxyHost) && getRequestingPort() == proxyPort) {
                                return new PasswordAuthentication(proxyUsername, proxyPassword.toCharArray());
                            }
                            return null;
                        }
                    });
                    System.setProperty("java.net.socks.username", proxyUsername);
                    System.setProperty("java.net.socks.password", proxyPassword);
                }
            }
        }

        return new OkHttpTelegramClient(builder.build(), botToken);
    }
}
