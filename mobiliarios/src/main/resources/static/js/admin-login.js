document.getElementById('loginForm').addEventListener('submit', async function (event) {
    event.preventDefault();

    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    try {
        const response = await fetch('/src/main/resources/static/php/admin-login.php', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ username, password }),
        });

        if (response.ok) {
            const result = await response.json();
            if (result && result.success) {
                window.location.href = '../admin/dashboard.html';
            } else {
                alert('Credenciales inválidas.');
            }
        } else {
            alert('Error al iniciar sesión. Intenta de nuevo.');
        }
    } catch (error) {
        console.error('Error en la solicitud:', error);
        alert('Error de red o del servidor.');
    }
});
