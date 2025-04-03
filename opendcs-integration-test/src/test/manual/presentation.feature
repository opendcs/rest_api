Feature: Presentation Page Functionality

  As a user of the OpenDCS Web Client
  I want to test the Presentation page
  So that I can ensure it functions correctly

  Scenario: Display of the Presentation table
    Given I am on the Presentation page
    When Observe how the Presentation page is displayed.
    Then The Presentation page should have the following 5 columns:     - Name     - Inherits From     - Last Modified     - Is Production     - Actions

  Scenario: Using the Search function
    Given I am on the Presentation page
    When Use the Search filter
    Then The content in the table should be filtered using the content entered in the Search filter.

  Scenario: Sorting based on a column
    Given I am on the Presentation page
    When Double click on the arrows on the right-side of the header and observe any changes in the table.
    Then When clicking on the arrows on the right-side of a column's header, this will transform the arrow to either point up or down.  When the arrow is pointing up, the table should be sorted in ascending order.  When the arrow is pointing down, the table should be sorted in descending order.

  Scenario: Default value of entries displayed in the Presentation table
    Given I am on the Presentation page
    When Observe the number of entries shown be default in the Presentation page
    Then By default, there should be 10 entries shown in the Presentation page.

  Scenario: Adjusting the number of entries shown in the Presentation page
    Given I am on the Presentation page
    When Change the number of entries shown and observe any changes in the Presentation page
    Then After adjusting the number of entries shown, the Presentation page should display that many entries.

  Scenario: Access to the Presentation editor by clicking on the Plus button.
    Given I am on the Presentation page
    When Click on the Plus button in the upper right of the page.
    Then The Presentation Element editor should be loaded when clicking on the Plus button in the upper right corner of the page.

  Scenario: Display of the Presentation Element editor
    Given I am on the Presentation page
    When Observe the content in the Presentation Element editor.
    Then The Presentation Editor should have a single table with the following 7 columns:     - Data Type Standard     - Data Type Code     - Units     - Fractional Digits     - Min Value     - Max Value     - Actions  Along with this, there should be fields for the Group Name and Inherits from and a toggle for whether the Group Name is production.

  Scenario: Configuring the Presentation Element table
    Given I am on the Presentation page
    When Click the Plus button in the Presentation Element table. and  and A new row should be added. and  and Configure the content for the columns.
    Then A new row should be added to the Properties Element table when the Plus button is clicked.  The entered information should persist in the table after being entered.

  Scenario: Saving updates to the Presentation editor
    Given I am on the Presentation page
    When Click the Save button in the Presentation editor.
    Then When clicking the Save button in the Presentation editor, the Presentationured information should be saved.  If the save is unsuccessful, the user should be informed.

  Scenario: Creating a new row
    Given I am on the Presentation page
    When Access the Presentation Element editor. and  and Configure the information needed in the Presentation Element editor. and  and Observe any changes to the Presentation page.
    Then After successfully configuring the information needed in the Presentation Element editor, the new Presentation Element should be added to the Presentation table with the information configured.

  Scenario: Access to the Presentation Element editor by clicking on an existing row in the Presentation page
    Given I am on the Presentation page
    When Click an existing row in the Presentation table.
    Then Clicking a row in the Presentation table should load the Presentation Element editor populated with the information for the clicked row.

  Scenario: Opening an existing entry (adding, editing, deleting information)
    Given I am on the Presentation page
    When Select an existing row in the Presentation table and access the Presentation editor. and  and Make edits to the information for the Presentation through the editor. Save the updates. and  and Observe the changes to the information for the selected existing entry.
    Then Edits to an existing entry should be done by accessing the Presentation Element editor from an existing entry.  Edits made in the Presentation Element editor should be reflected in the Presentation table after clicking the Save button in the editor.

  Scenario: Copying an existing entry
    Given I am on the Presentation page
    When Click on the menu icon under the Actions column and select the Copy option. and The Presentation Element editor should load. and Observe the content in the Presentation Element editor. and Configure the information for the copied presentation. and Observe any changes to the table.
    Then After selecting the Copy action, the Presentation Element editor should be loaded with the information for the selected row in the table. Edits made in the Presentation Element editor should be reflected in the Presentation table after clicking the Save button in the editor.

  Scenario: Deleting a row
    Given I am on the Presentation page
    When Click on the menu icon under the Actions column and select the Delete option. and Observe any changes to the table.
    Then After selecting the Delete action, the selected row in the table should be deleted.

