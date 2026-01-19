package ch.silberruecken.das.section

import ch.silberruecken.das.documentation.Documentation
import ch.silberruecken.das.documentation.DocumentationService
import ch.silberruecken.das.section.elasticsearch.SectionIndexRepository
import org.springframework.data.elasticsearch.core.SearchHit
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

// TODO: Security
@Service
class DocumentationSectionService(
    private val sectionIndexRepository: SectionIndexRepository,
    private val documentationService: DocumentationService
) {
    fun findByQuery(query: String) =
        sectionIndexRepository.findByMarkupTextContainingOrderByMarkupElementDepthDesc(query)

    fun findDocsByQuery(query: String) = findByQuery(query)
        .distinctBy { it.content.documentationId }
        .mapNotNull { withDocumentation(it) }

    private fun withDocumentation(searchHit: SearchHit<DocumentationSection>): DocumentationWithSection? {
        val documentation = documentationService.findById(searchHit.content.documentationId) ?: return null
        return DocumentationWithSection(
            documentation,
            searchHit.content,
            searchHit.highlightFields.values.first().joinToString("... ")
        )
    }
}

data class DocumentationWithSection(
    private val documentation: Documentation,
    private val section: DocumentationSection,
    val summary: String
) {
    fun getUri(): URI = UriComponentsBuilder.fromUri(documentation.uri).fragment(section.markup.elementId).build().toUri()
    fun getTitle() = documentation.service
}
