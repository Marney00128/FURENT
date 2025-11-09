# Cómo aprobar la reseña manualmente

## Opción 1: Desde el Panel de Administración (Recomendado)

1. Inicia sesión como administrador
2. Ve a: `http://localhost:8080/admin/resenas`
3. Verás la reseña pendiente del producto "GOJO VS SUKUNA"
4. Haz clic en el botón "Aprobar"
5. Recarga la página de store y verás las estrellas

## Opción 2: Desde la Consola del Navegador (Rápido)

1. Ve a la página de store: `http://localhost:8080/store`
2. Abre la consola del navegador (F12)
3. Pega este código y presiona Enter:

```javascript
// Aprobar todas las reseñas pendientes
fetch('/resenas/admin/pendientes')
  .then(res => res.json())
  .then(data => {
    if (data.success && data.resenas.length > 0) {
      console.log(`Encontradas ${data.resenas.length} reseñas pendientes`);
      
      // Aprobar cada reseña
      data.resenas.forEach(resena => {
        fetch(`/resenas/admin/aprobar/${resena.id}`, { method: 'POST' })
          .then(res => res.json())
          .then(result => {
            console.log(`✅ Reseña aprobada: ${resena.productoNombre} - ${resena.calificacion} estrellas`);
          });
      });
      
      // Recargar página después de 2 segundos
      setTimeout(() => {
        console.log('Recargando página...');
        location.reload();
      }, 2000);
    } else {
      console.log('No hay reseñas pendientes');
    }
  });
```

## Opción 3: Usando cURL (Desde terminal)

```bash
# 1. Primero obtén el ID de la reseña pendiente
curl http://localhost:8080/resenas/admin/pendientes

# 2. Luego aprueba usando el ID (reemplaza RESENA_ID)
curl -X POST http://localhost:8080/resenas/admin/aprobar/RESENA_ID
```

## ¿Por qué no aparece automáticamente?

El sistema de reseñas tiene un flujo de moderación:

1. **Usuario deja reseña** → Estado: PENDIENTE
2. **Admin revisa y aprueba** → Estado: APROBADA
3. **Reseña aparece públicamente** → Visible en store

Esto previene spam y contenido inapropiado.

## Verificar que funcionó

Después de aprobar, deberías ver en la tarjeta del producto:
- ⭐⭐⭐⭐⭐ (5 estrellas doradas)
- Número: 5.0
- Conteo: (1)
- Badge "Top Rated" (si tiene 5+ reseñas con promedio ≥ 4.5)
