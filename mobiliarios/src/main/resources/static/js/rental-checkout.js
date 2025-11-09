document.addEventListener('DOMContentLoaded', function() {
    // Check if user is logged in
    const currentUser = JSON.parse(localStorage.getItem('currentUser'));
    if (!currentUser) {
        alert('Por favor inicia sesión para completar tu alquiler');
        window.location.href = 'user-account.html';
        return;
    }
    
    // Load cart from localStorage
    const carrito = JSON.parse(localStorage.getItem('carritoTemp')) || [];
    const subtotal = parseFloat(localStorage.getItem('precioTemp')) || 0;
    
    if (carrito.length === 0) {
        alert('Tu carrito está vacío');
        window.location.href = '../index.html';
        return;
    }
    
    // Load user info
    const userInfo = document.getElementById('userInfo');
    userInfo.innerHTML = `
        <p><strong>Nombre:</strong> ${currentUser.name}</p>
        <p><strong>Email:</strong> ${currentUser.email}</p>
        <p><strong>Teléfono:</strong> ${currentUser.phone || 'No especificado'}</p>
    `;
    
    // Load rental items
    const rentalItems = document.getElementById('rentalItems');
    rentalItems.innerHTML = '';
    
    carrito.forEach(item => {
        const itemDiv = document.createElement('div');
        itemDiv.className = 'rental-item';
        itemDiv.innerHTML = `
            <img src="${item.url}" alt="${item.name}">
            <div class="rental-item-details">
                <p class="rental-item-name">${item.name}</p>
                <p class="rental-item-price">${item.price}</p>
            </div>
        `;
        rentalItems.appendChild(itemDiv);
    });
    
    // Set subtotal
    document.getElementById('subtotal').textContent = formatearMoneda(subtotal);
    
    // Set min date for date inputs to today
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('startDate').min = today;
    document.getElementById('endDate').min = today;
    
    // Date change handlers
    const startDateInput = document.getElementById('startDate');
    const endDateInput = document.getElementById('endDate');
    
    function updateRentalCalculation() {
        const startDate = new Date(startDateInput.value);
        const endDate = new Date(endDateInput.value);
        
        if (startDate && endDate && startDate <= endDate) {
            // Calculate days difference
            const diffTime = Math.abs(endDate - startDate);
            const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
            
            // Update days and total
            document.getElementById('rentalDays').textContent = diffDays;
            const total = subtotal * diffDays;
            document.getElementById('rentalTotal').textContent = formatearMoneda(total);
        } else {
            document.getElementById('rentalDays').textContent = '0';
            document.getElementById('rentalTotal').textContent = formatearMoneda(0);
        }
    }
    
    startDateInput.addEventListener('change', updateRentalCalculation);
    endDateInput.addEventListener('change', updateRentalCalculation);
    
    // Form submission
    document.getElementById('rentalForm').addEventListener('submit', function(e) {
        e.preventDefault();
        
        const startDate = new Date(startDateInput.value);
        const endDate = new Date(endDateInput.value);
        
        if (startDate > endDate) {
            alert('La fecha de fin debe ser posterior a la fecha de inicio');
            return;
        }
        
        // Calculate days difference
        const diffTime = Math.abs(endDate - startDate);
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        
        if (diffDays === 0) {
            alert('El período de alquiler debe ser de al menos 1 día');
            return;
        }
        
        // Calculate total
        const total = subtotal * diffDays;
        
        // Create order object
        const order = {
            id: Date.now(),
            date: new Date().toISOString(),
            deliveryAddress: document.getElementById('deliveryAddress').value,
            startDate: startDateInput.value,
            endDate: endDateInput.value,
            rentalDays: diffDays,
            products: carrito.map(item => ({
                id: Math.floor(Math.random() * 1000),
                name: item.name || '',
                price: parseFloat(item.price.replace('$', '').replace(',', '')) || 0,
                quantity: 1,
                image: item.url || ''
            })),
            subtotal: subtotal,
            total: total,
            status: 'Pendiente'
        };
        
        // Get users from localStorage
        const users = JSON.parse(localStorage.getItem('users')) || [];
        
        // Find current user
        const userIndex = users.findIndex(u => u.email === currentUser.email);
        
        if (userIndex !== -1) {
            // Add order to user's orders
            if (!users[userIndex].orders) {
                users[userIndex].orders = [];
            }
            
            users[userIndex].orders.push(order);
            
            // Save to localStorage
            localStorage.setItem('users', JSON.stringify(users));
            
            // Clear cart
            localStorage.removeItem('carritoTemp');
            localStorage.removeItem('precioTemp');
            localStorage.removeItem('carrito');
            localStorage.setItem('precio', '0');
            
            // Redirect to success page
            window.location.href = 'rental-success.html';
        }
    });
});