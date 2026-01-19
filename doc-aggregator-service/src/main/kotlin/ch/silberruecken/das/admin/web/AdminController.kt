package ch.silberruecken.das.admin.web

import ch.silberruecken.das.admin.web.AdminController.Companion.REQUEST_MAPPING
import ch.silberruecken.das.documentation.DocumentationService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping(REQUEST_MAPPING)
class AdminController(private val documentationService: DocumentationService) {

    @GetMapping("/docs")
    fun listDocumentations() = ModelAndView("admin/docs", mapOf("docs" to documentationService.findAll()))

    @PostMapping("/docs/delete")
    fun deleteDocumentation(body: DeleteDocumentationBody): String {
        documentationService.delete(body.id)
        return "redirect:$REQUEST_MAPPING/docs"
    }

    companion object {
        const val REQUEST_MAPPING = "/admin"
    }

    class DeleteDocumentationBody(val id: String)
}