package nl.jointeffort.jsonbuilder.model.test;

import java.util.Calendar;

import com.google.common.base.Strings;

public class User {

	public Long id;
	
	public UserType type;
	
	public String emailAddress;
	
	public Calendar accountCreationDate;
	
	public String name;
	
	public String middleName;
	
	public String firstName;
	
	public String lastModifiedDate;
	
	public User() {
	}

	public User(Long id, UserType type, String emailAddress) {
		this.id = id;
		this.type = type;
		this.emailAddress = emailAddress;
	}

	public String getFullName() {
		StringBuilder tmp = new StringBuilder(firstName == null ? "" : firstName);
		if (!Strings.isNullOrEmpty(middleName)) {
			tmp.append(tmp.length() > 0 ? " " : "").append(middleName);
		}
		return tmp.append(' ').append(name).toString();
	}
}
