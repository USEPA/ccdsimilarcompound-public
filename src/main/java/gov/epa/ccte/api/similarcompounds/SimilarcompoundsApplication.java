package gov.epa.ccte.api.similarcompounds;

import gov.epa.ccte.api.similarcompounds.config.ApplicationProperties;
import gov.epa.ccte.api.similarcompounds.config.Constants;
import gov.epa.ccte.api.similarcompounds.config.DefaultProfileUtil;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;

@Slf4j
@SpringBootApplication
@EnableDiscoveryClient
@EnableConfigurationProperties({ApplicationProperties.class})
public class SimilarcompoundsApplication implements InitializingBean {

	private static Integer startPort = 9300;
	private static Integer endPort = 9350;
	private final Environment env;
	private final ApplicationProperties appProps;

	public SimilarcompoundsApplication(Environment env, ApplicationProperties appProps) {
		this.env = env;
		this.appProps = appProps;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Collection<String> activeProfiles = Arrays.asList(env.getActiveProfiles());
		if (activeProfiles.contains(Constants.SPRING_PROFILE_DEVELOPMENT) && activeProfiles.contains(Constants.SPRING_PROFILE_PRODUCTION)) {
			log.error("You have misconfigured your application! It should not run " +
					"with both the 'dev' and 'prod' profiles at the same time.");
		}
		if (activeProfiles.contains(Constants.SPRING_PROFILE_DEVELOPMENT) && activeProfiles.contains(Constants.SPRING_PROFILE_CLOUD)) {
			log.error("You have misconfigured your application! It should not " +
					"run with both the 'dev' and 'cloud' profiles at the same time.");
		}

	}

	/**
	 * Main method, used to run the application.
	 *
	 * @param args the command line arguments.
	 */
	public static void main(String[] args) {
		log.info("*** Application is started. ***");

		String profile = System.getProperty("spring.profiles.active");

		if(profile == null)
			profile = "dev"; // default profile

		// This is the only way to use random port with eureka server otherwise eureka server has port number set in application.yml
		//(startPort, endPort);
		setRandomPort(startPort, endPort, profile);

		SpringApplication app = new SpringApplication(SimilarcompoundsApplication.class);
		DefaultProfileUtil.addDefaultProfile(app);
		Environment env = app.run(args).getEnvironment();
		logApplicationStartup(env);
	}

	private static void logApplicationStartup(Environment env) {
		String protocol = "http";
		if (env.getProperty("server.ssl.key-store") != null) {
			protocol = "https";
		}
		String serverPort = env.getProperty("server.port");
		String contextPath = env.getProperty("server.servlet.context-path");
		if (StringUtils.isEmpty(contextPath)) {
			contextPath = "/";
		}
		String hostAddress = "localhost";
		try {
			hostAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			log.warn("The host name could not be determined, using `localhost` as fallback");
		}

		log.info("\n----------------------------------------------------------\n\t" +
						"Application '{}' is running! Access URLs:\n\t" +
						"Local: \t\t{}://localhost:{}{}\n\t" +
						"External: \t{}://{}:{}{}\n\t" +
						"Profile(s): \t{}\n----------------------------------------------------------",
				env.getProperty("spring.application.name"),
				protocol,
				serverPort,
				contextPath,
				protocol,
				hostAddress,
				serverPort,
				contextPath,
				env.getActiveProfiles());

		String configServerStatus = env.getProperty("configserver.status");
		if (configServerStatus == null) {
			configServerStatus = "Not found or not setup for this application";
		}
		//log.info("Config Server: ----------------------------------------------------------" + configServerStatus.toString());
	}


	public static void setRandomPort(int startPOrt, int endPort, String profiles){
		int port =9500;

		if(profiles.contains("dynamic")){
			for (int port_index = startPOrt; port_index <= endPort; port_index++) {
				try {
					ServerSocket socket = new ServerSocket(port_index);
					socket.close();
					port = port_index;
				} catch (IOException e) {

				}
			}
			System.setProperty("server.port", String.valueOf(port));
			log.info("dynamic is a profile. Server port set to {} ", port);

		}else{
			System.setProperty("server.port", String.valueOf(port));
			log.info("No dynamic profile found. Server port set to {} ", port);
		}

		// int port = 9300;
	}
}
