package gov.epa.ccte.api.similarcompounds;

import gov.epa.ccte.api.similarcompounds.config.DefaultProfileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;


/**
 * This is a helper Java class that provides an alternative to creating a {@code web.xml}.
 * This will be invoked only when the application is deployed to a Servlet container like Tomcat, JBoss etc.
 */
@Slf4j
public class ApplicationWebXml extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		log.info("Web app is starting.");
		// set a default to use when no profile is configured.
		DefaultProfileUtil.addDefaultProfile(application.application());
		return application.sources(SimilarcompoundsApplication.class);
	}
}
