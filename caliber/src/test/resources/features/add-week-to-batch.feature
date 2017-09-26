# feature
@add-week-to-batch
Feature: Add a new week to a Batch
  As a trainer or VP or QC
  I can start a new week
  for a batch

  Scenario: Adding a new week to a Batch
  Given I am on Assess Batch page
  And I have chose the year 2016 tab
  And I have chose "Patrick Walsh - 4/25/16" as Trainer
  And I have clicked the add week button
  When I have clicked yes on the modal
  Then the new week appears on the page.
