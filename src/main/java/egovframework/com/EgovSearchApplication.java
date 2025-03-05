package egovframework.com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
public class EgovSearchApplication {

	public static void main(String[] args) {
		SpringApplication.run(EgovSearchApplication.class, args);
	}

}
