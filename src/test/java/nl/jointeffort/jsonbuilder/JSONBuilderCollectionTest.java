package nl.jointeffort.jsonbuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import nl.jointeffort.jsonbuilder.Filter;
import nl.jointeffort.jsonbuilder.JSONBuilder;
import nl.jointeffort.jsonbuilder.model.test.JobRequest;
import nl.jointeffort.jsonbuilder.model.test.Offer;
import nl.jointeffort.jsonbuilder.model.test.OfferState;
import nl.jointeffort.jsonbuilder.model.test.Professional;
import nl.jointeffort.jsonbuilder.model.test.Recruiter;
import nl.jointeffort.jsonbuilder.model.test.UserState;
import nl.jointeffort.jsonbuilder.model.test.UserType;

import org.junit.Before;
import org.junit.Test;

public class JSONBuilderCollectionTest extends AbstractJsonBuilderTest {

	private JobRequest jobRequest;
	
	@Before
	public void setup() {
		jobRequest = new JobRequest(1L, "Java", "HF2015012301");
		jobRequest.recruiter = new Recruiter(3L, "recruiter@hfs.nl");
		Offer offer1 = new Offer(1, 40, 75.0, "Motivation01", new Professional(UserState.Silver, 1L, UserType.Freelancer, "anonymous01@hfs.nl"));
		Offer offer2 = new Offer(2, 32, 77.0, "Motivation02", new Professional(UserState.Silver, 2L, UserType.Employee, "anonymous02@hfs.nl"));
		jobRequest.offers = Arrays.asList(offer1, offer2);
	}

	@Test
	public void testSerializationOfNonCollectionAssociaten() {
		JSONBuilder json = new JSONBuilder();
		json.include("id","experience","recruiter.id","recruiter.emailAddress");
		String s = json.serialize(jobRequest);
		assertValidJson(s);
		assertTrue("JSON does not contain 'recruiter' field.", s.contains("\"recruiter\":{"));
	}
	
	@Test
	public void testCollectionSerialization() {
		JSONBuilder json = new JSONBuilder();
		json.include("offers.professional.id","offers.professional.emailAddress","id","experience","offers.id","offers.hoursPerWeek","offers.motivation");
		String s = json.serialize(jobRequest);
		assertValidJson(s);
		assertContainsKVP(s, "emailAddress", "anonymous01@hfs.nl");
		assertContainsKVP(s, "emailAddress", "anonymous02@hfs.nl");
		assertContainsKVP(s, "experience", "Java");
	}

	@Test
	public void testCollectionSerializationWithFilter() {
		JSONBuilder json = new JSONBuilder();
		json.include("offers.professional.id","offers.professional.emailAddress","id","experience","offers.id","offers.hoursPerWeek","offers.motivation");
		json.withFilter("offers", new Filter<Offer>() {
			@Override
			public boolean include(Offer item) {
				return item.hourlyRate <= 75;
			}
		});
		String s = json.serialize(jobRequest);
		assertValidJson(s);
		assertContainsKVP(s, "emailAddress", "anonymous01@hfs.nl");
		assertContainsNotKVP(s, "emailAddress", "anonymous02@hfs.nl");
	}
	
	@Test
	public void testCollectionOfEnumTypes() {
		Offer offer = new Offer(3, 40, 75.0, "Motivation01", new Professional(UserState.Silver, 1L, UserType.Freelancer, "anonymous01@hfs.nl"));
		offer.states = Arrays.asList(OfferState.Valid, OfferState.Ready, OfferState.Concept);
		JSONBuilder json = new JSONBuilder();
		json.include("id", "states");
		String s = json.serialize(offer);
		assertValidJson(s);
		assertEquals("{\"id\":3,\"states\":[\"Valid\",\"Ready\",\"Concept\"]}", s);
	}

	@Test
	public void testCollectionOfDoubleTypes() {
		Offer offer = new Offer(3, 40, 75.0, "Motivation01", new Professional(UserState.Silver, 1L, UserType.Freelancer, "anonymous01@hfs.nl"));
		offer.tariffs = Arrays.asList(3.15, 1d, 2.0);
		JSONBuilder json = new JSONBuilder();
		json.include("id", "tariffs");
		String s = json.serialize(offer);
		assertValidJson(s);
		assertEquals("{\"id\":3,\"tariffs\":[3.15,1.0,2.0]}", s);
	}
	
	@Test
	public void testCollectionAsRootObject() {
		Offer offer1 = new Offer(1, 40, 75.0, "Motivation01", new Professional(UserState.Silver, 1L, UserType.Freelancer, "anonymous01@hfs.nl"));
		Offer offer2 = new Offer(2, 32, 77.0, "Motivation02", new Professional(UserState.Silver, 2L, UserType.Employee, "anonymous02@hfs.nl"));
		List<Offer> offers = Arrays.asList(offer1, offer2);
		
		JSONBuilder json = new JSONBuilder();
		json.include("id", "professional.id","professional.emailAddress");
		String s = json.serialize(offers);
		assertValidJson(s);
		assertContainsKVP(s, "emailAddress", "anonymous01@hfs.nl");
		assertContainsKVP(s, "emailAddress", "anonymous02@hfs.nl");
		assertContainsKVP(s, "id", 1);
		assertContainsKVP(s, "id", 2);

	}
	
	@Test
	public void testIntegerCollectionAsRootObject() {
		JSONBuilder json = new JSONBuilder();
		String s = json.serialize(Arrays.asList(1,2,3,4,5));
		assertValidJson(s);
		assertEquals("[1,2,3,4,5]", s);
	}

}
