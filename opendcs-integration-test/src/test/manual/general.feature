Feature: General Page Functionality

  As a user of the OpenDCS Web Client
  I want to test the General page
  So that I can ensure it functions correctly

  Scenario: Loading the site
    Given I am on the General page
    When Access the following URL: and https://dc3-vl-cwms-rdr:8443/opendcs-web-client/portal/login
    Then Must find a certificate  The user should be prompted to agree to a disclaimer regarding accessing the U.S. Government Infromation System.

  Scenario: Unnamed Scenario
    Given I am on the General page
    When Click the Agree button
    Then After clicking the Agree button, the OpenDCS Web Client Login page should be loaded.

  Scenario: Unnamed Scenario
    Given I am on the General page
    When Observe the OpenDCS Web Client Login page
    Then The OpenDCS Web Client Login page should have a Login with the option to either Login or the alternative of "Don't have an account?"

  Scenario: Unnamed Scenario
    Given I am on the General page
    When Click the Login button
    Then The user should be promted to enter their PIN

  Scenario: Display of the OpenDCS Web Client after successfully logging in
    Given I am on the General page
    When Successfully log in to the OpenDCS Web Client
    Then The OpenDCS Web Client should have 3 groups: - Decodes Database Editor - Computations - Reflist Editor  Right after successfully logging in, the OpenDCS Web Client should have the Decodes Database Editor selected and the Platforms item selected and displayed

  Scenario: Display of the Menu
    Given I am on the General page
    When nan
    Then This should be displayed in the upper left corner.

  Scenario: Clicking the Menu
    Given I am on the General page
    When Click on the Menu and observe any changes in the OpenDCS Web Client.
    Then The whole menu should collapse.

  Scenario: Hover over items in collapsed menu
    Given I am on the General page
    When After collapsing the menu, hover over a menu item and observe any changes in the OpenDCS Web Client.
    Then There should be a menu listing the options under the Decodes Database Editor.

  Scenario: Determine the number of items under the Decodes Database Editor Items
    Given I am on the General page
    When Note the available options under the Decodes Database Editor group.
    Then There should be 8 options:     - Platforms     - Sites     - Configs     - Presentation     - Routing     - Sources     - Netlists     - Schedule Entry

  Scenario: Determine the number of items under the Computations Items
    Given I am on the General page
    When Note the available options under the Computations group.
    Then There should be 3 options:     - Algorithms     - Computations     - Processes

  Scenario: Determine the number of items under the Reflist Editor Items
    Given I am on the General page
    When Note the available options under the Reflist Editor group.
    Then There should be 4 options:     - Enumerations     - Engineering Units     - EU Conversions     - Seasons

