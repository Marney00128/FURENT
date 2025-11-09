// ========== SISTEMA DE NOTIFICACIONES PARA ADMINISTRADOR ==========

class AdminNotificationManager {
    constructor() {
        this.notificationBtn = null;
        this.notificationBadge = null;
        this.notificationPanel = null;
        this.updateInterval = null;
    }

    /**
     * Inicializar el sistema de notificaciones
     */
    init() {
        this.notificationBtn = document.querySelector('.notification-btn');
        this.notificationBadge = document.querySelector('.notification-btn .badge');
        
        if (!this.notificationBtn) {
            console.warn('Botón de notificaciones no encontrado');
            return;
        }

        // Crear panel de notificaciones si no existe
        this.createNotificationPanel();

        // Cargar notificaciones iniciales
        this.loadNotifications();

        // Actualizar cada 30 segundos
        this.updateInterval = setInterval(() => {
            this.loadNotifications();
        }, 30000);

        // Event listener para abrir/cerrar panel
        this.notificationBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            this.toggleNotificationPanel();
        });

        // Cerrar panel al hacer clic fuera
        document.addEventListener('click', (e) => {
            // No cerrar el panel si el clic es dentro de un SweetAlert2
            if (e.target.closest('.swal2-container') || e.target.closest('.swal2-popup')) {
                return;
            }
            
            if (this.notificationPanel && 
                !this.notificationPanel.contains(e.target) && 
                !this.notificationBtn.contains(e.target)) {
                this.closeNotificationPanel();
            }
        });
    }

    /**
     * Crear panel de notificaciones
     */
    createNotificationPanel() {
        // Verificar si ya existe
        if (document.getElementById('adminNotificationPanel')) {
            this.notificationPanel = document.getElementById('adminNotificationPanel');
            return;
        }

        const panel = document.createElement('div');
        panel.id = 'adminNotificationPanel';
        panel.className = 'admin-notification-panel';
        panel.innerHTML = `
            <div class="notification-header">
                <h3><i class="bi bi-bell-fill"></i> Notificaciones</h3>
                <button class="btn-close-notifications" onclick="adminNotificationManager.closeNotificationPanel()">
                    <i class="bi bi-x"></i>
                </button>
            </div>
            <div class="notification-tabs">
                <button class="tab-btn active" data-tab="resenas">
                    <i class="bi bi-star-fill"></i>
                    Reseñas
                    <span class="tab-badge" id="resenasBadge">0</span>
                </button>
            </div>
            <div class="notification-content" id="notificationContent">
                <div class="notification-loading">
                    <i class="bi bi-hourglass-split"></i>
                    <p>Cargando notificaciones...</p>
                </div>
            </div>
            <div class="notification-footer">
                <button class="btn-view-all" onclick="adminNotificationManager.verTodasLasResenas()">
                    Ver todas las reseñas
                    <i class="bi bi-arrow-right"></i>
                </button>
            </div>
        `;

        document.body.appendChild(panel);
        this.notificationPanel = panel;

        // Event listeners para tabs
        const tabBtns = panel.querySelectorAll('.tab-btn');
        tabBtns.forEach(btn => {
            btn.addEventListener('click', () => {
                tabBtns.forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                this.loadTabContent(btn.dataset.tab);
            });
        });
    }

    /**
     * Cargar notificaciones
     */
    async loadNotifications() {
        try {
            const response = await fetch('/resenas/admin/pendientes/count', {
                credentials: 'same-origin'
            });
            const data = await response.json();

            if (data.success) {
                const count = data.count || 0;
                this.updateBadge(count);
                
                // Actualizar badge de la tab
                const resenasBadge = document.getElementById('resenasBadge');
                if (resenasBadge) {
                    resenasBadge.textContent = count;
                    resenasBadge.style.display = count > 0 ? 'inline-block' : 'none';
                }
            }
        } catch (error) {
            console.error('Error al cargar notificaciones:', error);
        }
    }

    /**
     * Actualizar badge de notificaciones
     */
    updateBadge(count) {
        if (!this.notificationBadge) return;

        if (count > 0) {
            this.notificationBadge.textContent = count > 99 ? '99+' : count;
            this.notificationBadge.style.display = 'flex';
            
            // Animación de pulso
            this.notificationBadge.style.animation = 'none';
            setTimeout(() => {
                this.notificationBadge.style.animation = 'pulse 2s ease-in-out infinite';
            }, 10);
        } else {
            this.notificationBadge.style.display = 'none';
        }
    }

    /**
     * Abrir/cerrar panel de notificaciones
     */
    toggleNotificationPanel() {
        if (!this.notificationPanel) return;

        const isOpen = this.notificationPanel.classList.contains('active');
        
        if (isOpen) {
            this.closeNotificationPanel();
        } else {
            this.openNotificationPanel();
        }
    }

    /**
     * Abrir panel de notificaciones
     */
    openNotificationPanel() {
        if (!this.notificationPanel) return;

        this.notificationPanel.classList.add('active');
        this.loadTabContent('resenas');
    }

    /**
     * Cerrar panel de notificaciones
     */
    closeNotificationPanel() {
        if (!this.notificationPanel) return;
        this.notificationPanel.classList.remove('active');
    }

    /**
     * Cargar contenido de una tab
     */
    async loadTabContent(tab) {
        const content = document.getElementById('notificationContent');
        if (!content) return;

        content.innerHTML = `
            <div class="notification-loading">
                <i class="bi bi-hourglass-split"></i>
                <p>Cargando...</p>
            </div>
        `;

        if (tab === 'resenas') {
            await this.loadResenasContent();
        }
    }

    /**
     * Cargar contenido de reseñas pendientes
     */
    async loadResenasContent() {
        const content = document.getElementById('notificationContent');
        if (!content) return;

        try {
            const response = await fetch('/resenas/admin/pendientes', {
                credentials: 'same-origin'
            });
            
            // Verificar si la respuesta es exitosa
            if (!response.ok) {
                console.error('Error HTTP:', response.status, response.statusText);
                content.innerHTML = `
                    <div class="notification-error">
                        <i class="bi bi-exclamation-triangle"></i>
                        <p>Error al cargar reseñas (${response.status})</p>
                        <small>Verifica que estés logueado como administrador</small>
                    </div>
                `;
                return;
            }
            
            const data = await response.json();
            console.log('Datos recibidos:', data);

            if (data.success) {
                const resenas = data.resenas || [];

                if (resenas.length === 0) {
                    content.innerHTML = `
                        <div class="notification-empty">
                            <i class="bi bi-check-circle"></i>
                            <h4>¡Todo al día!</h4>
                            <p>No hay reseñas pendientes de aprobación</p>
                        </div>
                    `;
                    return;
                }

                content.innerHTML = resenas.map(resena => this.createResenaCard(resena)).join('');

                // Event listeners para botones de acción
                content.querySelectorAll('.btn-aprobar-resena').forEach(btn => {
                    btn.addEventListener('click', () => this.aprobarResena(btn.dataset.resenaId));
                });

                content.querySelectorAll('.btn-rechazar-resena').forEach(btn => {
                    btn.addEventListener('click', () => this.rechazarResena(btn.dataset.resenaId));
                });
            } else {
                console.error('Error en respuesta:', data.message);
                content.innerHTML = `
                    <div class="notification-error">
                        <i class="bi bi-exclamation-triangle"></i>
                        <p>Error al cargar reseñas</p>
                        <small>${data.message || 'Error desconocido'}</small>
                    </div>
                `;
            }
        } catch (error) {
            console.error('Error al cargar reseñas:', error);
            content.innerHTML = `
                <div class="notification-error">
                    <i class="bi bi-exclamation-triangle"></i>
                    <p>Error al cargar reseñas</p>
                </div>
            `;
        }
    }

    /**
     * Crear tarjeta de reseña
     */
    createResenaCard(resena) {
        const estrellas = '★'.repeat(resena.calificacion) + '☆'.repeat(5 - resena.calificacion);
        const fecha = this.formatearFecha(resena.fechaCreacion);

        return `
            <div class="notification-item resena-notification" data-resena-id="${resena.id}">
                <div class="notification-icon resena-icon">
                    <i class="bi bi-star-fill"></i>
                </div>
                <div class="notification-body">
                    <div class="notification-title">
                        <strong>${resena.usuarioNombre}</strong> dejó una reseña
                        <span class="user-id">ID: ${resena.usuarioId}</span>
                    </div>
                    <div class="notification-product">
                        <i class="bi bi-box-seam"></i>
                        ${resena.productoNombre}
                    </div>
                    <div class="notification-rating">
                        <span class="stars">${estrellas}</span>
                        <span class="rating-number">${resena.calificacion}.0</span>
                    </div>
                    <div class="notification-comment" onclick="this.classList.toggle('expanded')">
                        "${resena.comentario}"
                    </div>
                    <div class="notification-actions">
                        <button class="btn-aprobar-resena" data-resena-id="${resena.id}">
                            <i class="bi bi-check-circle"></i>
                            Aprobar
                        </button>
                        <button class="btn-rechazar-resena" data-resena-id="${resena.id}">
                            <i class="bi bi-x-circle"></i>
                            Rechazar
                        </button>
                    </div>
                    <div class="notification-time">
                        <i class="bi bi-clock"></i>
                        ${fecha}
                    </div>
                </div>
            </div>
        `;
    }

    /**
     * Aprobar reseña
     */
    async aprobarResena(resenaId) {
        try {
            const response = await fetch(`/resenas/admin/aprobar/${resenaId}`, {
                method: 'POST',
                credentials: 'same-origin'
            });
            const data = await response.json();

            if (data.success) {
                // Mostrar mensaje de éxito
                this.showToast('Reseña aprobada exitosamente', 'success');
                
                // Recargar notificaciones
                await this.loadNotifications();
                await this.loadResenasContent();
            } else {
                this.showToast(data.message || 'Error al aprobar reseña', 'error');
            }
        } catch (error) {
            console.error('Error al aprobar reseña:', error);
            this.showToast('Error de conexión', 'error');
        }
    }

    /**
     * Rechazar reseña
     */
    async rechazarResena(resenaId) {
        try {
            const response = await fetch(`/resenas/admin/rechazar/${resenaId}`, {
                method: 'POST',
                credentials: 'same-origin'
            });
            const data = await response.json();

            if (data.success) {
                // Mostrar mensaje de éxito
                this.showToast('Reseña rechazada', 'success');
                
                // Recargar notificaciones
                await this.loadNotifications();
                await this.loadResenasContent();
            } else {
                this.showToast(data.message || 'Error al rechazar reseña', 'error');
            }
        } catch (error) {
            console.error('Error al rechazar reseña:', error);
            this.showToast('Error de conexión', 'error');
        }
    }

    /**
     * Mostrar toast de notificación
     */
    showToast(message, type = 'info') {
        const toast = document.createElement('div');
        toast.className = `admin-toast ${type}`;
        toast.innerHTML = `
            <i class="bi bi-${type === 'success' ? 'check-circle' : 'exclamation-circle'}"></i>
            <span>${message}</span>
        `;

        document.body.appendChild(toast);

        setTimeout(() => {
            toast.classList.add('show');
        }, 10);

        setTimeout(() => {
            toast.classList.remove('show');
            setTimeout(() => {
                toast.remove();
            }, 300);
        }, 3000);
    }

    /**
     * Formatear fecha
     */
    formatearFecha(fechaStr) {
        const fecha = new Date(fechaStr);
        const ahora = new Date();
        const diff = ahora - fecha;
        const minutos = Math.floor(diff / 60000);
        const horas = Math.floor(diff / 3600000);
        const dias = Math.floor(diff / 86400000);

        if (minutos < 1) return 'Hace un momento';
        if (minutos < 60) return `Hace ${minutos} minuto${minutos !== 1 ? 's' : ''}`;
        if (horas < 24) return `Hace ${horas} hora${horas !== 1 ? 's' : ''}`;
        if (dias < 7) return `Hace ${dias} día${dias !== 1 ? 's' : ''}`;
        
        return fecha.toLocaleDateString('es-ES', { 
            day: 'numeric', 
            month: 'short',
            year: fecha.getFullYear() !== ahora.getFullYear() ? 'numeric' : undefined
        });
    }

    /**
     * Ver todas las reseñas en modal personalizado
     */
    async verTodasLasResenas() {
        // Cerrar el panel de notificaciones
        this.closeNotificationPanel();
        
        try {
            const response = await fetch('/resenas/admin/pendientes', {
                credentials: 'same-origin'
            });
            const data = await response.json();
            
            if (!data.success) {
                this.mostrarError('Error', data.message || 'No se pudieron cargar las reseñas');
                return;
            }
            
            const resenas = data.resenas || [];
            
            if (resenas.length === 0) {
                this.mostrarError('Sin reseñas', 'No hay reseñas disponibles en este momento');
                return;
            }
            
            this.abrirModalResenasCompleto(resenas);
            
        } catch (error) {
            console.error('Error al cargar todas las reseñas:', error);
            this.mostrarError('Error de conexión', 'No se pudo conectar con el servidor');
        }
    }
    
    /**
     * Abrir modal con SweetAlert2 con todas las reseñas
     */
    abrirModalResenasCompleto(resenas) {
        // Detectar modo oscuro
        const isDarkMode = document.body.classList.contains('dark-mode');
        
        const resenasHTML = resenas.map((resena, index) => {
            const iniciales = resena.usuarioNombre.split(' ').map(n => n[0]).join('').toUpperCase().substring(0, 2);
            const estrellas = '★'.repeat(resena.calificacion) + '☆'.repeat(5 - resena.calificacion);
            const fecha = this.formatearFecha(resena.fechaCreacion);
            
            // Badge de estado mejorado
            let estadoBadgeHTML = '';
            if (resena.estado === 'PENDIENTE') {
                estadoBadgeHTML = '<span class="admin-estado-badge badge-pendiente"><i class="bi bi-clock-fill"></i> PENDIENTE</span>';
            } else if (resena.estado === 'APROBADA') {
                estadoBadgeHTML = '<span class="admin-estado-badge badge-aprobada"><i class="bi bi-check-circle-fill"></i> APROBADA</span>';
            } else if (resena.estado === 'RECHAZADA') {
                estadoBadgeHTML = '<span class="admin-estado-badge badge-rechazada"><i class="bi bi-x-circle-fill"></i> RECHAZADA</span>';
            }
            
            return `
                <div class="admin-resena-card" style="animation-delay: ${index * 0.1}s;">
                    <div class="admin-resena-header">
                        <div class="admin-resena-user">
                            <div class="admin-resena-avatar">
                                <span>${iniciales}</span>
                                <div class="admin-avatar-ring"></div>
                            </div>
                            <div class="admin-resena-user-info">
                                <h4>${resena.usuarioNombre}</h4>
                                <span class="admin-user-id"><i class="bi bi-person-badge"></i> ID: ${resena.usuarioId}</span>
                            </div>
                        </div>
                        ${estadoBadgeHTML}
                    </div>
                    
                    <div class="admin-resena-producto">
                        <div class="producto-icon">
                            <i class="bi bi-box-seam-fill"></i>
                        </div>
                        <span>${resena.productoNombre}</span>
                    </div>
                    
                    <div class="admin-resena-rating">
                        <div class="admin-stars">${estrellas}</div>
                        <span class="admin-rating-value">${resena.calificacion}.0 / 5.0</span>
                    </div>
                    
                    <div class="admin-resena-comment">
                        <i class="bi bi-quote quote-icon-left"></i>
                        <p>${resena.comentario}</p>
                        <i class="bi bi-quote quote-icon-right"></i>
                    </div>
                    
                    <div class="admin-resena-footer">
                        <i class="bi bi-calendar3"></i>
                        <span>${fecha}</span>
                    </div>
                </div>
            `;
        }).join('');

        // Contar reseñas pendientes
        const resenasPendientes = resenas.filter(r => r.estado === 'PENDIENTE');
        const hayPendientes = resenasPendientes.length > 0;

        Swal.fire({
            title: `<div class="admin-swal-title">
                        <i class="bi bi-star-fill"></i>
                        <span>Todas las Reseñas</span>
                        <button class="admin-swal-custom-close-btn" onclick="Swal.close()">
                            <i class="bi bi-x-lg"></i>
                        </button>
                    </div>`,
            html: `
                <div class="admin-resenas-subtitle">
                    <i class="bi bi-list-check"></i>
                    <span>${resenas.length} reseña${resenas.length !== 1 ? 's' : ''} disponible${resenas.length !== 1 ? 's' : ''}</span>
                </div>
                ${hayPendientes ? `
                    <div class="admin-resenas-actions-global">
                        <button class="btn-aprobar-todas-modal" onclick="adminNotificationManager.aprobarTodasResenas()">
                            <i class="bi bi-check-circle-fill"></i>
                            <span>Aprobar Todas (${resenasPendientes.length})</span>
                        </button>
                        <button class="btn-rechazar-todas-modal" onclick="adminNotificationManager.rechazarTodasResenas()">
                            <i class="bi bi-x-circle-fill"></i>
                            <span>Rechazar Todas (${resenasPendientes.length})</span>
                        </button>
                    </div>
                ` : ''}
                <div class="admin-resenas-container">
                    ${resenasHTML}
                </div>
            `,
            showConfirmButton: false,
            showCloseButton: false,
            allowOutsideClick: true,
            allowEscapeKey: true,
            width: '800px',
            padding: '0',
            customClass: {
                popup: `admin-swal-resenas-popup ${isDarkMode ? 'admin-swal-dark' : ''}`,
                title: 'admin-swal-resenas-title',
                htmlContainer: 'admin-swal-resenas-html'
            },
            showClass: {
                popup: 'admin-swal-show',
                backdrop: 'admin-swal-backdrop-show'
            },
            hideClass: {
                popup: 'admin-swal-hide',
                backdrop: 'admin-swal-backdrop-hide'
            }
        });
    }
    
    /**
     * Aprobar todas las reseñas pendientes
     */
    async aprobarTodasResenas() {
        // Confirmar acción
        const isDarkMode = document.body.classList.contains('dark-mode');
        const result = await Swal.fire({
            title: '¿Aprobar todas las reseñas?',
            text: 'Se aprobarán todas las reseñas pendientes',
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: '#10b981',
            cancelButtonColor: '#64748b',
            confirmButtonText: 'Sí, aprobar todas',
            cancelButtonText: 'Cancelar',
            customClass: {
                popup: isDarkMode ? 'swal2-dark' : '',
                title: isDarkMode ? 'swal2-dark-title' : '',
                htmlContainer: isDarkMode ? 'swal2-dark-text' : ''
            }
        });

        if (!result.isConfirmed) return;

        try {
            // Obtener todas las reseñas pendientes
            const response = await fetch('/resenas/admin/pendientes', {
                credentials: 'same-origin'
            });
            const data = await response.json();

            if (data.success && data.resenas) {
                const resenasPendientes = data.resenas;
                let aprobadas = 0;
                let errores = 0;

                // Aprobar cada reseña
                for (const resena of resenasPendientes) {
                    try {
                        const aprobarResponse = await fetch(`/resenas/admin/aprobar/${resena.id}`, {
                            method: 'POST',
                            credentials: 'same-origin'
                        });
                        const aprobarData = await aprobarResponse.json();
                        
                        if (aprobarData.success) {
                            aprobadas++;
                        } else {
                            errores++;
                        }
                    } catch (error) {
                        errores++;
                    }
                }

                // Mostrar resultado
                if (aprobadas > 0) {
                    this.showToast(`${aprobadas} reseña${aprobadas !== 1 ? 's' : ''} aprobada${aprobadas !== 1 ? 's' : ''}`, 'success');
                }
                if (errores > 0) {
                    this.showToast(`${errores} error${errores !== 1 ? 'es' : ''}`, 'error');
                }

                // Cerrar modal y recargar
                Swal.close();
                await this.loadNotifications();
                setTimeout(() => this.verTodasResenas(), 300);
            }
        } catch (error) {
            console.error('Error al aprobar todas las reseñas:', error);
            this.showToast('Error de conexión', 'error');
        }
    }

    /**
     * Rechazar todas las reseñas pendientes
     */
    async rechazarTodasResenas() {
        // Confirmar acción
        const isDarkMode = document.body.classList.contains('dark-mode');
        const result = await Swal.fire({
            title: '¿Rechazar todas las reseñas?',
            text: 'Se rechazarán todas las reseñas pendientes',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#ef4444',
            cancelButtonColor: '#64748b',
            confirmButtonText: 'Sí, rechazar todas',
            cancelButtonText: 'Cancelar',
            customClass: {
                popup: isDarkMode ? 'swal2-dark' : '',
                title: isDarkMode ? 'swal2-dark-title' : '',
                htmlContainer: isDarkMode ? 'swal2-dark-text' : ''
            }
        });

        if (!result.isConfirmed) return;

        try {
            // Obtener todas las reseñas pendientes
            const response = await fetch('/resenas/admin/pendientes', {
                credentials: 'same-origin'
            });
            const data = await response.json();

            if (data.success && data.resenas) {
                const resenasPendientes = data.resenas;
                let rechazadas = 0;
                let errores = 0;

                // Rechazar cada reseña
                for (const resena of resenasPendientes) {
                    try {
                        const rechazarResponse = await fetch(`/resenas/admin/rechazar/${resena.id}`, {
                            method: 'POST',
                            credentials: 'same-origin'
                        });
                        const rechazarData = await rechazarResponse.json();
                        
                        if (rechazarData.success) {
                            rechazadas++;
                        } else {
                            errores++;
                        }
                    } catch (error) {
                        errores++;
                    }
                }

                // Mostrar resultado
                if (rechazadas > 0) {
                    this.showToast(`${rechazadas} reseña${rechazadas !== 1 ? 's' : ''} rechazada${rechazadas !== 1 ? 's' : ''}`, 'success');
                }
                if (errores > 0) {
                    this.showToast(`${errores} error${errores !== 1 ? 'es' : ''}`, 'error');
                }

                // Cerrar modal y recargar
                Swal.close();
                await this.loadNotifications();
                setTimeout(() => this.verTodasResenas(), 300);
            }
        } catch (error) {
            console.error('Error al rechazar todas las reseñas:', error);
            this.showToast('Error de conexión', 'error');
        }
    }
    
    
    /**
     * Mostrar error con SweetAlert2
     */
    mostrarError(titulo, mensaje) {
        Swal.fire({
            icon: 'error',
            title: titulo,
            text: mensaje,
            confirmButtonColor: '#8cbc00'
        });
    }
    
    /**
     * Obtener badge de estado simplificado
     */
    getEstadoBadgeSimple(estado) {
        const badges = {
            'PENDIENTE': '<span class="estado-badge pendiente"><i class="bi bi-clock"></i> PENDIENTE</span>',
            'APROBADA': '<span class="estado-badge aprobada"><i class="bi bi-check-circle-fill"></i> APROBADA</span>',
            'RECHAZADA': '<span class="estado-badge rechazada"><i class="bi bi-x-circle-fill"></i> RECHAZADA</span>'
        };
        return badges[estado] || badges['PENDIENTE'];
    }
    
    /**
     * Obtener badge de estado
     */
    getEstadoBadge(estado, isDark = true) {
        const badgeStyles = {
            'PENDIENTE': {
                bg: isDark ? 'rgba(245, 158, 11, 0.2)' : 'rgba(245, 158, 11, 0.15)',
                color: isDark ? '#fbbf24' : '#d97706',
                icon: 'bi-clock',
                text: 'PENDIENTE'
            },
            'APROBADA': {
                bg: isDark ? 'rgba(16, 185, 129, 0.2)' : 'rgba(16, 185, 129, 0.15)',
                color: isDark ? '#10b981' : '#059669',
                icon: 'bi-check-circle-fill',
                text: 'APROBADA'
            },
            'RECHAZADA': {
                bg: isDark ? 'rgba(239, 68, 68, 0.2)' : 'rgba(239, 68, 68, 0.15)',
                color: isDark ? '#f87171' : '#dc2626',
                icon: 'bi-x-circle-fill',
                text: 'RECHAZADA'
            }
        };
        
        const badge = badgeStyles[estado] || badgeStyles['PENDIENTE'];
        
        return `<span style="
            background: ${badge.bg};
            color: ${badge.color};
            padding: 6px 12px;
            border-radius: 8px;
            font-size: 11px;
            font-weight: 700;
            letter-spacing: 0.5px;
            display: inline-flex;
            align-items: center;
            gap: 6px;
            box-shadow: ${isDark ? '0 2px 8px rgba(0, 0, 0, 0.2)' : '0 2px 8px rgba(0, 0, 0, 0.1)'};
        ">
            <i class="bi ${badge.icon}"></i>
            ${badge.text}
        </span>`;
    }
    
    /**
     * Obtener color de estado
     */
    getEstadoColor(estado) {
        const colors = {
            'PENDIENTE': '#f59e0b',
            'APROBADA': '#10b981',
            'RECHAZADA': '#ef4444'
        };
        return colors[estado] || colors['PENDIENTE'];
    }

    /**
     * Aprobar todas las reseñas pendientes
     */
    async aprobarTodasLasResenas() {
        // Mostrar confirmación
        const isDarkMode = document.body.classList.contains('dark-mode');
        const result = await Swal.fire({
            title: '¿Aprobar todas las reseñas?',
            text: 'Esto aprobará todas las reseñas pendientes de una vez',
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: '#10b981',
            cancelButtonColor: '#6b7280',
            confirmButtonText: '<i class="bi bi-check-circle"></i> Sí, aprobar todas',
            cancelButtonText: 'Cancelar',
            customClass: {
                popup: isDarkMode ? 'swal2-dark' : '',
                title: isDarkMode ? 'swal2-dark-title' : '',
                htmlContainer: isDarkMode ? 'swal2-dark-text' : ''
            }
        });

        if (!result.isConfirmed) return;

        try {
            // Mostrar loader
            const isDarkModeLoader = document.body.classList.contains('dark-mode');
            Swal.fire({
                title: 'Aprobando reseñas...',
                text: 'Por favor espera',
                allowOutsideClick: false,
                customClass: {
                    popup: isDarkModeLoader ? 'swal2-dark' : '',
                    title: isDarkModeLoader ? 'swal2-dark-title' : '',
                    htmlContainer: isDarkModeLoader ? 'swal2-dark-text' : ''
                },
                didOpen: () => {
                    Swal.showLoading();
                }
            });

            const response = await fetch('/resenas/admin/aprobar-todas', {
                method: 'POST',
                credentials: 'same-origin'
            });
            const data = await response.json();

            Swal.close();

            if (data.success) {
                const isDarkModeSuccess = document.body.classList.contains('dark-mode');
                await Swal.fire({
                    icon: 'success',
                    title: '¡Listo!',
                    text: data.message,
                    confirmButtonColor: '#8cbc00',
                    customClass: {
                        popup: isDarkModeSuccess ? 'swal2-dark' : '',
                        title: isDarkModeSuccess ? 'swal2-dark-title' : '',
                        htmlContainer: isDarkModeSuccess ? 'swal2-dark-text' : ''
                    }
                });

                // Recargar notificaciones
                await this.loadNotifications();
                await this.loadResenasContent();
            } else {
                const isDarkModeError = document.body.classList.contains('dark-mode');
                Swal.fire({
                    icon: 'error',
                    title: 'Error',
                    text: data.message || 'No se pudieron aprobar las reseñas',
                    confirmButtonColor: '#8cbc00',
                    customClass: {
                        popup: isDarkModeError ? 'swal2-dark' : '',
                        title: isDarkModeError ? 'swal2-dark-title' : '',
                        htmlContainer: isDarkModeError ? 'swal2-dark-text' : ''
                    }
                });
            }
        } catch (error) {
            console.error('Error al aprobar todas las reseñas:', error);
            const isDarkModeCatch = document.body.classList.contains('dark-mode');
            Swal.fire({
                icon: 'error',
                title: 'Error de conexión',
                text: 'No se pudo conectar con el servidor',
                confirmButtonColor: '#8cbc00',
                customClass: {
                    popup: isDarkModeCatch ? 'swal2-dark' : '',
                    title: isDarkModeCatch ? 'swal2-dark-title' : '',
                    htmlContainer: isDarkModeCatch ? 'swal2-dark-text' : ''
                }
            });
        }
    }

    /**
     * Rechazar todas las reseñas pendientes
     */
    async rechazarTodasLasResenas() {
        // Mostrar confirmación
        const isDarkMode = document.body.classList.contains('dark-mode');
        const result = await Swal.fire({
            title: '¿Rechazar todas las reseñas?',
            text: 'Esta acción no se puede deshacer',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#ef4444',
            cancelButtonColor: '#6b7280',
            confirmButtonText: '<i class="bi bi-x-circle"></i> Sí, rechazar todas',
            cancelButtonText: 'Cancelar',
            customClass: {
                popup: isDarkMode ? 'swal2-dark' : '',
                title: isDarkMode ? 'swal2-dark-title' : '',
                htmlContainer: isDarkMode ? 'swal2-dark-text' : ''
            }
        });

        if (!result.isConfirmed) return;

        try {
            // Mostrar loader
            const isDarkModeLoader = document.body.classList.contains('dark-mode');
            Swal.fire({
                title: 'Rechazando reseñas...',
                text: 'Por favor espera',
                allowOutsideClick: false,
                customClass: {
                    popup: isDarkModeLoader ? 'swal2-dark' : '',
                    title: isDarkModeLoader ? 'swal2-dark-title' : '',
                    htmlContainer: isDarkModeLoader ? 'swal2-dark-text' : ''
                },
                didOpen: () => {
                    Swal.showLoading();
                }
            });

            const response = await fetch('/resenas/admin/rechazar-todas', {
                method: 'POST',
                credentials: 'same-origin'
            });
            const data = await response.json();

            Swal.close();

            if (data.success) {
                const isDarkModeSuccess = document.body.classList.contains('dark-mode');
                await Swal.fire({
                    icon: 'success',
                    title: '¡Listo!',
                    text: data.message,
                    confirmButtonColor: '#8cbc00',
                    customClass: {
                        popup: isDarkModeSuccess ? 'swal2-dark' : '',
                        title: isDarkModeSuccess ? 'swal2-dark-title' : '',
                        htmlContainer: isDarkModeSuccess ? 'swal2-dark-text' : ''
                    }
                });

                // Recargar notificaciones
                await this.loadNotifications();
                await this.loadResenasContent();
            } else {
                const isDarkModeError = document.body.classList.contains('dark-mode');
                Swal.fire({
                    icon: 'error',
                    title: 'Error',
                    text: data.message || 'No se pudieron rechazar las reseñas',
                    confirmButtonColor: '#8cbc00',
                    customClass: {
                        popup: isDarkModeError ? 'swal2-dark' : '',
                        title: isDarkModeError ? 'swal2-dark-title' : '',
                        htmlContainer: isDarkModeError ? 'swal2-dark-text' : ''
                    }
                });
            }
        } catch (error) {
            console.error('Error al rechazar todas las reseñas:', error);
            const isDarkModeCatch = document.body.classList.contains('dark-mode');
            Swal.fire({
                icon: 'error',
                title: 'Error de conexión',
                text: 'No se pudo conectar con el servidor',
                confirmButtonColor: '#8cbc00',
                customClass: {
                    popup: isDarkModeCatch ? 'swal2-dark' : '',
                    title: isDarkModeCatch ? 'swal2-dark-title' : '',
                    htmlContainer: isDarkModeCatch ? 'swal2-dark-text' : ''
                }
            });
        }
    }

    /**
     * Destruir el manager
     */
    destroy() {
        if (this.updateInterval) {
            clearInterval(this.updateInterval);
        }
    }
}

// Instancia global
const adminNotificationManager = new AdminNotificationManager();

// Inicializar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', () => {
    adminNotificationManager.init();
});

// Limpiar al salir
window.addEventListener('beforeunload', () => {
    adminNotificationManager.destroy();
});
