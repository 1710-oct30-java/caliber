package com.revature.caliber.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Determines if notes are created by Trainer or QC 
 * and where they are applied (to the trainee or batch).
 * Public notes are made by QC that are not visible to trainers and below.
 * 
 * @author Patrick Walsh
 *
 */
public enum NoteType {
	@JsonProperty("TRAINEE")
	TRAINEE,
	@JsonProperty("BATCH")
	BATCH,
	@JsonProperty("QC_TRAINEE")
	QC_TRAINEE,
	@JsonProperty("QC_BATCH")
	QC_BATCH,
	@JsonProperty("PUBLIC_TRAINEE")
	PUBLIC_TRAINEE,
	@JsonProperty("PUBLIC_BATCH")
	PUBLIC_BATCH
}
