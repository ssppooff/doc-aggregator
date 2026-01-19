package ch.silberruecken.das.shared.security

import ch.silberruecken.das.admin.web.AdminController
import ch.silberruecken.das.section.web.DocumentationSectionController
import ch.silberruecken.das.shared.security.constants.Scopes
import ch.silberruecken.dashared.client.DocAggregatorServiceApi
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher.pathPattern
import org.springframework.security.web.util.matcher.RequestMatchers.anyOf

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfiguration {
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    fun apiSecurity(http: HttpSecurity): SecurityFilterChain {
        http {
            securityMatcher("/api/**")
            authorizeHttpRequests {
                authorize(DocAggregatorServiceApi.DOCUMENTATION_URL, hasAuthority(Scopes.DOCUMENTATIONS_WRITE))
                authorize(anyRequest, denyAll)
            }
            oauth2ResourceServer {
                jwt { }
            }
            csrf { disable() }
        }
        return http.build()
    }

    @Bean
    fun mvcSecurity(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeHttpRequests {
                authorize(EndpointRequest.toAnyEndpoint(), permitAll)
                authorize(anyOf(pathPattern("/webjars/**"), pathPattern("/css/**"), pathPattern("/favicon.ico")), permitAll)
                authorize(DocumentationSectionController.REQUEST_MAPPING, permitAll)
                authorize(AdminController.REQUEST_MAPPING + "/**", hasRole("ADMIN"))
                authorize(anyRequest, denyAll)
            }
            oauth2Login { }
            logout {
                logoutSuccessUrl = "/docs"
            }
        }
        return http.build()
    }

    /**
     * Reads the "roles" claim and adds the values as Spring Security roles.
     */
    @Bean
    fun userAuthoritiesMapper(): GrantedAuthoritiesMapper = GrantedAuthoritiesMapper { authorities: Collection<GrantedAuthority> ->
        authorities.flatMap { authority ->
            if (authority is OidcUserAuthority) {
                val roles = authority.idToken.claims["roles"] as? List<*> ?: emptyList<Any>()
                roles.filterIsInstance<String>()
                    .map { SimpleGrantedAuthority("ROLE_$it") }
            } else {
                listOf(authority)
            }
        }
    }
}
