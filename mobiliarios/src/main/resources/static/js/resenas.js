// ========== SISTEMA DE RESEÑAS Y CALIFICACIONES ==========

class ResenaManager {
    constructor() {
        this.currentRating = 0;
        this.currentProductoId = null;
        this.currentAlquilerId = null;
        this.currentProductos = [];
        this.modoIndividual = false;
        this.resenasIndividuales = new Map();
    }

    /**
     * Inicializar sistema de reseñas para un producto
     */
    async inicializarResenas(productoId) {
        this.currentProductoId = productoId;
        await this.cargarResenas(productoId);
        await this.cargarEstadisticas(productoId);
    }

    /**
     * Cargar reseñas de un producto
     */
    async cargarResenas(productoId) {
        try {
            const response = await fetch(`/resenas/producto/${productoId}`);
            const data = await response.json();

            if (data.success) {
                this.mostrarResenas(data.resenas);
                if (data.estadisticas) {
                    this.mostrarEstadisticas(data.estadisticas);
                }
            }
        } catch (error) {
            console.error('Error al cargar reseñas:', error);
        }
    }

    /**
     * Cargar estadísticas de un producto
     */
    async cargarEstadisticas(productoId) {
        try {
            const response = await fetch(`/resenas/estadisticas/${productoId}`);
            const data = await response.json();

            if (data.success && data.estadisticas) {
                this.mostrarEstadisticas(data.estadisticas);
            }
        } catch (error) {
            console.error('Error al cargar estadísticas:', error);
        }
    }

    /**
     * Mostrar estadísticas de calificación
     */
    mostrarEstadisticas(estadisticas) {
        const { promedioCalificacion, totalResenas, distribucionEstrellas } = estadisticas;

        // Actualizar promedio
        const ratingNumberEl = document.querySelector('.rating-number');
        if (ratingNumberEl) {
            ratingNumberEl.textContent = promedioCalificacion.toFixed(1);
        }

        // Actualizar estrellas grandes
        const ratingStarsLargeEl = document.querySelector('.rating-stars-large');
        if (ratingStarsLargeEl) {
            ratingStarsLargeEl.innerHTML = this.generarEstrellas(promedioCalificacion);
        }

        // Actualizar conteo
        const ratingCountEl = document.querySelector('.rating-count');
        if (ratingCountEl) {
            ratingCountEl.textContent = `${totalResenas} reseña${totalResenas !== 1 ? 's' : ''}`;
        }

        // Actualizar distribución
        if (distribucionEstrellas) {
            for (let i = 0; i < 5; i++) {
                const count = distribucionEstrellas[4 - i]; // Invertir orden (5 a 1)
                const percentage = totalResenas > 0 ? (count / totalResenas) * 100 : 0;
                
                const progressEl = document.querySelector(`[data-star="${5 - i}"] .rating-bar-progress`);
                const countEl = document.querySelector(`[data-star="${5 - i}"] .rating-bar-count`);
                
                if (progressEl) {
                    progressEl.style.width = `${percentage}%`;
                }
                if (countEl) {
                    countEl.textContent = count;
                }
            }
        }
    }

    /**
     * Mostrar lista de reseñas
     */
    mostrarResenas(resenas) {
        const container = document.querySelector('.resenas-list');
        if (!container) return;

        if (resenas.length === 0) {
            container.innerHTML = `
                <div class="empty-reviews">
                    <i class="bi bi-chat-left-text"></i>
                    <h3>No hay reseñas aún</h3>
                    <p>Sé el primero en dejar una reseña de este producto</p>
                </div>
            `;
            return;
        }

        container.innerHTML = resenas.map(resena => this.crearResenaCard(resena)).join('');
    }

    /**
     * Crear tarjeta de reseña
     */
    crearResenaCard(resena) {
        const iniciales = resena.usuarioNombre.split(' ').map(n => n[0]).join('').toUpperCase().substring(0, 2);
        const fecha = this.formatearFecha(resena.fechaCreacion);
        const estrellas = this.generarEstrellas(resena.calificacion);

        let respuestaHtml = '';
        if (resena.respuestaAdmin) {
            respuestaHtml = `
                <div class="admin-response">
                    <div class="admin-response-header">
                        <i class="bi bi-shield-check"></i>
                        <span>Respuesta de FURENT</span>
                    </div>
                    <div class="admin-response-text">${this.escapeHtml(resena.respuestaAdmin)}</div>
                </div>
            `;
        }

        return `
            <div class="resena-card">
                <div class="resena-header">
                    <div class="resena-user-info">
                        <div class="resena-avatar">${iniciales}</div>
                        <div class="resena-user-details">
                            <h4>${this.escapeHtml(resena.usuarioNombre)}</h4>
                            <div class="resena-date">${fecha}</div>
                        </div>
                    </div>
                    <div class="resena-rating">
                        <div class="resena-stars">${estrellas}</div>
                        <span class="resena-score">${resena.calificacion}.0</span>
                    </div>
                </div>
                <div class="resena-content">
                    <p class="resena-comment">${this.escapeHtml(resena.comentario)}</p>
                    ${respuestaHtml}
                </div>
            </div>
        `;
    }

    /**
     * Abrir modal para crear reseña
     */
    abrirModalResena(alquilerId, productoId, productoNombre) {
        this.currentAlquilerId = alquilerId;
        this.currentProductoId = productoId;
        this.currentRating = 0;

        const modal = document.getElementById('modalResena');
        if (!modal) {
            this.crearModalResena(productoNombre);
        } else {
            document.getElementById('productoNombreResena').textContent = productoNombre;
            this.actualizarEstrellas(0);
            document.getElementById('comentarioResena').value = '';
        }

        document.getElementById('modalResena').classList.add('active');
        document.body.style.overflow = 'hidden';
    }

    /**
     * Crear modal de reseña
     */
    crearModalResena(productoNombre) {
        const modalHtml = `
            <div id="modalResena" class="modal-resena">
                <div class="modal-resena-overlay" onclick="resenaManager.cerrarModalResena()"></div>
                <div class="modal-resena-content">
                    <div class="modal-resena-header">
                        <div class="modal-header-icon">
                            <i class="bi bi-star-fill"></i>
                        </div>
                        <div class="modal-header-text">
                            <h3>Comparte tu Experiencia</h3>
                            <p>Tu opinión ayuda a otros usuarios a tomar mejores decisiones</p>
                        </div>
                        <button class="modal-close" onclick="resenaManager.cerrarModalResena()">
                            <i class="bi bi-x-lg"></i>
                        </button>
                    </div>
                    <div class="resena-form">
                        <div class="form-group producto-section">
                            <label>
                                <i class="bi bi-box-seam"></i>
                                Producto
                            </label>
                            <div class="resena-producto-card">
                                <div class="producto-icon">
                                    <i class="bi bi-box2-heart-fill"></i>
                                </div>
                                <span id="productoNombreResena">${productoNombre}</span>
                            </div>
                        </div>
                        
                        <div class="form-group rating-section">
                            <label>
                                <i class="bi bi-star-half"></i>
                                ¿Qué calificación le das? *
                            </label>
                            <div class="rating-input-container">
                                <div class="star-rating" id="starRating">
                                    ${[1, 2, 3, 4, 5].map(i => `<span class="star" data-rating="${i}">★</span>`).join('')}
                                </div>
                                <div class="rating-feedback">
                                    <span class="rating-value" id="ratingValue">Selecciona una calificación</span>
                                    <span class="rating-description" id="ratingDescription"></span>
                                </div>
                            </div>
                        </div>
                        
                        <div class="form-group comment-section">
                            <label>
                                <i class="bi bi-chat-left-text-fill"></i>
                                Cuéntanos tu experiencia *
                            </label>
                            <div class="textarea-wrapper">
                                <textarea id="comentarioResena" class="form-control" 
                                    placeholder="¿Qué te pareció el producto? ¿Cumplió con tus expectativas? Comparte los detalles de tu experiencia..." 
                                    maxlength="500"
                                    rows="5"></textarea>
                                <div class="textarea-footer">
                                    <span class="char-count"><span id="charCount">0</span>/500 caracteres</span>
                                    <i class="bi bi-info-circle" title="Mínimo 10 caracteres"></i>
                                </div>
                            </div>
                        </div>
                        
                        <div class="form-actions">
                            <button class="btn-cancel" onclick="resenaManager.cerrarModalResena()">
                                <i class="bi bi-x-circle"></i>
                                Cancelar
                            </button>
                            <button class="btn-submit-review" onclick="resenaManager.enviarResena()">
                                <i class="bi bi-send-fill"></i>
                                Enviar Reseña
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;

        document.body.insertAdjacentHTML('beforeend', modalHtml);
        this.inicializarEstrellas();
        this.inicializarContadorCaracteres();
    }

    /**
     * Cerrar modal de reseña
     */
    cerrarModalResena() {
        const modal = document.getElementById('modalResena');
        if (modal) {
            modal.classList.remove('active');
            document.body.style.overflow = '';
        }
    }

    /**
     * Abrir modal para crear reseñas múltiples (general o individual)
     */
    async abrirModalResenaMultiple(alquilerId, productos) {
        this.currentAlquilerId = alquilerId;
        this.currentProductos = productos;
        this.modoIndividual = false;
        this.resenasIndividuales.clear();
        
        // Obtener productos ya reseñados
        let productosResenados = [];
        try {
            const response = await fetch(`/resenas/productos-resenados/${alquilerId}`);
            const data = await response.json();
            if (data.success) {
                productosResenados = data.productosResenados || [];
            }
        } catch (error) {
            console.error('Error al obtener productos reseñados:', error);
        }
        
        // Inicializar reseñas individuales con valores por defecto
        productos.forEach((producto, index) => {
            const yaResenado = productosResenados.includes(String(producto.id));
            this.resenasIndividuales.set(String(producto.id), {
                calificacion: 0,
                comentario: '',
                seleccionado: !yaResenado && index === 0, // Solo el primero no reseñado
                yaResenado: yaResenado // Marcar si ya fue reseñado
            });
        });

        const modal = document.getElementById('modalResenaMultiple');
        if (!modal) {
            this.crearModalResenaMultiple(productos);
        } else {
            this.actualizarModalResenaMultiple(productos);
        }

        document.getElementById('modalResenaMultiple').classList.add('active');
        document.body.style.overflow = 'hidden';
    }

    /**
     * Cerrar modal de reseñas múltiples
     */
    cerrarModalResenaMultiple() {
        const modal = document.getElementById('modalResenaMultiple');
        if (modal) {
            modal.classList.remove('active');
            document.body.style.overflow = '';
        }
    }

    /**
     * Crear modal de reseñas múltiples
     */
    crearModalResenaMultiple(productos) {
        const productosListHtml = productos.map(p => `
            <div class="producto-item-review">
                <div class="producto-item-icon">
                    <img src="${p.imagen}" alt="${p.nombre}" onerror="this.src='/images/placeholder.jpg'">
                </div>
                <span class="producto-item-name">${p.nombre}</span>
            </div>
        `).join('');

        const modalHtml = `
            <div id="modalResenaMultiple" class="modal-resena-multiple">
                <div class="modal-resena-overlay" onclick="resenaManager.cerrarModalResenaMultiple()"></div>
                <div class="modal-resena-content-multiple">
                    <div class="modal-resena-header">
                        <div class="modal-header-icon">
                            <i class="bi bi-star-fill"></i>
                        </div>
                        <div class="modal-header-text">
                            <h3>Comparte tu Experiencia</h3>
                            <p>Tu opinión ayuda a otros usuarios a tomar mejores decisiones</p>
                        </div>
                        <button class="modal-close" onclick="resenaManager.cerrarModalResenaMultiple()">
                            <i class="bi bi-x-lg"></i>
                        </button>
                    </div>
                    <div class="resena-form-multiple">
                        <div class="form-group productos-section">
                            <label>
                                <i class="bi bi-box-seam"></i>
                                Productos del Alquiler (${productos.length})
                            </label>
                            <div class="productos-list-review">
                                ${productosListHtml}
                            </div>
                        </div>
                        
                        <!-- Checkbox para cambiar modo -->
                        <div class="form-group modo-resena-section">
                            <div class="modo-resena-toggle">
                                <input type="checkbox" id="modoIndividualCheck" onchange="resenaManager.cambiarModoResena(this.checked)">
                                <label for="modoIndividualCheck" class="modo-label">
                                    <div class="modo-icon">
                                        <i class="bi bi-toggles"></i>
                                    </div>
                                    <div class="modo-text">
                                        <strong>Reseñas Individuales</strong>
                                        <span>Activa para calificar cada producto por separado</span>
                                    </div>
                                </label>
                            </div>
                        </div>
                        
                        <!-- Contenedor para reseña general o individuales -->
                        <div id="resenaContentContainer">
                            ${this.generarFormularioGeneral()}
                        </div>
                        
                        <div class="form-actions">
                            <button class="btn-cancel" onclick="resenaManager.cerrarModalResenaMultiple()">
                                <i class="bi bi-x-circle"></i>
                                Cancelar
                            </button>
                            <button class="btn-submit-review" onclick="resenaManager.enviarResenasMultiples()">
                                <i class="bi bi-send-fill"></i>
                                <span id="btnSubmitText">Enviar Reseña</span>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;

        document.body.insertAdjacentHTML('beforeend', modalHtml);
        this.inicializarEstrellasGenerales();
        this.inicializarContadorCaracteres();
    }

    /**
     * Actualizar modal de reseñas múltiples
     */
    actualizarModalResenaMultiple(productos) {
        this.modoIndividual = false;
        document.getElementById('modoIndividualCheck').checked = false;
        
        const productosListHtml = productos.map(p => `
            <div class="producto-item-review">
                <div class="producto-item-icon">
                    <img src="${p.imagen}" alt="${p.nombre}" onerror="this.src='/images/placeholder.jpg'">
                </div>
                <span class="producto-item-name">${p.nombre}</span>
            </div>
        `).join('');
        
        document.querySelector('.productos-list-review').innerHTML = productosListHtml;
        document.getElementById('resenaContentContainer').innerHTML = this.generarFormularioGeneral();
        
        this.inicializarEstrellasGenerales();
        this.inicializarContadorCaracteres();
    }

    /**
     * Generar formulario de reseña general
     */
    generarFormularioGeneral() {
        return `
            <div class="resena-general-form">
                <div class="form-group rating-section">
                    <label>
                        <i class="bi bi-star-half"></i>
                        ¿Qué calificación le das a tu experiencia? *
                    </label>
                    <div class="rating-input-container">
                        <div class="star-rating" id="starRatingGeneral">
                            ${[1, 2, 3, 4, 5].map(i => `<span class="star" data-rating="${i}">★</span>`).join('')}
                        </div>
                        <div class="rating-feedback">
                            <span class="rating-value" id="ratingValueGeneral">Selecciona una calificación</span>
                            <span class="rating-description" id="ratingDescriptionGeneral"></span>
                        </div>
                    </div>
                </div>
                
                <div class="form-group comment-section">
                    <label>
                        <i class="bi bi-chat-left-text-fill"></i>
                        Cuéntanos tu experiencia general *
                    </label>
                    <div class="textarea-wrapper">
                        <textarea id="comentarioGeneral" class="form-control" 
                            placeholder="¿Qué te pareció el servicio y los productos? ¿Cumplieron con tus expectativas? Comparte los detalles de tu experiencia..." 
                            maxlength="500"
                            rows="5"></textarea>
                        <div class="textarea-footer">
                            <span class="char-count"><span id="charCountGeneral">0</span>/500 caracteres</span>
                            <i class="bi bi-info-circle" title="Mínimo 10 caracteres"></i>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }

    /**
     * Generar formularios de reseñas individuales
     */
    generarFormulariosIndividuales() {
        return `
            <div class="resenas-individuales-container">
                <div class="resenas-individuales-info">
                    <i class="bi bi-info-circle"></i>
                    <span>Selecciona y califica los productos que deseas reseñar</span>
                </div>
                ${this.currentProductos.map((producto, index) => {
                    const resena = this.resenasIndividuales.get(String(producto.id));
                    const yaResenado = resena ? resena.yaResenado : false;
                    const seleccionado = resena ? resena.seleccionado : false;
                    
                    return `
                    <div class="resena-individual-card ${seleccionado ? 'producto-seleccionado' : ''} ${yaResenado ? 'producto-ya-resenado' : ''}" data-producto-id="${producto.id}" id="card_${producto.id}">
                        <div class="producto-header-card">
                            <div class="producto-checkbox-wrapper">
                                <input type="checkbox" 
                                       id="checkbox_${producto.id}" 
                                       class="producto-checkbox"
                                       ${seleccionado ? 'checked' : ''}
                                       ${yaResenado ? 'disabled' : ''}
                                       onchange="resenaManager.toggleProductoResena('${producto.id}', this.checked)">
                                <label for="checkbox_${producto.id}" class="checkbox-label ${yaResenado ? 'checkbox-disabled' : ''}">
                                    <i class="bi bi-${yaResenado ? 'check2-all' : 'check-circle-fill'}"></i>
                                </label>
                            </div>
                            <div class="producto-img-small">
                                <img src="${producto.imagen}" alt="${producto.nombre}" onerror="this.src='/images/placeholder.jpg'">
                            </div>
                            <h4 class="producto-nombre-individual">
                                ${producto.nombre}
                                ${yaResenado ? '<span class="badge-resenado"><i class="bi bi-star-fill"></i> Ya reseñado</span>' : ''}
                            </h4>
                        </div>
                        
                        <div class="form-group rating-section-individual">
                            <label>
                                <i class="bi bi-star-half"></i>
                                Calificación *
                            </label>
                            <div class="rating-input-container">
                                <div class="star-rating" id="starRating_${producto.id}">
                                    ${[1, 2, 3, 4, 5].map(i => `<span class="star" data-rating="${i}" data-producto-id="${producto.id}">★</span>`).join('')}
                                </div>
                                <div class="rating-feedback">
                                    <span class="rating-value" id="ratingValue_${producto.id}">Selecciona</span>
                                    <span class="rating-description" id="ratingDescription_${producto.id}"></span>
                                </div>
                            </div>
                        </div>
                        
                        <div class="form-group comment-section-individual">
                            <label>
                                <i class="bi bi-chat-left-text-fill"></i>
                                Tu opinión sobre este producto *
                            </label>
                            <textarea id="comentario_${producto.id}" class="form-control" 
                                placeholder="¿Qué te pareció este producto específicamente?" 
                                maxlength="500"
                                rows="3"></textarea>
                            <div class="textarea-footer">
                                <span class="char-count-small"><span id="charCount_${producto.id}">0</span>/500</span>
                            </div>
                        </div>
                    </div>
                `}).join('')}
            </div>
        `;
    }

    /**
     * Cambiar modo de reseña (general/individual)
     */
    cambiarModoResena(individual) {
        this.modoIndividual = individual;
        const container = document.getElementById('resenaContentContainer');
        const btnText = document.getElementById('btnSubmitText');
        
        if (individual) {
            container.innerHTML = this.generarFormulariosIndividuales();
            this.actualizarContadorProductosSeleccionados();
            this.inicializarEstrellasIndividuales();
            this.inicializarContadoresIndividuales();
        } else {
            container.innerHTML = this.generarFormularioGeneral();
            btnText.textContent = 'Enviar Reseña';
            this.inicializarEstrellasGenerales();
            this.inicializarContadorCaracteres();
        }
    }

    /**
     * Activar/Desactivar producto para reseña
     */
    toggleProductoResena(productoId, seleccionado) {
        const productoIdStr = String(productoId);
        const resena = this.resenasIndividuales.get(productoIdStr);
        if (resena) {
            resena.seleccionado = seleccionado;
        }
        
        // Actualizar estilos visuales de la tarjeta
        const card = document.getElementById(`card_${productoId}`);
        if (card) {
            if (seleccionado) {
                card.classList.add('producto-seleccionado');
            } else {
                card.classList.remove('producto-seleccionado');
            }
        }
        
        // Actualizar contador del botón
        this.actualizarContadorProductosSeleccionados();
    }

    /**
     * Actualizar contador de productos seleccionados en el botón
     */
    actualizarContadorProductosSeleccionados() {
        const btnText = document.getElementById('btnSubmitText');
        if (!btnText) return;
        
        let seleccionados = 0;
        this.resenasIndividuales.forEach(resena => {
            if (resena.seleccionado) seleccionados++;
        });
        
        if (seleccionados === 0) {
            btnText.textContent = 'Selecciona al menos 1 producto';
        } else if (seleccionados === 1) {
            btnText.textContent = 'Enviar 1 Reseña';
        } else {
            btnText.textContent = `Enviar ${seleccionados} Reseñas`;
        }
    }

    /**
     * Inicializar sistema de estrellas interactivo
     */
    inicializarEstrellas() {
        const stars = document.querySelectorAll('#starRating .star');
        
        stars.forEach(star => {
            star.addEventListener('click', () => {
                const rating = parseInt(star.dataset.rating);
                this.currentRating = rating;
                this.actualizarEstrellas(rating);
            });

            star.addEventListener('mouseenter', () => {
                const rating = parseInt(star.dataset.rating);
                this.highlightStars(rating);
            });
        });

        document.getElementById('starRating').addEventListener('mouseleave', () => {
            this.actualizarEstrellas(this.currentRating);
        });
    }

    /**
     * Actualizar estrellas seleccionadas
     */
    actualizarEstrellas(rating) {
        const stars = document.querySelectorAll('#starRating .star');
        const ratingValue = document.getElementById('ratingValue');
        const ratingDescription = document.getElementById('ratingDescription');

        stars.forEach((star, index) => {
            if (index < rating) {
                star.classList.add('active');
            } else {
                star.classList.remove('active');
            }
        });

        // Descripciones de calificaciones
        const descriptions = {
            0: 'Selecciona una calificación',
            1: 'Muy Malo',
            2: 'Malo',
            3: 'Regular',
            4: 'Bueno',
            5: 'Excelente'
        };

        if (ratingValue) {
            if (rating === 0) {
                ratingValue.textContent = descriptions[0];
            } else {
                ratingValue.textContent = `${rating}.0 / 5.0`;
            }
        }

        if (ratingDescription) {
            ratingDescription.textContent = rating > 0 ? descriptions[rating] : '';
        }
    }

    /**
     * Resaltar estrellas al pasar el mouse
     */
    highlightStars(rating) {
        const stars = document.querySelectorAll('#starRating .star');
        stars.forEach((star, index) => {
            if (index < rating) {
                star.style.color = '#ffc107';
                star.style.transform = 'scale(1.1)';
            } else {
                star.style.color = '#ddd';
                star.style.transform = 'scale(1)';
            }
        });
    }

    /**
     * Inicializar estrellas para reseña general
     */
    inicializarEstrellasGenerales() {
        this.currentRating = 0;
        const stars = document.querySelectorAll('#starRatingGeneral .star');
        
        stars.forEach(star => {
            star.addEventListener('click', () => {
                const rating = parseInt(star.dataset.rating);
                this.currentRating = rating;
                this.actualizarEstrellasGenerales(rating);
            });

            star.addEventListener('mouseenter', () => {
                const rating = parseInt(star.dataset.rating);
                this.highlightEstrellasGenerales(rating);
            });
        });

        document.getElementById('starRatingGeneral').addEventListener('mouseleave', () => {
            this.actualizarEstrellasGenerales(this.currentRating);
        });
    }

    /**
     * Actualizar estrellas generales
     */
    actualizarEstrellasGenerales(rating) {
        const stars = document.querySelectorAll('#starRatingGeneral .star');
        const ratingValue = document.getElementById('ratingValueGeneral');
        const ratingDescription = document.getElementById('ratingDescriptionGeneral');

        stars.forEach((star, index) => {
            if (index < rating) {
                star.classList.add('active');
                star.style.color = '#ffc107';
            } else {
                star.classList.remove('active');
                star.style.color = '#ddd';
            }
        });

        const descriptions = {
            0: 'Selecciona una calificación',
            1: 'Muy Malo',
            2: 'Malo',
            3: 'Regular',
            4: 'Bueno',
            5: 'Excelente'
        };

        if (ratingValue) {
            ratingValue.textContent = rating === 0 ? descriptions[0] : `${rating}.0 / 5.0`;
        }

        if (ratingDescription) {
            ratingDescription.textContent = rating > 0 ? descriptions[rating] : '';
        }
    }

    /**
     * Resaltar estrellas generales al pasar el mouse
     */
    highlightEstrellasGenerales(rating) {
        const stars = document.querySelectorAll('#starRatingGeneral .star');
        stars.forEach((star, index) => {
            star.style.color = index < rating ? '#ffc107' : '#ddd';
            star.style.transform = index < rating ? 'scale(1.1)' : 'scale(1)';
        });
    }

    /**
     * Inicializar estrellas para reseñas individuales
     */
    inicializarEstrellasIndividuales() {
        this.currentProductos.forEach(producto => {
            const stars = document.querySelectorAll(`#starRating_${producto.id} .star`);
            
            stars.forEach(star => {
                star.addEventListener('click', () => {
                    const rating = parseInt(star.dataset.rating);
                    const productoId = String(star.dataset.productoId);
                    
                    const resena = this.resenasIndividuales.get(productoId);
                    if (resena) {
                        resena.calificacion = rating;
                    }
                    
                    this.actualizarEstrellasIndividuales(productoId, rating);
                });

                star.addEventListener('mouseenter', () => {
                    const rating = parseInt(star.dataset.rating);
                    const productoId = String(star.dataset.productoId);
                    this.highlightEstrellasIndividuales(productoId, rating);
                });
            });

            document.getElementById(`starRating_${producto.id}`).addEventListener('mouseleave', () => {
                const resena = this.resenasIndividuales.get(String(producto.id));
                const rating = resena ? resena.calificacion : 0;
                this.actualizarEstrellasIndividuales(String(producto.id), rating);
            });
        });
    }

    /**
     * Actualizar estrellas individuales
     */
    actualizarEstrellasIndividuales(productoId, rating) {
        const stars = document.querySelectorAll(`#starRating_${productoId} .star`);
        const ratingValue = document.getElementById(`ratingValue_${productoId}`);
        const ratingDescription = document.getElementById(`ratingDescription_${productoId}`);

        stars.forEach((star, index) => {
            star.style.color = index < rating ? '#ffc107' : '#ddd';
        });

        const descriptions = {
            0: 'Selecciona',
            1: 'Muy Malo',
            2: 'Malo',
            3: 'Regular',
            4: 'Bueno',
            5: 'Excelente'
        };

        if (ratingValue) {
            ratingValue.textContent = rating === 0 ? descriptions[0] : `${rating}.0 / 5.0`;
        }

        if (ratingDescription) {
            ratingDescription.textContent = rating > 0 ? descriptions[rating] : '';
        }
    }

    /**
     * Resaltar estrellas individuales al pasar el mouse
     */
    highlightEstrellasIndividuales(productoId, rating) {
        const stars = document.querySelectorAll(`#starRating_${productoId} .star`);
        stars.forEach((star, index) => {
            star.style.color = index < rating ? '#ffc107' : '#ddd';
            star.style.transform = index < rating ? 'scale(1.1)' : 'scale(1)';
        });
    }

    /**
     * Inicializar contadores individuales
     */
    inicializarContadoresIndividuales() {
        this.currentProductos.forEach(producto => {
            const textarea = document.getElementById(`comentario_${producto.id}`);
            const charCount = document.getElementById(`charCount_${producto.id}`);
            
            if (textarea && charCount) {
                textarea.addEventListener('input', () => {
                    const count = textarea.value.length;
                    charCount.textContent = count;
                    
                    if (count > 450) {
                        charCount.style.color = '#ef4444';
                    } else if (count > 400) {
                        charCount.style.color = '#f59e0b';
                    } else {
                        charCount.style.color = '#6c757d';
                    }
                });
            }
        });
    }

    /**
     * Enviar reseñas múltiples (general o individuales)
     */
    async enviarResenasMultiples() {
        if (this.modoIndividual) {
            await this.enviarResenasIndividuales();
        } else {
            await this.enviarResenaGeneral();
        }
    }

    /**
     * Enviar reseña general para todos los productos
     */
    async enviarResenaGeneral() {
        const comentario = document.getElementById('comentarioGeneral').value.trim();

        // Validaciones
        if (this.currentRating === 0) {
            Swal.fire({
                icon: 'warning',
                title: 'Calificación requerida',
                text: 'Por favor selecciona una calificación de 1 a 5 estrellas',
                confirmButtonColor: '#8cbc00'
            });
            return;
        }

        if (!comentario) {
            Swal.fire({
                icon: 'warning',
                title: 'Comentario requerido',
                text: 'Por favor escribe un comentario sobre tu experiencia',
                confirmButtonColor: '#8cbc00'
            });
            return;
        }

        if (comentario.length < 10) {
            Swal.fire({
                icon: 'warning',
                title: 'Comentario muy corto',
                text: 'El comentario debe tener al menos 10 caracteres',
                confirmButtonColor: '#8cbc00'
            });
            return;
        }

        try {
            const formData = new URLSearchParams();
            formData.append('alquilerId', this.currentAlquilerId);
            formData.append('calificacion', this.currentRating);
            formData.append('comentario', comentario);
            
            // Agregar IDs de todos los productos (sin índices para Spring Boot)
            this.currentProductos.forEach(producto => {
                formData.append('productosIds', producto.id);
            });

            // Debug: Ver datos enviados
            console.log('=== ENVIANDO RESEÑA GENERAL ===');
            console.log('alquilerId:', this.currentAlquilerId);
            console.log('calificacion:', this.currentRating);
            console.log('comentario:', comentario);
            console.log('productos:', this.currentProductos.map(p => p.id));
            console.log('FormData completo:', formData.toString());

            const response = await fetch('/resenas/crear-multiple', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: formData
            });

            console.log('Response status:', response.status);
            const data = await response.json();
            console.log('Response data:', data);

            if (data.success) {
                this.cerrarModalResenaMultiple();
                Swal.fire({
                    icon: 'success',
                    title: '¡Reseña enviada!',
                    text: data.message || 'Tu reseña será revisada antes de publicarse',
                    confirmButtonColor: '#8cbc00'
                }).then(() => {
                    if (window.location.pathname.includes('mis-alquileres')) {
                        location.reload();
                    }
                });
            } else {
                console.error('Error del servidor:', data.message);
                Swal.fire({
                    icon: 'error',
                    title: 'Error',
                    text: data.message || 'No se pudo enviar la reseña',
                    confirmButtonColor: '#8cbc00'
                });
            }
        } catch (error) {
            console.error('Error al enviar reseña:', error);
            Swal.fire({
                icon: 'error',
                title: 'Error de Conexión',
                text: 'Ocurrió un error al enviar la reseña. Revisa la consola para más detalles.',
                confirmButtonColor: '#8cbc00'
            });
        }
    }

    /**
     * Enviar reseñas individuales
     */
    async enviarResenasIndividuales() {
        const resenas = [];
        let hayError = false;
        let mensajeError = '';

        // Validar y recopilar solo las reseñas de productos seleccionados
        for (const producto of this.currentProductos) {
            const productoIdStr = String(producto.id);
            const resena = this.resenasIndividuales.get(productoIdStr);
            
            // Saltar productos no seleccionados
            if (!resena || !resena.seleccionado) {
                continue;
            }
            
            const comentario = document.getElementById(`comentario_${producto.id}`).value.trim();

            if (resena.calificacion === 0) {
                hayError = true;
                mensajeError = `Por favor califica el producto: ${producto.nombre}`;
                break;
            }

            if (!comentario) {
                hayError = true;
                mensajeError = `Por favor escribe un comentario para: ${producto.nombre}`;
                break;
            }

            if (comentario.length < 10) {
                hayError = true;
                mensajeError = `El comentario para ${producto.nombre} debe tener al menos 10 caracteres`;
                break;
            }

            resenas.push({
                productoId: producto.id,
                calificacion: resena.calificacion,
                comentario: comentario
            });
        }

        // Validar que al menos haya seleccionado un producto
        if (resenas.length === 0) {
            Swal.fire({
                icon: 'warning',
                title: 'Sin productos seleccionados',
                text: 'Debes seleccionar al menos un producto para reseñar',
                confirmButtonColor: '#8cbc00'
            });
            return;
        }

        if (hayError) {
            Swal.fire({
                icon: 'warning',
                title: 'Validación',
                text: mensajeError,
                confirmButtonColor: '#8cbc00'
            });
            return;
        }

        try {
            const response = await fetch('/resenas/crear-individuales', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    alquilerId: this.currentAlquilerId,
                    resenas: resenas
                })
            });

            const data = await response.json();

            if (data.success) {
                this.cerrarModalResenaMultiple();
                Swal.fire({
                    icon: 'success',
                    title: '¡Reseñas enviadas!',
                    text: data.message || `${resenas.length} reseñas enviadas. Serán revisadas antes de publicarse`,
                    confirmButtonColor: '#8cbc00'
                }).then(() => {
                    if (window.location.pathname.includes('mis-alquileres')) {
                        location.reload();
                    }
                });
            } else {
                Swal.fire({
                    icon: 'error',
                    title: 'Error',
                    text: data.message || 'No se pudieron enviar las reseñas',
                    confirmButtonColor: '#8cbc00'
                });
            }
        } catch (error) {
            console.error('Error al enviar reseñas:', error);
            Swal.fire({
                icon: 'error',
                title: 'Error',
                text: 'Ocurrió un error al enviar las reseñas',
                confirmButtonColor: '#8cbc00'
            });
        }
    }

    /**
     * Enviar reseña
     */
    async enviarResena() {
        const comentario = document.getElementById('comentarioResena').value.trim();

        // Validaciones
        if (this.currentRating === 0) {
            Swal.fire({
                icon: 'warning',
                title: 'Calificación requerida',
                text: 'Por favor selecciona una calificación de 1 a 5 estrellas',
                confirmButtonColor: '#8cbc00'
            });
            return;
        }

        if (!comentario) {
            Swal.fire({
                icon: 'warning',
                title: 'Comentario requerido',
                text: 'Por favor escribe un comentario sobre tu experiencia',
                confirmButtonColor: '#8cbc00'
            });
            return;
        }

        if (comentario.length < 10) {
            Swal.fire({
                icon: 'warning',
                title: 'Comentario muy corto',
                text: 'El comentario debe tener al menos 10 caracteres',
                confirmButtonColor: '#8cbc00'
            });
            return;
        }

        try {
            const formData = new URLSearchParams();
            formData.append('alquilerId', this.currentAlquilerId);
            formData.append('productoId', this.currentProductoId);
            formData.append('calificacion', this.currentRating);
            formData.append('comentario', comentario);

            const response = await fetch('/resenas/crear', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: formData
            });

            const data = await response.json();

            if (data.success) {
                this.cerrarModalResena();
                Swal.fire({
                    icon: 'success',
                    title: '¡Reseña enviada!',
                    text: data.message || 'Tu reseña será revisada antes de publicarse',
                    confirmButtonColor: '#8cbc00'
                }).then(() => {
                    // Recargar reseñas si estamos en la página del producto
                    if (this.currentProductoId) {
                        this.cargarResenas(this.currentProductoId);
                    }
                    // Recargar página de alquileres si estamos ahí
                    if (window.location.pathname.includes('mis-alquileres')) {
                        location.reload();
                    }
                });
            } else {
                Swal.fire({
                    icon: 'error',
                    title: 'Error',
                    text: data.message || 'No se pudo enviar la reseña',
                    confirmButtonColor: '#8cbc00'
                });
            }
        } catch (error) {
            console.error('Error al enviar reseña:', error);
            Swal.fire({
                icon: 'error',
                title: 'Error',
                text: 'Ocurrió un error al enviar la reseña',
                confirmButtonColor: '#8cbc00'
            });
        }
    }

    /**
     * Generar HTML de estrellas
     */
    generarEstrellas(calificacion) {
        const fullStars = Math.floor(calificacion);
        const hasHalfStar = calificacion % 1 >= 0.5;
        const emptyStars = 5 - fullStars - (hasHalfStar ? 1 : 0);

        let html = '';
        
        // Estrellas llenas
        for (let i = 0; i < fullStars; i++) {
            html += '<span class="star filled">★</span>';
        }
        
        // Media estrella
        if (hasHalfStar) {
            html += '<span class="star half">★</span>';
        }
        
        // Estrellas vacías
        for (let i = 0; i < emptyStars; i++) {
            html += '<span class="star empty">★</span>';
        }

        return html;
    }

    /**
     * Formatear fecha
     */
    formatearFecha(fechaStr) {
        const fecha = new Date(fechaStr);
        const ahora = new Date();
        const diff = ahora - fecha;
        const dias = Math.floor(diff / (1000 * 60 * 60 * 24));

        if (dias === 0) return 'Hoy';
        if (dias === 1) return 'Ayer';
        if (dias < 7) return `Hace ${dias} días`;
        if (dias < 30) return `Hace ${Math.floor(dias / 7)} semanas`;
        if (dias < 365) return `Hace ${Math.floor(dias / 30)} meses`;
        
        return fecha.toLocaleDateString('es-ES', { 
            year: 'numeric', 
            month: 'long', 
            day: 'numeric' 
        });
    }

    /**
     * Inicializar contador de caracteres
     */
    inicializarContadorCaracteres() {
        // Para modal antiguo (individual)
        const textarea = document.getElementById('comentarioResena');
        const charCount = document.getElementById('charCount');
        
        if (textarea && charCount) {
            textarea.addEventListener('input', () => {
                const count = textarea.value.length;
                charCount.textContent = count;
                
                // Cambiar color si está cerca del límite
                if (count > 450) {
                    charCount.style.color = '#ef4444';
                } else if (count > 400) {
                    charCount.style.color = '#f59e0b';
                } else {
                    charCount.style.color = '#6c757d';
                }
            });
        }

        // Para modal múltiple (general)
        const textareaGeneral = document.getElementById('comentarioGeneral');
        const charCountGeneral = document.getElementById('charCountGeneral');
        
        if (textareaGeneral && charCountGeneral) {
            textareaGeneral.addEventListener('input', () => {
                const count = textareaGeneral.value.length;
                charCountGeneral.textContent = count;
                
                // Cambiar color si está cerca del límite
                if (count > 450) {
                    charCountGeneral.style.color = '#ef4444';
                } else if (count > 400) {
                    charCountGeneral.style.color = '#f59e0b';
                } else {
                    charCountGeneral.style.color = '#6c757d';
                }
            });
        }
    }

    /**
     * Escapar HTML para prevenir XSS
     */
    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    /**
     * Verificar si el usuario puede dejar reseña
     */
    async puedeDejarResena(alquilerId) {
        try {
            const response = await fetch(`/resenas/puede-resenar/${alquilerId}`);
            const data = await response.json();
            return data.success && data.puedeResenar;
        } catch (error) {
            console.error('Error al verificar si puede reseñar:', error);
            return false;
        }
    }
}

// Instancia global
const resenaManager = new ResenaManager();

// Cerrar modal al hacer clic fuera
document.addEventListener('click', (e) => {
    const modal = document.getElementById('modalResena');
    if (modal && e.target === modal) {
        resenaManager.cerrarModalResena();
    }
});

/**
 * Cargar calificaciones de productos en las tarjetas
 * Esta función se ejecuta en páginas con tarjetas de productos (store, favoritos, etc.)
 */
async function cargarCalificacionesProductos() {
    const containers = document.querySelectorAll('.product-rating-container[data-producto-id]');
    
    if (containers.length === 0) {
        return;
    }
    
    for (const container of containers) {
        const productoId = container.getAttribute('data-producto-id');
        
        if (!productoId) continue;
        
        try {
            const response = await fetch(`/resenas/estadisticas/${productoId}`);
            const data = await response.json();
            
            if (data.success && data.estadisticas) {
                actualizarEstrellas(container, data.estadisticas.promedioCalificacion, data.estadisticas.totalResenas);
            }
        } catch (error) {
            console.error(`Error al cargar calificaciones del producto ${productoId}:`, error);
        }
    }
}

/**
 * Actualizar estrellas en una tarjeta de producto
 */
function actualizarEstrellas(container, promedio, totalResenas) {
    const starsWrapper = container.querySelector('.product-stars-wrapper');
    if (!starsWrapper) return;
    
    const starsContainer = starsWrapper.querySelector('.product-stars');
    const ratingNumber = starsWrapper.querySelector('.product-rating-number');
    
    if (!starsContainer || !ratingNumber) return;
    
    // Calcular estrellas llenas, medias y vacías
    const fullStars = Math.floor(promedio);
    const hasHalfStar = (promedio % 1) >= 0.5;
    const emptyStars = 5 - fullStars - (hasHalfStar ? 1 : 0);
    
    // Generar HTML de estrellas
    let starsHtml = '';
    
    // Estrellas llenas
    for (let i = 0; i < fullStars; i++) {
        starsHtml += '<span class="product-star filled" style="animation-delay: ' + (i * 0.1) + 's">★</span>';
    }
    
    // Media estrella
    if (hasHalfStar) {
        starsHtml += '<span class="product-star half" style="animation-delay: ' + (fullStars * 0.1) + 's">★</span>';
    }
    
    // Estrellas vacías
    for (let i = 0; i < emptyStars; i++) {
        starsHtml += '<span class="product-star empty" style="animation-delay: ' + ((fullStars + (hasHalfStar ? 1 : 0) + i) * 0.1) + 's">★</span>';
    }
    
    starsContainer.innerHTML = starsHtml;
    ratingNumber.textContent = promedio.toFixed(1);
    
    // Actualizar contador de reseñas
    const resenasCount = container.querySelector('.resenas-count');
    if (resenasCount) {
        resenasCount.textContent = `(${totalResenas})`;
    }
    
    // Agregar badge "Top Rated" si aplica
    if (promedio >= 4.5 && totalResenas >= 5) {
        const existingBadge = container.querySelector('.product-rating-badge');
        if (!existingBadge) {
            const badge = document.createElement('div');
            badge.className = 'product-rating-badge';
            badge.innerHTML = '<i class="bi bi-award-fill"></i> Top Rated';
            container.appendChild(badge);
        }
    }
}

/**
 * Abrir panel de reseñas desde botón en tarjeta
 */
function abrirPanelResenas(button) {
    const productoId = button.getAttribute('data-producto-id');
    const productoNombre = button.getAttribute('data-producto-nombre');
    
    if (!productoId) {
        console.error('No se encontró el ID del producto');
        return;
    }
    
    // Aquí puedes implementar la lógica para abrir un modal o panel con las reseñas
    // Por ahora, redirigimos a la página del producto o mostramos un modal
    console.log('Abrir reseñas del producto:', productoId, productoNombre);
    
    // Ejemplo: Mostrar modal con reseñas (requiere implementación adicional)
    function verTodasResenas(productoId, productoNombre) {
        // Detectar modo oscuro
        const isDarkMode = document.body.classList.contains('dark-mode');
        
        Swal.fire({
            title: `<div class="swal-custom-title">
                        <i class="bi bi-star-fill" style="color: #ffc107; margin-right: 10px;"></i>
                        Reseñas de ${productoNombre}
                    </div>`,
            html: `<div id="resenas-modal-content" class="resenas-modal-loading">
                        <div class="loading-spinner">
                            <i class="bi bi-arrow-repeat"></i>
                        </div>
                        <p>Cargando reseñas...</p>
                   </div>`,
            width: '900px',
            padding: '0',
            showCloseButton: true,
            showConfirmButton: false,
            customClass: {
                popup: `swal-resenas-popup ${isDarkMode ? 'swal-dark-mode' : ''}`,
                title: 'swal-resenas-title',
                htmlContainer: 'swal-resenas-container',
                closeButton: 'swal-resenas-close'
            },
            showClass: {
                popup: 'swal2-show-custom',
                backdrop: 'swal2-backdrop-show-custom'
            },
            hideClass: {
                popup: 'swal2-hide-custom',
                backdrop: 'swal2-backdrop-hide-custom'
            },
            didOpen: async () => {
                try {
                    const response = await fetch(`/resenas/producto/${productoId}`);
                    const data = await response.json();
                    
                    if (data.success && data.resenas && data.resenas.length > 0) {
                        const resenasHtml = data.resenas.map((resena, index) => {
                            const iniciales = resena.usuarioNombre.split(' ').map(n => n[0]).join('').toUpperCase().substring(0, 2);
                            const estrellas = '★'.repeat(resena.calificacion) + '☆'.repeat(5 - resena.calificacion);
                            const fecha = new Date(resena.fechaCreacion).toLocaleDateString('es-ES', { 
                                year: 'numeric', 
                                month: 'long', 
                                day: 'numeric' 
                            });
                            
                            return `
                                <div class="resena-modal-card" style="animation-delay: ${index * 0.1}s;">
                                    <div class="resena-modal-header">
                                        <div class="resena-modal-user">
                                            <div class="resena-modal-avatar">
                                                <span>${iniciales}</span>
                                                <div class="avatar-ring"></div>
                                            </div>
                                            <div class="resena-modal-user-info">
                                                <h4>${resena.usuarioNombre}</h4>
                                                <span class="resena-modal-date">
                                                    <i class="bi bi-calendar3"></i>
                                                    ${fecha}
                                                </span>
                                            </div>
                                        </div>
                                        <div class="resena-modal-rating">
                                            <div class="resena-modal-stars">${estrellas}</div>
                                            <span class="resena-modal-score">${resena.calificacion}.0</span>
                                        </div>
                                    </div>
                                    <div class="resena-modal-content">
                                        <p class="resena-modal-comment">
                                            <i class="bi bi-quote quote-left"></i>
                                            ${resena.comentario}
                                            <i class="bi bi-quote quote-right"></i>
                                        </p>
                                        ${resena.respuestaAdmin ? `
                                            <div class="resena-modal-response">
                                                <div class="response-header">
                                                    <div class="response-badge">
                                                        <i class="bi bi-shield-check-fill"></i>
                                                        <span>Respuesta de FURENT</span>
                                                    </div>
                                                </div>
                                                <p class="response-text">${resena.respuestaAdmin}</p>
                                            </div>
                                        ` : ''}
                                    </div>
                                </div>
                            `;
                        }).join('');
                        
                        document.getElementById('resenas-modal-content').innerHTML = `
                            <div class="resenas-modal-list">
                                ${resenasHtml}
                            </div>
                        `;
                    } else {
                        document.getElementById('resenas-modal-content').innerHTML = `
                            <div class="resenas-modal-empty">
                                <div class="empty-icon">
                                    <i class="bi bi-chat-left-text"></i>
                                </div>
                                <h3>No hay reseñas aún</h3>
                                <p>Sé el primero en compartir tu experiencia con este producto</p>
                            </div>
                        `;
                    }
                } catch (error) {
                    console.error('Error al cargar reseñas:', error);
                    document.getElementById('resenas-modal-content').innerHTML = `
                        <div class="resenas-modal-error">
                            <div class="error-icon">
                                <i class="bi bi-exclamation-triangle"></i>
                            </div>
                            <h3>Error al cargar las reseñas</h3>
                            <p>Por favor, intenta nuevamente más tarde</p>
                        </div>
                    `;
                }
            }
        });
    }
    verTodasResenas(productoId, productoNombre);
}
