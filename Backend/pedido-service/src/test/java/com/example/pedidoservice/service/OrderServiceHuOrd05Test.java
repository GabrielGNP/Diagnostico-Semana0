package com.example.pedidoservice.service;

import com.example.pedidoservice.dto.OrderDto;
import com.example.pedidoservice.mapper.OrderMapper;
import com.example.pedidoservice.model.Order;
import com.example.pedidoservice.model.State;
import com.example.pedidoservice.repository.OrderJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TDD Test Suite for HU-ORD-05: Creación de nuevos pedidos con persistencia en PostgreSQL
 *
 * Story: Como Product Owner quiero crear pedidos nuevos y asegurar que cada entidad
 * se almacena correctamente en PostgreSQL con todos sus atributos.
 *
 * Requirements:
 * - FR-ORD-05-01: El sistema debe insertar el nuevo pedido en la tabla orders de PostgreSQL
 * - FR-ORD-05-02: Los campos requeridos son: name, description, idUser
 * - FR-ORD-05-03: El sistema debe asignar automáticamente: id (autogenerado), state (PENDING por defecto), active (true)
 * - FR-ORD-05-04: El endpoint POST /order/add debe retornar HTTP 200 OK con el pedido creado
 *
 * Non-Functional Requirements:
 * - NFR-ORD-05-01: La inserción debe ser transaccional (commit/rollback)
 * - NFR-ORD-05-02: La creación debe completarse en menos de 100ms
 *
 * PHASE: RED - Tests written first, expecting failures
 */
@DisplayName("HU-ORD-05: Creación de Pedidos con Persistencia PostgreSQL")
class OrderServiceHuOrd05Test {

    @Mock
    private OrderJpaRepository orderJpaRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("CA-01: Creación exitosa de pedido")
    class CreacionExitosaPedidoTests {

        @Test
        @DisplayName("Dado datos válidos (name, description, idUser), Cuando invoco createOrder, Entonces recibo pedido con ID asignado")
        void testCrearPedidoConDatosValidos() {
            // GIVEN: Datos válidos de entrada
            OrderDto inputDto = new OrderDto();
            inputDto.setName("Pedido Test");
            inputDto.setDescription("Descripción del pedido");
            inputDto.setIdUser(1);

            // Preparar entidad mapeada (sin ID, state, active)
            Order orderEntity = new Order();
            orderEntity.setName("Pedido Test");
            orderEntity.setDescription("Descripción del pedido");
            orderEntity.setIdUser(1);

            // Preparar entidad guardada (con ID autogenerado, state PENDING, active true)
            Order savedOrder = new Order();
            savedOrder.setId(100); // ID autogenerado por PostgreSQL
            savedOrder.setName("Pedido Test");
            savedOrder.setDescription("Descripción del pedido");
            savedOrder.setIdUser(1);
            savedOrder.setState(State.PROCESSING);
            savedOrder.setActive(true);

            // DTO de retorno
            OrderDto expectedDto = new OrderDto();
            expectedDto.setId(100);
            expectedDto.setName("Pedido Test");
            expectedDto.setDescription("Descripción del pedido");
            expectedDto.setIdUser(1);
            expectedDto.setState(State.PROCESSING);
            expectedDto.setActive(true);

            when(orderMapper.toEntity(inputDto)).thenReturn(orderEntity);
            when(orderJpaRepository.save(any(Order.class))).thenReturn(savedOrder);
            when(orderMapper.toDto(savedOrder)).thenReturn(expectedDto);

            // WHEN: Invoco createOrder
            OrderDto result = orderService.createOrder(inputDto);

            // THEN: Recibo pedido con ID asignado y valores por defecto
            assertNotNull(result, "El pedido creado no debe ser null");
            assertEquals(100, result.getId(), "El ID debe ser autogenerado");
            assertEquals("Pedido Test", result.getName(), "El nombre debe coincidir");
            assertEquals("Descripción del pedido", result.getDescription(), "La descripción debe coincidir");
            assertEquals(1, result.getIdUser(), "El idUser debe coincidir");
            assertEquals(State.PROCESSING, result.getState(), "El estado debe ser PENDING por defecto");
            assertTrue(result.isActive(), "El pedido debe estar activo por defecto");

            // Verificar que se llamó al repositorio
            verify(orderJpaRepository, times(1)).save(any(Order.class));
        }

        @Test
        @DisplayName("FR-ORD-05-03: El sistema asigna automáticamente state=PENDING y active=true")
        void testValoresPorDefectoAsignadosAutomaticamente() {
            // GIVEN: DTO con solo los campos requeridos
            OrderDto inputDto = new OrderDto();
            inputDto.setName("Pedido Mínimo");
            inputDto.setDescription("Solo campos requeridos");
            inputDto.setIdUser(2);

            Order orderEntity = new Order();
            orderEntity.setName("Pedido Mínimo");
            orderEntity.setDescription("Solo campos requeridos");
            orderEntity.setIdUser(2);

            Order savedOrder = new Order();
            savedOrder.setId(101);
            savedOrder.setName("Pedido Mínimo");
            savedOrder.setDescription("Solo campos requeridos");
            savedOrder.setIdUser(2);
            savedOrder.setState(State.PROCESSING);
            savedOrder.setActive(true);

            OrderDto expectedDto = new OrderDto();
            expectedDto.setId(101);
            expectedDto.setName("Pedido Mínimo");
            expectedDto.setDescription("Solo campos requeridos");
            expectedDto.setIdUser(2);
            expectedDto.setState(State.PROCESSING);
            expectedDto.setActive(true);

            when(orderMapper.toEntity(inputDto)).thenReturn(orderEntity);
            when(orderJpaRepository.save(any(Order.class))).thenReturn(savedOrder);
            when(orderMapper.toDto(savedOrder)).thenReturn(expectedDto);

            // WHEN
            OrderDto result = orderService.createOrder(inputDto);

            // THEN: Verificar que state y active fueron asignados automáticamente
            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            verify(orderJpaRepository).save(orderCaptor.capture());

            Order capturedOrder = orderCaptor.getValue();
            assertEquals(State.PROCESSING, capturedOrder.getState(), "El estado debe ser asignado como PENDING automáticamente");
            assertTrue(capturedOrder.isActive(), "Active debe ser asignado como true automáticamente");
        }

        @Test
        @DisplayName("NFR-ORD-05-02: La creación debe completarse en menos de 100ms")
        void testPerformanceCreacionPedido() {
            // GIVEN
            OrderDto inputDto = new OrderDto();
            inputDto.setName("Pedido Performance");
            inputDto.setDescription("Test de rendimiento");
            inputDto.setIdUser(3);

            Order orderEntity = new Order();
            Order savedOrder = new Order();
            savedOrder.setId(102);
            savedOrder.setState(State.PROCESSING);
            savedOrder.setActive(true);

            OrderDto expectedDto = new OrderDto();
            expectedDto.setId(102);

            when(orderMapper.toEntity(any())).thenReturn(orderEntity);
            when(orderJpaRepository.save(any(Order.class))).thenReturn(savedOrder);
            when(orderMapper.toDto(any())).thenReturn(expectedDto);

            // WHEN: Medir tiempo de ejecución
            long startTime = System.currentTimeMillis();
            orderService.createOrder(inputDto);
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // THEN: Debe completarse en menos de 100ms
            assertTrue(duration < 100, "La creación debe completarse en menos de 100ms. Tiempo actual: " + duration + "ms");
        }
    }

    @Nested
    @DisplayName("CA-02: Campos requeridos faltantes")
    class CamposRequeridosFaltantesTests {

        @Test
        @DisplayName("Dado body sin campo 'name', Cuando invoco createOrder, Entonces lanza IllegalArgumentException")
        void testCrearPedidoSinCampoName() {
            // GIVEN: DTO sin el campo name (requerido)
            OrderDto inputDto = new OrderDto();
            inputDto.setDescription("Descripción sin nombre");
            inputDto.setIdUser(1);

            // WHEN & THEN: Debe lanzar excepción
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder(inputDto),
                "Debe lanzar IllegalArgumentException cuando falta el campo 'name'"
            );

            assertTrue(exception.getMessage().contains("name") || exception.getMessage().contains("requerido"),
                "El mensaje de error debe indicar que 'name' es requerido");
        }

        @Test
        @DisplayName("Dado body sin campo 'description', Cuando invoco createOrder, Entonces lanza IllegalArgumentException")
        void testCrearPedidoSinCampoDescription() {
            // GIVEN: DTO sin el campo description (requerido)
            OrderDto inputDto = new OrderDto();
            inputDto.setName("Pedido sin descripción");
            inputDto.setIdUser(1);

            // WHEN & THEN
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder(inputDto),
                "Debe lanzar IllegalArgumentException cuando falta el campo 'description'"
            );

            assertTrue(exception.getMessage().contains("description") || exception.getMessage().contains("requerido"),
                "El mensaje de error debe indicar que 'description' es requerido");
        }

        @Test
        @DisplayName("Dado body sin campo 'idUser', Cuando invoco createOrder, Entonces lanza IllegalArgumentException")
        void testCrearPedidoSinCampoIdUser() {
            // GIVEN: DTO sin el campo idUser (requerido)
            OrderDto inputDto = new OrderDto();
            inputDto.setName("Pedido sin idUser");
            inputDto.setDescription("Descripción");
            // idUser no se setea (valor por defecto 0 en int primitivo)

            // WHEN & THEN
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder(inputDto),
                "Debe lanzar IllegalArgumentException cuando falta el campo 'idUser'"
            );

            assertTrue(exception.getMessage().contains("idUser") || exception.getMessage().contains("usuario") || exception.getMessage().contains("requerido"),
                "El mensaje de error debe indicar que 'idUser' es requerido");
        }
    }

    @Nested
    @DisplayName("CA-03: Tipo de dato inválido")
    class TipoDatoInvalidoTests {

        @Test
        @DisplayName("Dado idUser con valor negativo (inválido), Cuando invoco createOrder, Entonces lanza IllegalArgumentException")
        void testCrearPedidoConIdUserNegativo() {
            // GIVEN: DTO con idUser inválido (negativo)
            OrderDto inputDto = new OrderDto();
            inputDto.setName("Pedido con idUser negativo");
            inputDto.setDescription("Descripción");
            inputDto.setIdUser(-1); // Valor inválido

            // WHEN & THEN
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder(inputDto),
                "Debe lanzar IllegalArgumentException cuando idUser es negativo"
            );

            assertTrue(exception.getMessage().contains("idUser") || exception.getMessage().contains("válido") || exception.getMessage().contains("positivo"),
                "El mensaje de error debe indicar que 'idUser' debe ser un valor positivo válido");
        }

        @Test
        @DisplayName("Dado idUser con valor cero (inválido), Cuando invoco createOrder, Entonces lanza IllegalArgumentException")
        void testCrearPedidoConIdUserCero() {
            // GIVEN: DTO con idUser = 0 (inválido, no existe usuario con ID 0)
            OrderDto inputDto = new OrderDto();
            inputDto.setName("Pedido con idUser cero");
            inputDto.setDescription("Descripción");
            inputDto.setIdUser(0);

            // WHEN & THEN
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder(inputDto),
                "Debe lanzar IllegalArgumentException cuando idUser es cero"
            );

            assertTrue(exception.getMessage().contains("idUser") || exception.getMessage().contains("válido") || exception.getMessage().contains("positivo"),
                "El mensaje de error debe indicar que 'idUser' debe ser mayor que cero");
        }

        @Test
        @DisplayName("Dado name vacío (inválido), Cuando invoco createOrder, Entonces lanza IllegalArgumentException")
        void testCrearPedidoConNameVacio() {
            // GIVEN: DTO con name vacío
            OrderDto inputDto = new OrderDto();
            inputDto.setName(""); // Vacío
            inputDto.setDescription("Descripción válida");
            inputDto.setIdUser(1);

            // WHEN & THEN
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder(inputDto),
                "Debe lanzar IllegalArgumentException cuando name está vacío"
            );

            assertTrue(exception.getMessage().contains("name") || exception.getMessage().contains("vacío") || exception.getMessage().contains("blank"),
                "El mensaje de error debe indicar que 'name' no puede estar vacío");
        }

        @Test
        @DisplayName("Dado name solo con espacios (inválido), Cuando invoco createOrder, Entonces lanza IllegalArgumentException")
        void testCrearPedidoConNameSoloEspacios() {
            // GIVEN: DTO con name solo espacios
            OrderDto inputDto = new OrderDto();
            inputDto.setName("   "); // Solo espacios
            inputDto.setDescription("Descripción válida");
            inputDto.setIdUser(1);

            // WHEN & THEN
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder(inputDto),
                "Debe lanzar IllegalArgumentException cuando name solo contiene espacios"
            );

            assertTrue(exception.getMessage().contains("name") || exception.getMessage().contains("vacío") || exception.getMessage().contains("blank"),
                "El mensaje de error debe indicar que 'name' no puede estar vacío o ser solo espacios");
        }
    }

    @Nested
    @DisplayName("FR-ORD-05-01: Inserción en PostgreSQL")
    class InsercionPostgreSQLTests {

        @Test
        @DisplayName("Verificar que save() del repositorio JPA es invocado correctamente")
        void testInvocacionRepositorioJPA() {
            // GIVEN
            OrderDto inputDto = new OrderDto();
            inputDto.setName("Pedido DB");
            inputDto.setDescription("Test de inserción DB");
            inputDto.setIdUser(1);

            Order orderEntity = new Order();
            orderEntity.setName("Pedido DB");
            orderEntity.setDescription("Test de inserción DB");
            orderEntity.setIdUser(1);

            Order savedOrder = new Order();
            savedOrder.setId(200);
            savedOrder.setName("Pedido DB");
            savedOrder.setDescription("Test de inserción DB");
            savedOrder.setIdUser(1);
            savedOrder.setState(State.PROCESSING);
            savedOrder.setActive(true);

            OrderDto resultDto = new OrderDto();
            resultDto.setId(200);

            when(orderMapper.toEntity(inputDto)).thenReturn(orderEntity);
            when(orderJpaRepository.save(any(Order.class))).thenReturn(savedOrder);
            when(orderMapper.toDto(savedOrder)).thenReturn(resultDto);

            // WHEN
            orderService.createOrder(inputDto);

            // THEN: Verificar que se invocó save() exactamente una vez con la entidad correcta
            verify(orderJpaRepository, times(1)).save(argThat(order ->
                order.getName().equals("Pedido DB") &&
                order.getDescription().equals("Test de inserción DB") &&
                order.getIdUser() == 1 &&
                order.getState() == State.PROCESSING &&
                order.isActive()
            ));
        }
    }
}
