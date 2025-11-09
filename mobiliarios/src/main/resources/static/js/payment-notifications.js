/**
 * Sistema de Notificaciones de Pagos
 * Gestiona las notificaciones de pagos pendientes y los procesos de pago con SweetAlert2
 */

// Variable global para almacenar notificaciones
let paymentNotifications = [];

/**
 * Inicializa el sistema de notificaciones de pagos
 */
function initPaymentNotifications() {
    // Cargar conteo inicial de notificaciones
    updateNotificationBadge();
    
    // Actualizar cada 30 segundos
    setInterval(updateNotificationBadge, 30000);
    
    // Mostrar tooltip recordatorio cada 30 segundos si hay pagos pendientes
    setInterval(showPaymentReminder, 30000);
    
    // Cargar notificaciones completas al hacer clic en la campanita
    const bellBtn = document.getElementById('paymentBellBtn');
    if (bellBtn) {
        bellBtn.addEventListener('click', showPaymentNotifications);
    }
}

/**
 * Actualiza el badge con el número de notificaciones pendientes
 */
async function updateNotificationBadge() {
    try {
        const response = await fetch('/pagos/notificaciones-count');
        const data = await response.json();
        
        const badge = document.getElementById('paymentNotificationBadge');
        if (badge) {
            if (data.success && data.count > 0) {
                badge.textContent = data.count;
                badge.style.display = 'flex';
                
                // Animar campanita si hay nuevas notificaciones
                const bellBtn = document.getElementById('paymentBellBtn');
                if (bellBtn) {
                    bellBtn.classList.add('shake-bell');
                    setTimeout(() => bellBtn.classList.remove('shake-bell'), 500);
                }
            } else {
                badge.style.display = 'none';
            }
        }
    } catch (error) {
        console.error('Error al actualizar badge de notificaciones:', error);
    }
}

/**
 * Muestra tooltip recordatorio de pagos pendientes
 */
async function showPaymentReminder() {
    try {
        const response = await fetch('/pagos/notificaciones-count');
        const data = await response.json();
        
        // Solo mostrar tooltip si hay pagos pendientes
        if (data.success && data.count > 0) {
            const bellBtn = document.getElementById('paymentBellBtn');
            if (bellBtn) {
                // Crear tooltip si no existe
                let tooltip = document.getElementById('paymentTooltipReminder');
                if (!tooltip) {
                    tooltip = document.createElement('div');
                    tooltip.id = 'paymentTooltipReminder';
                    tooltip.className = 'payment-tooltip-reminder';
                    tooltip.innerHTML = `
                        <i class="bi bi-exclamation-circle-fill"></i>
                        <span>¡Tienes pagos pendientes!</span>
                    `;
                    // Agregar al botón de notificaciones para posicionamiento relativo
                    bellBtn.appendChild(tooltip);
                }
                
                // Agregar animación de vibración al botón
                bellBtn.classList.add('payment-shake-reminder');
                
                // Mostrar tooltip
                tooltip.classList.add('show');
                
                // Ocultar después de 5 segundos
                setTimeout(() => {
                    tooltip.classList.remove('show');
                    bellBtn.classList.remove('payment-shake-reminder');
                }, 5000);
            }
        }
    } catch (error) {
        console.error('Error al mostrar recordatorio de pagos:', error);
    }
}

/**
 * Muestra las notificaciones de pagos pendientes
 */
async function showPaymentNotifications() {
    try {
        const response = await fetch('/pagos/notificaciones');
        const data = await response.json();
        
        if (!data.success) {
            Swal.fire({
                icon: 'error',
                title: 'Error',
                text: data.message || 'No se pudieron cargar las notificaciones',
                confirmButtonColor: '#8cbc00'
            });
            return;
        }
        
        paymentNotifications = data.notificaciones || [];
        
        if (paymentNotifications.length === 0) {
            Swal.fire({
                icon: 'info',
                title: 'Sin pagos pendientes',
                text: 'No tienes pagos pendientes en este momento',
                confirmButtonColor: '#8cbc00'
            });
            return;
        }
        
        // Mostrar lista de notificaciones
        showNotificationsList(paymentNotifications);
        
    } catch (error) {
        console.error('Error al cargar notificaciones:', error);
        Swal.fire({
            icon: 'error',
            title: 'Error',
            text: 'No se pudieron cargar las notificaciones',
            confirmButtonColor: '#8cbc00'
        });
    }
}

/**
 * Muestra la lista de notificaciones en un SweetAlert
 */
function showNotificationsList(notifications) {
    const isDarkMode = document.body.classList.contains('dark-mode');
    
    let html = `
        <div style="
            text-align: left; 
            max-height: 450px; 
            overflow-y: auto;
            padding: 5px;
            margin-top: 10px;
        " class="payment-notifications-list">
    `;
    
    notifications.forEach((notif, index) => {
        const tipoPagoLabel = notif.tipoPago === 'PARCIAL' ? 'Pago Inicial (50%)' : 'Pago Final (50%)';
        const icon = notif.tipoPago === 'PARCIAL' ? '💳' : '✅';
        const borderColor = notif.tipoPago === 'PARCIAL' ? '#037bc0' : '#8cbc00';
        const gradientColor = notif.tipoPago === 'PARCIAL' 
            ? 'linear-gradient(135deg, rgba(3, 123, 192, 0.1) 0%, rgba(3, 123, 192, 0.05) 100%)'
            : 'linear-gradient(135deg, rgba(140, 188, 0, 0.1) 0%, rgba(140, 188, 0, 0.05) 100%)';
        
        html += `
            <div class="payment-notification-card" style="
                background: ${isDarkMode ? 'rgba(255, 255, 255, 0.05)' : gradientColor};
                border: 2px solid ${isDarkMode ? 'rgba(255, 255, 255, 0.1)' : '#e9ecef'};
                border-left: 5px solid ${borderColor};
                padding: 20px;
                margin-bottom: 15px;
                border-radius: 16px;
                cursor: pointer;
                transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
                position: relative;
                overflow: hidden;
                animation: slideInUp 0.5s ease ${index * 0.1}s both;
            " 
            onmouseover="this.style.transform='translateY(-5px) scale(1.02)'; this.style.boxShadow='0 12px 24px rgba(0,0,0,0.2)'; this.style.borderColor='${borderColor}';"
            onmouseout="this.style.transform='translateY(0) scale(1)'; this.style.boxShadow='0 4px 12px rgba(0,0,0,0.1)'; this.style.borderColor='${isDarkMode ? 'rgba(255, 255, 255, 0.1)' : '#e9ecef'}';"
            onclick="showPaymentDetail(${index})">
                
                <!-- Efecto de brillo en hover -->
                <div style="
                    position: absolute;
                    top: 0;
                    left: -100%;
                    width: 100%;
                    height: 100%;
                    background: linear-gradient(90deg, transparent, rgba(255,255,255,0.1), transparent);
                    transition: left 0.5s;
                    pointer-events: none;
                " class="shine-effect"></div>
                
                <!-- Header con icono y tipo -->
                <div style="
                    display: flex; 
                    align-items: center; 
                    gap: 12px; 
                    margin-bottom: 12px;
                ">
                    <div style="
                        width: 50px;
                        height: 50px;
                        background: ${borderColor};
                        border-radius: 12px;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        font-size: 24px;
                        box-shadow: 0 4px 12px ${borderColor}40;
                        animation: iconPulse 2s ease-in-out infinite;
                    ">${icon}</div>
                    <div style="flex: 1;">
                        <strong style="
                            color: ${isDarkMode ? '#fff' : '#2c3e50'}; 
                            font-size: 17px;
                            display: block;
                            margin-bottom: 4px;
                        ">${tipoPagoLabel}</strong>
                        <span style="
                            color: ${isDarkMode ? 'rgba(255,255,255,0.6)' : '#7f8c8d'}; 
                            font-size: 13px;
                        ">ID: ${notif.alquilerId}</span>
                    </div>
                </div>
                
                <!-- Descripción -->
                <div style="
                    color: ${isDarkMode ? 'rgba(255,255,255,0.7)' : '#7f8c8d'}; 
                    font-size: 14px; 
                    margin-bottom: 15px;
                    line-height: 1.5;
                ">${notif.descripcion}</div>
                
                <!-- Footer con monto y botón -->
                <div style="
                    display: flex; 
                    justify-content: space-between; 
                    align-items: center;
                    padding-top: 15px;
                    border-top: 1px solid ${isDarkMode ? 'rgba(255,255,255,0.1)' : '#e9ecef'};
                ">
                    <div>
                        <div style="
                            font-size: 12px;
                            color: ${isDarkMode ? 'rgba(255,255,255,0.5)' : '#95a5a6'};
                            margin-bottom: 4px;
                        ">Monto a pagar</div>
                        <span style="
                            font-size: 28px; 
                            font-weight: 800; 
                            background: linear-gradient(135deg, #8cbc00, #037bc0, #8cbc00);
                            background-size: 200% 200%;
                            -webkit-background-clip: text;
                            -webkit-text-fill-color: transparent;
                            background-clip: text;
                            animation: gradientShift 3s ease infinite;
                        ">$${formatearNumero(notif.monto, 2)}</span>
                    </div>
                    <button style="
                        background: linear-gradient(135deg, #8cbc00, #037bc0);
                        color: white;
                        padding: 10px 20px;
                        border: none;
                        border-radius: 25px;
                        font-size: 13px;
                        font-weight: 700;
                        cursor: pointer;
                        box-shadow: 0 4px 15px rgba(140, 188, 0, 0.3);
                        transition: all 0.3s ease;
                        text-transform: uppercase;
                        letter-spacing: 0.5px;
                    " onmouseover="this.style.transform='scale(1.1)'; this.style.boxShadow='0 6px 20px rgba(140, 188, 0, 0.5)';"
                       onmouseout="this.style.transform='scale(1)'; this.style.boxShadow='0 4px 15px rgba(140, 188, 0, 0.3)';">
                        Pagar ahora →
                    </button>
                </div>
            </div>
        `;
    });
    
    html += '</div>';
    
    Swal.fire({
        title: `<div style="
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 12px;
            font-size: 26px;
            font-weight: 800;
            color: ${isDarkMode ? '#fff' : '#2c3e50'};
        ">
            <span style="
                font-size: 32px;
                animation: bellRing 1s ease infinite;
            ">🔔</span>
            Pagos Pendientes
        </div>`,
        html: html,
        width: '650px',
        padding: '30px',
        background: isDarkMode ? '#1a1a2e' : '#ffffff',
        showConfirmButton: false,
        showCloseButton: true,
        customClass: {
            container: 'payment-notifications-modal',
            popup: 'payment-notifications-popup',
            closeButton: 'payment-close-button'
        },
        showClass: {
            popup: 'swal2-show-custom'
        },
        hideClass: {
            popup: 'swal2-hide-custom'
        }
    });
}

/**
 * Muestra el detalle de un pago pendiente
 */
function showPaymentDetail(index) {
    const notif = paymentNotifications[index];
    
    if (!notif) return;
    
    const isDarkMode = document.body.classList.contains('dark-mode');
    const tipoPagoLabel = notif.tipoPago === 'PARCIAL' ? 'Pago Inicial (50%)' : 'Pago Final (50%)';
    const icon = notif.tipoPago === 'PARCIAL' ? '💳' : '✅';
    const borderColor = notif.tipoPago === 'PARCIAL' ? '#037bc0' : '#8cbc00';
    
    let itemsHtml = '';
    if (notif.items && notif.items.length > 0) {
        itemsHtml = `
            <div style="
                margin: 15px 0;
                animation: fadeInUp 0.5s ease 0.2s both;
            ">
                <div style="
                    background: ${isDarkMode ? 'rgba(255,255,255,0.05)' : 'rgba(140, 188, 0, 0.05)'};
                    border-left: 3px solid ${borderColor};
                    padding: 12px;
                    border-radius: 10px;
                ">
                    <div style="
                        color: ${isDarkMode ? 'rgba(255,255,255,0.6)' : '#7f8c8d'};
                        font-size: 11px;
                        text-transform: uppercase;
                        letter-spacing: 0.5px;
                        font-weight: 600;
                        margin-bottom: 8px;
                    ">📦 Productos</div>
        `;
        notif.items.forEach((item, idx) => {
            itemsHtml += `
                <div style="
                    color: ${isDarkMode ? 'rgba(255,255,255,0.8)' : '#495057'};
                    font-size: 13px;
                    padding: 5px 0;
                    display: flex;
                    align-items: center;
                    gap: 8px;
                    animation: slideInLeft 0.4s ease ${0.3 + (idx * 0.1)}s both;
                ">
                    <span style="
                        width: 5px;
                        height: 5px;
                        background: ${borderColor};
                        border-radius: 50%;
                        display: inline-block;
                    "></span>
                    ${item}
                </div>
            `;
        });
        itemsHtml += '</div></div>';
    }
    
    Swal.fire({
        title: `<div style="
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 10px;
            font-size: 20px;
            font-weight: 800;
            color: ${isDarkMode ? '#fff' : '#2c3e50'};
        ">
            <span style="
                font-size: 28px;
                animation: iconBounce 1s ease infinite;
            ">${icon}</span>
            ${tipoPagoLabel}
        </div>`,
        html: `
            <div style="text-align: left;">
                <!-- Tarjeta de monto compacta -->
                <div style="
                    background: linear-gradient(135deg, #8cbc00 0%, #037bc0 100%);
                    color: white;
                    padding: 20px;
                    border-radius: 16px;
                    margin-bottom: 20px;
                    text-align: center;
                    box-shadow: 0 8px 24px rgba(140, 188, 0, 0.3);
                    position: relative;
                    overflow: hidden;
                    animation: cardSlideIn 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
                ">
                    <!-- Efecto de brillo animado -->
                    <div style="
                        position: absolute;
                        top: -50%;
                        left: -50%;
                        width: 200%;
                        height: 200%;
                        background: radial-gradient(circle, rgba(255,255,255,0.15) 0%, transparent 70%);
                        animation: rotateGlow 4s linear infinite;
                        pointer-events: none;
                    "></div>
                    
                    <div style="position: relative; z-index: 1;">
                        <div style="
                            font-size: 11px;
                            opacity: 0.85;
                            margin-bottom: 6px;
                            text-transform: uppercase;
                            letter-spacing: 1px;
                            font-weight: 600;
                        ">Monto a pagar</div>
                        <div style="
                            font-size: 36px;
                            font-weight: 900;
                            margin: 5px 0;
                            text-shadow: 0 2px 10px rgba(0,0,0,0.2);
                            animation: amountPulse 2s ease-in-out infinite;
                        ">$${formatearNumero(notif.monto, 2)}</div>
                        <div style="
                            font-size: 11px;
                            opacity: 0.8;
                            margin-top: 6px;
                        ">${notif.porcentaje}% del total ($${formatearNumero(notif.total, 2)})</div>
                    </div>
                </div>
                
                ${itemsHtml}
                
                <!-- Información del alquiler compacta -->
                <div style="
                    background: ${isDarkMode ? 'rgba(255,255,255,0.05)' : '#f8f9fa'};
                    border: 1px solid ${isDarkMode ? 'rgba(255,255,255,0.1)' : '#e9ecef'};
                    padding: 15px;
                    border-radius: 12px;
                    margin-top: ${itemsHtml ? '20px' : '0'};
                    animation: fadeInUp 0.5s ease 0.3s both;
                ">
                    <div style="
                        font-size: 13px;
                        color: ${isDarkMode ? 'rgba(255,255,255,0.8)' : '#495057'};
                        line-height: 1.8;
                    ">
                        <div style="display: flex; justify-content: space-between; padding: 6px 0; border-bottom: 1px solid ${isDarkMode ? 'rgba(255,255,255,0.08)' : '#e9ecef'};">
                            <strong style="font-size: 12px;">ID:</strong>
                            <span style="font-family: monospace; color: ${borderColor}; font-size: 12px;">${notif.alquilerId}</span>
                        </div>
                        <div style="display: flex; justify-content: space-between; padding: 6px 0; ${notif.fechaInicio ? 'border-bottom: 1px solid ' + (isDarkMode ? 'rgba(255,255,255,0.08)' : '#e9ecef') + ';' : ''}">
                            <strong style="font-size: 12px;">Estado:</strong>
                            <span style="
                                background: ${borderColor};
                                color: white;
                                padding: 2px 10px;
                                border-radius: 10px;
                                font-size: 11px;
                                font-weight: 600;
                            ">${notif.estado}</span>
                        </div>
                        ${notif.fechaInicio ? `
                        <div style="display: flex; justify-content: space-between; padding: 6px 0; ${notif.fechaFin ? 'border-bottom: 1px solid ' + (isDarkMode ? 'rgba(255,255,255,0.08)' : '#e9ecef') + ';' : ''}">
                            <strong style="font-size: 12px;">Inicio:</strong>
                            <span style="font-size: 12px;">📅 ${new Date(notif.fechaInicio).toLocaleDateString()}</span>
                        </div>` : ''}
                        ${notif.fechaFin ? `
                        <div style="display: flex; justify-content: space-between; padding: 6px 0;">
                            <strong style="font-size: 12px;">Fin:</strong>
                            <span style="font-size: 12px;">📅 ${new Date(notif.fechaFin).toLocaleDateString()}</span>
                        </div>` : ''}
                    </div>
                </div>
            </div>
        `,
        width: '550px',
        padding: '25px',
        background: isDarkMode ? '#1a1a2e' : '#ffffff',
        showCancelButton: true,
        confirmButtonText: '<span style="display: flex; align-items: center; gap: 8px;"><span>💳</span> Proceder al pago</span>',
        cancelButtonText: '← Volver',
        confirmButtonColor: '#8cbc00',
        cancelButtonColor: '#6c757d',
        customClass: {
            confirmButton: 'payment-detail-confirm-btn',
            cancelButton: 'payment-detail-cancel-btn',
            popup: 'payment-detail-popup'
        },
        showClass: {
            popup: 'swal2-show-custom'
        },
        hideClass: {
            popup: 'swal2-hide-custom'
        }
    }).then((result) => {
        if (result.isConfirmed) {
            showPaymentForm(notif);
        }
    });
}

/**
 * Muestra el formulario de pago (simulado/sandbox)
 */
async function showPaymentForm(notif) {
    const tipoPago = notif.tipoPago === 'PARCIAL' ? 'parcial' : 'final';
    const isDarkMode = document.body.classList.contains('dark-mode');
    
    // Cargar tarjetas guardadas
    let tarjetasGuardadas = [];
    try {
        const response = await fetch('/tarjetas/listar');
        const data = await response.json();
        if (data.success && data.tarjetas) {
            tarjetasGuardadas = data.tarjetas;
        }
    } catch (error) {
        console.error('Error al cargar tarjetas guardadas:', error);
    }
    
    // Generar HTML de tarjetas guardadas
    let tarjetasHTML = '';
    
    // Si hay tarjetas guardadas, mostrarlas de forma prominente
    if (tarjetasGuardadas.length > 0) {
        tarjetasHTML = `
            <div style="margin-bottom: 20px; animation: slideInUp 0.5s ease 0.05s both;">
                <div style="
                    display: flex;
                    align-items: center;
                    gap: 8px;
                    margin-bottom: 15px;
                ">
                    <span style="
                        width: 28px;
                        height: 28px;
                        background: linear-gradient(135deg, #8cbc00, #037bc0);
                        border-radius: 8px;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        font-size: 14px;
                    ">💾</span>
                    <label style="
                        font-weight: 700;
                        color: ${isDarkMode ? 'rgba(255,255,255,0.95)' : '#2c3e50'};
                        font-size: 15px;
                    ">Tus tarjetas guardadas</label>
                </div>
                <div id="tarjetasGuardadasContainer" style="
                    display: grid;
                    gap: 10px;
                    max-height: 250px;
                    overflow-y: auto;
                    padding: 5px;
                    margin-bottom: 15px;
                ">
        `;
        
        tarjetasGuardadas.forEach((tarjeta, index) => {
            const isVencida = tarjeta.estaVencida;
            tarjetasHTML += `
                <div onclick="seleccionarTarjetaGuardada('${tarjeta.id}', ${isVencida})" style="
                    padding: 14px 18px;
                    border: 2px solid ${isDarkMode ? 'rgba(255,255,255,0.1)' : '#e8e8e8'};
                    background: ${isDarkMode ? 'rgba(255,255,255,0.05)' : '#fff'};
                    border-radius: 12px;
                    cursor: ${isVencida ? 'not-allowed' : 'pointer'};
                    opacity: ${isVencida ? '0.5' : '1'};
                    transition: all 0.3s;
                    display: flex;
                    align-items: center;
                    gap: 14px;
                " class="tarjeta-guardada-item" data-tarjeta-id="${tarjeta.id}"
                   onmouseover="${!isVencida ? 'this.style.borderColor=\'#8cbc00\'; this.style.transform=\'translateX(5px)\'; this.style.boxShadow=\'0 4px 12px rgba(140, 188, 0, 0.2)\';' : ''}"
                   onmouseout="${!isVencida ? 'this.style.borderColor=\'' + (isDarkMode ? 'rgba(255,255,255,0.1)' : '#e8e8e8') + '\'; this.style.transform=\'translateX(0)\'; this.style.boxShadow=\'none\';' : ''}">
                    <div style="
                        width: 45px;
                        height: 45px;
                        background: linear-gradient(135deg, #667eea, #764ba2);
                        border-radius: 10px;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        font-size: 20px;
                        box-shadow: 0 4px 10px rgba(102, 126, 234, 0.3);
                    ">💳</div>
                    <div style="flex: 1;">
                        <div style="
                            font-weight: 700;
                            color: ${isDarkMode ? '#fff' : '#2c3e50'};
                            font-size: 15px;
                            font-family: 'Courier New', monospace;
                            margin-bottom: 4px;
                        ">${tarjeta.tarjetaEnmascarada}</div>
                        <div style="
                            font-size: 12px;
                            color: ${isDarkMode ? 'rgba(255,255,255,0.6)' : '#7f8c8d'};
                        ">
                            ${tarjeta.tipoTarjeta} • Exp: ${tarjeta.fechaExpiracion}
                            ${tarjeta.alias ? ' • ' + tarjeta.alias : ''}
                            ${isVencida ? ' • <span style="color: #e74c3c; font-weight: 600;">VENCIDA</span>' : ''}
                        </div>
                    </div>
                    ${tarjeta.esPredeterminada ? `
                        <div style="
                            background: linear-gradient(90deg, #8cbc00, #037bc0);
                            color: white;
                            padding: 5px 10px;
                            border-radius: 8px;
                            font-size: 11px;
                            font-weight: 700;
                            box-shadow: 0 2px 8px rgba(140, 188, 0, 0.3);
                        ">★ Predeterminada</div>
                    ` : ''}
                </div>
            `;
        });
        
        tarjetasHTML += `
                </div>
                
                <!-- Toggle Switch para usar otra tarjeta -->
                <div style="
                    background: ${isDarkMode ? 'rgba(255,255,255,0.03)' : '#f8f9fa'};
                    border: 2px solid ${isDarkMode ? 'rgba(255,255,255,0.1)' : '#e8e8e8'};
                    border-radius: 12px;
                    padding: 14px 16px;
                    margin-top: 15px;
                ">
                    <label style="
                        display: flex;
                        align-items: center;
                        gap: 12px;
                        cursor: pointer;
                        user-select: none;
                    " onclick="toggleUsarTarjetaNueva()">
                        <!-- Toggle Switch Container -->
                        <div style="
                            position: relative;
                            width: 50px;
                            height: 26px;
                            background: ${isDarkMode ? 'rgba(255,255,255,0.1)' : '#ddd'};
                            border-radius: 13px;
                            transition: background 0.3s ease;
                            flex-shrink: 0;
                        " id="toggleSwitchContainer">
                            <!-- Hidden Checkbox -->
                            <input 
                                type="checkbox" 
                                id="usarTarjetaNuevaCheckbox"
                                style="
                                    opacity: 0;
                                    width: 0;
                                    height: 0;
                                    position: absolute;
                                "
                            >
                            <!-- Toggle Ball -->
                            <div style="
                                position: absolute;
                                top: 3px;
                                left: 3px;
                                width: 20px;
                                height: 20px;
                                background: white;
                                border-radius: 50%;
                                transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                                box-shadow: 0 2px 4px rgba(0,0,0,0.2);
                            " id="toggleBall"></div>
                        </div>
                        <div style="flex: 1;">
                            <div style="
                                font-weight: 600;
                                color: ${isDarkMode ? 'rgba(255,255,255,0.9)' : '#2c3e50'};
                                font-size: 14px;
                                margin-bottom: 2px;
                            ">Usar otra tarjeta</div>
                            <div style="
                                font-size: 12px;
                                color: ${isDarkMode ? 'rgba(255,255,255,0.6)' : '#7f8c8d'};
                            ">Ingresa los datos de una tarjeta diferente</div>
                        </div>
                    </label>
                </div>
            </div>
        `;
    } else {
        // Si no hay tarjetas guardadas, mostrar mensaje
        tarjetasHTML = `
            <div style="
                padding: 25px 20px;
                text-align: center;
                color: ${isDarkMode ? 'rgba(255,255,255,0.6)' : '#7f8c8d'};
                border: 2px dashed ${isDarkMode ? 'rgba(255,255,255,0.1)' : '#e8e8e8'};
                border-radius: 12px;
                background: ${isDarkMode ? 'rgba(255,255,255,0.02)' : '#f8f9fa'};
                margin-bottom: 20px;
                animation: slideInUp 0.5s ease 0.05s both;
            ">
                <div style="font-size: 36px; margin-bottom: 10px;">💳</div>
                <div style="font-size: 14px; font-weight: 600; margin-bottom: 5px; color: ${isDarkMode ? 'rgba(255,255,255,0.8)' : '#495057'};">
                    No tienes tarjetas guardadas
                </div>
                <div style="font-size: 12px;">
                    Ingresa los datos de tu tarjeta para continuar
                </div>
            </div>
        `;
    }
    
    Swal.fire({
        title: `<div style="
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 10px;
            font-size: 22px;
            font-weight: 800;
            color: ${isDarkMode ? '#fff' : '#2c3e50'};
        ">
            <span style="
                font-size: 32px;
                animation: cardFlip 2s ease-in-out infinite;
            ">💳</span>
            Formulario de Pago
        </div>`,
        html: `
            <div style="text-align: left;">
                <!-- Tarjeta de crédito visual -->
                <div style="
                    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                    border-radius: 14px;
                    padding: 18px 20px;
                    margin-bottom: 20px;
                    box-shadow: 0 10px 40px rgba(102, 126, 234, 0.4);
                    position: relative;
                    overflow: hidden;
                    animation: cardFloat 3s ease-in-out infinite;
                ">
                    <!-- Efecto de brillo -->
                    <div style="
                        position: absolute;
                        top: -50%;
                        right: -50%;
                        width: 200%;
                        height: 200%;
                        background: radial-gradient(circle, rgba(255,255,255,0.15) 0%, transparent 70%);
                        animation: rotateGlow 6s linear infinite;
                        pointer-events: none;
                    "></div>
                    
                    <!-- Chip de tarjeta -->
                    <div style="
                        position: absolute;
                        top: 20px;
                        left: 25px;
                        width: 40px;
                        height: 30px;
                        background: linear-gradient(135deg, #f4d03f 0%, #d4af37 100%);
                        border-radius: 6px;
                        box-shadow: inset 0 2px 4px rgba(0,0,0,0.2);
                    "></div>
                    
                    <div style="position: relative; z-index: 1; margin-top: 35px;">
                        <div style="
                            font-size: 10px;
                            color: rgba(255,255,255,0.8);
                            text-transform: uppercase;
                            letter-spacing: 1px;
                            margin-bottom: 8px;
                        ">Total a pagar</div>
                        <div style="
                            font-size: 30px;
                            font-weight: 900;
                            color: white;
                            text-shadow: 0 4px 10px rgba(0,0,0,0.3);
                            animation: amountGlow 2s ease-in-out infinite;
                        ">$${formatearNumero(notif.monto, 2)}</div>
                        
                        <!-- Logos de tarjetas -->
                        <div style="
                            position: absolute;
                            bottom: 15px;
                            right: 20px;
                            display: flex;
                            gap: 6px;
                        ">
                            <div style="
                                width: 32px;
                                height: 20px;
                                background: white;
                                border-radius: 4px;
                                display: flex;
                                align-items: center;
                                justify-content: center;
                                font-size: 10px;
                                font-weight: 800;
                                color: #1a1f71;
                            ">VISA</div>
                            <div style="
                                width: 40px;
                                height: 25px;
                                background: white;
                                border-radius: 4px;
                                display: flex;
                                align-items: center;
                                justify-content: center;
                                font-size: 10px;
                                font-weight: 800;
                                color: #eb001b;
                            ">MC</div>
                        </div>
                    </div>
                </div>
                
                ${tarjetasHTML}
                
                <form id="paymentForm" style="text-align: left; ${tarjetasGuardadas.length > 0 ? 'display: none;' : ''}" data-usar-tarjeta-guardada="${tarjetasGuardadas.length > 0 ? 'true' : 'false'}">
                    <!-- Número de tarjeta -->
                    <div class="payment-input-group" style="margin-bottom: 18px; animation: slideInUp 0.5s ease 0.1s both;">
                        <label style="
                            display: flex;
                            align-items: center;
                            gap: 8px;
                            font-weight: 600;
                            color: ${isDarkMode ? 'rgba(255,255,255,0.9)' : '#2c3e50'};
                            margin-bottom: 8px;
                            font-size: 13px;
                        ">
                            <span style="
                                width: 24px;
                                height: 24px;
                                background: linear-gradient(135deg, #8cbc00, #037bc0);
                                border-radius: 6px;
                                display: flex;
                                align-items: center;
                                justify-content: center;
                                font-size: 12px;
                            ">💳</span>
                            Número de tarjeta
                        </label>
                        <input 
                            type="text" 
                            id="cardNumber" 
                            placeholder="1234 5678 9012 3456"
                            maxlength="19"
                            required
                            class="payment-input"
                            style="
                                width: 100%;
                                padding: 14px 16px;
                                border: 2px solid ${isDarkMode ? 'rgba(255,255,255,0.1)' : '#e8e8e8'};
                                background: ${isDarkMode ? 'rgba(255,255,255,0.05)' : '#fff'};
                                color: ${isDarkMode ? '#fff' : '#2c3e50'};
                                border-radius: 12px;
                                font-size: 16px;
                                font-family: 'Courier New', monospace;
                                letter-spacing: 1px;
                                transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                            "
                            onkeyup="formatCardNumber(this)"
                            onfocus="this.style.borderColor='#8cbc00'; this.style.boxShadow='0 0 0 4px rgba(140, 188, 0, 0.1)'; this.style.transform='translateY(-2px)';"
                            onblur="this.style.borderColor='${isDarkMode ? 'rgba(255,255,255,0.1)' : '#e8e8e8'}'; this.style.boxShadow='none'; this.style.transform='translateY(0)';"
                        >
                    </div>
                    
                    <!-- Nombre del titular -->
                    <div class="payment-input-group" style="margin-bottom: 18px; animation: slideInUp 0.5s ease 0.2s both;">
                        <label style="
                            display: flex;
                            align-items: center;
                            gap: 8px;
                            font-weight: 600;
                            color: ${isDarkMode ? 'rgba(255,255,255,0.9)' : '#2c3e50'};
                            margin-bottom: 8px;
                            font-size: 13px;
                        ">
                            <span style="
                                width: 24px;
                                height: 24px;
                                background: linear-gradient(135deg, #8cbc00, #037bc0);
                                border-radius: 6px;
                                display: flex;
                                align-items: center;
                                justify-content: center;
                                font-size: 12px;
                            ">👤</span>
                            Nombre del titular
                        </label>
                        <input 
                            type="text" 
                            id="cardName" 
                            placeholder="JUAN PÉREZ"
                            required
                            class="payment-input"
                            style="
                                width: 100%;
                                padding: 14px 16px;
                                border: 2px solid ${isDarkMode ? 'rgba(255,255,255,0.1)' : '#e8e8e8'};
                                background: ${isDarkMode ? 'rgba(255,255,255,0.05)' : '#fff'};
                                color: ${isDarkMode ? '#fff' : '#2c3e50'};
                                border-radius: 12px;
                                font-size: 16px;
                                text-transform: uppercase;
                                transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                            "
                            onfocus="this.style.borderColor='#8cbc00'; this.style.boxShadow='0 0 0 4px rgba(140, 188, 0, 0.1)'; this.style.transform='translateY(-2px)';"
                            onblur="this.style.borderColor='${isDarkMode ? 'rgba(255,255,255,0.1)' : '#e8e8e8'}'; this.style.boxShadow='none'; this.style.transform='translateY(0)';"
                        >
                    </div>
                    
                    <!-- Fecha y CVV -->
                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 16px;">
                        <div class="payment-input-group" style="animation: slideInUp 0.5s ease 0.3s both;">
                            <label style="
                                display: flex;
                                align-items: center;
                                gap: 8px;
                                font-weight: 600;
                                color: ${isDarkMode ? 'rgba(255,255,255,0.9)' : '#2c3e50'};
                                margin-bottom: 8px;
                                font-size: 13px;
                            ">
                                <span style="
                                    width: 24px;
                                    height: 24px;
                                    background: linear-gradient(135deg, #8cbc00, #037bc0);
                                    border-radius: 6px;
                                    display: flex;
                                    align-items: center;
                                    justify-content: center;
                                    font-size: 12px;
                                ">📅</span>
                                Expiración
                            </label>
                            <input 
                                type="text" 
                                id="cardExpiry" 
                                placeholder="MM/AA"
                                maxlength="5"
                                required
                                class="payment-input"
                                style="
                                    width: 100%;
                                    padding: 14px 16px;
                                    border: 2px solid ${isDarkMode ? 'rgba(255,255,255,0.1)' : '#e8e8e8'};
                                    background: ${isDarkMode ? 'rgba(255,255,255,0.05)' : '#fff'};
                                    color: ${isDarkMode ? '#fff' : '#2c3e50'};
                                    border-radius: 12px;
                                    font-size: 16px;
                                    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                                "
                                onkeyup="formatExpiry(this)"
                                onfocus="this.style.borderColor='#8cbc00'; this.style.boxShadow='0 0 0 4px rgba(140, 188, 0, 0.1)'; this.style.transform='translateY(-2px)';"
                                onblur="this.style.borderColor='${isDarkMode ? 'rgba(255,255,255,0.1)' : '#e8e8e8'}'; this.style.boxShadow='none'; this.style.transform='translateY(0)';"
                            >
                        </div>
                        <div class="payment-input-group" style="animation: slideInUp 0.5s ease 0.4s both;">
                            <label style="
                                display: flex;
                                align-items: center;
                                gap: 8px;
                                font-weight: 600;
                                color: ${isDarkMode ? 'rgba(255,255,255,0.9)' : '#2c3e50'};
                                margin-bottom: 8px;
                                font-size: 13px;
                            ">
                                <span style="
                                    width: 24px;
                                    height: 24px;
                                    background: linear-gradient(135deg, #8cbc00, #037bc0);
                                    border-radius: 6px;
                                    display: flex;
                                    align-items: center;
                                    justify-content: center;
                                    font-size: 12px;
                                ">🔒</span>
                                CVV
                            </label>
                            <input 
                                type="text" 
                                id="cardCVV" 
                                placeholder="123"
                                maxlength="4"
                                required
                                class="payment-input"
                                style="
                                    width: 100%;
                                    padding: 14px 16px;
                                    border: 2px solid ${isDarkMode ? 'rgba(255,255,255,0.1)' : '#e8e8e8'};
                                    background: ${isDarkMode ? 'rgba(255,255,255,0.05)' : '#fff'};
                                    color: ${isDarkMode ? '#fff' : '#2c3e50'};
                                    border-radius: 12px;
                                    font-size: 16px;
                                    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                                "
                                onkeypress="return event.charCode >= 48 && event.charCode <= 57"
                                onfocus="this.style.borderColor='#8cbc00'; this.style.boxShadow='0 0 0 4px rgba(140, 188, 0, 0.1)'; this.style.transform='translateY(-2px)';"
                                onblur="this.style.borderColor='${isDarkMode ? 'rgba(255,255,255,0.1)' : '#e8e8e8'}'; this.style.boxShadow='none'; this.style.transform='translateY(0)';"
                            >
                        </div>
                    </div>
                    
                    <!-- Mensaje de seguridad -->
                    <div style="
                        background: ${isDarkMode ? 'rgba(33, 150, 243, 0.1)' : '#e3f2fd'};
                        border: 1px solid ${isDarkMode ? 'rgba(33, 150, 243, 0.3)' : '#2196f3'};
                        padding: 10px 12px;
                        border-radius: 10px;
                        font-size: 12px;
                        color: ${isDarkMode ? '#64b5f6' : '#1565c0'};
                        margin-top: 20px;
                        display: flex;
                        align-items: center;
                        gap: 10px;
                        animation: slideInUp 0.5s ease 0.5s both;
                    ">
                        <span style="
                            font-size: 18px;
                            animation: lockPulse 2s ease-in-out infinite;
                        ">🔒</span>
                        <div>
                            <strong>Pago seguro:</strong> Esta es una simulación. No se realizará ningún cargo real.
                        </div>
                    </div>
                </form>
            </div>
        `,
        width: '520px',
        padding: '25px',
        background: isDarkMode ? '#1a1a2e' : '#ffffff',
        showCancelButton: true,
        confirmButtonText: '<span style="display: flex; align-items: center; gap: 8px;"><span>✓</span> Confirmar pago</span>',
        cancelButtonText: 'Cancelar',
        confirmButtonColor: '#8cbc00',
        cancelButtonColor: '#6c757d',
        customClass: {
            confirmButton: 'payment-form-confirm-btn',
            cancelButton: 'payment-form-cancel-btn',
            popup: 'payment-form-popup'
        },
        showClass: {
            popup: 'swal2-show-custom'
        },
        hideClass: {
            popup: 'swal2-hide-custom'
        },
        preConfirm: () => {
            const form = document.getElementById('paymentForm');
            const usarTarjetaGuardada = form.dataset.usarTarjetaGuardada === 'true';
            
            if (usarTarjetaGuardada) {
                const tarjetaId = form.dataset.tarjetaId;
                if (!tarjetaId) {
                    Swal.showValidationMessage('Por favor selecciona una tarjeta');
                    return false;
                }
                return { usarTarjetaGuardada: true, tarjetaId };
            } else {
                const cardNumber = document.getElementById('cardNumber').value.replace(/\s/g, '');
                const cardName = document.getElementById('cardName').value;
                const cardExpiry = document.getElementById('cardExpiry').value;
                const cardCVV = document.getElementById('cardCVV').value;
                
                // Validaciones básicas
                if (!cardNumber || cardNumber.length < 16) {
                    Swal.showValidationMessage('Número de tarjeta inválido');
                    return false;
                }
                if (!cardName) {
                    Swal.showValidationMessage('Nombre del titular es requerido');
                    return false;
                }
                if (!cardExpiry || cardExpiry.length < 5) {
                    Swal.showValidationMessage('Fecha de expiración inválida');
                    return false;
                }
                if (!cardCVV || cardCVV.length < 3) {
                    Swal.showValidationMessage('CVV inválido');
                    return false;
                }
                
                return { usarTarjetaGuardada: false, cardNumber, cardName, cardExpiry, cardCVV };
            }
        },
        allowOutsideClick: () => !Swal.isLoading()
    }).then((result) => {
        if (result.isConfirmed) {
            processPayment(notif.alquilerId, tipoPago, result.value);
        }
    });
}

/**
 * Procesa el pago (envía al servidor)
 */
async function processPayment(alquilerId, tipoPago, cardData) {
    // Mostrar loading
    Swal.fire({
        title: 'Procesando pago...',
        html: 'Por favor espera mientras procesamos tu pago',
        allowOutsideClick: false,
        allowEscapeKey: false,
        didOpen: () => {
            Swal.showLoading();
        }
    });
    
    try {
        let endpoint, formData;
        
        if (cardData.usarTarjetaGuardada) {
            // Usar tarjeta guardada
            endpoint = tipoPago === 'parcial' 
                ? '/pagos/procesar-pago-parcial-tarjeta-guardada' 
                : '/pagos/procesar-pago-final-tarjeta-guardada';
            
            formData = new URLSearchParams();
            formData.append('alquilerId', alquilerId);
            formData.append('tarjetaId', cardData.tarjetaId);
        } else {
            // Usar tarjeta nueva
            endpoint = tipoPago === 'parcial' 
                ? '/pagos/procesar-pago-parcial' 
                : '/pagos/procesar-pago-final';
            
            formData = new URLSearchParams();
            formData.append('alquilerId', alquilerId);
            formData.append('numeroTarjeta', cardData.cardNumber);
            formData.append('nombreTitular', cardData.cardName);
            formData.append('fechaExpiracion', cardData.cardExpiry);
            formData.append('cvv', cardData.cardCVV);
        }
        
        const response = await fetch(endpoint, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: formData
        });
        
        const data = await response.json();
        
        if (data.success) {
            const isDarkMode = document.body.classList.contains('dark-mode');
            
            // Mostrar éxito con diseño mejorado
            Swal.fire({
                title: `<div style="
                    font-size: 26px;
                    font-weight: 800;
                    color: ${isDarkMode ? '#fff' : '#2c3e50'};
                    margin-top: 10px;
                ">¡Pago recibido exitosamente!</div>`,
                html: `
                    <div style="text-align: center;">
                        <!-- Icono de éxito animado -->
                        <div style="
                            width: 120px;
                            height: 120px;
                            margin: 20px auto;
                            background: linear-gradient(135deg, #8cbc00, #037bc0);
                            border-radius: 50%;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            position: relative;
                            box-shadow: 0 10px 40px rgba(140, 188, 0, 0.4);
                            animation: successPulse 2s ease-in-out infinite;
                        ">
                            <!-- Anillos de éxito -->
                            <div style="
                                position: absolute;
                                width: 100%;
                                height: 100%;
                                border: 3px solid #8cbc00;
                                border-radius: 50%;
                                animation: successRing 1.5s ease-out infinite;
                            "></div>
                            <div style="
                                position: absolute;
                                width: 100%;
                                height: 100%;
                                border: 3px solid #037bc0;
                                border-radius: 50%;
                                animation: successRing 1.5s ease-out 0.3s infinite;
                            "></div>
                            
                            <!-- Check mark -->
                            <div style="
                                font-size: 70px;
                                color: white;
                                animation: checkBounce 0.6s cubic-bezier(0.68, -0.55, 0.265, 1.55);
                            ">✓</div>
                        </div>
                        
                        <!-- Mensaje -->
                        <p style="
                            font-size: 16px;
                            color: ${isDarkMode ? 'rgba(255,255,255,0.8)' : '#2c3e50'};
                            margin: 20px 0;
                            animation: fadeInUp 0.5s ease 0.3s both;
                        ">
                            Tu pago ha sido procesado correctamente
                        </p>
                        
                        <!-- Tarjeta de monto -->
                        <div style="
                            background: linear-gradient(135deg, #8cbc00 0%, #037bc0 100%);
                            padding: 25px;
                            border-radius: 16px;
                            margin: 25px 0;
                            box-shadow: 0 10px 30px rgba(140, 188, 0, 0.3);
                            position: relative;
                            overflow: hidden;
                            animation: fadeInUp 0.5s ease 0.5s both;
                        ">
                            <!-- Efecto de brillo -->
                            <div style="
                                position: absolute;
                                top: -50%;
                                left: -50%;
                                width: 200%;
                                height: 200%;
                                background: radial-gradient(circle, rgba(255,255,255,0.2) 0%, transparent 70%);
                                animation: rotateGlow 4s linear infinite;
                                pointer-events: none;
                            "></div>
                            
                            <div style="position: relative; z-index: 1;">
                                <div style="
                                    font-size: 12px;
                                    color: rgba(255,255,255,0.85);
                                    text-transform: uppercase;
                                    letter-spacing: 1px;
                                    margin-bottom: 8px;
                                ">Monto pagado</div>
                                <div style="
                                    font-size: 42px;
                                    font-weight: 900;
                                    color: white;
                                    text-shadow: 0 4px 15px rgba(0,0,0,0.3);
                                    animation: amountPop 0.6s cubic-bezier(0.68, -0.55, 0.265, 1.55) 0.7s both;
                                ">$${formatearNumero(data.montoPagado, 2)}</div>
                            </div>
                        </div>
                        
                        <!-- Mensaje de correo -->
                        <div style="
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            gap: 10px;
                            background: ${isDarkMode ? 'rgba(33, 150, 243, 0.1)' : '#e3f2fd'};
                            border: 2px solid ${isDarkMode ? 'rgba(33, 150, 243, 0.3)' : '#2196f3'};
                            padding: 14px 20px;
                            border-radius: 12px;
                            animation: fadeInUp 0.5s ease 0.7s both;
                        ">
                            <span style="
                                font-size: 24px;
                                animation: emailFloat 2s ease-in-out infinite;
                            ">📧</span>
                            <p style="
                                font-size: 13px;
                                color: ${isDarkMode ? '#64b5f6' : '#1565c0'};
                                margin: 0;
                            ">
                                <strong>Recibirás un comprobante por correo electrónico</strong>
                            </p>
                        </div>
                        
                        <!-- Confetti particles -->
                        <div style="
                            position: absolute;
                            top: 0;
                            left: 0;
                            width: 100%;
                            height: 100%;
                            pointer-events: none;
                            overflow: hidden;
                        ">
                            <div style="
                                position: absolute;
                                width: 10px;
                                height: 10px;
                                background: #8cbc00;
                                top: 20%;
                                left: 20%;
                                border-radius: 50%;
                                animation: confettiFall 3s ease-out infinite;
                            "></div>
                            <div style="
                                position: absolute;
                                width: 8px;
                                height: 8px;
                                background: #037bc0;
                                top: 10%;
                                left: 80%;
                                border-radius: 50%;
                                animation: confettiFall 3s ease-out 0.5s infinite;
                            "></div>
                            <div style="
                                position: absolute;
                                width: 12px;
                                height: 12px;
                                background: #f59e0b;
                                top: 15%;
                                left: 50%;
                                border-radius: 50%;
                                animation: confettiFall 3s ease-out 1s infinite;
                            "></div>
                        </div>
                    </div>
                `,
                width: '550px',
                padding: '30px',
                background: isDarkMode ? '#1a1a2e' : '#ffffff',
                showConfirmButton: true,
                confirmButtonText: '<span style="display: flex; align-items: center; gap: 8px;"><span>✓</span> Aceptar</span>',
                confirmButtonColor: '#8cbc00',
                allowOutsideClick: false,
                customClass: {
                    confirmButton: 'payment-success-btn',
                    popup: 'payment-success-popup'
                },
                showClass: {
                    popup: 'swal2-show-custom'
                },
                hideClass: {
                    popup: 'swal2-hide-custom'
                }
            }).then(() => {
                // Actualizar badge
                updateNotificationBadge();
            });
        } else {
            Swal.fire({
                icon: 'error',
                title: 'Error en el pago',
                text: data.message || 'No se pudo procesar el pago',
                confirmButtonColor: '#dc3545'
            });
        }
        
    } catch (error) {
        console.error('Error al procesar pago:', error);
        Swal.fire({
            icon: 'error',
            title: 'Error',
            text: 'Ocurrió un error al procesar el pago. Por favor intenta nuevamente.',
            confirmButtonColor: '#dc3545'
        });
    }
}

/**
 * Formatea el número de tarjeta con espacios
 */
function formatCardNumber(input) {
    let value = input.value.replace(/\s/g, '').replace(/\D/g, '');
    let formattedValue = value.match(/.{1,4}/g)?.join(' ') || value;
    input.value = formattedValue;
}

/**
 * Formatea la fecha de expiración MM/AA
 */
function formatExpiry(input) {
    let value = input.value.replace(/\D/g, '');
    if (value.length >= 2) {
        value = value.substring(0, 2) + '/' + value.substring(2, 4);
    }
    input.value = value;
}

/**
 * Variable global para almacenar la tarjeta seleccionada
 */
let tarjetaGuardadaSeleccionada = null;

/**
 * Selecciona una tarjeta guardada para usar en el pago
 */
function seleccionarTarjetaGuardada(tarjetaId, isVencida) {
    if (isVencida) {
        Swal.fire({
            icon: 'warning',
            title: 'Tarjeta vencida',
            text: 'Esta tarjeta ha expirado. Por favor usa otra tarjeta o ingresa una nueva.',
            confirmButtonColor: '#8cbc00'
        });
        return;
    }
    
    // Desmarcar el checkbox de "Usar otra tarjeta"
    const checkbox = document.getElementById('usarTarjetaNuevaCheckbox');
    if (checkbox) {
        checkbox.checked = false;
    }
    
    // Marcar tarjeta como seleccionada visualmente con efecto brillante
    document.querySelectorAll('.tarjeta-guardada-item').forEach(item => {
        if (item.dataset.tarjetaId === tarjetaId) {
            // Tarjeta seleccionada - Efecto brillante
            item.style.borderColor = '#8cbc00';
            item.style.borderWidth = '3px';
            item.style.background = 'linear-gradient(135deg, rgba(140, 188, 0, 0.15), rgba(3, 123, 192, 0.15))';
            item.style.boxShadow = '0 0 20px rgba(140, 188, 0, 0.6), 0 0 40px rgba(140, 188, 0, 0.3), inset 0 0 20px rgba(140, 188, 0, 0.1)';
            item.style.transform = 'scale(1.02)';
            
            // Agregar un indicador de selección
            const existingCheck = item.querySelector('.selection-check');
            if (!existingCheck) {
                const checkMark = document.createElement('div');
                checkMark.className = 'selection-check';
                checkMark.innerHTML = '✓';
                checkMark.style.cssText = `
                    position: absolute;
                    top: -8px;
                    right: -8px;
                    width: 28px;
                    height: 28px;
                    background: linear-gradient(135deg, #8cbc00, #037bc0);
                    color: white;
                    border-radius: 50%;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    font-size: 16px;
                    font-weight: bold;
                    box-shadow: 0 4px 12px rgba(140, 188, 0, 0.5);
                    animation: checkPop 0.3s cubic-bezier(0.68, -0.55, 0.265, 1.55);
                `;
                item.style.position = 'relative';
                item.appendChild(checkMark);
            }
        } else {
            // Tarjeta no seleccionada - Estado normal
            const isDarkMode = document.body.classList.contains('dark-mode');
            item.style.borderColor = isDarkMode ? 'rgba(255,255,255,0.1)' : '#e8e8e8';
            item.style.borderWidth = '2px';
            item.style.background = isDarkMode ? 'rgba(255,255,255,0.05)' : '#fff';
            item.style.boxShadow = 'none';
            item.style.transform = 'scale(1)';
            
            // Remover indicador de selección si existe
            const existingCheck = item.querySelector('.selection-check');
            if (existingCheck) {
                existingCheck.remove();
            }
        }
    });
    
    // Guardar ID de tarjeta seleccionada
    tarjetaGuardadaSeleccionada = tarjetaId;
    
    // Marcar formulario para usar tarjeta guardada
    const form = document.getElementById('paymentForm');
    if (form) {
        form.dataset.usarTarjetaGuardada = 'true';
        form.dataset.tarjetaId = tarjetaId;
        
        // Deshabilitar campos del formulario
        const inputs = form.querySelectorAll('input.payment-input');
        inputs.forEach(input => {
            input.disabled = true;
            input.style.opacity = '0.5';
            input.required = false;
            input.value = '';
        });
    }
}

/**
 * Toggle para usar una tarjeta nueva en lugar de guardada
 */
function toggleUsarTarjetaNueva() {
    const checkbox = document.getElementById('usarTarjetaNuevaCheckbox');
    const toggleBall = document.getElementById('toggleBall');
    const toggleContainer = document.getElementById('toggleSwitchContainer');
    
    // Alternar el estado del checkbox
    if (checkbox) {
        checkbox.checked = !checkbox.checked;
    }
    
    const isChecked = checkbox ? checkbox.checked : false;
    const isDarkMode = document.body.classList.contains('dark-mode');
    const form = document.getElementById('paymentForm');
    
    // Animar el toggle switch
    if (toggleBall && toggleContainer) {
        if (isChecked) {
            // Mover bolita a la derecha y cambiar color del fondo
            toggleBall.style.left = '27px';
            toggleBall.style.background = 'white';
            toggleContainer.style.background = 'linear-gradient(135deg, #8cbc00, #037bc0)';
            toggleContainer.style.boxShadow = '0 0 10px rgba(140, 188, 0, 0.4)';
        } else {
            // Mover bolita a la izquierda y restaurar color
            toggleBall.style.left = '3px';
            toggleBall.style.background = 'white';
            toggleContainer.style.background = isDarkMode ? 'rgba(255,255,255,0.1)' : '#ddd';
            toggleContainer.style.boxShadow = 'none';
        }
    }
    
    if (isChecked) {
        // Usuario quiere usar una tarjeta nueva
        tarjetaGuardadaSeleccionada = null;
        
        // Desmarcar y deshabilitar todas las tarjetas guardadas
        document.querySelectorAll('.tarjeta-guardada-item').forEach(item => {
            item.style.borderColor = isDarkMode ? 'rgba(255,255,255,0.1)' : '#e8e8e8';
            item.style.background = isDarkMode ? 'rgba(255,255,255,0.05)' : '#fff';
            item.style.boxShadow = 'none';
            item.style.opacity = '0.4';
            item.style.cursor = 'not-allowed';
            item.style.pointerEvents = 'none';
        });
        
        // Mostrar y habilitar formulario con animación
        if (form) {
            form.style.display = 'block';
            form.style.animation = 'slideInUp 0.4s ease both';
            form.dataset.usarTarjetaGuardada = 'false';
            form.dataset.tarjetaId = '';
            
            const inputs = form.querySelectorAll('input.payment-input');
            inputs.forEach(input => {
                input.disabled = false;
                input.style.opacity = '1';
                input.required = true;
            });
        }
    } else {
        // Usuario desmarcó el checkbox, ocultar formulario y re-habilitar tarjetas
        
        // Re-habilitar todas las tarjetas guardadas
        document.querySelectorAll('.tarjeta-guardada-item').forEach(item => {
            item.style.opacity = '1';
            item.style.cursor = 'pointer';
            item.style.pointerEvents = 'auto';
        });
        
        if (form) {
            form.style.animation = 'slideOutDown 0.3s ease both';
            
            setTimeout(() => {
                form.style.display = 'none';
                form.dataset.usarTarjetaGuardada = 'true';
                form.dataset.tarjetaId = '';
                
                const inputs = form.querySelectorAll('input.payment-input');
                inputs.forEach(input => {
                    input.disabled = false;
                    input.style.opacity = '1';
                    input.required = false;
                    input.value = '';
                });
            }, 300);
        }
    }
}

// Inicializar cuando el DOM esté listo
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initPaymentNotifications);
} else {
    initPaymentNotifications();
}
