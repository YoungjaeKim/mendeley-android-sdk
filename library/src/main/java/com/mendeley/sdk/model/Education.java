package com.mendeley.sdk.model;

import java.util.UUID;

/**
 * Model class representing education json object.
 */
public class Education {
	
	public final UUID id;
	public final String institution;
	public final String degree;
	public final String startDate;
	public final String endDate;
	public final String website;

	private Education(
			UUID id,
			String institution,
			String degree,
			String startDate,
			String endDate,
			String website) {
		this.id = id;
		this.institution = institution;
		this.degree = degree;
		this.startDate = startDate;
		this.endDate = endDate;
		this.website = website;
	}
	
	public static class Builder {
		private UUID id;
		private String institution;
		private String degree;
		private String startDate;
		private String endDate;
		private String website;
		
		public Builder() {}
		
		public Builder(Education from) {
			this.id = from.id;
			this.institution = from.institution;
			this.degree = from.degree;
			this.startDate = from.startDate;
			this.endDate = from.endDate;
			this.website = from.website;
		}

		public Builder setId(UUID id) {
			this.id = id;
			return this;
		}

		public Builder setInstitution(String institution) {
			this.institution = institution;
			return this;
		}

		public Builder setDegree(String degree) {
			this.degree = degree;
			return this;
		}

		public Builder setStartDate(String startDate) {
			this.startDate = startDate;
			return this;
		}
		
		public Builder setEndDate(String endDate) {
			this.endDate = endDate;
			return this;
		}
		
		public Builder setWebsite(String website) {
			this.website = website;
			return this;
		}
		
		public Education build() {
			return new Education(
					id,
					institution,
					degree,
					startDate,
					endDate,
					website);
		}
	}
}
