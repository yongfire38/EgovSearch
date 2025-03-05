package egovframework.com.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EgovSearchSwagger {
	
	private static final String SECURITY_SCHEME_NAME = "X-CODE-ID";

	@Bean
    public OpenAPI api() {
        Info info = new Info()
                .title("Open Search 연동 API Document")
                .version("v0.0.1")
                .description("Open Search - Spring Boot 연동의 API 명세서입니다.");

        // 보안 스키마 정의
        SecurityScheme securityScheme = new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .description("인증을 위한 X-CODE-ID 헤더 값을 입력하세요. 기본값: eGovFramework");

        // 보안 요구사항 추가
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(SECURITY_SCHEME_NAME);

        return new OpenAPI()
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME, securityScheme))
                .info(info)
                .addSecurityItem(securityRequirement);
    }

}
