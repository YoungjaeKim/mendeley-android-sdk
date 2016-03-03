package com.mendeley.sdk.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Model class representing employment json object.
 *
 */
public class Employment {

	public final UUID id;
	public final String institution;
	public final String position;
	public final String startDate;
	public final String endDate;
	public final String website;
	public final List<String> classes;
	public final Boolean isMainEmployment;

	private Employment(
			UUID id,
			String institution,
			String position,
			String startDate,
			String endDate,
			String website,
			List<String> classes,
			Boolean isMainEmployment) {

		this.id = id;
		this.institution = institution;
		this.position = position;
		this.startDate = startDate;
		this.endDate = endDate;
		this.website = website;
		this.classes = classes;
		this.isMainEmployment = isMainEmployment;
	}

	public static class Builder {
		private UUID id;
		private String institution;
		private String position;
		private String startDate;
		private String endDate;
		private String website;
		private List<String> classes;
		private Boolean isMainEmployment;

		public Builder() {}

		public Builder(Employment from) {
			this.id = from.id;
			this.institution = from.institution;
			this.position = from.position;
			this.startDate = from.startDate;
			this.endDate = from.endDate;
			this.website = from.website;
			this.classes = from.classes==null?new ArrayList<String>():from.classes;
			this.isMainEmployment = from.isMainEmployment;
		}

		public Builder setId(UUID id) {
			this.id = id;
			return this;
		}

		public Builder setInstitution(String institution) {
			this.institution = institution;
			return this;
		}

		public Builder setPosition(String position) {
			this.position = position;
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

		public Builder setClasses(List<String> classes) {
			this.classes = classes;
			return this;
		}

		public Builder setIsMainEmployment(Boolean isMainEmployment) {
			this.isMainEmployment = isMainEmployment;
			return this;
		}

		public Employment build() {
			return new Employment(
					id,
					institution,
					position,
					startDate,
					endDate,
					website,
					classes,
					isMainEmployment);
		}
	}
}
