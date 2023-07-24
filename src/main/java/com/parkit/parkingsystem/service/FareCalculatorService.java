package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

	public void calculateFare(Ticket ticket) {
		if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
			throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
		}
		long inTimeInMillis = ticket.getInTime().getTime();
		long inHour = inTimeInMillis / 60000;

		long outTimeInMillis = ticket.getOutTime().getTime();
		long outHour = outTimeInMillis / 60000;

		// TODO: Some tests are failing here. Need to check if this logic is correct
		long duration = outHour - inHour;

		if (duration <= 30) {
			ticket.setPrice(0);
		} else {
			switch (ticket.getParkingSpot().getParkingType()) {

			case CAR: {
				ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR / 60.0);
				break;
			}
			case BIKE: {
				ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR / 60.0);
				break;
			}
			default:
				throw new IllegalArgumentException("Unkown Parking Type");
			}

		}

	}
}