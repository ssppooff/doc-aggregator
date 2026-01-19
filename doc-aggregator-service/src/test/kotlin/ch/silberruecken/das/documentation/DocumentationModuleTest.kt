package ch.silberruecken.das.documentation

import ch.silberruecken.das.TestcontainersConfiguration
import ch.silberruecken.das.sh.OAuth2TestConfiguration
import ch.silberruecken.das.shared.security.constants.Scopes
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.modulith.test.ApplicationModuleTest
import org.springframework.modulith.test.AssertablePublishedEvents
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.put

@ApplicationModuleTest
@Import(TestcontainersConfiguration.MongoDbContainerConfiguration::class, OAuth2TestConfiguration::class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@AutoConfigureMockMvc
@ActiveProfiles("debug-security")
class DocumentationModuleTest(private val mvc: MockMvc) {
    private val uri = "https://my-service.io/docs/index.html"
    private val service = "my-service"

    @Test
    @WithMockUser(authorities = [Scopes.DOCUMENTATIONS_WRITE])
    fun `should persist documentation and trigger indexing`(events: AssertablePublishedEvents) {
        mvc.put("/api/documentations") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                "uri": "$uri",
                "type": "API",
                "service": "$service"
                }
            """.trimIndent()
            with(csrf())
        }.andExpect {
            status { isCreated() }
            header { exists("Location") }
            jsonPath("id") { isNotEmpty() }
            jsonPath("uri") { value(uri) }
            jsonPath("type") { value("API") }
            jsonPath("service") { value(service) }
            jsonPath("access") { value("PUBLIC") }
        }

        assertThat(events).contains(DocumentationUpdated::class.java)
            .matching({ it.documentation.uri.toString() }, uri)
    }
}
