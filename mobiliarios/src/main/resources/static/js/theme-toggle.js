// ========================================
// THEME TOGGLE - Dark Mode / Light Mode
// ========================================

(function() {
    'use strict';
    // Evitar transiciones durante la aplicación inicial del tema
    try { document.documentElement.classList.add('no-theme-transitions'); } catch (_) {}

    // Obtener usuario actual desde variable global o meta tag
    const getCurrentUserId = () => {
        if (window.USER_ID && typeof window.USER_ID === 'string') return window.USER_ID;
        const meta = document.querySelector('meta[name="current-user-id"]');
        return meta ? meta.getAttribute('content') : null;
    };
    
    // Construir menú móvil (panel y overlay) a partir de la nav existente
    const buildMobileNav = () => {
        if (document.querySelector('.mobile-nav')) return; // ya creado
        const panel = document.createElement('nav');
        panel.className = 'mobile-nav';
        panel.setAttribute('aria-hidden', 'true');
        panel.setAttribute('role', 'dialog');

        const overlay = document.createElement('div');
        overlay.className = 'mobile-overlay';

        // Clonar enlaces de la nav principal si existe
        const nav = document.querySelector('.nav-principal');
        if (nav) {
            const links = nav.querySelectorAll('a');
            links.forEach(a => {
                const c = a.cloneNode(true);
                c.addEventListener('click', () => closeMobileNav());
                panel.appendChild(c);
            });
        }
        
        // Sección de usuario (si está autenticado) o botón de login
        const userMenu = document.querySelector('.user-menu');
        if (userMenu) {
            try {
                const nameEl = userMenu.querySelector('.user-menu-toggle span');
                const name = (nameEl ? nameEl.textContent : 'Usuario')?.trim() || 'Usuario';
                const initial = name.charAt(0).toUpperCase();
                const userBox = document.createElement('div');
                userBox.className = 'mobile-user-summary';
                userBox.innerHTML = `
                    <div class="mus-avatar">${initial}</div>
                    <div class="mus-info">
                        <strong class="mus-name">${name}</strong>
                    </div>
                `;
                panel.appendChild(userBox);
            } catch(_) {}

            // Agregar enlace de Cerrar sesión
            const logoutLink = userMenu.querySelector('.logout-link');
            if (logoutLink) {
                const c = logoutLink.cloneNode(true);
                c.addEventListener('click', () => closeMobileNav());
                panel.appendChild(c);
            }
        } else {
            // Añadir acciones de usuario visibles (login-btn) si existen
            const loginBtn = document.querySelector('.login-btn');
            if (loginBtn) {
                const c = loginBtn.cloneNode(true);
                c.addEventListener('click', () => closeMobileNav());
                panel.appendChild(c);
            }
        }

        // Añadir botón de carrito si existe en el header
        const cartBtn = document.querySelector('.cart-btn-header');
        if (cartBtn) {
            const c = cartBtn.cloneNode(true);
            c.addEventListener('click', () => {
                try { cartBtn.click(); } catch (_) {}
                closeMobileNav();
            });
            panel.appendChild(c);
        }

        // Añadir botón de cambio de tema si existe
        const themeBtn = document.querySelector('.theme-toggle-btn');
        if (themeBtn) {
            const c = themeBtn.cloneNode(true);
            c.addEventListener('click', (e) => {
                e.preventDefault();
                try { themeBtn.click(); } catch (_) {}
                closeMobileNav();
            });
            panel.appendChild(c);
        }

        document.body.appendChild(panel);
        document.body.appendChild(overlay);

        // Cerrar al hacer click en overlay
        overlay.addEventListener('click', () => closeMobileNav());

        // Cerrar con Escape
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') closeMobileNav();
        });

        // Cerrar al cambiar a desktop
        window.addEventListener('resize', () => {
            if (window.innerWidth > 1024) closeMobileNav();
        });

        // Cerrar si hacemos click fuera del panel (y no en el botón)
        document.addEventListener('click', (e) => {
            const panelEl = document.querySelector('.mobile-nav');
            const burger = document.querySelector('.hamburger-btn');
            if (!panelEl) return;
            const isOpen = panelEl.classList.contains('open');
            if (!isOpen) return;
            const target = e.target;
            if (!target) return;
            if (panelEl.contains(target)) return; // clic dentro del panel
            if (burger && (target === burger || burger.contains(target))) return; // clic en burger
            closeMobileNav();
        }, true);

        return { panel, overlay };
    };

    // Guardar referencias para mover el logo
    let logoOriginalParent = null;
    let logoNextSibling = null;

    const openMobileNav = () => {
        const panel = document.querySelector('.mobile-nav');
        const overlay = document.querySelector('.mobile-overlay');
        if (!panel || !overlay) return;
        panel.classList.add('open');
        overlay.classList.add('show');
        panel.setAttribute('aria-hidden', 'false');
        document.body.style.overflow = 'hidden';

        // Mover el logo al panel y centrarlo
        const logo = document.querySelector('.main-header .logo');
        if (logo && !panel.contains(logo)) {
            logoOriginalParent = logo.parentNode;
            logoNextSibling = logo.nextSibling; // puede ser null
            try { panel.insertBefore(logo, panel.firstChild); } catch (_) { panel.appendChild(logo); }
            logo.classList.add('logo-in-panel');
        }
    };

    const closeMobileNav = () => {
        const panel = document.querySelector('.mobile-nav');
        const overlay = document.querySelector('.mobile-overlay');
        if (!panel || !overlay) return;
        panel.classList.remove('open');
        overlay.classList.remove('show');
        panel.setAttribute('aria-hidden', 'true');
        document.body.style.overflow = '';
        const btn = document.querySelector('.hamburger-btn');
        if (btn) btn.setAttribute('aria-expanded', 'false');

        // Devolver el logo a su lugar original si se movió
        const logo = document.querySelector('.mobile-nav .logo');
        if (logo && logoOriginalParent) {
            try {
                if (logoNextSibling) {
                    logoOriginalParent.insertBefore(logo, logoNextSibling);
                } else {
                    logoOriginalParent.appendChild(logo);
                }
            } catch (_) {
                logoOriginalParent.appendChild(logo);
            }
            logo.classList.remove('logo-in-panel');
        }
        logoOriginalParent = null;
        logoNextSibling = null;
    };

    // Inicializar botón hamburguesa y vincular al panel móvil
    const initHamburger = () => {
        // Crear panel y overlay si no existen
        buildMobileNav();

        // Si ya existe el botón, salir
        if (document.querySelector('.hamburger-btn')) return;

        const headerContent = document.querySelector('.header-content');
        if (!headerContent) return;

        const btn = document.createElement('button');
        btn.className = 'hamburger-btn';
        btn.setAttribute('aria-label', 'Abrir menú');
        btn.setAttribute('aria-expanded', 'false');
        btn.innerHTML = '<i class="bi bi-list"></i>';

        // Insertar al final del header-content
        headerContent.appendChild(btn);

        btn.addEventListener('click', () => {
            const isOpen = document.querySelector('.mobile-nav')?.classList.contains('open');
            if (isOpen) {
                closeMobileNav();
            } else {
                openMobileNav();
                btn.setAttribute('aria-expanded', 'true');
            }
        });
    };

    // Activar/desactivar layout móvil con clase en body
    const updateMobileUI = () => {
        const isMobile = window.innerWidth <= 991;
        if (isMobile) {
            document.body.classList.add('mobile-ui');
        } else {
            document.body.classList.remove('mobile-ui');
            closeMobileNav();
            // Eliminar el botón hamburguesa en desktop para evitar que se muestre
            const burger = document.querySelector('.hamburger-btn');
            if (burger && burger.parentNode) {
                burger.parentNode.removeChild(burger);
            }
        }
    };

    const API_BASE = '';

    // Verificar preferencia guardada o del sistema
    const getPreferredTheme = () => {
        const savedTheme = localStorage.getItem('theme');
        if (savedTheme) {
            return savedTheme;
        }
        
        // Verificar preferencia del sistema
        return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
    };
    
    // Aplicar tema
    const setTheme = (theme) => {
        if (theme === 'dark') {
            document.documentElement.classList.add('dark-mode');
            document.documentElement.classList.add('prefers-dark');
            document.body.classList.add('dark-mode');
        } else {
            document.documentElement.classList.remove('dark-mode');
            document.documentElement.classList.remove('prefers-dark');
            document.body.classList.remove('dark-mode');
        }
        localStorage.setItem('theme', theme);
        
        // Actualizar ícono del botón
        updateToggleIcon(theme);

        // Persistir en backend si hay usuario
        const uid = getCurrentUserId();
        if (uid) {
            try {
                fetch(`${API_BASE}/api/usuarios/${encodeURIComponent(uid)}/tema`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ tema: theme })
                }).catch(() => {});
            } catch (_) {}
        }
    };
    
    // Actualizar ícono del botón
    const updateToggleIcon = (theme) => {
        // Actualizar iconos en el sidebar
        const darkIcons = document.querySelectorAll('.theme-icon-dark');
        const lightIcons = document.querySelectorAll('.theme-icon-light');
        const darkTexts = document.querySelectorAll('.theme-text-dark');
        const lightTexts = document.querySelectorAll('.theme-text-light');
        
        if (theme === 'dark') {
            darkIcons.forEach(icon => icon.style.display = 'none');
            lightIcons.forEach(icon => icon.style.display = 'inline');
            darkTexts.forEach(text => text.style.display = 'none');
            lightTexts.forEach(text => text.style.display = 'inline');
        } else {
            darkIcons.forEach(icon => icon.style.display = 'inline');
            lightIcons.forEach(icon => icon.style.display = 'none');
            darkTexts.forEach(text => text.style.display = 'inline');
            lightTexts.forEach(text => text.style.display = 'none');
        }
        
        // Actualizar icono simple del botón de tema (header y panel móvil)
        const allThemeIcons = document.querySelectorAll('.theme-toggle-btn i, .theme-toggle i:not(.theme-icon-dark):not(.theme-icon-light)');
        allThemeIcons.forEach(icon => {
            if (theme === 'dark') {
                // En oscuro: mostrar sol blanco para indicar cambiar a claro
                icon.className = 'bi bi-sun-fill';
                icon.style.color = '#ffffff';
            } else {
                // En claro: mostrar luna oscura para indicar cambiar a oscuro
                icon.className = 'bi bi-moon-fill';
                icon.style.color = '#0f172a';
            }
        });
    };
    
    // Cambiar tema
    const toggleTheme = () => {
        const currentTheme = (localStorage.getItem('theme')) || (document.documentElement.classList.contains('dark-mode') ? 'dark' : 'light');
        const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
        setTheme(newTheme);
        
        // Animación de transición solo al cambiar manualmente
        document.body.style.transition = 'all 0.3s ease';
        setTimeout(() => {
            document.body.style.transition = '';
        }, 300);
    };
    
    // Cargar tema desde backend si hay usuario
    const loadUserTheme = async () => {
        const uid = getCurrentUserId();
        if (!uid) return null;
        try {
            const res = await fetch(`${API_BASE}/api/usuarios/${encodeURIComponent(uid)}/tema`, { cache: 'no-store' });
            if (!res.ok) return null;
            const txt = await res.text();
            const theme = (txt || '').replace(/^["']|["']$/g, '');
            return theme === 'dark' || theme === 'light' ? theme : null;
        } catch (_) {
            return null;
        }
    };

    // Inicializar tema al cargar la página
    const initTheme = () => {
        // Aplicar preferencia local inmediatamente para evitar flash
        setTheme(getPreferredTheme());
        // Luego sincronizar con backend si corresponde
        loadUserTheme().then(serverTheme => {
            if (serverTheme) {
                setTheme(serverTheme);
            }
            // Quitar el bloqueo de transiciones en el siguiente frame
            try { requestAnimationFrame(() => document.documentElement.classList.remove('no-theme-transitions')); } catch (_) {
                setTimeout(() => document.documentElement.classList.remove('no-theme-transitions'), 0);
            }
            // Asegurar que el fallback condicionado no quede activo tras inicializar
            try { document.documentElement.classList.remove('prefers-dark'); } catch (_) {}
        });
    };
    
    // Inicializar botón existente en el header
    const initToggleButton = () => {
        const button = document.querySelector('.theme-toggle-btn');
        if (button) {
            button.addEventListener('click', toggleTheme);
            updateToggleIcon(getPreferredTheme());
        }
    };

    // Hacer el logo clickable hacia inicio sin modificar HTML
    const initLogoLink = () => {
        const logo = document.querySelector('.logo');
        if (!logo) return;
        const goHome = (e) => {
            if (e) e.preventDefault();
            // Evitar activar si se selecciona texto
            if (window.getSelection && String(window.getSelection()).length > 0) return;
            window.location.assign('/');
        };

        // Asegurar que el logo sea interactivo y no quede debajo de overlays
        try {
            logo.style.cursor = 'pointer';
            logo.style.pointerEvents = 'auto';
            logo.style.position = logo.style.position || 'relative';
            logo.style.zIndex = logo.style.zIndex || '10';
            logo.setAttribute('role', 'link');
            logo.setAttribute('tabindex', '0');
        } catch (_) {}

        // Si el logo ya es un enlace, garantizar href correcto
        if (logo.tagName.toLowerCase() === 'a') {
            if (!logo.getAttribute('href')) logo.setAttribute('href', '/');
            logo.addEventListener('click', goHome);
        } else {
            // Adjuntar listeners directos
            logo.addEventListener('click', goHome);
        }

        // Fallback: delegación por si un overlay intercepta el logo
        document.addEventListener('click', (e) => {
            const target = e.target;
            if (!target) return;
            if (target.closest && target.closest('.logo')) {
                goHome(e);
            }
        }, true);

        // Accesibilidad con teclado
        logo.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                goHome(e);
            }
        });
    };
    
    // Escuchar cambios en la preferencia del sistema
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
        if (!localStorage.getItem('theme')) {
            setTheme(e.matches ? 'dark' : 'light');
        }
    });
    
    
// Inicializar cuando el DOM esté listo
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', function(event) {
        if (event.persisted) {
            window.location.reload();
        } else {
            initTheme();
            initToggleButton();
            initLogoLink();
            initHamburger();
            updateMobileUI();
        }
    });
} else {
    initTheme();
    initToggleButton();
    initLogoLink();
    initHamburger();
    updateMobileUI();
}
    // Exponer funciones globalmente
    window.toggleTheme = toggleTheme;
    window.updateThemeIcon = updateToggleIcon;
})();

// Evitar mostrar páginas obsoletas al volver desde el historial (BFCache)
window.addEventListener('pageshow', function(event) {
    if (event.persisted) {
        window.location.reload();
    }
});

// Sincronizar layout móvil en cambios de tamaño
window.addEventListener('resize', function() {
    try { updateMobileUI(); } catch (_) {}
});

// Sticky header scroll effect
window.addEventListener('scroll', function() {
    const header = document.querySelector('.main-header');
    if (header) {
        if (window.scrollY > 50) {
            header.classList.add('scrolled');
        } else {
            header.classList.remove('scrolled');
        }
    }
});

