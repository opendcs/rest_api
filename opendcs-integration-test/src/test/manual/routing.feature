Feature: Routing Page Functionality

  As a user of the OpenDCS Web Client
  I want to test the Routing page
  So that I can ensure it functions correctly

  Scenario: Display of the Routing table
    Given I am on the Routing page
    When Observe how the Routing page is displayed.
    Then The Routing page should have the following 5 columns:     - Name     - Data Source     - Consumer     - Last Modified     - Actions

  Scenario: Using the Search function
    Given I am on the Routing page
    When Use the Search filter
    Then The content in the table should be filtered using the content entered in the Search filter.

  Scenario: Sorting based on a column
    Given I am on the Routing page
    When Double click on the arrows on the right-side of the header and observe any changes in the table.
    Then When clicking on the arrows on the right-side of a column's header, this will transform the arrow to either point up or down.  When the arrow is pointing up, the table should be sorted in ascending order.  When the arrow is pointing down, the table should be sorted in descending order.

  Scenario: Default value of entries displayed in the Routing table
    Given I am on the Routing page
    When Observe the number of entries shown be default in the Routing page
    Then By default, there should be 10 entries shown in the Routing page.

  Scenario: Adjusting the number of entries shown in the Routing page
    Given I am on the Routing page
    When Change the number of entries shown and observe any changes in the Routing page
    Then After adjusting the number of entries shown, the Routing page should display that many entries.

  Scenario: Access to the Routing editor by clicking on the Plus button.
    Given I am on the Routing page
    When Click on the Plus button in the upper right of the page.
    Then The Routing Element editor should be loaded when clicking on the Plus button in the upper right corner of the page.

  Scenario: Display of the Routing editor
    Given I am on the Routing page
    When Observe the content in the Routing editor.
    Then The Configs Editor should have 5 separate sections:     - Routing Details     - Date/Time     - Properties     - Platform Selection     - Platform/Message Types

  Scenario: Configuring the Routing Details
    Given I am on the Routing page
    When Configure the information for the following fields: and     - Name and     - Data Source and     - Destination and     - Host/Port and     - Output Format and     - Time Zone and     - Presentation Group and  and Configure the toggles for the following: and     - In-line computations and     - Is Production
    Then Configurations for the information fields should persist.

  Scenario: Configuring the Decoding Script table
    Given I am on the Routing page
    When Click the Plus button in the Decoding Script table. and  and The Decoding Script editor should be loaded. and  and Enter content for the editor.
    Then The Decoding Script editor should be loaded when the Plus button is clicked.  The configured information in the Decoding Script editor should be displayed as a new row in the Decoding Script table after clicking Ok in the editor.

  Scenario: Configuring the Routing table
    Given I am on the Routing page
    When Click the Plus button in the Routing Element table. and  and A new row should be added. and  and Configure the content for the columns.
    Then A new row should be added to the Properties Element table when the Plus button is clicked.  The entered information should persist in the table after being entered.

  Scenario: Saving updates to the Routing editor
    Given I am on the Routing page
    When Click the Save button in the Routing editor.
    Then When clicking the Save button in the Routing editor, the Routingured information should be saved.  If the save is unsuccessful, the user should be informed.

  Scenario: Creating a new row
    Given I am on the Routing page
    When Access the Routing Element editor. and  and Configure the information needed in the Routing Element editor. and  and Observe any changes to the Routing page.
    Then After successfully configuring the information needed in the Routing Element editor, the new Routing Element should be added to the Routing table with the information configured.

  Scenario: Access to the Routing Element editor by clicking on an existing row in the Routing page
    Given I am on the Routing page
    When Click an existing row in the Routing table.
    Then Clicking a row in the Routing table should load the Routing Element editor populated with the information for the clicked row.

  Scenario: Opening an existing entry (adding, editing, deleting information)
    Given I am on the Routing page
    When Select an existing row in the Routing table and access the Routing editor. and  and Make edits to the information for the Routing through the editor. Save the updates. and  and Observe the changes to the information for the selected existing entry.
    Then Edits to an existing entry should be done by accessing the Routing Element editor from an existing entry.  Edits made in the Routing Element editor should be reflected in the Routing table after clicking the Save button in the editor.

  Scenario: Copying an existing entry
    Given I am on the Routing page
    When nan
    Then nan

  Scenario: Deleting a row
    Given I am on the Routing page
    When Click on the menu icon under the Actions column and select the Delete option. and Observe any changes to the table.
    Then After selecting the Delete action, the selected row in the table should be deleted.

