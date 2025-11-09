/**
 * Utilidades para formateo de números en formato español
 * Punto (.) como separador de miles
 * Coma (,) como separador decimal
 */

/**
 * Formatea un número al formato español con separador de miles y decimales
 * @param {number} numero - El número a formatear
 * @param {number} decimales - Cantidad de decimales (por defecto 2)
 * @returns {string} - Número formateado (ejemplo: 25.000,00)
 */
function formatearNumero(numero, decimales = 2) {
    if (numero === null || numero === undefined || isNaN(numero)) {
        return '0' + (decimales > 0 ? ',' + '0'.repeat(decimales) : '');
    }
    
    return numero.toLocaleString('es-ES', {
        minimumFractionDigits: decimales,
        maximumFractionDigits: decimales
    });
}

/**
 * Formatea un número como moneda en formato español
 * @param {number} numero - El número a formatear
 * @returns {string} - Número formateado como moneda (ejemplo: $25.000,00)
 */
function formatearMoneda(numero) {
    return '$' + formatearNumero(numero, 2);
}

/**
 * Formatea un número sin decimales
 * @param {number} numero - El número a formatear
 * @returns {string} - Número formateado sin decimales (ejemplo: 25.000)
 */
function formatearEntero(numero) {
    return formatearNumero(numero, 0);
}

/**
 * Parsea un número en formato español a número JavaScript
 * @param {string} numeroFormateado - Número formateado (ejemplo: "25.000,50")
 * @returns {number} - Número JavaScript (ejemplo: 25000.50)
 */
function parsearNumero(numeroFormateado) {
    if (!numeroFormateado) return 0;
    
    // Remover espacios y símbolo de moneda
    let limpio = numeroFormateado.toString().replace(/[$\s]/g, '');
    
    // Reemplazar punto de miles por nada, y coma decimal por punto
    limpio = limpio.replace(/\./g, '').replace(/,/g, '.');
    
    return parseFloat(limpio) || 0;
}
