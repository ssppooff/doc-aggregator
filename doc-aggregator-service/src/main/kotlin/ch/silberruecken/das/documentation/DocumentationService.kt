package ch.silberruecken.das.documentation

import ch.silberruecken.das.documentation.mongodb.DocumentationRepository
import ch.silberruecken.das.shared.security.constants.Scopes
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DocumentationService(
    private val documentationRepository: DocumentationRepository,
    private val eventPublisher: ApplicationEventPublisher
) {
    @PreAuthorize("hasAuthority('${Scopes.DOCUMENTATIONS_WRITE}')")
    @Transactional
    fun createOrUpdateDocumentation(documentation: Documentation): Documentation {
        return documentation.createOrUpdate(documentationRepository, eventPublisher)
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    fun delete(id: String) {
        documentationRepository.findByIdOrNull(id)?.delete(documentationRepository, eventPublisher)
    }

    @PreAuthorize("hasRole('ADMIN')")
    fun findAll(): List<Documentation> {
        return documentationRepository.findAll()
    }

    // TODO: Security
    fun findById(id: DocumentationId) = documentationRepository.findByIdOrNull(id.toString())
}
