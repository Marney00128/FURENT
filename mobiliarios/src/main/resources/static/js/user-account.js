document.addEventListener('DOMContentLoaded', function () {
    const tabButtons = document.querySelectorAll('.tab-button');
    const tabContents = document.querySelectorAll('.tab-content');
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');
    const userDashboard = document.querySelector('.user-dashboard');
    const logoutBtn = document.getElementById('logoutBtn');
    const API_BASE_URL = "http://localhost:8080/api";

    
    checkLoginStatus();

    tabButtons.forEach(button => {
        button.addEventListener('click', function () {
            tabButtons.forEach(btn => btn.classList.remove('active'));
            tabContents.forEach(content => content.classList.remove('active'));

            this.classList.add('active');
            const tabId = this.getAttribute('data-tab');
            document.getElementById(tabId).classList.add('active');
        });
    });

     // Inicio de sesión de usuario
    loginForm.addEventListener("submit", async (e) => {
        e.preventDefault();
    
        const correo = document.getElementById("loginEmail").value;
        const contrasena = document.getElementById("loginPassword").value;
        
        try {
            const respuesta = await fetch(`${API_BASE_URL}/usuarios/login`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({ correo, contrasena }),
            });
    
            if (respuesta.ok) {
                const usuario = await respuesta.json();
                if (usuario) {
                    const ultimoInicioSesion = usuario.ultimoInicioSesion 
                        ? new Date(usuario.ultimoInicioSesion).toLocaleString('es-ES') 
                        : 'No disponible';
                        
                    document.querySelector(".user-dashboard").style.display = "block";
                    document.getElementById("userName").textContent = usuario.nombre;
                    document.getElementById("userEmail").textContent = usuario.correo;
                    document.getElementById("userPhone").textContent = usuario.telefono;
                    document.getElementById('userLastLogin').textContent = ultimoInicioSesion;
                    sessionStorage.setItem('currentUser', JSON.stringify({ ...usuario, tipo: 'usuario' }));
                    
                    // Ocultar los tabs de login/register
                    tabContents.forEach(content => content.classList.remove('active'));
                    loginForm.reset();
                    
                    loadOrderHistory();
                } else {
                    alert("Error: No se recibieron datos de usuario.");
                }
            } else {
                const errorData = await respuesta.json();
                alert(errorData || "Error en el inicio de sesión.");
            }
        } catch (err) {
            console.error("Error al iniciar sesión:", err);
            alert("Error de conexión al servidor. Inténtelo más tarde.");
        }
    });

    registerForm.addEventListener("submit", async (e) => {
        e.preventDefault();

        const nombre = document.getElementById("registerName").value;
        const correo = document.getElementById("registerEmail").value;
        const telefono = document.getElementById("registerPhone").value;
        const contrasena = document.getElementById("registerPassword").value;
        const confirmar = document.getElementById("registerConfirmPassword").value;

        if (contrasena !== confirmar) {
            alert("Las contraseñas no coinciden.");
            return;
        }

        const usuario = { nombre, correo, telefono, contrasena };

        try {
            const respuesta = await fetch(`${API_BASE_URL}/usuarios/registrar`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(usuario),
            });

            if (respuesta.ok) {
                const usuarioRegistrado = await respuesta.json();
                alert("Usuario registrado correctamente.");
                registerForm.reset();
                
                // Cambiar a la pestaña de login automáticamente
                tabButtons.forEach(btn => btn.classList.remove('active'));
                tabContents.forEach(content => content.classList.remove('active'));
                document.querySelector('[data-tab="login"]').classList.add('active');
                document.getElementById('login').classList.add('active');
            } else {
                const errorData = await respuesta.json();
                alert(errorData || "Error al registrar usuario.");
            }
        } catch (err) {
            console.error("Error al registrar:", err);
            alert("Error de conexión al servidor. Inténtelo más tarde.");
        }
    });

    if (logoutBtn) {
        logoutBtn.addEventListener('click', function () {
            sessionStorage.removeItem('currentUser');
            userDashboard.style.display = 'none';
            document.getElementById('login').classList.add('active');
            document.querySelector('[data-tab="login"]').classList.add('active');
            document.querySelector('[data-tab="register"]').classList.remove('active');
            document.getElementById('register').classList.remove('active');
        });
    }

    function checkLoginStatus() {
        const currentUser = JSON.parse(sessionStorage.getItem('currentUser'));
        if (currentUser) {
            showUserDashboard();
        }
    }

    function showUserDashboard() {
        const currentUser = JSON.parse(sessionStorage.getItem('currentUser'));
        const ultimoInicioSesion = currentUser.ultimoInicioSesion 
            ? new Date(currentUser.ultimoInicioSesion).toLocaleString('es-ES') 
            : 'No disponible';

        if (currentUser) {
            tabContents.forEach(content => content.classList.remove('active'));
            tabButtons.forEach(btn => btn.classList.remove('active'));

            userDashboard.style.display = 'block';
            document.getElementById('userName').textContent = currentUser.nombre;
            document.getElementById('userEmail').textContent = currentUser.correo;
            document.getElementById('userPhone').textContent = currentUser.telefono;
            document.getElementById('userLastLogin').textContent = ultimoInicioSesion;
            loadOrderHistory();
        }
    }

    function loadOrderHistory() {
        const currentUser = JSON.parse(sessionStorage.getItem('currentUser'));
        const users = [] // Eliminado uso de localStorage || [];
        const user = users.find(u => u.email === currentUser.email);

        const ordersList = document.getElementById('ordersList');

        if (user && user.orders && user.orders.length > 0) {
            ordersList.innerHTML = '';

            user.orders.forEach(order => {
                const orderItem = document.createElement('div');
                orderItem.className = 'order-item';

                const date = new Date(order.date);
                const formattedDate = date.toLocaleDateString('es-ES', {
                    year: 'numeric',
                    month: 'long',
                    day: 'numeric'
                });

                let rentalPeriod = '';
                if (order.startDate && order.endDate) {
                    const startDate = new Date(order.startDate);
                    const endDate = new Date(order.endDate);

                    const formattedStartDate = startDate.toLocaleDateString('es-ES', {
                        year: 'numeric',
                        month: 'long',
                        day: 'numeric'
                    });

                    const formattedEndDate = endDate.toLocaleDateString('es-ES', {
                        year: 'numeric',
                        month: 'long',
                        day: 'numeric'
                    });

                    rentalPeriod = `
                        <p><strong>Período de Alquiler:</strong> ${formattedStartDate} al ${formattedEndDate}</p>
                        <p><strong>Días de Alquiler:</strong> ${order.rentalDays}</p>
                    `;
                }

                let deliveryAddress = '';
                if (order.deliveryAddress) {
                    deliveryAddress = `<p><strong>Dirección de Entrega:</strong> ${order.deliveryAddress}</p>`;
                }

                orderItem.innerHTML = `
                    <h4>Orden #${order.id}</h4>
                    <p><strong>Fecha de Orden:</strong> ${formattedDate}</p>
                    ${rentalPeriod}
                    ${deliveryAddress}
                    <p><strong>Subtotal:</strong> $${(order.subtotal || order.total).toLocaleString()}</p>
                    <p><strong>Total:</strong> $${order.total.toLocaleString()}</p>
                    <div class="order-products">
                        <h5>Productos:</h5>
                        ${order.products.map(product => `
                            <div class="order-product">
                                <img src="${product.image}" alt="${product.name}">
                                <div>
                                    <p><strong>${product.name}</strong></p>
                                    <p>Cantidad: ${product.quantity || 1}</p>
                                    <p>Precio: $${(typeof product.price === 'number' ? product.price : parseFloat(product.price)).toLocaleString()}</p>
                                </div>
                            </div>
                        `).join('')}
                    </div>
                `;

                ordersList.appendChild(orderItem);
            });
        } else {
            ordersList.innerHTML = '<p class="no-orders">No tienes alquileres previos.</p>';
        }
    }

    function loadUserOrders() {
        const currentUser = JSON.parse(sessionStorage.getItem('currentUser'));
        if (!currentUser) return;

        const users = [] // Eliminado uso de localStorage || [];
        const user = users.find(u => u.email === currentUser.email);

        const ordersContainer = document.getElementById('userOrders');
        if (!user || !user.orders || user.orders.length === 0) {
            if (ordersContainer) {
                ordersContainer.innerHTML = '<p class="no-orders">No tienes alquileres registrados.</p>';
            }
            return;
        }

        const orders = [...user.orders].sort((a, b) => new Date(b.date) - new Date(a.date));

        if (ordersContainer) {
            ordersContainer.innerHTML = '';

            orders.forEach(order => {
                const orderCard = document.createElement('div');
                orderCard.className = 'order-card';

                const orderDate = new Date(order.date);
                const formattedDate = orderDate.toLocaleDateString('es-ES', {
                    year: 'numeric',
                    month: 'long',
                    day: 'numeric'
                });

                const startDate = new Date(order.startDate);
                const endDate = new Date(order.endDate);

                const formattedStartDate = startDate.toLocaleDateString('es-ES', {
                    year: 'numeric',
                    month: 'long',
                    day: 'numeric'
                });

                const formattedEndDate = endDate.toLocaleDateString('es-ES', {
                    year: 'numeric',
                    month: 'long',
                    day: 'numeric'
                });

                let statusClass = '';
                switch (order.status) {
                    case 'Completado':
                        statusClass = 'status-completed';
                        break;
                    case 'En Proceso':
                        statusClass = 'status-processing';
                        break;
                    case 'Cancelado':
                        statusClass = 'status-cancelled';
                        break;
                    default:
                        statusClass = 'status-pending';
                }

                orderCard.innerHTML = `
                    <div class="order-header">
                        <div class="order-info">
                            <h3>Alquiler #${order.id}</h3>
                            <p class="order-date">Fecha: ${formattedDate}</p>
                        </div>
                        <div class="order-status">
                            <span class="status-badge ${statusClass}">${order.status || 'Pendiente'}</span>
                        </div>
                    </div>
                    <div class="order-details">
                        <div class="order-period">
                            <p><strong>Período de Alquiler:</strong> ${formattedStartDate} al ${formattedEndDate}</p>
                            <p><strong>Días de Alquiler:</strong> ${order.rentalDays}</p>
                        </div>
                        <div class="order-address">
                            <p><strong>Dirección de Entrega:</strong> ${order.deliveryAddress}</p>
                        </div>
                        <div class="order-products">
                            <h4>Productos Alquilados</h4>
                            <div class="order-products-list">
                                ${order.products.map(product => `
                                    <div class="order-product-item">
                                        <img src="${product.image}" alt="${product.name}">
                                        <div class="order-product-details">
                                            <p class="order-product-name">${product.name}</p>
                                            <p class="order-product-price">$${product.price.toLocaleString()}</p>
                                        </div>
                                    </div>
                                `).join('')}
                            </div>
                        </div>
                        <div class="order-total">
                            <p><strong>Subtotal:</strong> $${order.subtotal.toLocaleString()}</p>
                            <p><strong>Total:</strong> $${order.total.toLocaleString()}</p>
                        </div>
                    </div>
                `;

                ordersContainer.appendChild(orderCard);
            });
        }
    }

    loadUserOrders();
});
