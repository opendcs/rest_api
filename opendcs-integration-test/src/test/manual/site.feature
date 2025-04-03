Feature: Site Page Functionality

  As a user of the OpenDCS Web Client
  I want to test the Site page
  So that I can ensure it functions correctly

  Scenario: Display of the Site table
    Given I am on the Site page
    When Observe how the Sites page is displayed.
    Then The Sites page should have the following 4 columns:     - Site Name     - Configured Site Names     - Description     - Actions

  Scenario: Using the Search function
    Given I am on the Site page
    When Use the Search filter
    Then The content in the table should be filtered using the content entered in the Search filter.

  Scenario: Sorting based on a column
    Given I am on the Site page
    When Double click on the arrows on the right-side of the header and observe any changes in the table.
    Then When clicking on the arrows on the right-side of a column's header, this will transform the arrow to either point up or down.  When the arrow is pointing up, the table should be sorted in ascending order.  When the arrow is pointing down, the table should be sorted in descending order.

  Scenario: Default value of entries displayed in the Sites table
    Given I am on the Site page
    When Observe the number of entries shown be default in the Sites page
    Then By default, there should be 10 entries shown in the Sites page.

  Scenario: Adjusting the number of entries shown in the Sites page
    Given I am on the Site page
    When Change the number of entries shown and observe any changes in the Sites page
    Then After adjusting the number of entries shown, the Sites page should display that many entries.

  Scenario: Access to the Sites editor by clicking on the Plus button.
    Given I am on the Site page
    When Click on the Plus button in the upper right of the page.
    Then The Sites editor should be loaded when clicking on the Plus button in the upper right corner of the page.

  Scenario: Display of the Sites editor
    Given I am on the Site page
    When Observe the content in the Sites editor.
    Then The Sites Editor should have 3 separate sections:     - Details     - Site Names     - Site Names

  Scenario: Configuring the Site Names table
    Given I am on the Site page
    When Click the Plus button in the Site Names table. and  and A new row should be added. and  and Configure the content for the Type and Identifier.
    Then A new row should be added to the Site Names table when the Plus button is clicked.  The entered information under the Type and Identifier should persist in the table after being entered.

  Scenario: Configuring the Properties table
    Given I am on the Site page
    When Click the Plus button in the Properties table. and  and A new row should be added. and  and Configure the content for the Property Name and Vaue.
    Then A new row should be added to the Properties table when the Plus button is clicked.  The entered information under the Property Name and Value should persist in the table after being entered.

  Scenario: Configuring the Details in the Sites editor
    Given I am on the Site page
    When Configure the information for the following fields: and     - Latitude and     - Longitude and     - Elevation and     - Elev Units and     - Nearest City and     - Time Zone and     - State and     - Country and     - Region and     - Public Name and     - Description
    Then The information configured in the Details section should persist after being entered.

  Scenario: Saving updates to the Sites editor
    Given I am on the Site page
    When Click the Save button in the Sites editor.
    Then When clicking the Save button in the Sites editor, the configured information should be saved.  If the save is unsuccessful, the user should be informed.

  Scenario: Creating a new row
    Given I am on the Site page
    When Access the Sites editor. and  and Configure the information needed in the Sites editor. and  and Observe any changes to the Sites page.
    Then After successfully configuring the information needed in the Sites editor, the new Site should be added to the Sites table with the information configured.

  Scenario: Access to the Sites data editor by clicking on an existing row in the Sites page
    Given I am on the Site page
    When Click an existing row in the Site table.
    Then Clicking a row in the Sites table should load the Sites editor populated with the information for the clicked row.

  Scenario: Opening an existing entry (adding, editing, deleting information)
    Given I am on the Site page
    When Select an existing row in the Sites table and access the Sites editor. and Make edits to the information for the Site through the editor. Save the updates. and Observe the changes to the information for the selected existing entry.
    Then Edits to an existing entry should be done by accessing the Sites editor from an existing entry. Edits made in the Sites editor should be reflected in the Sites table after clicking the Save button in the editor.

  Scenario: Deleting a row
    Given I am on the Site page
    When Click on the menu icon under the Actions column and select the Delete option. and Observe any changes to the table.
    Then After selecting the Delete action, the selected row in the table should be deleted.

