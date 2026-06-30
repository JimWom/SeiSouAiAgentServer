package com.seisou.aiagentserver;

import com.seisou.aiagentserver.config.AppSecurityProperties;
import com.seisou.aiagentserver.config.OpenClawProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({OpenClawProperties.class, AppSecurityProperties.class})
public class SeiSouAiAgentServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeiSouAiAgentServerApplication.class, args);
    }
}
