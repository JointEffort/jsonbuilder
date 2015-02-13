package nl.jointeffort.jsonbuilder;

import java.util.Arrays;

import nl.jointeffort.jsonbuilder.JSONBuilder;
import nl.jointeffort.jsonbuilder.model.test.JobRequest;
import nl.jointeffort.jsonbuilder.model.test.Offer;
import nl.jointeffort.jsonbuilder.model.test.Professional;
import nl.jointeffort.jsonbuilder.model.test.Recruiter;
import nl.jointeffort.jsonbuilder.model.test.UserState;
import nl.jointeffort.jsonbuilder.model.test.UserType;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.CaseFormat;

public class JSONBuilderRestructureTest extends AbstractJsonBuilderTest {

	private JobRequest jobRequest;
	
	@Before
	public void setup() {
		jobRequest = new JobRequest(1L, "Java", "HF2015012301");
		jobRequest.recruiter = new Recruiter(3L, "recruiter@hfs.nl");
		Offer offer1 = new Offer(1, 40, 75.0, "Motivation01", new Professional(UserState.Silver, 1L, UserType.Freelancer, "anonymous01@hfs.nl"));
		offer1.professional.firstName = "First";
		offer1.professional.name = "Lastname01";
		Offer offer2 = new Offer(2, 32, 77.0, "Motivation02", new Professional(UserState.Silver, 2L, UserType.Employee, "anonymous02@hfs.nl"));
		offer2.professional.firstName = "First";
		offer2.professional.name = "Lastname02";
		jobRequest.offers = Arrays.asList(offer1, offer2);
	}

	@Test
	public void testIncudePropertyUnderDifferentName() {
		JSONBuilder json = new JSONBuilder(CaseFormat.LOWER_CAMEL, CaseFormat.LOWER_UNDERSCORE);
		json.include("id","referenceCode","offers.id","offers.motivation");
		json.includeTransposed("offers.professional.name","offers.professional_name");
		json.includeTransposed("offers.professional.id","offers.professional_id");
		json.includeTransposed("offers.professional.fullName","offers.professional_full_name");
		json.includeTransposed("recruiter.fullName","recruiter_name");
		String s = json.serialize(jobRequest);
		assertValidJson(s);
	}
}
