document.addEventListener('DOMContentLoaded', function() {
    // Check if admin is logged in
    if (!localStorage.getItem('adminLoggedIn')) {
        window.location.href = 'login.html';
        return;
    }
    
    // Initialize products data from localStorage or use default
    let products = JSON.parse(localStorage.getItem('products')) || [
        {
            id: 1,
            name: 'Sillas (Tiffany, Avant Garde)',
            description: 'Moderna y sofisticada, perfecta para eventos vanguardistas y exclusivos. (Precio por unidad)',
            price: 25000,
            category: 'Sillas',
            image: '../img/tiffany-chair-white-1.jpg'
        },
        {
            id: 2,
            name: 'Mesa Redonda',
            description: 'Mesa redonda para 10 personas, ideal para eventos formales.',
            price: 50000,
            category: 'Mesas',
            image: '../img/producto2.jpg'
        }
    ];
    
    // Initialize categories data from localStorage or use default
    let categories = JSON.parse(localStorage.getItem('categories')) || [
        { id: 1, name: 'Sillas', description: 'Todo tipo de sillas para eventos' },
        { id: 2, name: 'Mesas', description: 'Mesas para diferentes ocasiones' },
        { id: 3, name: 'Decoración', description: 'Elementos decorativos para eventos' }
    ];
    
    // DOM elements
    const menuLinks = document.querySelectorAll('.admin-menu a');
    const sections = document.querySelectorAll('.admin-section');
    const productsTableBody = document.getElementById('productsTableBody');
    const categoriesTableBody = document.getElementById('categoriesTableBody');
    const addProductBtn = document.getElementById('addProductBtn');
    const addCategoryBtn = document.getElementById('addCategoryBtn');
    const productModal = document.getElementById('productModal');
    const categoryModal = document.getElementById('categoryModal');
    const productForm = document.getElementById('productForm');
    const categoryForm = document.getElementById('categoryForm');
    const modalCloseButtons = document.querySelectorAll('.admin-modal-close');
    const logoutBtn = document.getElementById('logoutBtn');
    const productCategory = document.getElementById('productCategory');
    
    // Navigation between sections
    menuLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            
            // Remove active class from all links and sections
            menuLinks.forEach(l => l.classList.remove('active'));
            sections.forEach(s => s.classList.remove('active'));
            
            // Add active class to clicked link and corresponding section
            this.classList.add('active');
            const sectionId = this.getAttribute('data-section');
            document.getElementById(sectionId).classList.add('active');
        });
    });
    
    // Load products table
    function loadProducts() {
        productsTableBody.innerHTML = '';
        
        products.forEach(product => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td><img src="${product.image}" alt="${product.name}"></td>
                <td>${product.name}</td>
                <td>$${product.price.toLocaleString()}</td>
                <td>${product.category}</td>
                <td class="action-buttons">
                    <button class="edit-button" data-id="${product.id}"><i class="bi bi-pencil"></i></button>
                    <button class="delete-button" data-id="${product.id}"><i class="bi bi-trash"></i></button>
                </td>
            `;
            productsTableBody.appendChild(row);
        });
        
        // Add event listeners to edit and delete buttons
        document.querySelectorAll('.edit-button').forEach(button => {
            button.addEventListener('click', editProduct);
        });
        
        document.querySelectorAll('.delete-button').forEach(button => {
            button.addEventListener('click', deleteProduct);
        });
    }
    
    // Load categories table
    function loadCategories() {
        categoriesTableBody.innerHTML = '';
        
        categories.forEach(category => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${category.name}</td>
                <td>${category.description}</td>
                <td class="action-buttons">
                    <button class="edit-button" data-id="${category.id}"><i class="bi bi-pencil"></i></button>
                    <button class="delete-button" data-id="${category.id}"><i class="bi bi-trash"></i></button>
                </td>
            `;
            categoriesTableBody.appendChild(row);
        });
        
        // Add event listeners to edit and delete buttons
        document.querySelectorAll('#categories .edit-button').forEach(button => {
            button.addEventListener('click', editCategory);
        });
        
        document.querySelectorAll('#categories .delete-button').forEach(button => {
            button.addEventListener('click', deleteCategory);
        });
    }
    
    // Load category options in product form
    function loadCategoryOptions() {
        productCategory.innerHTML = '';
        
        categories.forEach(category => {
            const option = document.createElement('option');
            option.value = category.name;
            option.textContent = category.name;
            productCategory.appendChild(option);
        });
    }
    
    // Add product
    addProductBtn.addEventListener('click', function() {
        // Reset form
        productForm.reset();
        document.getElementById('productId').value = '';
        document.getElementById('modalTitle').textContent = 'Agregar Producto';
        document.getElementById('imagePreview').innerHTML = '';
        
        // Show modal
        productModal.style.display = 'block';
    });
    
    // Add category
    addCategoryBtn.addEventListener('click', function() {
        // Reset form
        categoryForm.reset();
        document.getElementById('categoryId').value = '';
        document.getElementById('categoryModalTitle').textContent = 'Agregar Categoría';
        
        // Show modal
        categoryModal.style.display = 'block';
    });
    
    // Edit product
    function editProduct() {
        const productId = parseInt(this.getAttribute('data-id'));
        const product = products.find(p => p.id === productId);
        
        if (product) {
            document.getElementById('productId').value = product.id;
            document.getElementById('productName').value = product.name;
            document.getElementById('productDescription').value = product.description;
            document.getElementById('productPrice').value = product.price;
            document.getElementById('productCategory').value = product.category;
            
            // Show image preview
            const imagePreview = document.getElementById('imagePreview');
            imagePreview.innerHTML = `<img src="${product.image}" alt="${product.name}">`;
            
            document.getElementById('modalTitle').textContent = 'Editar Producto';
            productModal.style.display = 'block';
        }
    }
    
    // Edit category
    function editCategory() {
        const categoryId = parseInt(this.getAttribute('data-id'));
        const category = categories.find(c => c.id === categoryId);
        
        if (category) {
            document.getElementById('categoryId').value = category.id;
            document.getElementById('categoryName').value = category.name;
            document.getElementById('categoryDescription').value = category.description;
            
            document.getElementById('categoryModalTitle').textContent = 'Editar Categoría';
            categoryModal.style.display = 'block';
        }
    }
    
    // Delete product
    function deleteProduct() {
        if (confirm('¿Estás seguro de que deseas eliminar este producto?')) {
            const productId = parseInt(this.getAttribute('data-id'));
            products = products.filter(p => p.id !== productId);
            
            // Save to localStorage
            localStorage.setItem('products', JSON.stringify(products));
            
            // Reload products table
            loadProducts();
            
            // Update products on user pages
            updateUserPages();
        }
    }
    
    // Delete category
    function deleteCategory() {
        if (confirm('¿Estás seguro de que deseas eliminar esta categoría?')) {
            const categoryId = parseInt(this.getAttribute('data-id'));
            categories = categories.filter(c => c.id !== categoryId);
            
            // Save to localStorage
            localStorage.setItem('categories', JSON.stringify(categories));
            
            // Reload categories table
            loadCategories();
            
            // Reload category options in product form
            loadCategoryOptions();
        }
    }
    
    // Save product
    productForm.addEventListener('submit', function(e) {
        e.preventDefault();
        
        const productId = document.getElementById('productId').value;
        const name = document.getElementById('productName').value;
        const description = document.getElementById('productDescription').value;
        const price = parseFloat(document.getElementById('productPrice').value);
        const category = document.getElementById('productCategory').value;
        
        // Handle image upload (in a real app, this would upload to a server)
        const imageInput = document.getElementById('productImage');
        let imagePath = '';
        
        if (productId) {
            // Editing existing product
            const product = products.find(p => p.id === parseInt(productId));
            if (product) {
                imagePath = product.image; // Keep existing image if no new one is uploaded
            } else {
                imagePath = '../img/producto-default.jpg';
            }
        } else {
            // New product, use default image if none provided
            imagePath = '../img/producto-default.jpg';
        }
        
        // If a file is selected, create a fake path (in a real app, this would be a server upload)
        if (imageInput.files.length > 0) {
            // In a real app, this would upload the file and get a URL
            // For this demo, we'll just pretend and keep using existing images
            const fileName = imageInput.files[0].name;
            if (fileName.includes('tiffany') || fileName.includes('chair')) {
                imagePath = '../img/tiffany-chair-white-1.jpg';
            } else {
                imagePath = '../img/producto' + (Math.floor(Math.random() * 6) + 1) + '.jpg';
            }
        }
        
        if (productId) {
            // Update existing product
            const index = products.findIndex(p => p.id === parseInt(productId));
            if (index !== -1) {
                products[index] = {
                    ...products[index],
                    name,
                    description,
                    price,
                    category,
                    image: imagePath
                };
            }
        } else {
            // Add new product
            const newId = products.length > 0 ? Math.max(...products.map(p => p.id)) + 1 : 1;
            products.push({
                id: newId,
                name,
                description,
                price,
                category,
                image: imagePath
            });
        }
        
        // Save to localStorage
        localStorage.setItem('products', JSON.stringify(products));
        
        // Close modal
        productModal.style.display = 'none';
        
        // Reload products table
        loadProducts();
        
        // Update products on user pages
        updateUserPages();
        
        // Show success message
        alert(productId ? 'Producto actualizado correctamente' : 'Producto agregado correctamente');
    });
    
    // Save category
    categoryForm.addEventListener('submit', function(e) {
        e.preventDefault();
        
        const categoryId = document.getElementById('categoryId').value;
        const name = document.getElementById('categoryName').value;
        const description = document.getElementById('categoryDescription').value;
        
        if (categoryId) {
            // Update existing category
            const index = categories.findIndex(c => c.id === parseInt(categoryId));
            if (index !== -1) {
                categories[index] = {
                    ...categories[index],
                    name,
                    description
                };
            }
        } else {
            // Add new category
            const newId = categories.length > 0 ? Math.max(...categories.map(c => c.id)) + 1 : 1;
            categories.push({
                id: newId,
                name,
                description
            });
        }
        
        // Save to localStorage
        localStorage.setItem('categories', JSON.stringify(categories));
        
        // Close modal
        categoryModal.style.display = 'none';
        
        // Reload categories table
        loadCategories();
        
        // Reload category options in product form
        loadCategoryOptions();
    });
    
    // Close modals
    modalCloseButtons.forEach(button => {
        button.addEventListener('click', function() {
            productModal.style.display = 'none';
            categoryModal.style.display = 'none';
        });
    });
    
    // Close modals when clicking outside
    window.addEventListener('click', function(e) {
        if (e.target === productModal) {
            productModal.style.display = 'none';
        }
        if (e.target === categoryModal) {
            categoryModal.style.display = 'none';
        }
    });
    
    // Image preview
    document.getElementById('productImage').addEventListener('change', function() {
        const imagePreview = document.getElementById('imagePreview');
        imagePreview.innerHTML = '';
        
        if (this.files.length > 0) {
            const file = this.files[0];
            const reader = new FileReader();
            
            reader.onload = function(e) {
                const img = document.createElement('img');
                img.src = e.target.result;
                img.alt = 'Preview';
                imagePreview.appendChild(img);
            };
            
            reader.readAsDataURL(file);
        }
    });
    
    // Logout
    logoutBtn.addEventListener('click', function() {
        localStorage.removeItem('adminLoggedIn');
        window.location.href = 'login.html';
    });
    
    // Function to update products on user pages
    function updateUserPages() {
        // This would typically be a server-side operation
        // For this demo, we're using localStorage to simulate it
        localStorage.setItem('products', JSON.stringify(products));
    }
    
    // Initialize
    loadProducts();
    loadCategories();
    loadCategoryOptions();
});

// Add this to your admin.js file

// Add orders section to the dashboard
document.addEventListener('DOMContentLoaded', function() {
    // Add orders section after products and categories are loaded
    setTimeout(addOrdersSection, 500);
});

// Add a new section for orders in the admin dashboard
function addOrdersSection() {
    // Create orders section if it doesn't exist
    if (!document.getElementById('orders')) {
        // Add menu link
        const adminMenu = document.querySelector('.admin-menu');
        const ordersLink = document.createElement('a');
        ordersLink.setAttribute('href', '#');
        ordersLink.setAttribute('data-section', 'orders');
        ordersLink.innerHTML = '<i class="bi bi-box"></i> Alquileres';
        adminMenu.appendChild(ordersLink);
        
        // Add section
        const adminContent = document.querySelector('.admin-content');
        const ordersSection = document.createElement('div');
        ordersSection.id = 'orders';
        ordersSection.className = 'admin-section';
        
        ordersSection.innerHTML = `
            <h2>Gestión de Alquileres</h2>
            <div class="admin-card">
                <div class="admin-card-header">
                    <h3>Alquileres Recientes</h3>
                </div>
                <div class="admin-card-body">
                    <div id="ordersTableContainer">
                        <table class="admin-table">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Cliente</th>
                                    <th>Fecha</th>
                                    <th>Período</th>
                                    <th>Total</th>
                                    <th>Estado</th>
                                    <th>Acciones</th>
                                </tr>
                            </thead>
                            <tbody id="ordersTableBody">
                                <!-- Orders will be loaded here -->
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        `;
        
        adminContent.appendChild(ordersSection);
        
        // Add event listener to the new link
        ordersLink.addEventListener('click', function(e) {
            e.preventDefault();
            
            // Remove active class from all links and sections
            document.querySelectorAll('.admin-menu a').forEach(l => l.classList.remove('active'));
            document.querySelectorAll('.admin-section').forEach(s => s.classList.remove('active'));
            
            // Add active class to clicked link and corresponding section
            this.classList.add('active');
            document.getElementById('orders').classList.add('active');
            
            // Load orders
            loadOrders();
        });
        
        // Create order details modal
        const orderModal = document.createElement('div');
        orderModal.id = 'orderModal';
        orderModal.className = 'admin-modal';
        
        orderModal.innerHTML = `
            <div class="admin-modal-content">
                <span class="admin-modal-close">&times;</span>
                <h2 id="orderModalTitle">Detalles del Alquiler</h2>
                <div id="orderDetails">
                    <!-- Order details will be loaded here -->
                </div>
                <div class="admin-modal-footer">
                    <button id="updateOrderStatusBtn" class="admin-btn admin-btn-primary">Actualizar Estado</button>
                </div>
            </div>
        `;
        
        document.body.appendChild(orderModal);
        
        // Add close button functionality
        document.querySelector('#orderModal .admin-modal-close').addEventListener('click', function() {
            document.getElementById('orderModal').style.display = 'none';
        });
        
        // Close when clicking outside
        window.addEventListener('click', function(e) {
            if (e.target === document.getElementById('orderModal')) {
                document.getElementById('orderModal').style.display = 'none';
            }
        });
        
        // Add update order status functionality
        document.getElementById('updateOrderStatusBtn').addEventListener('click', updateOrderStatus);
    }
}

// Load all orders from all users
function loadOrders() {
    const users = JSON.parse(localStorage.getItem('users')) || [];
    const ordersTableBody = document.getElementById('ordersTableBody');
    
    if (ordersTableBody) {
        ordersTableBody.innerHTML = '';
        
        // Collect all orders from all users
        const allOrders = [];
        
        users.forEach(user => {
            if (user.orders && user.orders.length > 0) {
                user.orders.forEach(order => {
                    allOrders.push({
                        ...order,
                        userName: user.name,
                        userEmail: user.email,
                        userPhone: user.phone,
                        status: order.status || 'Pendiente'
                    });
                });
            }
        });
        
        // Sort orders by date (newest first)
        allOrders.sort((a, b) => new Date(b.date) - new Date(a.date));
        
        if (allOrders.length > 0) {
            allOrders.forEach(order => {
                const row = document.createElement('tr');
                
                const date = new Date(order.date);
                const formattedDate = date.toLocaleDateString('es-ES', {
                    year: 'numeric',
                    month: 'short',
                    day: 'numeric'
                });
                
                // Format rental period
                let rentalPeriod = 'N/A';
                if (order.startDate && order.endDate) {
                    const startDate = new Date(order.startDate);
                    const endDate = new Date(order.endDate);
                    
                    const formattedStartDate = startDate.toLocaleDateString('es-ES', {
                        month: 'short',
                        day: 'numeric'
                    });
                    
                    const formattedEndDate = endDate.toLocaleDateString('es-ES', {
                        month: 'short',
                        day: 'numeric'
                    });
                    
                    rentalPeriod = `${formattedStartDate} - ${formattedEndDate}`;
                }
                
                // Status badge class
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
                
                row.innerHTML = `
                    <td>#${order.id}</td>
                    <td>${order.userName}</td>
                    <td>${formattedDate}</td>
                    <td>${rentalPeriod}</td>
                    <td>$${order.total.toLocaleString()}</td>
                    <td><span class="status-badge ${statusClass}">${order.status || 'Pendiente'}</span></td>
                    <td class="action-buttons">
                        <button class="view-button" data-id="${order.id}" data-user="${order.userEmail}"><i class="bi bi-eye"></i></button>
                    </td>
                `;
                
                ordersTableBody.appendChild(row);
            });
            
            // Add event listeners to view buttons
            document.querySelectorAll('#ordersTableBody .view-button').forEach(button => {
                button.addEventListener('click', viewOrderDetails);
            });
        } else {
            ordersTableBody.innerHTML = '<tr><td colspan="7" class="no-data">No hay alquileres registrados</td></tr>';
        }
    }
}

// View order details
function viewOrderDetails() {
    const orderId = parseInt(this.getAttribute('data-id'));
    const userEmail = this.getAttribute('data-user');
    
    const users = JSON.parse(localStorage.getItem('users')) || [];
    const user = users.find(u => u.email === userEmail);
    
    if (user && user.orders) {
        const order = user.orders.find(o => o.id === orderId);
        
        if (order) {
            const orderDetails = document.getElementById('orderDetails');
            
            const date = new Date(order.date);
            const formattedDate = date.toLocaleDateString('es-ES', {
                year: 'numeric',
                month: 'long',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
            });
            
            // Format rental dates if they exist
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
                    <div class="order-detail-item">
                        <span class="order-detail-label">Período de Alquiler:</span>
                        <span class="order-detail-value">${formattedStartDate} al ${formattedEndDate}</span>
                    </div>
                    <div class="order-detail-item">
                        <span class="order-detail-label">Días de Alquiler:</span>
                        <span class="order-detail-value">${order.rentalDays}</span>
                    </div>
                `;
            }
            
            // Add delivery address if it exists
            let deliveryAddress = '';
            if (order.deliveryAddress) {
                deliveryAddress = `
                    <div class="order-detail-item">
                        <span class="order-detail-label">Dirección de Entrega:</span>
                        <span class="order-detail-value">${order.deliveryAddress}</span>
                    </div>
                `;
            }
            
            // Status options
            const statusOptions = ['Pendiente', 'En Proceso', 'Completado', 'Cancelado']
                .map(status => `<option value="${status}" ${(order.status || 'Pendiente') === status ? 'selected' : ''}>${status}</option>`)
                .join('');
            
            orderDetails.innerHTML = `
                <div class="order-detail-section">
                    <h3>Información del Alquiler</h3>
                    <div class="order-detail-item">
                        <span class="order-detail-label">ID:</span>
                        <span class="order-detail-value">#${order.id}</span>
                    </div>
                    <div class="order-detail-item">
                        <span class="order-detail-label">Fecha:</span>
                        <span class="order-detail-value">${formattedDate}</span>
                    </div>
                    ${rentalPeriod}
                    ${deliveryAddress}
                    <div class="order-detail-item">
                        <span class="order-detail-label">Estado:</span>
                        <select id="orderStatus" class="admin-select">
                            ${statusOptions}
                        </select>
                    </div>
                </div>
                
                <div class="order-detail-section">
                    <h3>Información del Cliente</h3>
                    <div class="order-detail-item">
                        <span class="order-detail-label">Nombre:</span>
                        <span class="order-detail-value">${user.name}</span>
                    </div>
                    <div class="order-detail-item">
                        <span class="order-detail-label">Email:</span>
                        <span class="order-detail-value">${user.email}</span>
                    </div>
                    <div class="order-detail-item">
                        <span class="order-detail-label">Teléfono:</span>
                        <span class="order-detail-value">${user.phone}</span>
                    </div>
                </div>
                
                <div class="order-detail-section">
                    <h3>Productos Alquilados</h3>
                    <div class="order-products-list">
                        ${order.products.map(product => `
                            <div class="order-product-item">
                                <img src="${product.image}" alt="${product.name}">
                                <div class="order-product-details">
                                    <h4>${product.name}</h4>
                                    <p>Cantidad: ${product.quantity || 1}</p>
                                    <p>Precio: $${(typeof product.price === 'number' ? product.price : parseFloat(product.price)).toLocaleString()}</p>
                                </div>
                            </div>
                        `).join('')}
                    </div>
                </div>
                
                <div class="order-detail-section">
                    <h3>Resumen</h3>
                    <div class="order-detail-item">
                        <span class="order-detail-label">Subtotal:</span>
                        <span class="order-detail-value">$${(order.subtotal || order.total).toLocaleString()}</span>
                    </div>
                    <div class="order-detail-item">
                        <span class="order-detail-label">Total:</span>
                        <span class="order-detail-value">$${order.total.toLocaleString()}</span>
                    </div>
                </div>
            `;
            
            // Store order data for update
            document.getElementById('updateOrderStatusBtn').setAttribute('data-id', order.id);
            document.getElementById('updateOrderStatusBtn').setAttribute('data-user', userEmail);
            
            // Show modal
            document.getElementById('orderModal').style.display = 'block';
        }
    }
}

// Update order status
function updateOrderStatus() {
    const orderId = parseInt(this.getAttribute('data-id'));
    const userEmail = this.getAttribute('data-user');
    const newStatus = document.getElementById('orderStatus').value;
    
    const users = JSON.parse(localStorage.getItem('users')) || [];
    const userIndex = users.findIndex(u => u.email === userEmail);
    
    if (userIndex !== -1) {
        const orderIndex = users[userIndex].orders.findIndex(o => o.id === orderId);
        
        if (orderIndex !== -1) {
            // Update order status
            users[userIndex].orders[orderIndex].status = newStatus;
            
            // Save to localStorage
            localStorage.setItem('users', JSON.stringify(users));
            
            // Close modal
            document.getElementById('orderModal').style.display = 'none';
            
            // Reload orders
            loadOrders();
            
            // Show success message
            alert('Estado del alquiler actualizado correctamente');
        }
    }
}

// Add this function to update dashboard stats
function updateDashboardStats() {
    // Get data from localStorage
    const products = JSON.parse(localStorage.getItem('products')) || [];
    const categories = JSON.parse(localStorage.getItem('categories')) || [];
    const users = JSON.parse(localStorage.getItem('users')) || [];
    
    // Get all orders from all users
    let allOrders = [];
    users.forEach(user => {
        if (user.orders && user.orders.length > 0) {
            allOrders = allOrders.concat(user.orders);
        }
    });
    
    // Update dashboard counters
    document.getElementById('productCount').textContent = products.length;
    document.getElementById('categoryCount').textContent = categories.length;
    document.getElementById('userCount').textContent = users.length;
    document.getElementById('orderCount').textContent = allOrders.length;
    
    // Update recent orders table
    updateRecentOrders(allOrders);
}

// Function to update recent orders table
function updateRecentOrders(allOrders) {
    const recentOrdersTableBody = document.getElementById('recentOrdersTableBody');
    if (!recentOrdersTableBody) return;
    
    // Clear existing rows
    recentOrdersTableBody.innerHTML = '';
    
    // Sort orders by date (newest first)
    allOrders.sort((a, b) => new Date(b.date) - new Date(a.date));
    
    // Get only the 5 most recent orders
    const recentOrders = allOrders.slice(0, 5);
    
    if (recentOrders.length === 0) {
        recentOrdersTableBody.innerHTML = '<tr><td colspan="5" class="text-center">No hay alquileres recientes</td></tr>';
        return;
    }
    
    // Add rows for each recent order
    recentOrders.forEach(order => {
        const date = new Date(order.date);
        const formattedDate = date.toLocaleDateString('es-ES', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
        
        // Status badge class
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
        
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>#${order.id}</td>
            <td>${order.userName || 'Usuario'}</td>
            <td>${formattedDate}</td>
            <td>$${order.total.toLocaleString()}</td>
            <td><span class="status-badge ${statusClass}">${order.status || 'Pendiente'}</span></td>
        `;
        recentOrdersTableBody.appendChild(row);
    });
}

// Call this function when the page loads
document.addEventListener('DOMContentLoaded', function() {
    // Your existing code...
    
    // Update dashboard stats
    updateDashboardStats();
    
    // Also update stats whenever products, categories, or orders change
    loadProducts();
    loadCategories();
    loadOrders();
});
document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('loginForm');
    
    if (loginForm) {
        loginForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;
            
            fetch('/api/administrador/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    nombreUsuario: username,
                    contrasena: password
                })
            })
            .then(response => response.json())
            .then(data => {
                if (data && data.id) {
                    // Login exitoso
                    localStorage.setItem('adminId', data.id);
                    localStorage.setItem('adminUsername', data.nombreUsuario);
                    window.location.href = '/admin/dashboard';
                } else {
                    // Login fallido
                    alert('Credenciales incorrectas');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Error al intentar iniciar sesión');
            });
        });
    }
});
