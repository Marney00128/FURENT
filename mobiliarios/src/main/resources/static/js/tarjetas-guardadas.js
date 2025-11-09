// Gestión de Tarjetas Guardadas

// Abrir modal de tarjetas con animación
function abrirModalTarjetas(event) {
    if (event) event.preventDefault();
    
    const modal = document.getElementById('modalTarjetas');
    modal.style.display = 'flex';
    document.body.style.overflow = 'hidden';
    
    // Agregar efecto de sonido visual
    agregarEfectoApertura(modal);
    
    // Cargar tarjetas
    cargarTarjetas();
    
    // Llenar años de expiración
    llenarAniosExpiracion();
}

// Efecto visual de apertura
function agregarEfectoApertura(modal) {
    const content = modal.querySelector('.modal-tarjetas-content');
    if (content) {
        content.style.animation = 'none';
        setTimeout(() => {
            content.style.animation = 'modalEntrance 0.7s cubic-bezier(0.34, 1.56, 0.64, 1)';
        }, 10);
    }
}

// Cerrar modal de tarjetas con animación
function cerrarModalTarjetas() {
    const modal = document.getElementById('modalTarjetas');
    const content = modal.querySelector('.modal-tarjetas-content');
    
    // Animación de salida
    if (content) {
        content.style.animation = 'modalExit 0.4s cubic-bezier(0.4, 0, 0.2, 1) forwards';
        setTimeout(() => {
            modal.style.display = 'none';
            document.body.style.overflow = 'auto';
            ocultarFormularioTarjeta();
        }, 400);
    } else {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
        ocultarFormularioTarjeta();
    }
}

// Agregar animación de salida al CSS dinámicamente
if (!document.getElementById('modal-exit-animation')) {
    const style = document.createElement('style');
    style.id = 'modal-exit-animation';
    style.textContent = `
        @keyframes modalExit {
            0% {
                opacity: 1;
                transform: scale(1) translateY(0) rotateX(0deg);
            }
            100% {
                opacity: 0;
                transform: scale(0.7) translateY(-50px) rotateX(-20deg);
                filter: blur(10px);
            }
        }
    `;
    document.head.appendChild(style);
}

// Cerrar modal al hacer clic fuera
document.addEventListener('click', function(event) {
    const modal = document.getElementById('modalTarjetas');
    if (event.target === modal) {
        cerrarModalTarjetas();
    }
});

// Llenar años de expiración (próximos 15 años)
function llenarAniosExpiracion() {
    const selectAnio = document.getElementById('anioExpiracion');
    if (!selectAnio) return;
    
    const anioActual = new Date().getFullYear();
    selectAnio.innerHTML = '<option value="">Año</option>';
    
    for (let i = 0; i < 15; i++) {
        const anio = anioActual + i;
        const option = document.createElement('option');
        option.value = anio;
        option.textContent = anio;
        selectAnio.appendChild(option);
    }
}

// Validaciones y formateo de campos del formulario
document.addEventListener('DOMContentLoaded', function() {
    // Número de tarjeta: solo números y formateo automático
    const inputNumeroTarjeta = document.getElementById('numeroTarjeta');
    if (inputNumeroTarjeta) {
        inputNumeroTarjeta.addEventListener('input', function(e) {
            // Remover todo lo que no sea número
            let valor = e.target.value.replace(/\D/g, '');
            // Limitar a 16 dígitos
            valor = valor.substring(0, 16);
            // Formatear en grupos de 4
            let valorFormateado = valor.match(/.{1,4}/g)?.join(' ') || valor;
            e.target.value = valorFormateado;
        });
        
        // Prevenir pegado de texto no numérico
        inputNumeroTarjeta.addEventListener('paste', function(e) {
            e.preventDefault();
            const pastedText = (e.clipboardData || window.clipboardData).getData('text');
            const numeros = pastedText.replace(/\D/g, '').substring(0, 16);
            const formateado = numeros.match(/.{1,4}/g)?.join(' ') || numeros;
            e.target.value = formateado;
        });
    }
    
    // Nombre del titular: convertir automáticamente a mayúsculas
    const inputNombreTitular = document.getElementById('nombreTitular');
    if (inputNombreTitular) {
        inputNombreTitular.addEventListener('input', function(e) {
            // Convertir a mayúsculas automáticamente
            const cursorPos = e.target.selectionStart;
            e.target.value = e.target.value.toUpperCase();
            e.target.setSelectionRange(cursorPos, cursorPos);
        });
    }
    
    // CVV: solo números y máximo 3 dígitos
    const inputCvv = document.getElementById('cvv');
    if (inputCvv) {
        inputCvv.addEventListener('input', function(e) {
            // Remover todo lo que no sea número
            let valor = e.target.value.replace(/\D/g, '');
            // Limitar a 3 dígitos
            valor = valor.substring(0, 3);
            e.target.value = valor;
        });
        
        // Prevenir pegado de texto no numérico
        inputCvv.addEventListener('paste', function(e) {
            e.preventDefault();
            const pastedText = (e.clipboardData || window.clipboardData).getData('text');
            const numeros = pastedText.replace(/\D/g, '').substring(0, 3);
            e.target.value = numeros;
        });
    }
});

// Cargar tarjetas del usuario
async function cargarTarjetas() {
    const listaTarjetas = document.getElementById('listaTarjetas');
    const btnAgregarTarjeta = document.querySelector('.btn-agregar-tarjeta');
    
    try {
        const response = await fetch('/tarjetas/listar');
        const data = await response.json();
        
        if (data.success && data.tarjetas && data.tarjetas.length > 0) {
            listaTarjetas.innerHTML = '';
            
            // Agregar tarjetas con animación escalonada
            data.tarjetas.forEach((tarjeta, index) => {
                setTimeout(() => {
                    const tarjetaCard = crearTarjetaCard(tarjeta);
                    tarjetaCard.style.opacity = '0';
                    tarjetaCard.style.animation = `slideInCard 0.6s ease ${index * 0.1}s forwards`;
                    listaTarjetas.appendChild(tarjetaCard);
                }, index * 50);
            });
            
            // Deshabilitar botón si alcanzó el límite
            if (data.tarjetas.length >= 5) {
                btnAgregarTarjeta.disabled = true;
                btnAgregarTarjeta.innerHTML = '<i class="bi bi-exclamation-circle"></i> Límite alcanzado (5/5)';
                btnAgregarTarjeta.style.opacity = '0.6';
                btnAgregarTarjeta.style.cursor = 'not-allowed';
            } else {
                btnAgregarTarjeta.disabled = false;
                btnAgregarTarjeta.innerHTML = `<i class="bi bi-plus-circle"></i> Agregar Nueva Tarjeta (${data.tarjetas.length}/5)`;
                btnAgregarTarjeta.style.opacity = '1';
                btnAgregarTarjeta.style.cursor = 'pointer';
            }
        } else {
            listaTarjetas.innerHTML = `
                <div class="no-tarjetas">
                    <i class="bi bi-credit-card"></i>
                    <p>No tienes tarjetas guardadas</p>
                    <small>Agrega una tarjeta para realizar pagos más rápido</small>
                </div>
            `;
            
            // Habilitar botón si no hay tarjetas
            btnAgregarTarjeta.disabled = false;
            btnAgregarTarjeta.innerHTML = '<i class="bi bi-plus-circle"></i> Agregar Nueva Tarjeta (0/5)';
            btnAgregarTarjeta.style.opacity = '1';
            btnAgregarTarjeta.style.cursor = 'pointer';
        }
    } catch (error) {
        console.error('Error al cargar tarjetas:', error);
        listaTarjetas.innerHTML = `
            <div class="error-tarjetas">
                <i class="bi bi-exclamation-triangle"></i>
                <p>Error al cargar tarjetas</p>
            </div>
        `;
    }
}

// Crear card de tarjeta
function crearTarjetaCard(tarjeta) {
    const card = document.createElement('div');
    card.className = 'tarjeta-card';
    if (tarjeta.esPredeterminada) {
        card.classList.add('predeterminada');
    }
    if (tarjeta.estaVencida) {
        card.classList.add('vencida');
    }
    
    // Determinar icono según tipo de tarjeta
    let iconoTarjeta = 'bi-credit-card';
    if (tarjeta.tipoTarjeta === 'VISA') iconoTarjeta = 'bi-credit-card-2-front';
    else if (tarjeta.tipoTarjeta === 'MASTERCARD') iconoTarjeta = 'bi-credit-card-2-back';
    
    card.innerHTML = `
        <div class="tarjeta-info">
            <div class="tarjeta-icono">
                <i class="bi ${iconoTarjeta}"></i>
            </div>
            <div class="tarjeta-detalles">
                <div class="tarjeta-numero">${tarjeta.tarjetaEnmascarada}</div>
                <div class="tarjeta-titular">${tarjeta.nombreTitular}</div>
                <div class="tarjeta-meta">
                    <span class="tarjeta-tipo">${tarjeta.tipoTarjeta}</span>
                    <span class="tarjeta-expiracion">Exp: ${tarjeta.fechaExpiracion}</span>
                    ${tarjeta.alias ? `<span class="tarjeta-alias">${tarjeta.alias}</span>` : ''}
                </div>
                ${tarjeta.esPredeterminada ? '<span class="badge-predeterminada"><i class="bi bi-star-fill"></i> Predeterminada</span>' : ''}
                ${tarjeta.estaVencida ? '<span class="badge-vencida"><i class="bi bi-exclamation-circle"></i> Vencida</span>' : ''}
            </div>
        </div>
        <div class="tarjeta-acciones">
            ${!tarjeta.esPredeterminada ? `
                <button class="btn-accion" onclick="establecerPredeterminada('${tarjeta.id}')" title="Establecer como predeterminada">
                    <i class="bi bi-star"></i>
                </button>
            ` : ''}
            <button class="btn-accion btn-eliminar" onclick="eliminarTarjeta('${tarjeta.id}')" title="Eliminar tarjeta">
                <i class="bi bi-trash"></i>
            </button>
        </div>
    `;
    
    return card;
}

// Mostrar formulario para agregar tarjeta
function mostrarFormularioTarjeta() {
    const btnAgregarTarjeta = document.querySelector('.btn-agregar-tarjeta');
    
    // Verificar si el botón está deshabilitado
    if (btnAgregarTarjeta.disabled) {
        Swal.fire({
            icon: 'warning',
            title: 'Límite alcanzado',
            html: `
                <p>Has alcanzado el límite máximo de <strong>5 tarjetas guardadas</strong>.</p>
                <p>Elimina una tarjeta existente para agregar una nueva.</p>
            `,
            confirmButtonColor: '#8cbc00',
            showClass: {
                popup: 'animate__animated animate__shakeX'
            }
        });
        return;
    }
    
    const formulario = document.getElementById('formularioTarjeta');
    formulario.style.display = 'block';
    
    // Scroll al formulario
    formulario.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

// Ocultar formulario
function ocultarFormularioTarjeta() {
    const formulario = document.getElementById('formularioTarjeta');
    formulario.style.display = 'none';
    
    // Limpiar formulario
    document.getElementById('formNuevaTarjeta').reset();
}

// Variable para prevenir doble clic
let guardandoTarjeta = false;

// Guardar nueva tarjeta
async function guardarTarjeta(event) {
    event.preventDefault();
    
    // Prevenir doble clic
    if (guardandoTarjeta) {
        console.log('Ya se está guardando una tarjeta...');
        return;
    }
    
    console.log('Guardando tarjeta...');
    guardandoTarjeta = true;
    
    const numeroTarjeta = document.getElementById('numeroTarjeta').value.replace(/\s/g, '');
    const nombreTitular = document.getElementById('nombreTitular').value.toUpperCase();
    const mesExpiracion = document.getElementById('mesExpiracion').value;
    const anioExpiracion = document.getElementById('anioExpiracion').value;
    const cvv = document.getElementById('cvv').value;
    const alias = document.getElementById('alias').value;
    
    console.log('Datos de tarjeta:', { numeroTarjeta: numeroTarjeta.substring(0, 4) + '****', nombreTitular, mesExpiracion, anioExpiracion });
    
    // Validaciones con SweetAlert2
    if (numeroTarjeta.length < 13 || numeroTarjeta.length > 19) {
        guardandoTarjeta = false;
        Swal.fire({
            icon: 'error',
            title: 'Número de tarjeta inválido',
            text: 'El número de tarjeta debe tener entre 13 y 19 dígitos',
            confirmButtonColor: '#8cbc00',
            showClass: {
                popup: 'animate__animated animate__shakeX'
            }
        });
        return;
    }
    
    if (cvv.length !== 3) {
        guardandoTarjeta = false;
        Swal.fire({
            icon: 'error',
            title: 'CVV inválido',
            text: 'El CVV debe tener exactamente 3 dígitos',
            confirmButtonColor: '#037bc0',
            showClass: {
                popup: 'animate__animated animate__shakeX'
            }
        });
        return;
    }
    
    // Mostrar loading
    const form = event.target;
    const btnGuardar = form.querySelector('button[type="submit"]');
    const textoOriginal = btnGuardar.innerHTML;
    btnGuardar.disabled = true;
    btnGuardar.innerHTML = '<i class="bi bi-hourglass-split"></i> Guardando...';
    
    try {
        const formData = new URLSearchParams();
        formData.append('numeroTarjeta', numeroTarjeta);
        formData.append('nombreTitular', nombreTitular);
        formData.append('mesExpiracion', mesExpiracion);
        formData.append('anioExpiracion', anioExpiracion);
        formData.append('cvv', cvv);
        if (alias) formData.append('alias', alias);
        
        const response = await fetch('/tarjetas/guardar', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: formData
        });
        
        const data = await response.json();
        
        if (data.success) {
            // Animación de éxito espectacular
            Swal.fire({
                icon: 'success',
                title: '¡Tarjeta Guardada!',
                html: `
                    <div style="text-align: center;">
                        <div style="
                            font-size: 80px;
                            animation: bounceIn 0.8s cubic-bezier(0.68, -0.55, 0.265, 1.55);
                        ">💳</div>
                        <p style="
                            font-size: 18px;
                            color: #666;
                            margin-top: 20px;
                            animation: fadeIn 0.5s ease 0.3s both;
                        ">Tu tarjeta ha sido guardada de forma segura</p>
                        <div style="
                            display: inline-flex;
                            align-items: center;
                            gap: 8px;
                            padding: 10px 20px;
                            background: linear-gradient(135deg, #8cbc00, #6a9600);
                            color: white;
                            border-radius: 20px;
                            font-size: 14px;
                            font-weight: 600;
                            margin-top: 15px;
                            animation: pulse 2s ease-in-out infinite;
                        ">
                            <i class="bi bi-shield-check"></i>
                            Encriptación AES-256
                        </div>
                    </div>
                `,
                confirmButtonText: '¡Genial!',
                confirmButtonColor: '#8cbc00',
                showClass: {
                    popup: 'animate__animated animate__zoomIn'
                },
                hideClass: {
                    popup: 'animate__animated animate__zoomOut'
                }
            }).then(() => {
                ocultarFormularioTarjeta();
                cargarTarjetas();
            });
        } else {
            Swal.fire({
                icon: 'error',
                title: 'Error al guardar',
                text: data.message,
                confirmButtonColor: '#e74c3c'
            });
        }
    } catch (error) {
        console.error('Error al guardar tarjeta:', error);
        Swal.fire({
            icon: 'error',
            title: 'Error',
            text: 'No se pudo guardar la tarjeta. Intenta nuevamente.',
            confirmButtonColor: '#e74c3c'
        });
    } finally {
        guardandoTarjeta = false;
        btnGuardar.disabled = false;
        btnGuardar.innerHTML = textoOriginal;
    }
}

// Establecer tarjeta como predeterminada
async function establecerPredeterminada(tarjetaId) {
    const result = await Swal.fire({
        title: '¿Establecer como predeterminada?',
        text: 'Esta tarjeta se usará por defecto en tus pagos',
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: 'Sí, establecer',
        cancelButtonText: 'Cancelar',
        confirmButtonColor: '#8cbc00',
        cancelButtonColor: '#6c757d',
        showClass: {
            popup: 'animate__animated animate__fadeInDown'
        }
    });
    
    if (!result.isConfirmed) {
        return;
    }
    
    try {
        const formData = new URLSearchParams();
        formData.append('tarjetaId', tarjetaId);
        
        const response = await fetch('/tarjetas/establecer-predeterminada', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: formData
        });
        
        const data = await response.json();
        
        if (data.success) {
            Swal.fire({
                icon: 'success',
                title: '¡Listo!',
                text: 'Tarjeta establecida como predeterminada',
                timer: 2000,
                showConfirmButton: false,
                showClass: {
                    popup: 'animate__animated animate__bounceIn'
                }
            });
            cargarTarjetas();
        } else {
            Swal.fire({
                icon: 'error',
                title: 'Error',
                text: data.message,
                confirmButtonColor: '#e74c3c'
            });
        }
    } catch (error) {
        console.error('Error:', error);
        Swal.fire({
            icon: 'error',
            title: 'Error',
            text: 'No se pudo establecer la tarjeta predeterminada',
            confirmButtonColor: '#e74c3c'
        });
    }
}

// Limpiar tarjetas duplicadas
async function limpiarTarjetasDuplicadas() {
    const result = await Swal.fire({
        title: '¿Limpiar tarjetas duplicadas?',
        html: `
            <div style="text-align: center;">
                <div style="
                    font-size: 60px;
                    color: #8cbc00;
                    margin: 20px 0;
                ">🧹</div>
                <p style="font-size: 16px; color: #666;">
                    Se eliminarán las tarjetas duplicadas.<br>
                    Se mantendrá solo una tarjeta por cada número.
                </p>
            </div>
        `,
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: 'Sí, limpiar',
        cancelButtonText: 'Cancelar',
        confirmButtonColor: '#8cbc00',
        cancelButtonColor: '#6c757d',
        showClass: {
            popup: 'animate__animated animate__fadeInDown'
        }
    });
    
    if (!result.isConfirmed) {
        return;
    }
    
    try {
        const response = await fetch('/tarjetas/limpiar-duplicadas', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            }
        });
        
        const data = await response.json();
        
        if (data.success) {
            Swal.fire({
                icon: 'success',
                title: '¡Limpieza completada!',
                text: data.message,
                timer: 3000,
                showConfirmButton: true,
                confirmButtonColor: '#8cbc00',
                showClass: {
                    popup: 'animate__animated animate__bounceIn'
                }
            });
            cargarTarjetas();
        } else {
            Swal.fire({
                icon: 'error',
                title: 'Error',
                text: data.message,
                confirmButtonColor: '#e74c3c'
            });
        }
    } catch (error) {
        console.error('Error:', error);
        Swal.fire({
            icon: 'error',
            title: 'Error',
            text: 'No se pudo limpiar las tarjetas duplicadas',
            confirmButtonColor: '#e74c3c'
        });
    }
}

// Eliminar tarjeta
async function eliminarTarjeta(tarjetaId) {
    console.log('Intentando eliminar tarjeta:', tarjetaId);
    
    const result = await Swal.fire({
        title: '¿Eliminar tarjeta?',
        html: `
            <div style="text-align: center;">
                <div style="
                    font-size: 60px;
                    color: #e74c3c;
                    margin: 20px 0;
                    animation: pulse 1s ease-in-out infinite;
                ">⚠️</div>
                <p style="font-size: 16px; color: #666;">
                    Esta acción no se puede deshacer.<br>
                    La tarjeta será eliminada permanentemente.
                </p>
            </div>
        `,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: 'Sí, eliminar',
        cancelButtonText: 'Cancelar',
        confirmButtonColor: '#e74c3c',
        cancelButtonColor: '#6c757d',
        showClass: {
            popup: 'animate__animated animate__shakeX'
        }
    });
    
    if (!result.isConfirmed) {
        return;
    }
    
    try {
        const formData = new URLSearchParams();
        formData.append('tarjetaId', tarjetaId);
        
        const response = await fetch('/tarjetas/eliminar', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: formData
        });
        
        const data = await response.json();
        
        if (data.success) {
            Swal.fire({
                icon: 'success',
                title: 'Tarjeta eliminada',
                text: 'La tarjeta ha sido eliminada exitosamente',
                timer: 2000,
                showConfirmButton: false,
                showClass: {
                    popup: 'animate__animated animate__fadeOut'
                }
            });
            cargarTarjetas();
        } else {
            Swal.fire({
                icon: 'error',
                title: 'Error',
                text: data.message,
                confirmButtonColor: '#e74c3c'
            });
        }
    } catch (error) {
        console.error('Error:', error);
        Swal.fire({
            icon: 'error',
            title: 'Error',
            text: 'No se pudo eliminar la tarjeta',
            confirmButtonColor: '#e74c3c'
        });
    }
}
