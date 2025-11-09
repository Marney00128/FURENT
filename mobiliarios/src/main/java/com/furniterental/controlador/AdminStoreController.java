package com.furniterental.controlador;

import com.furniterental.modelo.Producto;
import com.furniterental.modelo.Categoria;
import com.furniterental.repositorio.ProductoRepository;
import com.furniterental.servicio.CategoriaServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class AdminStoreController {

    @Autowired
    private ProductoRepository productoRepository; // Repositorio de productos
    
    @Autowired
    private CategoriaServicio categoriaServicio;

    @GetMapping("/admin-store")
    public String adminStore(Model model) {
        // Obtener todos los productos de la base de datos
        List<Producto> productos = productoRepository.findAll();
        List<Categoria> categorias = categoriaServicio.obtenerTodasCategorias();
        
        // Pasar los productos a la vista como productosAdmin
        model.addAttribute("productosAdmin", productos);
        model.addAttribute("categorias", categorias);
        
        return "pages/admin-store";
    }
}