package nl.jointeffort.jsonbuilder.model.test;

public class Recruiter extends User {

	public Recruiter() {
		super();
	}

	public Recruiter(Long id, String emailAddress) {
		super(id, UserType.Recruiter, emailAddress);
	}
	
}
