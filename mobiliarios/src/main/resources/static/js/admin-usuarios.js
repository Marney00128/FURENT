// Script para la gestión de usuarios en el panel de administración
console.log('Script admin-usuarios.js cargado correctamente');

// Verificar que SweetAlert2 esté disponible
if (typeof Swal === 'undefined') {
    console.error('SweetAlert2 no está cargado');
}

function toggleSidebar() {
    document.querySelector('.sidebar').classList.toggle('collapsed');
    document.querySelector('.main-content').classList.toggle('expanded');
}

function filtrarUsuarios() {
    const input = document.getElementById('searchInput');
    const filter = input.value.toLowerCase();
    const table = document.getElementById('usuariosTable');
    const rows = table.getElementsByTagName('tr');

    for (let i = 1; i < rows.length; i++) {
        const cells = rows[i].getElementsByTagName('td');
        let found = false;

        for (let j = 0; j < cells.length - 1; j++) {
            const cell = cells[j];
            if (cell) {
                const textValue = cell.textContent || cell.innerText;
                if (textValue.toLowerCase().indexOf(filter) > -1) {
                    found = true;
                    break;
                }
            }
        }

        rows[i].style.display = found ? '' : 'none';
    }
}

function filtrarPorRol(rol) {
    const table = document.getElementById('usuariosTable');
    const rows = table.getElementsByTagName('tr');

    for (let i = 1; i < rows.length; i++) {
        const rolCell = rows[i].getElementsByTagName('td')[3];
        if (rolCell) {
            const rolText = rolCell.textContent.trim();
            if (rol === '' || rolText === rol) {
                rows[i].style.display = '';
            } else {
                rows[i].style.display = 'none';
            }
        }
    }
}

function cambiarRol(id, nombre, rolActual) {
    console.log('Cambiando rol:', { id, nombre, rolActual });
    
    if (typeof Swal === 'undefined') {
        alert('Error: SweetAlert2 no está disponible');
        return;
    }
    
    const nuevoRol = rolActual === 'ADMIN' ? 'USER' : 'ADMIN';
    
    Swal.fire({
        title: '¿Cambiar rol de usuario?',
        html: `¿Deseas cambiar el rol de <strong>${nombre}</strong> de <strong>${rolActual}</strong> a <strong>${nuevoRol}</strong>?`,
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#8cbc00',
        cancelButtonColor: '#d33',
        confirmButtonText: 'Sí, cambiar',
        cancelButtonText: 'Cancelar'
    }).then((result) => {
        if (result.isConfirmed) {
            const form = document.createElement('form');
            form.method = 'POST';
            form.action = `/admin/usuarios/cambiar-rol/${id}`;
            
            const input = document.createElement('input');
            input.type = 'hidden';
            input.name = 'nuevoRol';
            input.value = nuevoRol;
            
            form.appendChild(input);
            document.body.appendChild(form);
            form.submit();
        }
    });
}

function confirmarEliminar(id, nombre) {
    console.log('Eliminando usuario:', { id, nombre });
    
    if (typeof Swal === 'undefined') {
        alert('Error: SweetAlert2 no está disponible');
        return;
    }
    
    Swal.fire({
        title: '¿Eliminar usuario?',
        html: `¿Estás seguro de eliminar a <strong>${nombre}</strong>?<br><span style="color: #d33;">Esta acción no se puede deshacer.</span>`,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#6c757d',
        confirmButtonText: 'Sí, eliminar',
        cancelButtonText: 'Cancelar'
    }).then((result) => {
        if (result.isConfirmed) {
            const form = document.createElement('form');
            form.method = 'POST';
            form.action = `/admin/usuarios/eliminar/${id}`;
            document.body.appendChild(form);
            form.submit();
        }
    });
}

// Auto-hide alerts
document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM cargado, inicializando alertas');
    
    setTimeout(() => {
        document.querySelectorAll('.alert').forEach(alert => {
            alert.style.transition = 'opacity 0.3s';
            alert.style.opacity = '0';
            setTimeout(() => alert.remove(), 300);
        });
    }, 5000);
});

// Manejo de errores global
window.addEventListener('error', function(event) {
    console.error('Error en la página:', event.error);
});
