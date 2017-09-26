package com.revature.caliber.controllers;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.revature.caliber.beans.Grade;
import com.revature.caliber.beans.Note;
import com.revature.caliber.services.EvaluationService;

/**
 * Used to add grades for assessments and input notes
 * 
 * @author Patrick Walsh
 *
 */
@RestController
@PreAuthorize("isAuthenticated()")
public class EvaluationController {

	private static final Logger log = Logger.getLogger(EvaluationController.class);
	private EvaluationService evaluationService;
	private static final String FINDING_WEEK = "Finding week ";

	@Autowired
	public void setEvaluationService(EvaluationService evaluationService) {
		this.evaluationService = evaluationService;
	}

	/*
	 *******************************************************
	 * TODO GRADE SERVICES
	 *
	 *******************************************************
	 */

	/**
	 * Create grade
	 *
	 * @param grade
	 * @return
	 */
	@RequestMapping(value = "/trainer/grade/create", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAnyRole('VP', 'QC', 'TRAINER')")
	public ResponseEntity<Grade> createGrade(@Valid @RequestBody Grade grade) {
		log.info("Saving grade: " + grade);
		evaluationService.save(grade);
		return new ResponseEntity<>(grade, HttpStatus.CREATED);
	}

	/**
	 * Update grade
	 *
	 * @param grade
	 * @return
	 */
	@RequestMapping(value = "/trainer/grade/update", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAnyRole('VP', 'QC', 'TRAINER')")
	public ResponseEntity<Void> updateGrade(@Valid @RequestBody Grade grade) {
		log.info("Updating grade: " + grade);
		evaluationService.update(grade);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	/**
	 * Returns absolutely all grades for only the most coarsely-grained reporting.
	 * Useful for feeding data into application for statistical analyses, such as
	 * regression analysis, calculating mean, and finding average ;)
	 * 
	 * @param traineeId
	 * @return
	 */
	@RequestMapping(value = "/vp/grade/all", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAnyRole('VP', 'QC', 'TRAINER', 'STAGING')")
	public List<Grade> findAll() {
		log.info("Finding all grades");
		return evaluationService.findAllGrades();
	}

	/**
	 * Returns grades for all trainees that took a particular assignment. Great for
	 * finding average/median/highest/lowest grades for a test
	 * 
	 * @param assessmentId
	 * @return
	 */
	@RequestMapping(value = "/all/grades/assessment/{assessmentId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAnyRole('VP', 'QC', 'TRAINER', 'STAGING')")
	public List<Grade> findByAssessment(@PathVariable Long assessmentId) {
		log.info("Finding grades for assessment: " + assessmentId);
		return evaluationService.findGradesByAssessment(assessmentId);
	}

	/**
	 * Returns all grades for a trainee. Useful for generating a full-view of
	 * individual trainee performance.
	 * 
	 * @param traineeId
	 * @return
	 */
	@RequestMapping(value = "/all/grade/trainee/{traineeId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAnyRole('VP', 'QC', 'TRAINER', 'STAGING')")
	public List<Grade> findByTrainee(@PathVariable Integer traineeId) {
		log.info("Finding all grades for trainee: " + traineeId);
		return evaluationService.findGradesByTrainee(traineeId);
	}

	/**
	 * Returns all grades for a batch. Useful for calculating coarsely-grained data
	 * for reporting.
	 * 
	 * @param batchId
	 * @return
	 */
	@RequestMapping(value = "/all/grade/batch/{batchId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAnyRole('VP', 'QC', 'TRAINER', 'STAGING')")
	public List<Grade> findByBatch(@PathVariable Integer batchId) {
		log.info("Finding all grades for batch: " + batchId);
		return evaluationService.findGradesByBatch(batchId);
	}

	/**
	 * Returns all grades for a category. Useful for improving performance time of
	 * company-wide reporting
	 * 
	 * @param batchId
	 * @return
	 */
	@RequestMapping(value = "/all/grade/category/{categoryId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAnyRole('VP', 'QC', 'TRAINER', 'STAGING')")
	public List<Grade> findByCategory(@PathVariable Integer categoryId) {
		log.info("Finding all grades for category: " + categoryId);
		return evaluationService.findGradesByCategory(categoryId);
	}

	/**
	 * Returns grades for all trainees in the batch on a given week. Used to load
	 * grade data onto the input spreadsheet, as well as tabular/chart reporting.
	 * 
	 * @param batchId
	 * @param week
	 * @return
	 */
	@RequestMapping(value = "/all/grades/batch/{batchId}/week/{week}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAnyRole('VP', 'QC', 'TRAINER', 'STAGING')")
	public ResponseEntity<Map<Integer, List<Grade>>> findByWeek(@PathVariable Integer batchId,
			@PathVariable Integer week) {
		log.info(FINDING_WEEK + week + " grades for batch: " + batchId);
		Map<Integer, List<Grade>> table = evaluationService.findGradesByWeek(batchId, week);
		return new ResponseEntity<>(table, HttpStatus.OK);
	}

	/**
	 * Returns all grades issued as acting trainer or cotrainer to a batch. Useful
	 * for calculating coarsely-grained data for reporting. Potential refactor
	 * here.. this queries database twice where we could find way to simply join.
	 * 
	 * @param trainerId
	 * @return
	 */
	@RequestMapping(value = "/all/grade/trainer/{trainerId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAnyRole('VP', 'QC', 'TRAINER', 'STAGING')")
	public List<Grade> findByTrainer(@PathVariable Integer trainerId) {
		log.info("Finding all grades for trainer: " + trainerId);
		return evaluationService.findGradesByTrainer(trainerId);
	}

	/*
	 *******************************************************
	 * TODO ALL NOTE SERVICES
	 *
	 *******************************************************
	 */

	/**
	 * Create note
	 *
	 * @param note
	 * @return
	 */
	@RequestMapping(value = "/note/create", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAnyRole('VP', 'QC', 'TRAINER')")
	public ResponseEntity<Integer> createNote(@Valid @RequestBody Note note) {
		log.info("Creating note: " + note);
		return new ResponseEntity<>(evaluationService.save(note), HttpStatus.CREATED);
	}

	/**
	 * Update note
	 * 
	 * @param note
	 * @return
	 */
	@RequestMapping(value = "/note/update", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAnyRole('VP', 'QC', 'TRAINER')")
	public ResponseEntity<Note> updateNote(@Valid @RequestBody Note note) {
		log.info("Updating note: " + note);
		evaluationService.update(note);
		return new ResponseEntity<>(note, HttpStatus.CREATED);
	}

	/*
	 *******************************************************
	 * TODO TRAINER NOTE SERVICES
	 *
	 *******************************************************
	 */
	/**
	 * FIND WEEKLY BATCH NOTES (TRAINER/PUBLIC)
	 * 
	 * @param batch
	 * @param week
	 * @return
	 */
	@RequestMapping(value = "/trainer/note/batch/{batchId}/{week}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAnyRole('VP', 'QC', 'TRAINER', 'STAGING')")
	public ResponseEntity<List<Note>> findBatchNotes(@PathVariable Integer batchId, @PathVariable Integer week) {
		log.info(FINDING_WEEK + week + " batch notes for batch: " + batchId);
		return new ResponseEntity<>(evaluationService.findBatchNotes(batchId, week), HttpStatus.OK);
	}

	/**
	 * FIND WEEKLY INDIVIDUAL NOTES (TRAINER/PUBLIC)
	 * 
	 * @param trainee
	 * @param week
	 * @return
	 */
	@RequestMapping(value = "/trainer/note/trainee/{batchId}/{week}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAnyRole('VP', 'QC', 'TRAINER', 'STAGING')")
	public ResponseEntity<List<Note>> findIndividualNotes(@PathVariable Integer batchId, @PathVariable Integer week) {
		log.info(FINDING_WEEK + week + " individual notes for trainee: " + batchId);
		return new ResponseEntity<>(evaluationService.findIndividualNotes(batchId, week), HttpStatus.OK);
	}

	/**
	 * FIND TRAINEE NOTE FOR THE WEEK
	 * 
	 * @param trainee
	 * @param week
	 * @return
	 */
	@RequestMapping(value = "/trainer/note/trainee/{traineeId}/for/{week}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAnyRole('VP', 'QC', 'TRAINER', 'STAGING')")
	public ResponseEntity<Note> findTraineeNote(@PathVariable Integer traineeId, @PathVariable Integer week) {
		return new ResponseEntity<>(evaluationService.findTraineeNote(traineeId, week), HttpStatus.OK);
	}

	/**
	 * FIND TRAINEE NOTE FOR THE WEEK(Michael)
	 * 
	 * @param QCtrainee
	 * @param week
	 * @return
	 */
	@RequestMapping(value = "/qc/note/trainee/{traineeId}/for/{week}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAnyRole('VP', 'QC', 'STAGING')")
	public ResponseEntity<Note> findQCTraineeNote(@PathVariable Integer traineeId, @PathVariable Integer week) {
		return new ResponseEntity<>(evaluationService.findQCTraineeNote(traineeId, week), HttpStatus.OK);
	}

	/**
	 * FIND THE WEEKLY QC BATCH NOTE FOR THE WEEK
	 * 
	 * @param batch
	 * @param week
	 * @return
	 */
	@RequestMapping(value = "/qc/note/batch/{batchId}/{week}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAnyRole('VP', 'QC', 'TRAINER', 'STAGING')")
	public ResponseEntity<Note> findQCBatchNotes(@PathVariable Integer batchId, @PathVariable Integer week) {
		log.info("EVAL CONTROLLER"+FINDING_WEEK + week + " QC batch notes for batch: " + batchId);
		return new ResponseEntity<>(evaluationService.findQCBatchNotes(batchId, week), HttpStatus.OK);
	}

	/**
	 * Find all QC trainee notes in a batch for the week
	 * 
	 * @return
	 */
	@RequestMapping(value = "/qc/note/trainee/{batchId}/{week}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAnyRole('VP', 'QC', 'TRAINER', 'STAGING')")
	public ResponseEntity<List<Note>> getAllQCTraineeNotes(@PathVariable Integer batchId, @PathVariable Integer week) {
		log.info("Getting all trainee notes by QC");
		return new ResponseEntity<>(evaluationService.findAllQCTraineeNotes(batchId, week), HttpStatus.OK);
	}

	/**
	 * Find all QC trainee notes in a batch
	 * 
	 * @return
	 */
	@RequestMapping(value = "/qc/note/trainee/{traineeId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAnyRole('VP', 'QC', 'TRAINER', 'STAGING')")
	public ResponseEntity<List<Note>> getAllQCTraineeOverallNotes(@PathVariable Integer traineeId) {
		log.info("Getting all trainee notes by QC for that trainee");
		return new ResponseEntity<>(evaluationService.findAllQCTraineeOverallNotes(traineeId), HttpStatus.OK);
	}

	/*
	 *******************************************************
	 * TODO VP NOTE SERVICES
	 *
	 *******************************************************
	 */

	/**
	 * FIND ALL WEEKLY BATCH NOTES (VP ONLY)
	 * 
	 * @param batch
	 * @param week
	 * @return
	 */
	@RequestMapping(value = "/vp/note/batch/{batchId}/{week}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasRole('VP')")
	public ResponseEntity<List<Note>> findAllBatchNotes(@PathVariable Integer batchId, @PathVariable Integer week) {
		log.info(FINDING_WEEK + week + " batch notes for batch: " + batchId);
		return new ResponseEntity<>(evaluationService.findAllBatchNotes(batchId, week), HttpStatus.OK);
	}

	/**
	 * FIND ALL WEEKLY INDIVIDUAL NOTES (VP ONLY)
	 * 
	 * @param trainee
	 * @param week
	 * @return
	 */
	@RequestMapping(value = "/vp/note/trainee/{traineeId}/{week}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasRole('VP')")
	public ResponseEntity<List<Note>> findAllIndividualNotes(@PathVariable Integer traineeId,
			@PathVariable Integer week) {
		log.info("Finding all week " + week + " individual notes for trainee: " + traineeId);
		return new ResponseEntity<>(evaluationService.findAllIndividualNotes(traineeId, week), HttpStatus.OK);
	}

	@RequestMapping(value = "/all/notes/trainee/{traineeId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasRole('VP')")
	public ResponseEntity<List<Note>> findAllTraineeNotes(@PathVariable Integer traineeId) {
		return new ResponseEntity<>(evaluationService.findAllIndividualNotesOverall(traineeId), HttpStatus.OK);
	}
}