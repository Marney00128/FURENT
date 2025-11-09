package com.furniterental.controlador;

import com.furniterental.modelo.ActivityLog;
import com.furniterental.servicio.ActivityLogServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequestMapping("/admin/reportes")
public class ReportesControlador {
    
    @Autowired
    private ActivityLogServicio activityLogServicio;
    
    @GetMapping
    public String mostrarReportes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        // Verificar si el usuario es administrador
        String rol = (String) session.getAttribute("usuarioRol");
        if (!"ADMIN".equals(rol)) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos de administrador");
            return "redirect:/login";
        }
        
        // Verificar autenticación 2FA
        Boolean is2FAVerified = (Boolean) session.getAttribute("reportes2FAVerified");
        if (!Boolean.TRUE.equals(is2FAVerified)) {
            redirectAttributes.addFlashAttribute("info", "Se requiere verificación de dos factores para acceder a los reportes");
            return "redirect:/admin/reportes/verify";
        }
        
        // Obtener logs con filtros
        Page<ActivityLog> logs;
        if (action != null || userName != null || dateFrom != null || dateTo != null) {
            logs = activityLogServicio.findWithFilters(action, userName, dateFrom, dateTo, page, size);
        } else {
            logs = activityLogServicio.getAllLogs(page, size);
        }
        
        // Obtener estadísticas
        Map<String, Long> stats = activityLogServicio.getStatistics();
        
        // Agregar atributos al modelo
        model.addAttribute("logs", logs.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", logs.getTotalPages());
        model.addAttribute("totalElements", logs.getTotalElements());
        model.addAttribute("stats", stats);
        
        // Mantener filtros en el formulario
        model.addAttribute("filterAction", action);
        model.addAttribute("filterUser", userName);
        model.addAttribute("filterDateFrom", dateFrom);
        model.addAttribute("filterDateTo", dateTo);
        
        return "admin/reportes";
    }
    
    @GetMapping("/api/logs")
    @ResponseBody
    public Map<String, Object> getLogsApi(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo) {
        
        Page<ActivityLog> logs;
        if (action != null || userName != null || dateFrom != null || dateTo != null) {
            logs = activityLogServicio.findWithFilters(action, userName, dateFrom, dateTo, page, size);
        } else {
            logs = activityLogServicio.getAllLogs(page, size);
        }
        
        return Map.of(
            "logs", logs.getContent(),
            "currentPage", page,
            "totalPages", logs.getTotalPages(),
            "totalElements", logs.getTotalElements()
        );
    }
    
    @GetMapping("/api/stats")
    @ResponseBody
    public Map<String, Long> getStats() {
        return activityLogServicio.getStatistics();
    }
}
