package com.mendeley.sdk.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Model class representing profile json object.
 *
 */
public class Profile {

	public final UUID id;
	public final String displayName;
	public final String userType;
	public final String url;
	public final String email;
	public final String link;
	public final String firstName;
	public final String lastName;
	public final String researchInterests;
	public final String academicStatus;
	public final Boolean verified;
	public final Date createdAt;
	
	public final Discipline discipline;
	public final Photo photo;
	
	public final List<Education> education;
	public final List<Employment> employment;
	
	public Profile(
			UUID id,
			String displayName,
			String userType,
			String url,
			String email,
			String link,
			String firstName,
			String lastName,
			String researchInterests,
			String academicStatus,
			Boolean verified,
			Date createdAt,
			Discipline discipline,
			Photo photo,
			List<Education> education,
			List<Employment> employment) {

		this.id = id;
		this.displayName = displayName;
		this.userType = userType;
		this.url = url;
		this.email = email;
		this.link = link;
		this.firstName = firstName;
		this.lastName = lastName;
		this.researchInterests = researchInterests;
		this.academicStatus = academicStatus;
		this.verified = verified;
		this.createdAt = createdAt;
		this.discipline = discipline;
		this.photo = photo;
		this.education = education;
		this.employment = employment;
	}
	
	@Override
	public String toString() {
		
		String educationString = "";
		String employmentString = "";
		
		for (Education e : education) {
			educationString = "\n" + e;
		}
		
		for (Employment e : employment) {
			employmentString = "\n" + e;
		}
		
		return " id: " + id +
				", displayName: " + displayName + 
				", userType: " + userType + 
				", url: " + url + 
				", email: " + email + 
				", link: " + link +
				", firstName: " + firstName + 
				", lastName: " + lastName + 
				", researchInterests: " + researchInterests + 
				", academicStatus: " + academicStatus + 
				", verified: " + verified + 
				", createdAt: " + createdAt + 
				", discipline: " + discipline + 
				", photo: " + photo +
				", employment: " + employmentString +
				", education: " + educationString;			
	} 
	
	public static class Builder {
		private UUID id;
		private String displayName;
		private String userType;
		private String url;
		private String email;
		private String link;
		private String firstName;
		private String lastName;
		private String researchInterests;
		private String academicStatus;
		private Boolean verified;
		private Date createdAt;
		private Discipline discipline;
		private Photo photo;		
		private List<Education> education;
		private List<Employment> employment;
		
		public Builder() {}

		public Builder(Profile from) {
			this.id = from.id;
			this.displayName = from.displayName;
			this.userType = from.userType;
			this.url = from.url;
			this.email = from.email;
			this.link = from.link;
			this.firstName = from.firstName;
			this.lastName = from.lastName;
			this.researchInterests = from.researchInterests;
			this.academicStatus = from.academicStatus;
			this.verified = from.verified;
			this.createdAt = from.createdAt;
			this.discipline = from.discipline;
			this.photo = from.photo;
			this.education = from.education==null?new ArrayList<Education>():from.education;
			this.employment = from.employment==null?new ArrayList<Employment>():from.employment;
		}
		
		public Builder setId(UUID id) {
			this.id = id;
			return this;
		}
		
		public Builder setDisplayName(String displayName) {
			this.displayName = displayName;
			return this;
		}
		
		public Builder setUserType(String userType) {
			this.userType = userType;
			return this;
		}
		
		public Builder setUrl(String url) {
			this.url = url;
			return this;
		}
		
		public Builder setEmail(String email) {
			this.email = email;
			return this;
		}
		
		public Builder setLink(String link) {
			this.link = link;
			return this;
		}
		
		public Builder setFirstName(String firstName) {
			this.firstName = firstName;
			return this;
		}
		
		public Builder setLastName(String lastName) {
			this.lastName = lastName;
			return this;
		}
		
		public Builder setResearchInterests(String researchInterests) {
			this.researchInterests = researchInterests;
			return this;
		}
		
		public Builder setAcademicStatus(String academicStatus) {
			this.academicStatus = academicStatus;
			return this;
		}
		
		public Builder setVerified(Boolean verified) {
			this.verified = verified;
			return this;
		}
		
		public Builder setCreatedAt(Date createdAt) {
			this.createdAt = createdAt;
			return this;
		}
		
		public Builder setDiscipline(Discipline discipline) {
			this.discipline = discipline;
			return this;
		}
		
		public Builder setPhoto(Photo photo) {
			this.photo = photo;
			return this;
		}
		
		public Builder setEducation(List<Education> education) {
			this.education = education;
			return this;
		}
		
		public Builder setEmployment(List<Employment> employment) {
			this.employment = employment;
			return this;
		}		

		public Profile build() {
			return new Profile(
					id,
					displayName,
					userType,
					url,
					email,
					link,
				    firstName,
					lastName,
					researchInterests,
					academicStatus,
					verified,
					createdAt,
					discipline,
					photo,
					education,
					employment);
		}
	}
}
