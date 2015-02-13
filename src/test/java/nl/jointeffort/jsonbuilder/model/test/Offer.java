package nl.jointeffort.jsonbuilder.model.test;

import java.util.List;

public class Offer {

	public Integer id;
	
	public int hoursPerWeek;
	
	public Double hourlyRate;
	
	public String motivation;
	
	public Professional professional;
	
	public List<OfferState> states;

	public List<Double> tariffs;
	
	public Offer(Integer id, int hoursPerWeek, Double hourlyRate, String motivation, Professional professional) {
		this.id = id;
		this.hoursPerWeek = hoursPerWeek;
		this.hourlyRate = hourlyRate;
		this.motivation = motivation;
		this.professional = professional;
	}
	
	
}
