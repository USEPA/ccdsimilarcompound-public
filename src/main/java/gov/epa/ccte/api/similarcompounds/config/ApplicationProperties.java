package gov.epa.ccte.api.similarcompounds.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to Chemicalist.
 * <p>
 * Properties are configured in the {@code application.yml} file.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

    private String docHostReplacement;
    private int docHostPort;
    private String docHostHttpProtocol;
    private Integer startPort;
    private Integer endPort;
    private Integer serverPort;

}
