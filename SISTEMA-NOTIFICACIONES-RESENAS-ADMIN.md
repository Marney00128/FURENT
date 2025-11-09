# Sistema de Notificaciones de Reseñas para Administrador

## ✅ Implementación Completada

Se ha implementado un sistema completo de notificaciones en la campanita del panel de administración para gestionar reseñas pendientes de aprobación.

---

## 📋 Componentes Creados

### Backend

#### 1. **ResenaControlador.java** - Nuevo Endpoint
```java
GET /resenas/admin/pendientes/count
```
- Retorna el conteo de reseñas pendientes
- Requiere rol ADMIN
- Respuesta: `{ "success": true, "count": 5 }`

### Frontend

#### 2. **admin-notifications.js** (Nuevo archivo)
**Ubicación:** `/static/js/admin-notifications.js`

**Características:**
- ✅ Clase `AdminNotificationManager` para gestionar notificaciones
- ✅ Actualización automática cada 30 segundos
- ✅ Panel desplegable con tabs (actualmente solo Reseñas)
- ✅ Badge animado con contador de notificaciones
- ✅ Carga dinámica de reseñas pendientes
- ✅ Botones de acción: Aprobar/Rechazar directamente desde el panel
- ✅ Toast notifications para feedback
- ✅ Formateo inteligente de fechas (hace X minutos/horas/días)

**Métodos principales:**
- `init()`: Inicializa el sistema
- `loadNotifications()`: Carga conteo de notificaciones
- `loadResenasContent()`: Carga lista de reseñas pendientes
- `aprobarResena(resenaId)`: Aprueba una reseña
- `rechazarResena(resenaId)`: Rechaza una reseña
- `showToast(message, type)`: Muestra notificación temporal

#### 3. **admin-notifications.css** (Nuevo archivo)
**Ubicación:** `/static/css/admin-notifications.css`

**Estilos incluidos:**
- ✅ Botón de campanita con badge animado
- ✅ Panel desplegable moderno con animaciones
- ✅ Tabs para diferentes tipos de notificaciones
- ✅ Tarjetas de reseñas con información completa
- ✅ Botones de acción con gradientes
- ✅ Toast notifications
- ✅ Estados de carga, vacío y error
- ✅ Soporte completo para dark mode
- ✅ Responsive design

---

## 🎨 Diseño Visual

### Panel de Notificaciones

```
┌─────────────────────────────────────┐
│ 🔔 Notificaciones              ✕    │
├─────────────────────────────────────┤
│ [⭐ Reseñas (3)]                    │
├─────────────────────────────────────┤
│ ┌─────────────────────────────────┐ │
│ │ ⭐ Juan Pérez dejó una reseña   │ │
│ │ 📦 GOJO VS SUKUNA               │ │
│ │ ★★★★★ 5.0                       │ │
│ │ "Excelente producto..."         │ │
│ │ [✓ Aprobar] [✕ Rechazar]       │ │
│ │ 🕐 Hace 5 minutos               │ │
│ └─────────────────────────────────┘ │
│                                     │
│ [Ver todas las reseñas →]          │
└─────────────────────────────────────┘
```

### Badge de Notificaciones

- **Sin notificaciones:** Badge oculto
- **Con notificaciones:** Badge rojo con número (1-99+)
- **Animación:** Pulso continuo para llamar la atención

---

## 📄 Archivos Modificados

### Páginas de Administración Actualizadas

Todas las siguientes páginas ahora incluyen el sistema de notificaciones:

1. ✅ `admin/dashboard.html`
2. ✅ `admin/productos.html`
3. ✅ `admin/usuarios.html`
4. ✅ `admin/pedidos.html`
5. ✅ `admin/categorias.html`
6. ✅ `admin/reportes.html`
7. ✅ `admin/reportes-analytics.html`
8. ✅ `admin/configuracion.html`
9. ✅ `admin/resenas.html`

**Cambios aplicados en cada archivo:**
```html
<!-- En el <head> -->
<link rel="stylesheet" href="/css/admin-notifications.css">

<!-- Antes de </body> -->
<script src="/js/admin-notifications.js"></script>
```

---

## 🔄 Flujo de Funcionamiento

### 1. Carga Inicial
```
Usuario entra al panel admin
    ↓
JavaScript se inicializa automáticamente
    ↓
Llama a /resenas/admin/pendientes/count
    ↓
Actualiza badge con el número de reseñas pendientes
```

### 2. Actualización Automática
```
Cada 30 segundos:
    ↓
Consulta endpoint de conteo
    ↓
Actualiza badge si hay cambios
```

### 3. Ver Notificaciones
```
Admin hace clic en campanita
    ↓
Panel se despliega con animación
    ↓
Carga lista completa de reseñas pendientes
    ↓
Muestra información detallada de cada reseña
```

### 4. Aprobar/Rechazar Reseña
```
Admin hace clic en "Aprobar" o "Rechazar"
    ↓
Envía petición POST al endpoint correspondiente
    ↓
Muestra toast de confirmación
    ↓
Recarga automáticamente la lista
    ↓
Actualiza el contador del badge
```

---

## 🎯 Características Destacadas

### ✨ Experiencia de Usuario

1. **Notificaciones en Tiempo Real**
   - Actualización automática cada 30 segundos
   - No requiere recargar la página

2. **Gestión Rápida**
   - Aprobar/rechazar directamente desde el panel
   - No necesita ir a la página de reseñas

3. **Información Completa**
   - Nombre del usuario
   - Producto reseñado
   - Calificación con estrellas
   - Comentario (primeros 100 caracteres)
   - Tiempo transcurrido

4. **Feedback Visual**
   - Badge animado con pulso
   - Toast notifications
   - Animaciones suaves
   - Estados de carga

### 🎨 Diseño

1. **Moderno y Profesional**
   - Gradientes sutiles
   - Iconos de Bootstrap Icons
   - Animaciones fluidas
   - Sombras y profundidad

2. **Dark Mode**
   - Soporte completo
   - Colores adaptados
   - Contraste adecuado

3. **Responsive**
   - Funciona en móviles
   - Panel se adapta al ancho de pantalla
   - Touch-friendly

### 🔒 Seguridad

1. **Validación de Rol**
   - Solo usuarios ADMIN pueden acceder
   - Verificación en cada petición

2. **Sesión Requerida**
   - Requiere autenticación
   - Token de sesión validado

---

## 📊 Endpoints Utilizados

### Obtener Conteo
```http
GET /resenas/admin/pendientes/count
Authorization: Session (ADMIN)

Response:
{
  "success": true,
  "count": 5
}
```

### Obtener Lista Completa
```http
GET /resenas/admin/pendientes
Authorization: Session (ADMIN)

Response:
{
  "success": true,
  "resenas": [
    {
      "id": "abc123",
      "usuarioNombre": "Juan Pérez",
      "productoNombre": "GOJO VS SUKUNA",
      "calificacion": 5,
      "comentario": "Excelente producto...",
      "fechaCreacion": "2025-11-06T22:30:00",
      "estado": "PENDIENTE"
    }
  ]
}
```

### Aprobar Reseña
```http
POST /resenas/admin/aprobar/{resenaId}
Authorization: Session (ADMIN)

Response:
{
  "success": true,
  "message": "Reseña aprobada exitosamente",
  "resena": { ... }
}
```

### Rechazar Reseña
```http
POST /resenas/admin/rechazar/{resenaId}
Authorization: Session (ADMIN)

Response:
{
  "success": true,
  "message": "Reseña rechazada",
  "resena": { ... }
}
```

---

## 🚀 Cómo Usar

### Para el Administrador

1. **Ver Notificaciones**
   - Inicia sesión como administrador
   - Observa el badge rojo en la campanita (si hay reseñas pendientes)
   - Haz clic en la campanita para ver el panel

2. **Aprobar una Reseña**
   - Abre el panel de notificaciones
   - Lee la reseña
   - Haz clic en "✓ Aprobar"
   - Verás un mensaje de confirmación
   - La reseña desaparece del panel
   - El contador se actualiza

3. **Rechazar una Reseña**
   - Abre el panel de notificaciones
   - Lee la reseña
   - Haz clic en "✕ Rechazar"
   - Verás un mensaje de confirmación
   - La reseña desaparece del panel
   - El contador se actualiza

4. **Ver Todas las Reseñas**
   - En el panel, haz clic en "Ver todas las reseñas"
   - Serás redirigido a `/admin/resenas`
   - Allí puedes gestionar todas las reseñas con más detalle

---

## 🔧 Configuración

### Intervalo de Actualización

Para cambiar el intervalo de actualización automática, edita en `admin-notifications.js`:

```javascript
// Cambiar de 30 segundos a otro valor (en milisegundos)
this.updateInterval = setInterval(() => {
    this.loadNotifications();
}, 30000); // 30000 = 30 segundos
```

### Límite de Caracteres del Comentario

Para cambiar cuántos caracteres del comentario se muestran:

```javascript
// En el método createResenaCard()
"${resena.comentario.substring(0, 100)}..." // Cambiar 100 por otro valor
```

---

## 🎨 Personalización de Estilos

### Colores del Badge

```css
.notification-btn .badge {
    background: linear-gradient(135deg, #ef4444, #dc2626); /* Cambiar colores */
}
```

### Tamaño del Panel

```css
.admin-notification-panel {
    width: 420px; /* Cambiar ancho */
    max-height: 600px; /* Cambiar altura máxima */
}
```

---

## 📱 Responsive

El sistema es completamente responsive:

- **Desktop:** Panel de 420px de ancho
- **Tablet:** Panel se adapta al ancho disponible
- **Mobile:** Panel ocupa casi todo el ancho (con margen de 40px)

---

## 🐛 Solución de Problemas

### El badge no aparece

1. Verifica que haya reseñas pendientes en la base de datos
2. Abre la consola del navegador (F12)
3. Busca errores en la consola
4. Verifica que el endpoint `/resenas/admin/pendientes/count` funcione

### El panel no se abre

1. Verifica que el CSS esté cargado
2. Revisa la consola por errores de JavaScript
3. Asegúrate de que el botón tenga la clase `notification-btn`

### Las acciones no funcionan

1. Verifica que estés logueado como ADMIN
2. Revisa la consola por errores de red
3. Verifica que los endpoints de aprobar/rechazar funcionen

---

## ✅ Testing

### Probar el Sistema

1. **Crear una reseña pendiente:**
   - Como usuario, deja una reseña en un alquiler completado
   - La reseña quedará en estado PENDIENTE

2. **Ver la notificación:**
   - Inicia sesión como administrador
   - Deberías ver el badge con "1"
   - Haz clic en la campanita
   - Deberías ver la reseña en el panel

3. **Aprobar la reseña:**
   - Haz clic en "Aprobar"
   - Deberías ver un toast verde de confirmación
   - El badge debería actualizarse a "0"
   - La reseña debería desaparecer del panel

4. **Verificar en store:**
   - Ve a la página de store
   - Busca el producto reseñado
   - Deberías ver las estrellas y la calificación

---

## 🎉 Resultado Final

Ahora el administrador puede:

✅ Ver en tiempo real cuántas reseñas están pendientes
✅ Recibir notificaciones visuales con badge animado
✅ Gestionar reseñas sin salir de la página actual
✅ Aprobar o rechazar con un solo clic
✅ Ver información completa de cada reseña
✅ Recibir feedback inmediato de sus acciones
✅ Acceder rápidamente a la página completa de reseñas

El sistema mejora significativamente la eficiencia del administrador y la experiencia de moderación de contenido.
