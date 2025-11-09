// Variables globales
let productos = [];
let productoEditando = null;

// Cargar productos al iniciar la página
document.addEventListener('DOMContentLoaded', function() {
    cargarProductos();
    
    // Configurar evento para abrir modal de agregar producto
    document.getElementById('btnAgregarProducto').addEventListener('click', function() {
        // Limpiar formulario
        document.getElementById('formAgregarProducto').reset();
        productoEditando = null;
        
        // Cambiar título del modal
        document.getElementById('modalAgregarProductoLabel').textContent = 'Agregar Producto';
        
        // Mostrar modal
        const modal = new bootstrap.Modal(document.getElementById('modalAgregarProducto'));
        modal.show();
    });
    
    // Configurar evento para guardar producto
    document.getElementById('btnGuardarProducto').addEventListener('click', guardarProducto);
});

// Función para cargar productos desde el servidor
function cargarProductos() {
    // Simulación de datos (reemplazar con llamada a API real)
    productos = [
        { id: 1, nombre: 'Sillas (Tiffany, Avant Garde)', precio: 1000000, categoria: 'Sillas', imagen: '/img/productos/silla.jpg' },
        { id: 2, nombre: 'Mesas (Redondas, Cuadradas)', precio: 1500000, categoria: 'Mesas', imagen: '/img/productos/mesa.jpg' }
    ];
    
    mostrarProductos();
}

// Función para mostrar productos en la tabla
function mostrarProductos() {
    const tbody = document.getElementById('productosTableBody');
    tbody.innerHTML = '';
    
    productos.forEach(producto => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${producto.id}</td>
            <td><img src="${producto.imagen}" alt="${producto.nombre}" width="50" height="50"></td>
            <td>${producto.nombre}</td>
            <td>$${formatearPrecio(producto.precio)}</td>
            <td>${producto.categoria}</td>
            <td>
                <button class="btn btn-primary btn-sm btn-editar" data-id="${producto.id}">EDITAR</button>
                <button class="btn btn-danger btn-sm btn-eliminar" data-id="${producto.id}">ELIMINAR</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
    
    // Agregar eventos a los botones de editar y eliminar
    document.querySelectorAll('.btn-editar').forEach(btn => {
        btn.addEventListener('click', function() {
            const id = parseInt(this.getAttribute('data-id'));
            editarProducto(id);
        });
    });
    
    document.querySelectorAll('.btn-eliminar').forEach(btn => {
        btn.addEventListener('click', function() {
            const id = parseInt(this.getAttribute('data-id'));
            eliminarProducto(id);
        });
    });
}

// Función para formatear precio
function formatearPrecio(precio) {
    return precio.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ".");
}

// Función para editar un producto
function editarProducto(id) {
    const producto = productos.find(p => p.id === id);
    if (!producto) return;
    
    // Llenar formulario con datos del producto
    document.getElementById('nombreProducto').value = producto.nombre;
    document.getElementById('descripcionProducto').value = producto.descripcion || '';
    document.getElementById('precioProducto').value = producto.precio;
    document.getElementById('categoriaProducto').value = producto.categoria;
    
    // Guardar referencia al producto que se está editando
    productoEditando = producto;
    
    // Cambiar título del modal
    document.getElementById('modalAgregarProductoLabel').textContent = 'Editar Producto';
    
    // Mostrar modal
    const modal = new bootstrap.Modal(document.getElementById('modalAgregarProducto'));
    modal.show();
}

// Función para eliminar un producto
function eliminarProducto(id) {
    if (confirm('¿Está seguro que desea eliminar este producto?')) {
        // Aquí iría la llamada a la API para eliminar el producto
        // Por ahora, solo lo eliminamos del array local
        productos = productos.filter(p => p.id !== id);
        mostrarProductos();
    }
}

// Función para guardar un producto (nuevo o editado)
function guardarProducto() {
    // Obtener datos del formulario
    const nombre = document.getElementById('nombreProducto').value;
    const descripcion = document.getElementById('descripcionProducto').value;
    const precio = parseFloat(document.getElementById('precioProducto').value);
    const categoria = document.getElementById('categoriaProducto').value;
    const imagenInput = document.getElementById('imagenProducto');
    
    // Validar datos
    if (!nombre || isNaN(precio) || precio <= 0 || !categoria) {
        alert('Por favor complete todos los campos obligatorios correctamente');
        return;
    }
    
    // Crear objeto con datos del producto
    const productoData = {
        nombre,
        descripcion,
        precio,
        categoria,
        imagen: '/img/productos/default.jpg' // Valor por defecto
    };
    
    // Si hay un archivo de imagen seleccionado, procesarlo
    if (imagenInput.files.length > 0) {
        // Aquí iría el código para subir la imagen al servidor
        // Por ahora, solo simulamos que se ha subido
        productoData.imagen = URL.createObjectURL(imagenInput.files[0]);
    }
    
    // Si estamos editando un producto existente
    if (productoEditando) {
        // Actualizar producto existente
        Object.assign(productoEditando, productoData);
    } else {
        // Agregar nuevo producto
        productoData.id = productos.length > 0 ? Math.max(...productos.map(p => p.id)) + 1 : 1;
        productos.push(productoData);
    }
    
    // Cerrar modal
    const modalElement = document.getElementById('modalAgregarProducto');
    const modal = bootstrap.Modal.getInstance(modalElement);
    modal.hide();
    
    // Actualizar tabla de productos
    mostrarProductos();
}