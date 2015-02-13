package nl.jointeffort.jsonbuilder.model.test;

public class Professional extends User {

	public UserState state;
	
	public Boolean trueFlag;
	
	public boolean falseFlag;
	
	public Professional() {
	}

	public Professional(UserState state, Long id, UserType type, String emailAddress) {
		super(id, type, emailAddress);
		this.state = state;
	}
	
	public Boolean isNerd() {
		return true;
	}
	
	public boolean isLooser() {
		return false;
	}
	
	public boolean hasSuccess() {
		return true;
	}
}
