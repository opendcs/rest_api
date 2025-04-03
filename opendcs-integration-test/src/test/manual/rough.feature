Feature: Rough Page Functionality

  As a user of the OpenDCS Web Client
  I want to test the Rough page
  So that I can ensure it functions correctly

  Scenario: Unnamed Scenario
    Given I am on the Rough page
    When nan
    Then nan

  Scenario: Loading the site
    Given I am on the Rough page
    When Access the following URL: and https://dc3-vl-cwms-rdr:8443/opendcs-web-client/portal/login
    Then Must find a certificate  The user should be prompted to agree to a disclaimer regarding accessing the U.S. Government Infromation System.

  Scenario: Unnamed Scenario
    Given I am on the Rough page
    When Click the Agree button
    Then After clicking the Agree button, the OpenDCS Web Client Login page should be loaded.

  Scenario: Unnamed Scenario
    Given I am on the Rough page
    When Observe the OpenDCS Web Client Login page
    Then The OpenDCS Web Client Login page should have a Login with the option to either Login or the alternative of "Don't have an account?"

  Scenario: Unnamed Scenario
    Given I am on the Rough page
    When Click the Login button
    Then The user should be promted to enter their PIN

  Scenario: Display of the OpenDCS Web Client after successfully logging in
    Given I am on the Rough page
    When Successfully log in to the OpenDCS Web Client
    Then The OpenDCS Web Client should have 3 groups: - Decodes Database Editor - Computations - Reflist Editor  Right after successfully logging in, the OpenDCS Web Client should have the Decodes Database Editor selected and the Platforms item selected and displayed

  Scenario: Display of the Menu
    Given I am on the Rough page
    When nan
    Then This should be displayed in the upper left corner.

  Scenario: Clicking the Menu
    Given I am on the Rough page
    When Click on the Menu and observe any changes in the OpenDCS Web Client.
    Then The whole menu should collapse.

  Scenario: Hover over items in collapsed menu
    Given I am on the Rough page
    When After collapsing the menu, hover over a menu item and observe any changes in the OpenDCS Web Client.
    Then There should be a menu listing the options under the Decodes Database Editor.

  Scenario: Determine the number of items under the Decodes Database Editor Items
    Given I am on the Rough page
    When Note the available options under the Decodes Database Editor group.
    Then There should be 8 options:     - Platforms     - Sites     - Configs     - Presentation     - Routing     - Sources     - Netlists     - Schedule Entry

  Scenario: Display of the Platform table
    Given I am on the Rough page
    When Observe how the Platforms page is displayed.
    Then The Platforms page should have the following 7 columns:     - Platform     - Agency     - Transport-ID     - Config     - Expiration     - Description     - Action

  Scenario: Using the Search function
    Given I am on the Rough page
    When Use the Search filter
    Then The content in the table should be filtered using the content entered in the Search filter.

  Scenario: Sorting based on a column
    Given I am on the Rough page
    When Double click on the arrows on the right-side of the header and observe any changes in the table.
    Then When clicking on the arrows on the right-side of a column's header, this will transform the arrow to either point up or down.  When the arrow is pointing up, the table should be sorted in ascending order.  When the arrow is pointing down, the table should be sorted in descending order.

  Scenario: Default value of entries displayed in the Platforms table
    Given I am on the Rough page
    When Observe the number of entries shown be default in the Platforms page
    Then By default, there should be 10 entries shown in the Platforms page.

  Scenario: Adjusting the number of entries shown in the Platforms page
    Given I am on the Rough page
    When Change the number of entries shown and observe any changes in the Platforms page
    Then After adjusting the number of entries shown, the Platforms page should display that many entries.

  Scenario: Access to the Platforms editor by clicking on the Plus button.
    Given I am on the Rough page
    When Click on the Plus button in the upper right of the page.
    Then The Platforms editor should be loaded when clicking on the Plus button in the upper right corner of the page.

  Scenario: Display of the Platforms editor
    Given I am on the Rough page
    When Observe the content in the Platforms editor.
    Then The Platforms Editor should have 4 separate sections:     - Details     - Platform Sensor Information     - Properties     - Transport Media

  Scenario: Configuring the Details in the Platforms editor
    Given I am on the Rough page
    When Configure the information for the following fields: and     - Site and     - Designator and     - Config and     - Owner Agency and     - Description and  and Toggle the Prodution radio button.
    Then The information configured in the Details section should persist after being entered.

  Scenario: Configuring the Properties table
    Given I am on the Rough page
    When Click the Plus button in the Properties table. and  and A new row should be added. and  and Configure the content for the Property Name and Vaue.
    Then A new row should be added to the Properties table when the Plus button is clicked.  The entered information under the Property Name and Value should persist in the table after being entered.

  Scenario: Configuring the Transport Media table
    Given I am on the Rough page
    When Click the Plus button in the Transport Media table. and  and The Transport Medium editor should be loaded. and  and Enter content for hte General Details section. and  and When applicable, configure the information for parameters based on the selected Medium Type.
    Then The Transport Medium editor should be loaded when the Plus button is clicked.  The Parameters section in the bottom half of the Transport Medium editor should update based on the selected Medium Type.  The configured information in the Transport Medium editor should be displayed a sa new row in the Transport Medium table after clicking Ok in the editor.

  Scenario: Saving updates to the Platforms editor
    Given I am on the Rough page
    When Click the Save button in the Platforms editor.
    Then When clicking the Save button in the Platforms editor, the configured information should be saved.  If the save is unsuccessful, the user should be informed.

  Scenario: Creating a new row
    Given I am on the Rough page
    When Access the Platforms editor. and  and Configure the information needed in the Platfrom editor. and  and Observe any changes to the Platforms page.
    Then After successfully configuring the information needed in the Platforms editor, the new platform should be added to the Platforms table with the information configured.

  Scenario: Access to the Platforms data editor by clicking on an existing row in the Platforms page
    Given I am on the Rough page
    When Click an existing row in the Platform table.
    Then Clicking a row in the Platforms table should load the Platforms editor populated with the information for the clicked row.

  Scenario: Opening an existing entry (adding, editing, deleting information)
    Given I am on the Rough page
    When Select an existing row in the platforms table and access the Platforms editor. and Make edits to the information for the platform through the editor. Save the updates. and Observe the changes to the information for the selected existing entry.
    Then Edits to an existing entry should be done by accessing the Platforms editor from an existing entry. Edits made in the Platforms editor should be reflected in the Platforms table after clicking the Save button in the editor.

  Scenario: Copying an existing entry
    Given I am on the Rough page
    When nan
    Then nan

  Scenario: Deleting a row
    Given I am on the Rough page
    When nan
    Then nan

  Scenario: Display of the Platform table
    Given I am on the Rough page
    When Observe how the Platforms page is displayed.
    Then The Platforms page should have the following 7 columns:     - Platform     - Agency     - Transport-ID     - Config     - Expiration     - Description     - Action

  Scenario: Using the Search function
    Given I am on the Rough page
    When Use the Search filter
    Then The content in the table should be filtered using the content entered in the Search filter.

  Scenario: Sorting based on a column
    Given I am on the Rough page
    When Double click on the arrows on the right-side of the header and observe any changes in the table.
    Then When clicking on the arrows on the right-side of a column's header, this will transform the arrow to either point up or down.  When the arrow is pointing up, the table should be sorted in ascending order.  When the arrow is pointing down, the table should be sorted in descending order.

  Scenario: Default value of entries displayed in the Platforms table
    Given I am on the Rough page
    When Observe the number of entries shown be default in the Platforms page
    Then By default, there should be 10 entries shown in the Platforms page.

  Scenario: Adjusting the number of entries shown in the Platforms page
    Given I am on the Rough page
    When Change the number of entries shown and observe any changes in the Platforms page
    Then After adjusting the number of entries shown, the Platforms page should display that many entries.

  Scenario: Access to the Platforms editor by clicking on the Plus button.
    Given I am on the Rough page
    When Click on the Plus button in the upper right of the page.
    Then The Platforms editor should be loaded when clicking on the Plus button in the upper right corner of the page.

  Scenario: Display of the Platforms editor
    Given I am on the Rough page
    When Observe the content in the Platforms editor.
    Then The Platforms Editor should have 4 separate sections:     - Details     - Platform Sensor Information     - Properties     - Transport Media

  Scenario: Configuring the Details in the Platforms editor
    Given I am on the Rough page
    When Configure the information for the following fields: and     - Site and     - Designator and     - Config and     - Owner Agency and     - Description and  and Toggle the Prodution radio button.
    Then The information configured in the Details section should persist after being entered.

  Scenario: Configuring the Properties table
    Given I am on the Rough page
    When Click the Plus button in the Properties table. and  and A new row should be added. and  and Configure the content for the Property Name and Vaue.
    Then A new row should be added to the Properties table when the Plus button is clicked.  The entered information under the Property Name and Value should persist in the table after being entered.

  Scenario: Configuring the Transport Media table
    Given I am on the Rough page
    When Click the Plus button in the Transport Media table. and  and The Transport Medium editor should be loaded. and  and Enter content for hte General Details section. and  and When applicable, configure the information for parameters based on the selected Medium Type.
    Then The Transport Medium editor should be loaded when the Plus button is clicked.  The Parameters section in the bottom half of the Transport Medium editor should update based on the selected Medium Type.  The configured information in the Transport Medium editor should be displayed a sa new row in the Transport Medium table after clicking Ok in the editor.

  Scenario: Saving updates to the Platforms editor
    Given I am on the Rough page
    When Click the Save button in the Platforms editor.
    Then When clicking the Save button in the Platforms editor, the configured information should be saved.  If the save is unsuccessful, the user should be informed.

  Scenario: Creating a new row
    Given I am on the Rough page
    When Access the Platforms editor. and  and Configure the information needed in the Platfrom editor. and  and Observe any changes to the Platforms page.
    Then After successfully configuring the information needed in the Platforms editor, the new platform should be added to the Platforms table with the information configured.

  Scenario: Access to the Platforms data editor by clicking on an existing row in the Platforms page
    Given I am on the Rough page
    When Click an existing row in the Platform table.
    Then Clicking a row in the Platforms table should load the Platforms editor populated with the information for the clicked row.

  Scenario: Opening an existing entry (adding, editing, deleting information)
    Given I am on the Rough page
    When Select an existing row in the platforms table and access the Platforms editor. and Make edits to the information for the platform through the editor. Save the updates. and Observe the changes to the information for the selected existing entry.
    Then Edits to an existing entry should be done by accessing the Platforms editor from an existing entry. Edits made in the Platforms editor should be reflected in the Platforms table after clicking the Save button in the editor.

  Scenario: Copying an existing entry
    Given I am on the Rough page
    When nan
    Then nan

  Scenario: Deleting a row
    Given I am on the Rough page
    When nan
    Then nan

