package nl.jointeffort.jsonbuilder.model.test;

import java.util.List;

public class JobRequest {

	public Long id;
	
	public String experience;
	
	public String referenceCode;

	public Recruiter recruiter;
	
	public List<Professional> preferredCandidates;
	
	public List<Offer> offers;

	public JobRequest(Long id, String experience, String referenceCode) {
		this.id = id;
		this.experience = experience;
		this.referenceCode = referenceCode;
	}
}
