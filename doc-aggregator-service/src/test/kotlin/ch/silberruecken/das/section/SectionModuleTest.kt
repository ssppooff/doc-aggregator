package ch.silberruecken.das.section

import ch.silberruecken.das.TestcontainersConfiguration
import ch.silberruecken.das.documentation.*
import ch.silberruecken.das.section.elasticsearch.SectionIndexRepository
import ch.silberruecken.das.sh.OAuth2TestConfiguration
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.Import
import org.springframework.modulith.test.ApplicationModuleTest
import org.springframework.modulith.test.Scenario
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.wiremock.spring.ConfigureWireMock
import org.wiremock.spring.EnableWireMock
import org.wiremock.spring.InjectWireMock
import java.net.URI

@ApplicationModuleTest
@Import(TestcontainersConfiguration::class, OAuth2TestConfiguration::class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@EnableWireMock(ConfigureWireMock())
class SectionModuleTest(private val sectionIndexRepository: SectionIndexRepository, @MockitoBean private val documentationService: DocumentationService) {
    private val documentationId = DocumentationId("1")

    @InjectWireMock
    private lateinit var wiremock: WireMockServer

    @BeforeEach
    fun initWiremock() {
        wiremock.stubFor(
            get("/docs").willReturn(
                aResponse()
                    .withHeader("Content-Type", "text/html")
                    .withBody(
                        """
                            <html><head></head><body>
                              <h1>Goodbye World</h1>
                              <div>
                                <p id="_hello">Hello World</p>
                              </div>
                            </body></html>
                        """
                    )
            )
        )
    }

    @BeforeEach
    fun initElasticsearchData() {
        sectionIndexRepository.deleteAll()
        sectionIndexRepository.save(DocumentationSection(null, documentationId, SectionMarkup(null, 0, "Hello World")))
    }

    @Test
    fun `should re-index changed documentation`(scenario: Scenario) {
        val documentation = Documentation(documentationId, DocumentationType.API, "my-service", URI(wiremock.url("/docs")), DocumentationAccess.PUBLIC, null)
        scenario.publish(DocumentationUpdated(documentation))
            .andWaitForStateChange { sectionIndexRepository.count() > 1 }

        assertThat(sectionIndexRepository.count()).isEqualTo(2)
    }
}
