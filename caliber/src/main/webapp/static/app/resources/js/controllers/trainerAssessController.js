/**
 * Refactor to use week index instead of Week object
 */
angular
		.module("trainer")
		.controller(
				"trainerAssessController",
				function($log, $scope, chartsDelegate, caliberDelegate,
						allBatches) {
					// Week object
					function Week(weekNumb, assessments) {
						this.weekNumb = weekNumb;
						this.assessments = assessments;
					}
					
					// array of weeks to parse through and display tabs
					
					$log.debug("Booted Trainer Aesess Controller");

					$scope.trainerBatchNote = null;
					// load categories
					$scope.skill_categories = function() {
						caliberDelegate.all.getAllCategories().then(
								function(categories) {
									$scope.categories = categories;
									$log.debug("all Categories");
									// $log.debug(categories);
								});
					};
					/**
					 * ***************************************** UI
					 * ********************************************
					 */
					$scope.grades={};
					 $scope.getAllGradesForWeek=function(batchId,weekId) {							
							caliberDelegate.all
									.getGradesForWeek(batchId,
											weekId).then(
											function(data) {
												$scope.grades = data;
												// $log.debug("These are the
												// grades");
												// $log.debug(data);
												// for ( var i in data) {
												// $log.debug("Fetching ");
												// $log.debug(data[i]);
												// pushUnique($scope.grades,
												// data[i]);
												// }
											});
					 }
						$scope.assignTraineeScope = function(traineeId){
							if($scope.trainees[traineeId] === undefined){
								$scope.trainees[traineeId] = {};
								$scope.trainees[traineeId].assessments=[];
								$scope.trainees[traineeId].note={};
							}								
							return $scope.trainees[traineeId];							
						}	
						//get trainee notes and put into $scope.trainees[traineeId].note
						$scope.getTraineeBatchNotesForWeek=function(batchId,weekId){
							caliberDelegate.trainer.getTraineeBatchNotesForWeek(batchId,weekId).then(function(data){
								if(data.length > 0){
									$scope.notes = data;
									for(note of $scope.notes){
										if($scope.trainees[note.trainee.traineeId].note.hasOwnProperty('noteId')){
											$scope.trainees[note.trainee.traineeId].note = {};
										}
										$scope.trainees[note.trainee.traineeId].note = note;
									}
								}else{
									angular.forEach($scope.trainees,function(key,value){
										$scope.trainees[value].note = {};
									});
								}
							});
						}
					// ////////////////////////////////////////////////////////////////////////
					// load note types
					caliberDelegate.all.enumNoteType().then(
							function(noteTypes) {
								$log.debug(noteTypes);
								// do something with note type
							});
					$scope.assessmentType = {
						model : null,
						options : []
					};
					caliberDelegate.all
							.enumAssessmentType()
							.then(
									function(assessmentTypes) {
										$log.debug(assessmentTypes);
										$scope.assessmentType.options = assessmentTypes;
									});
					$log.debug("Batches " + allBatches);
					$log.debug(allBatches);
					
					(function start(allBatches) {
						$scope.batches = allBatches;
						if (!allBatches) return;
						if (allBatches.length > 0) {
							$scope.currentBatch = allBatches[allBatches.length-1]; // shows
																					// the
																					// latest
																					// batches
							$log.debug("This is the current batch "
									+ $scope.currentBatch);
							if (allBatches[0].weeks > 0) {
                                $scope.category = {
									model : null,
									options : []
								};
								caliberDelegate.all
										.getAllCategories()
										.then(
												function(categories) {
													$log.debug(categories);
													$scope.category.options = categories;
												});


								caliberDelegate.all.enumNoteType().then(
										function(noteTypes) {
											$log.debug(noteTypes);
											// do something with note type
										});
								$scope.assessmentType = {
									model : null,
									options : []
								};
								caliberDelegate.all
										.enumAssessmentType()
										.then(
												function(assessmentTypes) {
													$log.debug(assessmentTypes);
													$scope.assessmentType.options = assessmentTypes;
												});
								$log.debug("Batches " + allBatches);
								$log.debug(allBatches);
								

								var totalWeeks = allBatches[allBatches.length-1].weeks; // the
								// number
								// of
								// weeks
								// for
								// that
								// batch
								$log
										.debug("this is the total week for this batch "
												+ allBatches[allBatches.length-1].trainingName
												+ ": " + totalWeeks);

								$scope.currentWeek = totalWeeks;
								/**
								 * *****************************************
								 * getAllAssessmentsForWeek
								 * ***************************************************************************************88888******************************************************************************************************************************************************************************************
								 */
								getAllAssessmentsForWeek(
										$scope.currentBatch.batchId,
										$scope.currentWeek);

							} else
								$scope.currentWeek = null;
						} else {
							$scope.currentBatch = null;
							$scope.currentWeek = null;
						}
						$log.debug("Starting Values: currentBatch and currentWeek");
						$log.debug($scope.currentBatch);
						$log.debug($scope.currentWeek);
						
						$scope.trainees={};						
						
						for(trainee of $scope.currentBatch.trainees){
							$scope.assignTraineeScope(trainee.traineeId);
						}
						
					})(allBatches);

					// default -- view assessments table
					$scope.currentView = true;

					// back button
					$scope.back = function() {
						$scope.currentView = true;
					};

					// batch drop down select
					$scope.selectCurrentBatch = function(index) {
						$scope.currentBatch = $scope.batches[index];
						$log.debug("Selected batch " + index);
					// create new scope of trainees
						$scope.trainees={};						
						for(trainee of $scope.currentBatch.trainees){
							$scope.assignTraineeScope(trainee.traineeId);
						}
						if ($scope.currentBatch.weeks > 0) {
							$scope.currentWeek = $scope.currentBatch.weeks;
							getAllAssessmentsForWeek(
									$scope.currentBatch.batchId,
									$scope.currentWeek);
						} else
							$scope.currentWeek = null;

						getAllAssessmentsForWeek($scope.currentBatch.batchId,
								$scope.currentWeek);
					};

					// select week
					$scope.selectWeek = function(index) {
						
						$scope.currentWeek = $scope.currentBatch.arrayWeeks[index];
						$log.debug("[***********This is the week selected*************]:  "+$scope.currentWeek);
	
						getAllAssessmentsForWeek($scope.currentBatch.batchId,
								$scope.currentWeek);
						
					};
					
					

					// active week
					$scope.showActiveWeek = function(index) {
						if ($scope.currentWeek === $scope.currentBatch.arrayWeeks[index])
							return "active";

					};

					// create week
					$scope.createWeek = function() {
						caliberDelegate.trainer.createWeek($scope.currentBatch.batchId).then(
								function(response) {
									$scope.currentBatch.weeks += 1;
									$scope.currentBatch.arrayWeeks.push($scope.currentBatch.weeks);
									$scope.showActiveWeek($scope.currentBatch.weeks);
									$scope.selectWeek($scope.currentBatch.weeks-1); // the
																					// new
																					// index
																					// of
																					// the
																					// week
																					// selected
								});
					};

					// select assessment from list
					$scope.selectAssessment = function(index) {
						$scope.currentAssessment = $scope.currentAssessment[index];
						$scope.currentView = false;
						/** replace with ajax call to get grades by assessmentId * */
					};

					$scope.addAssessment = function() {
						getAllAssessmentsForWeek($scope.currentBatch.batchId,
								$scope.currentWeek);
						var assessment = {
								batch : $scope.currentBatch,
								type : $scope.assessmentType.model,
								category : angular.fromJson($scope.category.model),
								week : $scope.currentWeek,
								rawScore : $scope.rawScore
						};
						$log.debug(assessment);
						caliberDelegate.trainer
								.createAssessment(assessment)
								.then(
										function(response) {
											$log.debug(response);
											getAllAssessmentsForWeek(
													$scope.currentBatch.batchId,
													$scope.currentWeek);
											if ($scope.currentAssessments > 0)
												$scope.currentAssessments
														.unshift(assessment);
											else
												$scope.currentAssessments = assessment;
											angular.element(
													"#createAssessmentModal")
													.modal("hide");
										});
					};
					$scope.selectedCategories = [];

					$scope.toggleSelection = function(category) {
						var index = $scope.selectedCategories.indexOf(category);
						if (index > -1)
							$scope.selectedCategories.splice(index, 1);
						else
							$scope.selectedCategories.push(category);
					};
					
					/** *******TrainerBatch Notes********** */	
					$scope.getTBatchNote = function (batchId, week){	
				
								caliberDelegate.trainer
										.getTrainerBatchNote(batchId, week)
										.then(
												function(trainerBatchNotes) {
													$scope.trainerBatchNote = trainerBatchNotes[0];
													$log.debug(trainerBatchNotes);
												});
						}			

					// get all assesments
					// **********************************************************8888888888***********************************************************
					function getAllAssessmentsForWeek(batchId, weekNumb) {
						if (!weekNumb)
							return;
						caliberDelegate.trainer
								.getAllAssessmentsForWeek(batchId, weekNumb)
								.then(
										function(data) {
											$log.debug(data);
											$scope.currentAssessments = data;
											$scope.getAllGradesForWeek($scope.currentBatch.batchId,$scope.currentWeek);
											var week = new Week(
													$scope.currentWeek,
													$scope.currentAssessments);
											$scope.currentBatch.displayWeek = week;
											$scope.currentBatch.arrayWeeks = [];
											// create array of assessments
											// mapped by assessment Id;
											$scope.assessmentsById=[]
											
											$scope.generateArrAssessmentById(data);
											
											for(i = 1; i <= $scope.currentBatch.weeks; i++){
												$scope.currentBatch.arrayWeeks.push(i);
											}
											$scope.getTBatchNote($scope.currentBatch.batchId, $scope.currentWeek);
											$scope.getTraineeBatchNotesForWeek($scope.currentBatch.batchId, $scope.currentWeek);
										});
										
					};
					
					$scope.generateArrAssessmentById = function(assessments){
						for(a of assessments){
							$scope.assessmentsById[a.assessmentId] = {};
							$scope.assessmentsById[a.assessmentId].total = 0;
						}						
					}

					/**
					 * Updates Grade if exists, else create new Grade, then
					 * saves to $scope
					 * 
					 * @param gradeId
					 * @param traineeId
					 * @param assessment
					 */

					$scope.updateGrade = function(trainee,assessment) {
						
						// constructs Grade object from the data in table
						var grade = {
							trainee : trainee,
							assessment : assessment,
							dateReceived : new Date(),
							score : angular.fromJson($scope.trainees[trainee.traineeId].assessments[assessment.assessmentId].score)
						};
						/*
						 * if assessment object has gradeId, define it in grade
						 * object
						 */
						if($scope.trainees[trainee.traineeId].assessments[assessment.assessmentId].gradeId){
							grade.gradeId = $scope.trainees[trainee.traineeId].assessments[assessment.assessmentId].gradeId;
						}
						// adds new Grade if not exists, else update,
						// response contains the ID of the created/updated Grade
						caliberDelegate.trainer.addGrade(grade).then(
								function(response) {
									$log.debug("Adding grade to $scope");
									$log.debug(response);
								})
					}; 

					$scope.findGrade = function(traineeId, assessmentId) {
							if($scope && $scope.grades && ($scope.grades[traineeId] === undefined)){ 
								return;
							}
							for(var grade of $scope.grades[traineeId]){
								/*
								 * create a assessment object that contains
								 * gradeId for each $scope.trainees[trainee]
								 */
								if(grade.assessment.assessmentId === assessmentId){
									if($scope.trainees[traineeId].assessments[grade.assessment.assessmentId] === undefined){
										$scope.trainees[traineeId].assessments[grade.assessment.assessmentId] = {};
									}
									if($scope.trainees[traineeId].assessments[grade.assessment.assessmentId].gradeId === undefined){
										$scope.trainees[traineeId].assessments[grade.assessment.assessmentId].gradeId = grade.gradeId;
										$scope.trainees[traineeId].assessments[grade.assessment.assessmentId].score = grade.score;										
									}
									return grade.score;
								}
							}
					};
					
					/* - save trainee note
					   - send to "/note/update"
					   By Jack	
					  */					 
					$scope.saveOrUpdateTraineeNote=function(traineeId){
						console.log($scope.trainees[traineeId].note.content);
						var traineeNote = $scope.trainees[traineeId].note
						var trainee = $scope.currentBatch.trainees.filter(function(trainee) {
							  return trainee.traineeId === traineeId;
							});
						//create noteObj to send to controller
						var noteObj={
								content:traineeNote.content,
								week:$scope.currentWeek,
								trainee:trainee[0],
								type:"TRAINEE",
								batch:$scope.currentBatch
						}
						//if noteId exists, add it to noteObj to get noteObj in db to update
						if($scope.trainees[traineeId].note.noteId !== undefined){
							noteObj.noteId = traineeNote.noteId;
						}
						caliberDelegate.trainer.saveOrUpdateNote(noteObj)
						.then(function(response){
							return response;
						}).then(function(response){
							//set persisted note object into trainee.note
							$log.debug("setting response note obj to trainee scope note obj")
							$scope.trainees[response.data.trainee.traineeId].note = response.data
						});
					
					}

					/**
					 * **********************************************TODO
					 * REFACTOR**************************************
					 */

					/**
					 * **********************************************TODO
					 * POSSIBLE REFACTOR FOR WEEK
					 * PROBLEM**************************************
					 */
					function pushUnique(arr, item) {
						if (arr.indexOf(item) === -1) {
							arr.push(item);
						}
					}

					/**
					 * **********************************************TODO
					 * POSSIBLE REFACTOR**************************************
					 */

					$scope.getTotalAssessmentAvgForWeek = function(assessment,trainees){
						
						if($scope.assessmentTotals === undefined) $scope.assessmentTotals=[];
						if($scope.assessmentTotals[assessment.assessmentId] === undefined) $scope.assessmentTotals[assessment.assessmentId] = {};
							
						$scope.assessmentTotals[assessment.assessmentId].total = 0;
						$scope.assessmentTotals[assessment.assessmentId].count = 0;
							for(var traineeKey in trainees){
								if(trainees[traineeKey].assessments[assessment.assessmentId]){
									$scope.assessmentTotals[assessment.assessmentId].total+= Number(trainees[traineeKey].assessments[assessment.assessmentId].score);								
									$scope.assessmentTotals[assessment.assessmentId].count +=1;
								}
							}
						return $scope.assessmentTotals[assessment.assessmentId].total / $scope.assessmentTotals[assessment.assessmentId].count ;
					}

				});