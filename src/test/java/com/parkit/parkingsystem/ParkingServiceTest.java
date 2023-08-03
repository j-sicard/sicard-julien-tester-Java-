package com.parkit.parkingsystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

	private static ParkingService parkingService;

	@Mock
	private static InputReaderUtil inputReaderUtil;
	@Mock
	private static ParkingSpotDAO parkingSpotDAO;
	@Mock
	private static TicketDAO ticketDAO;

	@BeforeEach
	private void setUpPerTest() {
		try {
			parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to set up test mock objects");
		}
	}

	@Test
	// test de l’appel de la méthode processIncomingVehicle() où tout se déroule
	// comme attendu.
	public void testProcessIncomingVehicle() throws Exception {
		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);

		parkingService.processIncomingVehicle();

		verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
		verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
	}

	@Test
	public void testForTheMessageForRedcurantUsersDoesNotAppearIfTheUserComesForTheFirstTime() {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		System.setOut(new PrintStream(outputStream));

		parkingService.processIncomingVehicle();

		// Convertir la sortie de la console en chaîne de caractères
		String consoleOutput = outputStream.toString();

		// Vérifier que le message attendu n 'est présent pas dans la sortie de la
		// console
		assertFalse(consoleOutput.contains(
				"Heureux de vous revoir ! En tant qu’utilisateur régulier de notre parking, vous allez obtenir une remise de 5%"));
	}

	// exécution du test dans le cas où la méthode updateTicket() de ticketDAO
	// renvoie false lors de l’appel de processExitingVehicle().
	@Test
	public void processExitingVehicleTestUnableUpdate() throws Exception {
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
		Ticket ticket = new Ticket();
		ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
		ticket.setParkingSpot(parkingSpot);
		ticket.setVehicleRegNumber("ABCDEF");

		when(ticketDAO.getTicket(anyString())).thenReturn(ticket);

		parkingService.processExitingVehicle();

		assertFalse(ticketDAO.updateTicket(any(Ticket.class)));
	}

	// test de l’appel de la méthode getNextParkingNumberIfAvailable() avec pour
	// résultat l’obtention d’un spot dont l’ID est 1 et qui est disponible.
	@Test
	public void testGetNextParkingNumberIfAvailable() {
		ParkingType parkingType = ParkingType.CAR;
		ParkingSpot parkingSpot = new ParkingSpot(1, parkingType, true);

		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(parkingSpotDAO.getNextAvailableSlot(parkingType)).thenReturn(1);

		ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();

		assertEquals(parkingSpot, result);
	}

	// test de l’appel de la méthode getNextParkingNumberIfAvailable() avec pour
	// résultat aucun spot disponible (la méthode renvoie null).

	@Test
	public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(0);
		assertNull(parkingService.getNextParkingNumberIfAvailable());
	}

	// test de l’appel de la méthode getNextParkingNumberIfAvailable() avec pour
	// résultat aucun spot (la méthode renvoie null) car l’argument saisi par
	// l’utilisateur concernant le type de véhicule est erroné (par exemple,
	// l’utilisateur a saisi 3)

	@Test
	public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgum() {
		when(inputReaderUtil.readSelection()).thenReturn(3);
		assertNull(parkingService.getNextParkingNumberIfAvailable());
	}

	@Test
	public void processExitingVehicleTest() throws Exception {
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
		Ticket ticket = new Ticket();
		ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
		ticket.setParkingSpot(parkingSpot);
		ticket.setVehicleRegNumber("ABCDEF");
		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
		when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

		parkingService.processExitingVehicle();

		verify(ticketDAO, Mockito.times(1)).getNbTicket(anyString());
		verify(ticketDAO, Mockito.times(1)).getTicket(anyString());
		verify(ticketDAO, Mockito.times(1)).updateTicket(any(Ticket.class));
		verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));

	}

}
