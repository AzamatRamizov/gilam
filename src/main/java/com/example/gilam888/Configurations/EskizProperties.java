package com.example.gilam888.Configurations;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * application.properties dagi eskiz.* sozlamalarini o'qiydi.
 * (TelegramBotConfig uslubidagi konfiguratsiya.)
 */
@Configuration
@ConfigurationProperties(prefix = "eskiz")
public class EskizProperties {

    private String email;
    private String password;
    private String from = "4546";                       // test jo'natuvchi; prod uchun tasdiqlangan nom
    private String baseUrl = "https://notify.eskiz.uz/api";
    private boolean enabled = true;                      // false -> SMS yuborilmaydi (dev rejim)

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
