package com.universidad.productos_service.service;

import com.universidad.productos_service.domain.Producto;
import com.universidad.productos_service.repository.ProductoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Suite de pruebas unitarias para ProductoServiceImpl.
 * Usa MockitoExtension para aislar la lógica de negocio del repositorio JPA.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de ProductoServiceImpl")
class ProductoServiceImplTest {

    // ─── Dependencias mockeadas ────────────────────────────────────────────────

    @Mock
    private ProductoRepository productoRepository;

    // ─── Sistema bajo prueba (SUT) ─────────────────────────────────────────────

    @InjectMocks
    private ProductoServiceImpl productoService;

    // ─── Captor de argumentos ──────────────────────────────────────────────────

    @Captor
    private ArgumentCaptor<Producto> productoCaptor;


    // ══════════════════════════════════════════════════════════════════════════
    // PASO 3: Happy Path — Casos exitosos
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("crear: datos válidos → retorna producto guardado con ID")
    void crear_datosValidos_retornaProductoGuardado() {
        // ARRANGE: preparar el mock para simular que el repositorio guarda y retorna
        Producto guardado = new Producto(1L, "Laptop", 1500.0, 10);
        when(productoRepository.save(any(Producto.class))).thenReturn(guardado);

        // ACT: llamar al método bajo prueba
        Producto resultado = productoService.crear("Laptop", 1500.0, 10);

        // ASSERT: verificar el resultado
        assertNotNull(resultado.getId(),
                "El producto guardado debe tener un ID asignado");
        assertEquals("Laptop", resultado.getNombre(),
                "El nombre debe coincidir con el ingresado");
        assertEquals(1500.0, resultado.getPrecio(),
                "El precio debe coincidir con el ingresado");
        assertEquals(10, resultado.getStock(),
                "El stock debe coincidir con el ingresado");

        // VERIFY: el repositorio debe haber sido llamado exactamente una vez
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    @DisplayName("buscarPorId: ID existente → retorna el producto correcto")
    void buscarPorId_existente_retornaProducto() {
        // ARRANGE
        Producto producto = new Producto(1L, "Mouse", 50.0, 100);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        // ACT
        Producto resultado = productoService.buscarPorId(1L);

        // ASSERT
        assertEquals("Mouse", resultado.getNombre());
        assertEquals(50.0, resultado.getPrecio());
        assertEquals(100, resultado.getStock());
        assertEquals(1L, resultado.getId());
    }

    @Test
    @DisplayName("actualizarStock: stock válido → retorna producto con stock actualizado")
    void actualizarStock_stockValido_retornaProductoActualizado() {
        // ARRANGE
        Producto existente = new Producto(1L, "Teclado", 80.0, 20);
        Producto actualizado = new Producto(1L, "Teclado", 80.0, 50);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(productoRepository.save(any(Producto.class))).thenReturn(actualizado);

        // ACT
        Producto resultado = productoService.actualizarStock(1L, 50);

        // ASSERT
        assertEquals(50, resultado.getStock(),
                "El stock debe actualizarse al nuevo valor");
        verify(productoRepository, times(1)).save(any(Producto.class));
        verify(productoRepository, times(1)).findById(1L);
    }


    // ══════════════════════════════════════════════════════════════════════════
    // PASO 4: Pruebas de Error y @ParameterizedTest
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("buscarPorId: ID inexistente → lanza RuntimeException")
    void buscarPorId_noExistente_lanzaRuntimeException() {
        // ARRANGE: simular que el repositorio no encuentra el producto
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        RuntimeException excepcion = assertThrows(RuntimeException.class,
                () -> productoService.buscarPorId(99L),
                "Debe lanzar RuntimeException cuando el producto no existe");

        assertTrue(excepcion.getMessage().contains("99"),
                "El mensaje de error debe contener el ID buscado");
    }

    @ParameterizedTest(name = "nombre=''{0}'' debe lanzar IllegalArgumentException")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    @DisplayName("crear: nombre inválido (nulo, vacío, espacios) → lanza IllegalArgumentException")
    void crear_nombreInvalido_lanzaIllegalArgumentException(String nombre) {
        // ACT & ASSERT
        assertThrows(IllegalArgumentException.class,
                () -> productoService.crear(nombre, 100.0, 5),
                "Debe lanzar IllegalArgumentException para nombre: '" + nombre + "'");

        // El repositorio NO debe ser llamado cuando la validación falla
        verifyNoInteractions(productoRepository);
    }

    @ParameterizedTest(name = "precio={0} debe lanzar IllegalArgumentException")
    @ValueSource(doubles = {0.0, -1.0, -100.0, -0.01})
    @DisplayName("crear: precio inválido (cero o negativo) → lanza IllegalArgumentException")
    void crear_precioInvalido_lanzaIllegalArgumentException(double precio) {
        // ACT & ASSERT
        assertThrows(IllegalArgumentException.class,
                () -> productoService.crear("Producto", precio, 5),
                "Debe lanzar IllegalArgumentException para precio: " + precio);

        verifyNoInteractions(productoRepository);
    }

    @Test
    @DisplayName("crear: stock negativo → lanza IllegalArgumentException")
    void crear_stockNegativo_lanzaIllegalArgumentException() {
        // ACT & ASSERT
        assertThrows(IllegalArgumentException.class,
                () -> productoService.crear("Producto", 100.0, -1),
                "Debe lanzar IllegalArgumentException cuando el stock es negativo");

        verifyNoInteractions(productoRepository);
    }

    @Test
    @DisplayName("actualizarStock: stock negativo → lanza IllegalArgumentException")
    void actualizarStock_stockNegativo_lanzaIllegalArgumentException() {
        // ACT & ASSERT
        assertThrows(IllegalArgumentException.class,
                () -> productoService.actualizarStock(1L, -5),
                "Debe lanzar IllegalArgumentException cuando el nuevo stock es negativo");

        verifyNoInteractions(productoRepository);
    }

    @Test
    @DisplayName("eliminar: producto inexistente → lanza RuntimeException")
    void eliminar_productoInexistente_lanzaRuntimeException() {
        // ARRANGE
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(RuntimeException.class,
                () -> productoService.eliminar(999L),
                "Debe lanzar RuntimeException al intentar eliminar un producto inexistente");

        // deleteById NO debe ser llamado si el producto no existe
        verify(productoRepository, never()).deleteById(any());
    }


    // ══════════════════════════════════════════════════════════════════════════
    // PASO 5: ArgumentCaptor y Verificación Avanzada
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("crear: nombre con espacios → guarda nombre normalizado (sin espacios extra)")
    void crear_nombreConEspacios_guardaNombreNormalizado() {
        // ARRANGE: simular guardado retornando el mismo objeto con ID asignado
        when(productoRepository.save(any())).thenAnswer(inv -> {
            Producto p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        // ACT: crear con espacios al inicio y al final
        productoService.crear("  Laptop Pro  ", 1500.0, 5);

        // ASSERT con ArgumentCaptor: capturar el objeto pasado a save()
        verify(productoRepository).save(productoCaptor.capture());
        Producto capturado = productoCaptor.getValue();

        // Lo importante: verificar que el nombre fue normalizado con strip()
        assertEquals("Laptop Pro", capturado.getNombre(),
                "El nombre debe estar normalizado (sin espacios al inicio/final)");
        assertEquals(1500.0, capturado.getPrecio(),
                "El precio debe conservarse sin modificación");
        // Nota: el ID ya fue asignado por el thenAnswer, verificamos que no es null
        assertNotNull(capturado.getId(),
                "El objeto fue procesado por el repositorio correctamente");
    }

    @Test
    @DisplayName("eliminar: producto existente → llama findById y deleteById exactamente una vez")
    void eliminar_productoExistente_llamaDeleteById() {
        // ARRANGE
        Producto producto = new Producto(1L, "Teclado", 80.0, 20);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        doNothing().when(productoRepository).deleteById(1L);

        // ACT
        productoService.eliminar(1L);

        // ASSERT: verificar la secuencia de llamadas al repositorio
        verify(productoRepository, times(1)).findById(1L);
        verify(productoRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("crear: precio nulo → lanza IllegalArgumentException")
    void crear_precioNulo_lanzaIllegalArgumentException() {
        // ACT & ASSERT
        assertThrows(IllegalArgumentException.class,
                () -> productoService.crear("Producto", null, 5),
                "Debe lanzar IllegalArgumentException cuando el precio es null");

        verifyNoInteractions(productoRepository);
    }

    @Test
    @DisplayName("crear: stock cero → producto creado exitosamente (stock 0 es válido)")
    void crear_stockCero_retornaProductoCreado() {
        // ARRANGE: stock=0 es válido (no es negativo)
        Producto guardado = new Producto(1L, "Producto Agotado", 25.0, 0);
        when(productoRepository.save(any(Producto.class))).thenReturn(guardado);

        // ACT
        Producto resultado = productoService.crear("Producto Agotado", 25.0, 0);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(0, resultado.getStock(),
                "Stock cero es válido y debe guardarse correctamente");
        verify(productoRepository, times(1)).save(any(Producto.class));
    }
}