package ch.silberruecken.das.shared.web

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute

@ControllerAdvice
class NavigationControllerAdvice {
    @ModelAttribute("navigationItems")
    fun getNavigationItems(request: HttpServletRequest) = listOfNotNull(
        item("Home", "/", request),
        item("Search documentation", "/docs", request),
        item("Admin", "/admin/docs", request, "ADMIN")
    )

    private fun item(title: String, uri: String, request: HttpServletRequest, role: String? = null): NavigationItem? {
        return if (role != null && !hasRole(role)) {
            null
        } else {
            NavigationItem(title, uri, request.servletPath == uri)
        }
    }

    private fun hasRole(role: String) = SecurityContextHolder.getContext().authentication?.authorities?.any { it.authority == "ROLE_$role" } ?: false

    class NavigationItem(val title: String, val uri: String, val active: Boolean)
}