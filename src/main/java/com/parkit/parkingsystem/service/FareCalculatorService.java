package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

	public void calculateFare(Ticket ticket, boolean discount) {
		if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
			throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
		}
		long inTimeInMillis = ticket.getInTime().getTime();
		long inHour = inTimeInMillis / 60000;

		long outTimeInMillis = ticket.getOutTime().getTime();
		long outHour = outTimeInMillis / 60000;

		long duration = outHour - inHour;

		// free for less than 30 minutes
		if (duration <= 30) {
			ticket.setPrice(0);
		} else {

			// if the user is a recurring customer, he benefits from a 5% discount
			if (discount == true) {
				switch (ticket.getParkingSpot().getParkingType()) {

				case CAR: {
					ticket.setPrice((duration * Fare.CAR_RATE_PER_HOUR / 60.0) * 0.95);
					break;
				}
				case BIKE: {
					ticket.setPrice((duration * Fare.BIKE_RATE_PER_HOUR / 60.0) * 0.95);
					break;
				}
				default:
					throw new IllegalArgumentException("Unkown Parking Type");
				}
			} else {

				// otherwise he will pay the normal rate
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

	public void calculateFare(Ticket ticket) {
		calculateFare(ticket, false);
	}

	public void calculateFareTrue(Ticket ticket) {
		calculateFare(ticket, true);
	}
}