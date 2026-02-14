package com.example.pedidoservice;

import com.example.pedidoservice.dto.OrderDto;
import com.example.pedidoservice.dto.OrderWithUserDto;
import com.example.pedidoservice.mapper.OrderMapper;
import com.example.pedidoservice.messaging.UserResponse;
import com.example.pedidoservice.messaging.UserServiceConsumer;
import com.example.pedidoservice.messaging.UserServiceProducer;
import com.example.pedidoservice.model.Order;
import com.example.pedidoservice.model.State;
import com.example.pedidoservice.repository.OrderRepository;
import com.example.pedidoservice.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private OrderMapper orderMapper;

	@Mock
	private UserServiceProducer userServiceProducer;

	@Mock
	private UserServiceConsumer userServiceConsumer;

	@InjectMocks
	private OrderService orderService;

	@Nested
	class CreateOrderTests {

		private OrderDto testOrderDto;
		private Order testOrder;

		@BeforeEach
		void setUp() {
			// Preparar datos de prueba
			testOrderDto = new OrderDto(0, "Laptop", "Gaming Laptop", 1, null, false);
			testOrder = new Order(0, "Laptop", "Gaming Laptop", 1, State.PROCESSING, true);
		}

		// Test 1: Creación Exitosa
		@Test
		void testCreateOrder_Success() {
			// Arrange
			Order savedOrder = new Order(1, "Laptop", "Gaming Laptop", 1, State.PROCESSING, true);
			OrderDto expectedOrderDto = new OrderDto(1, "Laptop", "Gaming Laptop", 1, State.PROCESSING, true);

			when(orderRepository.findAll()).thenReturn(new ArrayList<>());
			when(orderMapper.toEntity(testOrderDto)).thenReturn(testOrder);
			when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
			when(orderMapper.toDto(savedOrder)).thenReturn(expectedOrderDto);

			// Act
			OrderDto result = orderService.createOrder(testOrderDto);

			// Assert
			assertNotNull(result);
			assertEquals(1, result.getId());
			assertEquals("Laptop", result.getName());
			assertEquals("Gaming Laptop", result.getDescription());
			assertEquals(1, result.getIdUser());
			assertEquals(State.PROCESSING, result.getState());
			assertTrue(result.isActive());
			verify(orderRepository, times(1)).save(any(Order.class));
		}

		// Test 2: Asignación de ID Automático - Sin órdenes previas
		@Test
		void testCreateOrder_IdAssignment_NoExistingOrders() {
			// Arrange
			Order savedOrder = new Order(1, "Laptop", "Gaming Laptop", 1, State.PROCESSING, true);
			OrderDto expectedOrderDto = new OrderDto(1, "Laptop", "Gaming Laptop", 1, State.PROCESSING, true);

			when(orderRepository.findAll()).thenReturn(new ArrayList<>());
			when(orderMapper.toEntity(testOrderDto)).thenReturn(testOrder);
			when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
			when(orderMapper.toDto(savedOrder)).thenReturn(expectedOrderDto);

			// Act
			orderService.createOrder(testOrderDto);

			// Assert
			ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
			verify(orderRepository).save(orderCaptor.capture());
			Order capturedOrder = orderCaptor.getValue();
			assertEquals(1, capturedOrder.getId());
		}

		// Test 3: Asignación de ID Automático - Con órdenes previas
		@Test
		void testCreateOrder_IdAssignment_WithExistingOrders() {
			// Arrange
			List<Order> existingOrders = new ArrayList<>();
			existingOrders.add(new Order(3, "Monitor", "4K Monitor", 1, State.PROCESSING, true));
			existingOrders.add(new Order(7, "Keyboard", "Mechanical Keyboard", 2, State.PROCESSING, true));
			existingOrders.add(new Order(5, "Mouse", "Wireless Mouse", 1, State.PROCESSING, true));

			Order savedOrder = new Order(8, "Laptop", "Gaming Laptop", 1, State.PROCESSING, true);
			OrderDto expectedOrderDto = new OrderDto(8, "Laptop", "Gaming Laptop", 1, State.PROCESSING, true);

			when(orderRepository.findAll()).thenReturn(existingOrders);
			when(orderMapper.toEntity(testOrderDto)).thenReturn(testOrder);
			when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
			when(orderMapper.toDto(savedOrder)).thenReturn(expectedOrderDto);

			// Act
			orderService.createOrder(testOrderDto);

			// Assert
			ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
			verify(orderRepository).save(orderCaptor.capture());
			Order capturedOrder = orderCaptor.getValue();
			assertEquals(8, capturedOrder.getId()); // maxId (7) + 1 = 8
		}

		// Test 4: Mapeo Correcto
		@Test
		void testCreateOrder_CorrectMapping() {
			// Arrange
			OrderDto inputDto = new OrderDto(0, "Phone", "Smartphone", 5, null, false);
			Order mappedOrder = new Order(0, "Phone", "Smartphone", 5, State.PROCESSING, false);
			Order savedOrder = new Order(1, "Phone", "Smartphone", 5, State.PROCESSING, true);
			OrderDto expectedOutput = new OrderDto(1, "Phone", "Smartphone", 5, State.PROCESSING, true);

			when(orderRepository.findAll()).thenReturn(new ArrayList<>());
			when(orderMapper.toEntity(inputDto)).thenReturn(mappedOrder);
			when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
			when(orderMapper.toDto(savedOrder)).thenReturn(expectedOutput);

			// Act
			OrderDto result = orderService.createOrder(inputDto);

			// Assert
			// Verificar que toEntity fue invocado con el OrderDto correctamente
			verify(orderMapper).toEntity(inputDto);
			assertEquals("Phone", result.getName());
			assertEquals("Smartphone", result.getDescription());
			assertEquals(5, result.getIdUser());
			// Verificar que toDto fue invocado con el Order persistido
			verify(orderMapper).toDto(savedOrder);
		}

		// Test 5: Persistencia
		@Test
		void testCreateOrder_Persistence() {
			// Arrange
			Order savedOrder = new Order(1, "Tablet", "iPad Pro", 3, State.PROCESSING, true);
			OrderDto expectedOrderDto = new OrderDto(1, "Tablet", "iPad Pro", 3, State.PROCESSING, true);

			when(orderRepository.findAll()).thenReturn(new ArrayList<>());
			when(orderMapper.toEntity(testOrderDto)).thenReturn(testOrder);
			when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
			when(orderMapper.toDto(savedOrder)).thenReturn(expectedOrderDto);

			// Act
			orderService.createOrder(testOrderDto);

			// Assert
			// Verificar que save() fue invocado exactamente una vez
			verify(orderRepository, times(1)).save(any(Order.class));
			
			// Capturar el Order que se pasó a save() y verificar sus propiedades
			ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
			verify(orderRepository).save(orderCaptor.capture());
			Order persistedOrder = orderCaptor.getValue();

			// Verificar que la orden tiene el estado PROCESSING y está activa
			assertEquals(State.PROCESSING, persistedOrder.getState());
			assertTrue(persistedOrder.isActive());
			assertEquals("Laptop", persistedOrder.getName());
			assertEquals(1, persistedOrder.getIdUser());
		}
	}

	@Nested
	class DeleteOrderTests {
		
		// Test 1: Eliminación Exitosa
		@Test
		void testDeleteOrder_Success() {
			// Arrange
			int orderId = 5;

			// Act
			orderService.deleteOrder(orderId);

			// Assert
			verify(orderRepository, times(1)).deleteById(orderId);
		}

		// Test 2: Parámetro Correcto
		@Test
		void testDeleteOrder_CorrectParameter() {
			// Arrange
			int orderId = 42;
			ArgumentCaptor<Integer> idCaptor = ArgumentCaptor.forClass(Integer.class);

			// Act
			orderService.deleteOrder(orderId);

			// Assert
			verify(orderRepository).deleteById(idCaptor.capture());
			assertEquals(42, idCaptor.getValue());
		}

		// Test 3: Excepción en Repositorio
		@Test
		void testDeleteOrder_RepositoryThrowsException() {
			// Arrange
			int orderId = 10;
			doThrow(new RuntimeException("Database connection error")).when(orderRepository).deleteById(orderId);

			// Act & Assert
			assertThrows(RuntimeException.class, () -> orderService.deleteOrder(orderId));
			verify(orderRepository, times(1)).deleteById(orderId);
		}

		// Test 4: ID Inválido
		@Test
		void testDeleteOrder_InvalidId() {
			// Arrange
			int invalidId = -1;

			// Act
			orderService.deleteOrder(invalidId);

			// Assert
			// El servicio no valida IDs negativos, solo delega al repositorio
			verify(orderRepository, times(1)).deleteById(invalidId);
		}
	}

	@Nested
	class ChangeStateOrderTests {
		
		// Test 1: Cambio de Estado Exitoso
		@Test
		void testChangeStateOrder_Success() {
			// Arrange
			int orderId = 5;
			State newState = State.TRAVELING_TO_YOUR_HOUSE;
			Order existingOrder = new Order(5, "Laptop", "Gaming Laptop", 1, State.PROCESSING, true);
			Order updatedOrder = new Order(5, "Laptop", "Gaming Laptop", 1, State.TRAVELING_TO_YOUR_HOUSE, true);
			OrderDto expectedOrderDto = new OrderDto(5, "Laptop", "Gaming Laptop", 1, State.TRAVELING_TO_YOUR_HOUSE, true);

			when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(existingOrder));
			when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder);
			when(orderMapper.toDto(updatedOrder)).thenReturn(expectedOrderDto);

			// Act
			OrderDto result = orderService.changeStateOrder(orderId, newState);

			// Assert
			assertNotNull(result);
			assertEquals(State.TRAVELING_TO_YOUR_HOUSE, result.getState());
			assertEquals(5, result.getId());
			assertEquals("Laptop", result.getName());
			verify(orderRepository, times(1)).findById(orderId);
			verify(orderRepository, times(1)).save(any(Order.class));
		}

		// Test 2: Orden No Encontrada
		@Test
		void testChangeStateOrder_OrderNotFound() {
			// Arrange
			int orderId = 999;
			State newState = State.DELIVERED;

			when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.empty());

			// Act
			OrderDto result = orderService.changeStateOrder(orderId, newState);

			// Assert
			assertNull(result);
			verify(orderRepository, times(1)).findById(orderId);
			verify(orderRepository, never()).save(any(Order.class));
		}

		// Test 3: Persistencia del Nuevo Estado
		@Test
		void testChangeStateOrder_PersistenceOfNewState() {
			// Arrange
			int orderId = 3;
			State newState = State.IN_WAREHOUSE;
			Order existingOrder = new Order(3, "Monitor", "4K Monitor", 2, State.PROCESSING, true);
			Order updatedOrder = new Order(3, "Monitor", "4K Monitor", 2, State.IN_WAREHOUSE, true);
			OrderDto expectedOrderDto = new OrderDto(3, "Monitor", "4K Monitor", 2, State.IN_WAREHOUSE, true);

			when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(existingOrder));
			when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder);
			when(orderMapper.toDto(updatedOrder)).thenReturn(expectedOrderDto);

			// Act
			orderService.changeStateOrder(orderId, newState);

			// Assert
			ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
			verify(orderRepository).save(orderCaptor.capture());
			Order persistedOrder = orderCaptor.getValue();
			assertEquals(State.IN_WAREHOUSE, persistedOrder.getState());
			assertEquals(3, persistedOrder.getId());
		}

		// Test 4: Mapeo de Resultado
		@Test
		void testChangeStateOrder_CorrectMapping() {
			// Arrange
			int orderId = 7;
			State newState = State.ON_THE_STREET;
			Order existingOrder = new Order(7, "Phone", "Smartphone", 1, State.TRAVELING_TO_YOUR_HOUSE, true);
			Order updatedOrder = new Order(7, "Phone", "Smartphone", 1, State.ON_THE_STREET, true);
			OrderDto expectedOrderDto = new OrderDto(7, "Phone", "Smartphone", 1, State.ON_THE_STREET, true);

			when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(existingOrder));
			when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder);
			when(orderMapper.toDto(updatedOrder)).thenReturn(expectedOrderDto);

			// Act
			OrderDto result = orderService.changeStateOrder(orderId, newState);

			// Assert
			verify(orderMapper).toDto(updatedOrder);
			assertEquals("Phone", result.getName());
			assertEquals("Smartphone", result.getDescription());
			assertEquals(1, result.getIdUser());
			assertEquals(State.ON_THE_STREET, result.getState());
		}

		// Test 5: Transición de Estados Múltiples
		@Test
		void testChangeStateOrder_MultipleStateTransitions() {
			// Arrange
			int orderId = 1;
			Order order = new Order(1, "Laptop", "Gaming Laptop", 1, State.PROCESSING, true);

			// Primera transición: PROCESSING -> TRAVELING_TO_WAREHOUSE
			State firstState = State.TRAVELING_TO_WAREHOUSE;
			Order updatedOrder1 = new Order(1, "Laptop", "Gaming Laptop", 1, State.TRAVELING_TO_WAREHOUSE, true);
			OrderDto dto1 = new OrderDto(1, "Laptop", "Gaming Laptop", 1, State.TRAVELING_TO_WAREHOUSE, true);

			when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(order));
			when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder1);
			when(orderMapper.toDto(updatedOrder1)).thenReturn(dto1);

			// Act - Primera transición
			OrderDto result1 = orderService.changeStateOrder(orderId, firstState);

			// Assert - Primera transición
			assertNotNull(result1);
			assertEquals(State.TRAVELING_TO_WAREHOUSE, result1.getState());

			// Prepare para segunda transición: TRAVELING_TO_WAREHOUSE -> DELIVERED
			State secondState = State.DELIVERED;
			Order updatedOrder2 = new Order(1, "Laptop", "Gaming Laptop", 1, State.DELIVERED, true);
			OrderDto dto2 = new OrderDto(1, "Laptop", "Gaming Laptop", 1, State.DELIVERED, true);

			when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(updatedOrder1));
			when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder2);
			when(orderMapper.toDto(updatedOrder2)).thenReturn(dto2);

			// Act - Segunda transición
			OrderDto result2 = orderService.changeStateOrder(orderId, secondState);

			// Assert - Segunda transición
			assertNotNull(result2);
			assertEquals(State.DELIVERED, result2.getState());
			verify(orderRepository, times(2)).findById(orderId);
			verify(orderRepository, times(2)).save(any(Order.class));
		}
	}

	@Nested
	class ListOrdersByIdUserTests {
		
		// Test 1: Listado Exitoso
		@Test
		void testListOrdersByIdUser_Success() {
			// Arrange
			int idUser = 1;
			List<Order> orders = new ArrayList<>();
			orders.add(new Order(1, "Laptop", "Gaming Laptop", 1, State.PROCESSING, true));
			orders.add(new Order(2, "Monitor", "4K Monitor", 1, State.DELIVERED, true));

			List<OrderDto> expectedDtos = new ArrayList<>();
			expectedDtos.add(new OrderDto(1, "Laptop", "Gaming Laptop", 1, State.PROCESSING, true));
			expectedDtos.add(new OrderDto(2, "Monitor", "4K Monitor", 1, State.DELIVERED, true));

			when(orderRepository.findByUserId(idUser)).thenReturn(orders);
			when(orderMapper.toDto(orders.get(0))).thenReturn(expectedDtos.get(0));
			when(orderMapper.toDto(orders.get(1))).thenReturn(expectedDtos.get(1));

			// Act
			List<OrderDto> result = orderService.listOrdersByIdUser(idUser);

			// Assert
			assertNotNull(result);
			assertEquals(2, result.size());
			assertEquals("Laptop", result.get(0).getName());
			assertEquals("Monitor", result.get(1).getName());
			verify(orderRepository, times(1)).findByUserId(idUser);
		}

		// Test 2: Usuario sin Órdenes
		@Test
		void testListOrdersByIdUser_NoOrders() {
			// Arrange
			int idUser = 999;
			when(orderRepository.findByUserId(idUser)).thenReturn(new ArrayList<>());

			// Act
			List<OrderDto> result = orderService.listOrdersByIdUser(idUser);

			// Assert
			assertNotNull(result);
			assertTrue(result.isEmpty());
			assertEquals(0, result.size());
			verify(orderRepository, times(1)).findByUserId(idUser);
		}

		// Test 3: Mapeo Correcto
		@Test
		void testListOrdersByIdUser_CorrectMapping() {
			// Arrange
			int idUser = 2;
			Order order = new Order(5, "Phone", "Smartphone", 2, State.TRAVELING_TO_YOUR_HOUSE, true);
			OrderDto expectedDto = new OrderDto(5, "Phone", "Smartphone", 2, State.TRAVELING_TO_YOUR_HOUSE, true);

			when(orderRepository.findByUserId(idUser)).thenReturn(java.util.Collections.singletonList(order));
			when(orderMapper.toDto(order)).thenReturn(expectedDto);

			// Act
			List<OrderDto> result = orderService.listOrdersByIdUser(idUser);

			// Assert
			verify(orderMapper).toDto(order);
			assertEquals(1, result.size());
			assertEquals("Phone", result.get(0).getName());
			assertEquals("Smartphone", result.get(0).getDescription());
			assertEquals(5, result.get(0).getId());
		}

		// Test 4: Múltiples Órdenes
		@Test
		void testListOrdersByIdUser_MultipleOrders() {
			// Arrange
			int idUser = 3;
			List<Order> orders = new ArrayList<>();
			orders.add(new Order(10, "Tablet", "iPad", 3, State.PROCESSING, true));
			orders.add(new Order(11, "Keyboard", "Mechanical", 3, State.IN_WAREHOUSE, true));
			orders.add(new Order(12, "Mouse", "Wireless", 3, State.ON_THE_STREET, true));
			orders.add(new Order(13, "Monitor", "4K", 3, State.DELIVERED, true));

			List<OrderDto> expectedDtos = new ArrayList<>();
			expectedDtos.add(new OrderDto(10, "Tablet", "iPad", 3, State.PROCESSING, true));
			expectedDtos.add(new OrderDto(11, "Keyboard", "Mechanical", 3, State.IN_WAREHOUSE, true));
			expectedDtos.add(new OrderDto(12, "Mouse", "Wireless", 3, State.ON_THE_STREET, true));
			expectedDtos.add(new OrderDto(13, "Monitor", "4K", 3, State.DELIVERED, true));

			when(orderRepository.findByUserId(idUser)).thenReturn(orders);
			for (int i = 0; i < orders.size(); i++) {
				when(orderMapper.toDto(orders.get(i))).thenReturn(expectedDtos.get(i));
			}

			// Act
			List<OrderDto> result = orderService.listOrdersByIdUser(idUser);

			// Assert
			assertNotNull(result);
			assertEquals(4, result.size());
			assertEquals(10, result.get(0).getId());
			assertEquals(11, result.get(1).getId());
			assertEquals(12, result.get(2).getId());
			assertEquals(13, result.get(3).getId());
			verify(orderRepository, times(1)).findByUserId(idUser);
			verify(orderMapper, times(4)).toDto(any(Order.class));
		}

		// Test 5: Consistencia de Datos
		@Test
		void testListOrdersByIdUser_DataConsistency() {
			// Arrange
			int idUser = 5;
			List<Order> orders = new ArrayList<>();
			orders.add(new Order(100, "Product A", "Description A", 5, State.PROCESSING, true));
			orders.add(new Order(101, "Product B", "Description B", 5, State.TRAVELING_TO_WAREHOUSE, false));

			List<OrderDto> expectedDtos = new ArrayList<>();
			expectedDtos.add(new OrderDto(100, "Product A", "Description A", 5, State.PROCESSING, true));
			expectedDtos.add(new OrderDto(101, "Product B", "Description B", 5, State.TRAVELING_TO_WAREHOUSE, false));

			when(orderRepository.findByUserId(idUser)).thenReturn(orders);
			when(orderMapper.toDto(orders.get(0))).thenReturn(expectedDtos.get(0));
			when(orderMapper.toDto(orders.get(1))).thenReturn(expectedDtos.get(1));

			// Act
			List<OrderDto> result = orderService.listOrdersByIdUser(idUser);

			// Assert
			assertNotNull(result);
			assertEquals(2, result.size());

			// Validar primer orden
			assertEquals(100, result.get(0).getId());
			assertEquals("Product A", result.get(0).getName());
			assertEquals("Description A", result.get(0).getDescription());
			assertEquals(5, result.get(0).getIdUser());
			assertEquals(State.PROCESSING, result.get(0).getState());
			assertTrue(result.get(0).isActive());

			// Validar segunda orden
			assertEquals(101, result.get(1).getId());
			assertEquals("Product B", result.get(1).getName());
			assertEquals("Description B", result.get(1).getDescription());
			assertEquals(5, result.get(1).getIdUser());
			assertEquals(State.TRAVELING_TO_WAREHOUSE, result.get(1).getState());
			assertFalse(result.get(1).isActive());
		}
	}

	@Nested
	class GetOrderWithUserInfoTests {
		
		// Test 1: Obtención Exitosa
		@Test
		void testGetOrderWithUserInfo_Success() {
			// Arrange
			int orderId = 1;
			int idUser = 5;
			Order order = new Order(1, "Laptop", "Gaming Laptop", idUser, State.PROCESSING, true);
			OrderDto orderDto = new OrderDto(1, "Laptop", "Gaming Laptop", idUser, State.PROCESSING, true);
			UserResponse userResponse = new UserResponse(idUser, "Juan", "juan@email.com", true);

			when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(order));
			when(orderMapper.toDto(order)).thenReturn(orderDto);
			when(userServiceConsumer.getUserResponse(idUser, 3000)).thenReturn(userResponse);

			// Act
			OrderWithUserDto result = orderService.getOrderWithUserInfo(orderId);

			// Assert
			assertNotNull(result);
			assertEquals(1, result.getId());
			assertEquals("Laptop", result.getName());
			assertEquals(idUser, result.getIdUser());
			assertNotNull(result.getUserResponse());
			assertEquals("Juan", result.getUserResponse().getName());
			verify(userServiceProducer, times(1)).requestUserInfo(idUser);
			verify(userServiceConsumer, times(1)).getUserResponse(idUser, 3000);
		}

		// Test 2: Orden No Encontrada
		@Test
		void testGetOrderWithUserInfo_OrderNotFound() {
			// Arrange
			int orderId = 999;

			when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.empty());

			// Act
			OrderWithUserDto result = orderService.getOrderWithUserInfo(orderId);

			// Assert
			assertNull(result);
			verify(userServiceProducer, never()).requestUserInfo(anyInt());
			verify(userServiceConsumer, never()).getUserResponse(anyInt(), anyLong());
		}

		// Test 3: Fallo en Comunicación RabbitMQ
		@Test
		void testGetOrderWithUserInfo_RabbitMQFailure() {
			// Arrange
			int orderId = 2;
			int idUser = 3;
			Order order = new Order(2, "Monitor", "4K Monitor", idUser, State.DELIVERED, true);
			OrderDto orderDto = new OrderDto(2, "Monitor", "4K Monitor", idUser, State.DELIVERED, true);

			when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(order));
			when(orderMapper.toDto(order)).thenReturn(orderDto);
			doThrow(new RuntimeException("RabbitMQ connection error"))
					.when(userServiceProducer).requestUserInfo(idUser);

			// Act
			OrderWithUserDto result = orderService.getOrderWithUserInfo(orderId);

			// Assert
			assertNotNull(result);
			assertEquals(2, result.getId());
			assertEquals("Monitor", result.getName());
			// userResponse debe ser null cuando falla la comunicación
			assertNull(result.getUserResponse());
			verify(userServiceProducer, times(1)).requestUserInfo(idUser);
		}

		// Test 4: Timeout en RabbitMQ
		@Test
		void testGetOrderWithUserInfo_RabbitMQTimeout() {
			// Arrange
			int orderId = 3;
			int idUser = 7;
			Order order = new Order(3, "Keyboard", "Mechanical", idUser, State.IN_WAREHOUSE, true);
			OrderDto orderDto = new OrderDto(3, "Keyboard", "Mechanical", idUser, State.IN_WAREHOUSE, true);

			when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(order));
			when(orderMapper.toDto(order)).thenReturn(orderDto);
			doNothing().when(userServiceProducer).requestUserInfo(idUser);
			// Simular timeout retornando null (comportamiento real tras timeout)
			when(userServiceConsumer.getUserResponse(idUser, 3000)).thenReturn(null);

			// Act
			OrderWithUserDto result = orderService.getOrderWithUserInfo(orderId);

			// Assert
			assertNotNull(result);
			assertEquals("Keyboard", result.getName());
			// userResponse debe ser null cuando hay timeout
			assertNull(result.getUserResponse());
			verify(userServiceProducer, times(1)).requestUserInfo(idUser);
			verify(userServiceConsumer, times(1)).getUserResponse(idUser, 3000);
		}

		// Test 5: Mapeo Correcto
		@Test
		void testGetOrderWithUserInfo_CorrectMapping() {
			// Arrange
			int orderId = 4;
			int idUser = 10;
			Order order = new Order(4, "Mouse", "Wireless", idUser, State.ON_THE_STREET, false);
			OrderDto orderDto = new OrderDto(4, "Mouse", "Wireless", idUser, State.ON_THE_STREET, false);
			UserResponse userResponse = new UserResponse(idUser, "Maria", "maria@email.com", true);

			when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(order));
			when(orderMapper.toDto(order)).thenReturn(orderDto);
			when(userServiceConsumer.getUserResponse(idUser, 3000)).thenReturn(userResponse);

			// Act
			OrderWithUserDto result = orderService.getOrderWithUserInfo(orderId);

			// Assert
			assertNotNull(result);
			// Validar todos los campos de la orden
			assertEquals(4, result.getId());
			assertEquals("Mouse", result.getName());
			assertEquals("Wireless", result.getDescription());
			assertEquals(idUser, result.getIdUser());
			assertEquals(State.ON_THE_STREET, result.getState());
			assertFalse(result.isActive());
			// Validar datos del usuario
			assertEquals("Maria", result.getUserResponse().getName());
			assertEquals("maria@email.com", result.getUserResponse().getMail());
		}

		// Test 6: Manejo de Excepciones
		@Test
		void testGetOrderWithUserInfo_ExceptionHandling() {
			// Arrange
			int orderId = 5;
			int idUser = 8;
			Order order = new Order(5, "Tablet", "iPad Pro", idUser, State.TRAVELING_TO_WAREHOUSE, true);
			OrderDto orderDto = new OrderDto(5, "Tablet", "iPad Pro", idUser, State.TRAVELING_TO_WAREHOUSE, true);

			when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(order));
			when(orderMapper.toDto(order)).thenReturn(orderDto);
			doNothing().when(userServiceProducer).requestUserInfo(idUser);
			when(userServiceConsumer.getUserResponse(idUser, 3000)).thenThrow(new IllegalStateException("Invalid user state"));

			// Act (debe no lanzar excepción)
			OrderWithUserDto result = orderService.getOrderWithUserInfo(orderId);

			// Assert
			assertNotNull(result);
			assertEquals(5, result.getId());
			assertEquals("Tablet", result.getName());
			// La excepción fue capturada, userResponse es null
			assertNull(result.getUserResponse());
			// Verificar que el flujo continuó sin lanzar excepción
			verify(userServiceProducer, times(1)).requestUserInfo(idUser);
		}
	}

	@Nested
	class ListAllOrdersTests {
		
		// Test 1: Listado Exitoso
		@Test
		void testListAllOrders_Success() {
			// Arrange
			List<Order> orders = new ArrayList<>();
			orders.add(new Order(1, "Laptop", "Gaming Laptop", 1, State.PROCESSING, true));
			orders.add(new Order(2, "Monitor", "4K Monitor", 2, State.DELIVERED, true));

			List<OrderDto> expectedDtos = new ArrayList<>();
			expectedDtos.add(new OrderDto(1, "Laptop", "Gaming Laptop", 1, State.PROCESSING, true));
			expectedDtos.add(new OrderDto(2, "Monitor", "4K Monitor", 2, State.DELIVERED, true));

			when(orderRepository.findAll()).thenReturn(orders);
			when(orderMapper.toDto(orders.get(0))).thenReturn(expectedDtos.get(0));
			when(orderMapper.toDto(orders.get(1))).thenReturn(expectedDtos.get(1));

			// Act
			List<OrderDto> result = orderService.listAllOrders();

			// Assert
			assertNotNull(result);
			assertEquals(2, result.size());
			assertEquals("Laptop", result.get(0).getName());
			assertEquals("Monitor", result.get(1).getName());
			verify(orderRepository, times(1)).findAll();
		}

		// Test 2: Sin Órdenes
		@Test
		void testListAllOrders_NoOrders() {
			// Arrange
			when(orderRepository.findAll()).thenReturn(new ArrayList<>());

			// Act
			List<OrderDto> result = orderService.listAllOrders();

			// Assert
			assertNotNull(result);
			assertTrue(result.isEmpty());
			assertEquals(0, result.size());
			verify(orderRepository, times(1)).findAll();
		}

		// Test 3: Mapeo Correcto
		@Test
		void testListAllOrders_CorrectMapping() {
			// Arrange
			Order order = new Order(5, "Phone", "Smartphone", 3, State.TRAVELING_TO_YOUR_HOUSE, true);
			OrderDto expectedDto = new OrderDto(5, "Phone", "Smartphone", 3, State.TRAVELING_TO_YOUR_HOUSE, true);

			when(orderRepository.findAll()).thenReturn(java.util.Collections.singletonList(order));
			when(orderMapper.toDto(order)).thenReturn(expectedDto);

			// Act
			List<OrderDto> result = orderService.listAllOrders();

			// Assert
			verify(orderMapper).toDto(order);
			assertEquals(1, result.size());
			assertEquals("Phone", result.get(0).getName());
			assertEquals("Smartphone", result.get(0).getDescription());
			assertEquals(5, result.get(0).getId());
		}

		// Test 4: Múltiples Órdenes de Diferentes Usuarios
		@Test
		void testListAllOrders_MultipleOrdersDifferentUsers() {
			// Arrange
			List<Order> orders = new ArrayList<>();
			orders.add(new Order(10, "Tablet", "iPad", 1, State.PROCESSING, true));
			orders.add(new Order(11, "Keyboard", "Mechanical", 2, State.IN_WAREHOUSE, true));
			orders.add(new Order(12, "Mouse", "Wireless", 1, State.ON_THE_STREET, true));
			orders.add(new Order(13, "Monitor", "4K", 3, State.DELIVERED, true));
			orders.add(new Order(14, "Speaker", "Bluetooth", 2, State.TRAVELING_TO_WAREHOUSE, false));

			List<OrderDto> expectedDtos = new ArrayList<>();
			expectedDtos.add(new OrderDto(10, "Tablet", "iPad", 1, State.PROCESSING, true));
			expectedDtos.add(new OrderDto(11, "Keyboard", "Mechanical", 2, State.IN_WAREHOUSE, true));
			expectedDtos.add(new OrderDto(12, "Mouse", "Wireless", 1, State.ON_THE_STREET, true));
			expectedDtos.add(new OrderDto(13, "Monitor", "4K", 3, State.DELIVERED, true));
			expectedDtos.add(new OrderDto(14, "Speaker", "Bluetooth", 2, State.TRAVELING_TO_WAREHOUSE, false));

			when(orderRepository.findAll()).thenReturn(orders);
			for (int i = 0; i < orders.size(); i++) {
				when(orderMapper.toDto(orders.get(i))).thenReturn(expectedDtos.get(i));
			}

			// Act
			List<OrderDto> result = orderService.listAllOrders();

			// Assert
			assertNotNull(result);
			assertEquals(5, result.size());
			assertEquals(10, result.get(0).getId());
			assertEquals(11, result.get(1).getId());
			assertEquals(12, result.get(2).getId());
			assertEquals(13, result.get(3).getId());
			assertEquals(14, result.get(4).getId());
			// Verificar que hay órdenes de diferentes usuarios
			assertEquals(1, result.get(0).getIdUser());
			assertEquals(2, result.get(1).getIdUser());
			assertEquals(1, result.get(2).getIdUser());
			assertEquals(3, result.get(3).getIdUser());
			assertEquals(2, result.get(4).getIdUser());
			verify(orderRepository, times(1)).findAll();
			verify(orderMapper, times(5)).toDto(any(Order.class));
		}

		// Test 5: Consistencia de Datos
		@Test
		void testListAllOrders_DataConsistency() {
			// Arrange
			List<Order> orders = new ArrayList<>();
			orders.add(new Order(100, "Product A", "Description A", 5, State.PROCESSING, true));
			orders.add(new Order(101, "Product B", "Description B", 6, State.CANCELED, false));
			orders.add(new Order(102, "Product C", "Description C", 7, State.TRAVELING_TO_WAREHOUSE, true));

			List<OrderDto> expectedDtos = new ArrayList<>();
			expectedDtos.add(new OrderDto(100, "Product A", "Description A", 5, State.PROCESSING, true));
			expectedDtos.add(new OrderDto(101, "Product B", "Description B", 6, State.CANCELED, false));
			expectedDtos.add(new OrderDto(102, "Product C", "Description C", 7, State.TRAVELING_TO_WAREHOUSE, true));

			when(orderRepository.findAll()).thenReturn(orders);
			when(orderMapper.toDto(orders.get(0))).thenReturn(expectedDtos.get(0));
			when(orderMapper.toDto(orders.get(1))).thenReturn(expectedDtos.get(1));
			when(orderMapper.toDto(orders.get(2))).thenReturn(expectedDtos.get(2));

			// Act
			List<OrderDto> result = orderService.listAllOrders();

			// Assert
			assertNotNull(result);
			assertEquals(3, result.size());

			// Validar primer orden
			assertEquals(100, result.get(0).getId());
			assertEquals("Product A", result.get(0).getName());
			assertEquals("Description A", result.get(0).getDescription());
			assertEquals(5, result.get(0).getIdUser());
			assertEquals(State.PROCESSING, result.get(0).getState());
			assertTrue(result.get(0).isActive());

			// Validar segunda orden
			assertEquals(101, result.get(1).getId());
			assertEquals("Product B", result.get(1).getName());
			assertEquals("Description B", result.get(1).getDescription());
			assertEquals(6, result.get(1).getIdUser());
			assertEquals(State.CANCELED, result.get(1).getState());
			assertFalse(result.get(1).isActive());

			// Validar tercera orden
			assertEquals(102, result.get(2).getId());
			assertEquals("Product C", result.get(2).getName());
			assertEquals("Description C", result.get(2).getDescription());
			assertEquals(7, result.get(2).getIdUser());
			assertEquals(State.TRAVELING_TO_WAREHOUSE, result.get(2).getState());
			assertTrue(result.get(2).isActive());
		}
	}

	@Nested
	class ShowOrderByIdTests {
		
		// Test 1: Búsqueda Exitosa
		@Test
		void testShowOrderById_Success() {
			// Arrange
			int orderId = 5;
			Order order = new Order(5, "Laptop", "Gaming Laptop", 1, State.PROCESSING, true);
			OrderDto expectedDto = new OrderDto(5, "Laptop", "Gaming Laptop", 1, State.PROCESSING, true);

			when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(order));
			when(orderMapper.toDto(order)).thenReturn(expectedDto);

			// Act
			OrderDto result = orderService.showOrderById(orderId);

			// Assert
			assertNotNull(result);
			assertEquals(5, result.getId());
			assertEquals("Laptop", result.getName());
			assertEquals("Gaming Laptop", result.getDescription());
			assertEquals(1, result.getIdUser());
			assertEquals(State.PROCESSING, result.getState());
			assertTrue(result.isActive());
			verify(orderRepository, times(1)).findById(orderId);
			verify(orderMapper, times(1)).toDto(order);
		}

		// Test 2: Orden No Encontrada
		@Test
		void testShowOrderById_OrderNotFound() {
			// Arrange
			int orderId = 999;

			when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.empty());

			// Act
			OrderDto result = orderService.showOrderById(orderId);

			// Assert
			assertNull(result);
			verify(orderRepository, times(1)).findById(orderId);
			verify(orderMapper, never()).toDto(any(Order.class));
		}

		// Test 3: Mapeo Correcto
		@Test
		void testShowOrderById_CorrectMapping() {
			// Arrange
			int orderId = 3;
			Order order = new Order(3, "Phone", "Smartphone", 2, State.DELIVERED, true);
			OrderDto expectedDto = new OrderDto(3, "Phone", "Smartphone", 2, State.DELIVERED, true);

			when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(order));
			when(orderMapper.toDto(order)).thenReturn(expectedDto);

			// Act
			OrderDto result = orderService.showOrderById(orderId);

			// Assert
			verify(orderMapper).toDto(order);
			assertEquals("Phone", result.getName());
			assertEquals("Smartphone", result.getDescription());
			assertEquals(2, result.getIdUser());
			assertEquals(State.DELIVERED, result.getState());
		}

		// Test 4: Consistencia de Datos
		@Test
		void testShowOrderById_DataConsistency() {
			// Arrange
			int orderId = 7;
			Order order = new Order(7, "Monitor", "4K Monitor", 5, State.ON_THE_STREET, false);
			OrderDto expectedDto = new OrderDto(7, "Monitor", "4K Monitor", 5, State.ON_THE_STREET, false);

			when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(order));
			when(orderMapper.toDto(order)).thenReturn(expectedDto);

			// Act
			OrderDto result = orderService.showOrderById(orderId);

			// Assert
			assertNotNull(result);
			assertEquals(7, result.getId());
			assertEquals("Monitor", result.getName());
			assertEquals("4K Monitor", result.getDescription());
			assertEquals(5, result.getIdUser());
			assertEquals(State.ON_THE_STREET, result.getState());
			assertFalse(result.isActive());
		}

		// Test 5: Diferentes IDs
		@Test
		void testShowOrderById_DifferentIds() {
			// Arrange - Primer ID
			int orderId1 = 10;
			Order order1 = new Order(10, "Tablet", "iPad Pro", 1, State.PROCESSING, true);
			OrderDto expectedDto1 = new OrderDto(10, "Tablet", "iPad Pro", 1, State.PROCESSING, true);

			when(orderRepository.findById(orderId1)).thenReturn(java.util.Optional.of(order1));
			when(orderMapper.toDto(order1)).thenReturn(expectedDto1);

			// Act - Primera búsqueda
			OrderDto result1 = orderService.showOrderById(orderId1);

			// Assert - Primera búsqueda
			assertNotNull(result1);
			assertEquals(10, result1.getId());
			assertEquals("Tablet", result1.getName());

			// Arrange - Segundo ID
			int orderId2 = 20;
			Order order2 = new Order(20, "Keyboard", "Mechanical", 2, State.DELIVERED, true);
			OrderDto expectedDto2 = new OrderDto(20, "Keyboard", "Mechanical", 2, State.DELIVERED, true);

			when(orderRepository.findById(orderId2)).thenReturn(java.util.Optional.of(order2));
			when(orderMapper.toDto(order2)).thenReturn(expectedDto2);

			// Act - Segunda búsqueda
			OrderDto result2 = orderService.showOrderById(orderId2);

			// Assert - Segunda búsqueda
			assertNotNull(result2);
			assertEquals(20, result2.getId());
			assertEquals("Keyboard", result2.getName());

			// Arrange - Tercer ID (no encontrado)
			int orderId3 = 999;
			when(orderRepository.findById(orderId3)).thenReturn(java.util.Optional.empty());

			// Act - Tercera búsqueda
			OrderDto result3 = orderService.showOrderById(orderId3);

			// Assert - Tercera búsqueda
			assertNull(result3);

			// Verificar que findById fue llamado con los IDs correctos
			verify(orderRepository).findById(orderId1);
			verify(orderRepository).findById(orderId2);
			verify(orderRepository).findById(orderId3);
		}
	}
}
