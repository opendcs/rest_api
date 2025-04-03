Feature: Configs Page Functionality

  As a user of the OpenDCS Web Client
  I want to test the Configs page
  So that I can ensure it functions correctly

  Scenario: Display of the Configs table
    Given I am on the Configs page
    When Observe how the Configs page is displayed.
    Then The Configs page should have the following 5 columns:     - Name     - Equipment Id     - # Platforms     - Description     - Actions

  Scenario: Using the Search function
    Given I am on the Configs page
    When Use the Search filter
    Then The content in the table should be filtered using the content entered in the Search filter.

  Scenario: Sorting based on a column
    Given I am on the Configs page
    When Double click on the arrows on the right-side of the header and observe any changes in the table.
    Then When clicking on the arrows on the right-side of a column's header, this will transform the arrow to either point up or down.  When the arrow is pointing up, the table should be sorted in ascending order.  When the arrow is pointing down, the table should be sorted in descending order.

  Scenario: Default value of entries displayed in the Configs table
    Given I am on the Configs page
    When Observe the number of entries shown be default in the Configs page
    Then By default, there should be 10 entries shown in the Configs page.

  Scenario: Adjusting the number of entries shown in the Configs page
    Given I am on the Configs page
    When Change the number of entries shown and observe any changes in the Configs page
    Then After adjusting the number of entries shown, the Configs page should display that many entries.

  Scenario: Access to the Configs editor by clicking on the Plus button.
    Given I am on the Configs page
    When Click on the Plus button in the upper right of the page.
    Then The Configs editor should be loaded when clicking on the Plus button in the upper right corner of the page.

  Scenario: Display of the Configs editor
    Given I am on the Configs page
    When Observe the content in the Configs editor.
    Then The Configs Editor should have 2 separate sections:     - Sensors     - Decoding Scripts

  Scenario: Configuring the Sensors table
    Given I am on the Configs page
    When Click the Plus button in the Sensors table. and  and The Config Sensor editor should be loaded. and  and Enter content for the editor.
    Then The Config Sensor editor should be loaded when the Plus button is clicked.  The configured information in the Config Sensor editor should be displayed as a new row in the Sensors table after clicking Ok in the editor.

  Scenario: Configuring the Decoding Script table
    Given I am on the Configs page
    When Click the Plus button in the Decoding Script table. and  and The Decoding Script editor should be loaded. and  and Enter content for the editor.
    Then The Decoding Script editor should be loaded when the Plus button is clicked.  The configured information in the Decoding Script editor should be displayed as a new row in the Decoding Script table after clicking Ok in the editor.

  Scenario: Saving updates to the Configs editor
    Given I am on the Configs page
    When Click the Save button in the Configs editor.
    Then When clicking the Save button in the Configs editor, the configured information should be saved.  If the save is unsuccessful, the user should be informed.

  Scenario: Creating a new row
    Given I am on the Configs page
    When Access the Configs editor. and  and Configure the information needed in the Configs editor. and  and Observe any changes to the Configs page.
    Then After successfully configuring the information needed in the Configs editor, the new Config should be added to the Configs table with the information configured.

  Scenario: Access to the Configs data editor by clicking on an existing row in the Configs page
    Given I am on the Configs page
    When Click an existing row in the Config table.
    Then Clicking a row in the Configs table should load the Configs editor populated with the information for the clicked row.

  Scenario: Opening an existing entry (adding, editing, deleting information)
    Given I am on the Configs page
    When Select an existing row in the Configs table and access the Configs editor. and Make edits to the information for the Config through the editor. Save the updates. and Observe the changes to the information for the selected existing entry.
    Then Edits to an existing entry should be done by accessing the Configs editor from an existing entry. Edits made in the Configs editor should be reflected in the Configs table after clicking the Save button in the editor.

  Scenario: Copying an existing entry
    Given I am on the Configs page
    When Click on the menu icon under the Actions column and select the Copy option. and The Configs editor should load. and Observe the content in the Configs editor. and Configure the information for the copied config. and Observe any changes to the table.
    Then After selecting the Copy action, the Configs editor should be loaded with the information for the selected row in the table. Edits made in the Configs editor should be reflected in the Configs table after clicking the Save button in the editor.

  Scenario: Deleting a row
    Given I am on the Configs page
    When Click on the menu icon under the Actions column and select the Delete option. and Observe any changes to the table.
    Then After selecting the Delete action, the selected row in the table should be deleted.

