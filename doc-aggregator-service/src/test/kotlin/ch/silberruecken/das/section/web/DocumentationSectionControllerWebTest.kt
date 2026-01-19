package ch.silberruecken.das.section.web

import ch.silberruecken.das.section.DocumentationSectionService
import ch.silberruecken.das.section.DocumentationWithSection
import ch.silberruecken.das.sh.OAuth2TestConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.htmlunit.WebClient
import org.htmlunit.html.HtmlAnchor
import org.htmlunit.html.HtmlButton
import org.htmlunit.html.HtmlInput
import org.htmlunit.html.HtmlPage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.htmlunit.MockMvcWebClientBuilder
import org.springframework.web.context.WebApplicationContext
import java.net.URI

@WebMvcTest(controllers = [DocumentationSectionController::class])
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Import(DummyCsrfConfiguration::class, OAuth2TestConfiguration::class)
class DocumentationSectionControllerWebTest(@MockitoBean private val documentationSectionService: DocumentationSectionService) {
    private lateinit var webClient: WebClient

    private val query = "query"
    private val href = "http://test.test"
    private val title = "API documentation"

    @BeforeEach
    fun initWebClient(context: WebApplicationContext) {
        webClient = MockMvcWebClientBuilder
            .webAppContextSetup(context)
            .build()
        webClient.options.isJavaScriptEnabled = false
    }

    @BeforeEach
    fun initMock() {
        val result = mock<DocumentationWithSection> {
            on { getTitle() } doReturn title
            on { getUri() } doReturn URI(href)
        }
        whenever(documentationSectionService.findDocsByQuery(query)) doReturn listOf(result)
    }

    @Test
    fun `should find documentation`() {
        val page = webClient.getPage<HtmlPage>("http://localhost/docs")
        val queryInput = page.querySelector<HtmlInput>("input#query")
        queryInput.type(query)
        val searchResult = page.querySelector<HtmlButton>("button[type=submit]").click<HtmlPage>()
        val items = searchResult.querySelectorAll(".doc-item")
        assertThat(items).hasSize(1)
        val link = items[0].querySelector<HtmlAnchor>("a.doc-title")
        assertThat(link.hrefAttribute).isEqualTo(href)
        assertThat(link.textContent).isEqualTo(title)
    }
}
