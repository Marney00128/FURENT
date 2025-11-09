// Funciones para la vista previa del carrito

// Abrir/cerrar vista previa del carrito
function toggleCartPreview() {
    const preview = document.getElementById('cartPreview');
    const overlay = document.getElementById('cartPreviewOverlay');
    
    if (preview.classList.contains('active')) {
        closeCartPreview();
    } else {
        preview.classList.add('active');
        overlay.classList.add('active');
        cargarCarritoPreview();
    }
}

function closeCartPreview() {
    document.getElementById('cartPreview').classList.remove('active');
    document.getElementById('cartPreviewOverlay').classList.remove('active');
}

// Cargar contenido del carrito en la vista previa
function cargarCarritoPreview() {
    fetch('/carrito/datos')
        .then(response => response.json())
        .then(data => {
            const content = document.getElementById('cartPreviewContent');
            const footer = document.getElementById('cartPreviewFooter');
            const totalElement = document.getElementById('cartPreviewTotal');
            
            if (!data.items || data.items.length === 0) {
                content.innerHTML = `
                    <div class="cart-preview-empty">
                        <i class="bi bi-cart-x"></i>
                        <p>Tu carrito está vacío</p>
                    </div>
                `;
                footer.style.display = 'none';
            } else {
                let itemsHTML = '';
                let total = 0;
                
                data.items.forEach(item => {
                    const subtotal = item.precioProducto * item.cantidad * item.diasAlquiler;
                    total += subtotal;
                    
                    itemsHTML += `
                        <div class="cart-preview-item">
                            <div class="cart-preview-item-image">
                                <img src="${item.imagenProducto}" alt="${item.nombreProducto}">
                            </div>
                            <div class="cart-preview-item-details">
                                <p class="cart-preview-item-name">${item.nombreProducto}</p>
                                <p class="cart-preview-item-info">Cantidad: ${item.cantidad}</p>
                                <p class="cart-preview-item-info">Días: ${item.diasAlquiler}</p>
                                <p class="cart-preview-item-price">$${subtotal.toLocaleString('es-ES', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</p>
                            </div>
                            <button class="cart-preview-item-remove" onclick="eliminarItemPreview('${item.productoId}')">
                                <i class="bi bi-trash"></i>
                            </button>
                        </div>
                    `;
                });
                
                content.innerHTML = itemsHTML;
                totalElement.textContent = '$' + total.toLocaleString('es-ES', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
                footer.style.display = 'block';
            }
        })
        .catch(error => {
            console.error('Error al cargar carrito:', error);
        });
}

// Eliminar item del carrito desde la vista previa
function eliminarItemPreview(productoId) {
    fetch('/carrito/eliminar', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: `productoId=${productoId}`
    })
    .then(() => {
        cargarCarritoPreview();
        actualizarContadorCarrito();
    });
}

// Vaciar carrito desde la vista previa
function vaciarCarritoPreview() {
    Swal.fire({
        title: '¿Vaciar carrito?',
        text: 'Se eliminarán todos los productos del carrito',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#e74c3c',
        cancelButtonColor: '#95a5a6',
        confirmButtonText: 'Sí, vaciar',
        cancelButtonText: 'Cancelar'
    }).then((result) => {
        if (result.isConfirmed) {
            fetch('/carrito/vaciar', {
                method: 'POST'
            })
            .then(() => {
                cargarCarritoPreview();
                actualizarContadorCarrito();
                Swal.fire({
                    icon: 'success',
                    title: 'Carrito vaciado',
                    timer: 1500,
                    showConfirmButton: false
                });
            });
        }
    });
}

// Actualizar contador del carrito
function actualizarContadorCarrito() {
    fetch('/carrito/cantidad')
        .then(response => response.json())
        .then(data => {
            const cartCountElement = document.getElementById('cartCount'); if (cartCountElement) { cartCountElement.textContent = data.cantidadItems; }
        });
}

// Funci�n global para el bot�n del header
function toggleCart() {
    toggleCartPreview();
}

// Actualizar badge del carrito en el header
function updateCartBadge() {
    fetch('/carrito/datos')
        .then(response => response.json())
        .then(data => {
            const badge = document.getElementById('cartBadge');
            if (badge) {
                const totalItems = data.items ? data.items.reduce((sum, item) => sum + item.cantidad, 0) : 0;
                if (totalItems > 0) {
                    badge.textContent = totalItems;
                    badge.style.display = 'flex';
                } else {
                    badge.style.display = 'none';
                }
            }
        })
        .catch(error => console.error('Error al actualizar badge:', error));
}

// Mostrar tooltip recordatorio del carrito
function showCartReminder() {
    fetch('/carrito/datos')
        .then(response => response.json())
        .then(data => {
            // Solo mostrar si hay items en el carrito
            if (data.items && data.items.length > 0) {
                const cartBtn = document.querySelector('.cart-btn-header');
                if (cartBtn) {
                    // Crear tooltip si no existe
                    let tooltip = document.getElementById('cartTooltipReminder');
                    if (!tooltip) {
                        tooltip = document.createElement('div');
                        tooltip.id = 'cartTooltipReminder';
                        tooltip.className = 'cart-tooltip-reminder';
                        tooltip.innerHTML = `
                            <i class="bi bi-check-circle-fill"></i>
                            <span>¡Tienes productos listos para alquilar!</span>
                        `;
                        // Agregar al botón del carrito para posicionamiento relativo
                        cartBtn.appendChild(tooltip);
                    }
                    
                    // Agregar animación de vibración al botón
                    cartBtn.classList.add('cart-shake-reminder');
                    
                    // Mostrar tooltip
                    tooltip.classList.add('show');
                    
                    // Ocultar después de 5 segundos
                    setTimeout(() => {
                        tooltip.classList.remove('show');
                        cartBtn.classList.remove('cart-shake-reminder');
                    }, 5000);
                }
            }
        })
        .catch(error => console.error('Error al verificar carrito:', error));
}

// Actualizar badge al cargar la p�gina
document.addEventListener('DOMContentLoaded', function() {
    updateCartBadge();
    // Actualizar cada 5 segundos
    setInterval(updateCartBadge, 5000);
    
    // Mostrar recordatorio del carrito cada 30 segundos
    setInterval(showCartReminder, 30000);
});

