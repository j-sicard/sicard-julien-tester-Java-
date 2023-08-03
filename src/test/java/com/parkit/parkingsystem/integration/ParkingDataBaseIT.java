package com.parkit.parkingsystem.integration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)

public class ParkingDataBaseIT {

	private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
	private static ParkingSpotDAO parkingSpotDAO;
	private static TicketDAO ticketDAO;
	private static DataBasePrepareService dataBasePrepareService;

	@Mock
	private static InputReaderUtil inputReaderUtil;

	@BeforeAll
	private static void setUp() throws Exception {
		parkingSpotDAO = new ParkingSpotDAO();
		parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
		ticketDAO = new TicketDAO();
		ticketDAO.dataBaseConfig = dataBaseTestConfig;
		dataBasePrepareService = new DataBasePrepareService();
	}

	@BeforeEach
	private void setUpPerTest() throws Exception {

		dataBasePrepareService.clearDataBaseEntries();

	}

	@AfterAll
	private static void tearDown() {

	}

	@Test
	public void testParkingACar() throws Exception {
		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

		parkingService.processIncomingVehicle();
		// check that a ticket is actualy saved in DB and Parking table is updated
		// with availability

		assertTrue(ticketDAO.getNbTicket("ABCDEF") > 0);
		Ticket ticket = ticketDAO.getTicket("ABCDEF");
		assertFalse(ticket.getParkingSpot().isAvailable());

	}

	@Test
	public void testParkingLotExit() throws Exception {
		// testParkingACar();

		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		ParkingSpot parkingSpot = new ParkingSpot(2, ParkingType.CAR, false);
		Ticket ticketToValidate = new Ticket();
		ticketToValidate.setParkingSpot(parkingSpot);
		ticketToValidate.setVehicleRegNumber("ABCDEF");
		ticketToValidate.setPrice(0);
		ticketToValidate.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
		ticketToValidate.setOutTime(null);
		ticketDAO.saveTicket(ticketToValidate);
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processExitingVehicle();

		// check that the fare generated and out time are populated correctly
		// in
		// the database
		Ticket ticket = ticketDAO.getTicket("ABCDEF");

		assertTrue(ticket.getOutTime() != null);
		assertTrue(ticket.getPrice() == 1.5);
	}

	@Test
	public void testParkingLotExitRecurringUser() throws Exception {

		ParkingSpot parkingSpot = new ParkingSpot(2, ParkingType.CAR, false);
		Ticket FirstTicketValidate = new Ticket();
		FirstTicketValidate.setParkingSpot(parkingSpot);
		FirstTicketValidate.setVehicleRegNumber("GHIJKL");
		FirstTicketValidate.setPrice(0);
		FirstTicketValidate.setInTime(new Date(System.currentTimeMillis() - (120 * 60 * 1000)));
		FirstTicketValidate.setOutTime(new Date(System.currentTimeMillis() - (105 * 60 * 1000)));
		ticketDAO.saveTicket(FirstTicketValidate);

		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("GHIJKL");
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

		parkingService.processIncomingVehicle();
		Ticket newTimeIn = ticketDAO.getTicket("GHIJKL");
		newTimeIn.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
		ticketDAO.UpdateInTime(newTimeIn);
		parkingService.processExitingVehicle();
		Ticket ticket = ticketDAO.getTicket("GHIJKL");
		assertTrue(ticket.getPrice() == (1.5 * 0.95));
		assertTrue(ticketDAO.getNbTicket("GHIJKL") > 0);
	}

}
