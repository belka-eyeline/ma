# Google Sheets Plugin
## Goal

To enable interworking between a MiniApps bot and Google Sheets whereby the bot uses a Google Sheets document as a database. It should make possible e.g. registration of users and recording their data into the table. The plugin also can:

    - retrieve the last record from the table;
    - notify administrators by their phone numbers (in Telegram) and email addresses;
    - localize answers the system gives to users coming from various places.

## Usage

To enable interworking between the plugin and your Google Sheet document allow miniapps@miniappstesterbot.iam.gserviceaccount.com to edit it. To do it:

a. Create the sheet and press the SHARE button in the upper right corner:
![](https://i.imgur.com/lEjAFXd.png)
b. Then insert the above shown address in the People entry box and press Send button:
![](https://i.imgur.com/X8rP7vb.png)

Then you should open MiniApps Visual Editor and configure your Google Sheets bot (refer to [Constructing Google Sheets Bot]()) and frame an appropriate request that the bot should send to the Google Sheets plugin (refer to [Framing Request to Google Sheets Plugin]()).

## Constructing Google Sheets Bot

This chapter explains how to configure your bot using the Visual Editor to make it interoperable with Google Sheets Plugin.

### General Information About Visual Editor Configuration

    1. Connection of page names and spreadsheet columns.

    When working with pages in the Visual Editor you can name them by setting their IDs:
    ![](https://i.imgur.com/IMJDrNS.png)

    The page ID will be the same as the name of its associated spreadsheet column:

    The information contained in this column cells will be the recorded answers that users give on the associated page.
    Button IDs in the Visual Editor and their corresponding values in the column, answer evaluation (parameter evaluable 1 - evaluate).
    Button IDs set in the Visual Editor are recorded to cells of the spreadsheet when these buttons are pushed.




    If keyboard input is needed (e.g. the user's email), you can set the default target page:




    The answer is evaluated by the button number and the corresponding score:



    If the answer is right, the user scores 1 point, if the answer is wrong - 0 points.

    Calling Plugin:

        Create a page and change its type to External Service.




        Insert the URL that you have configured following the Framing Request to Google Sheets instruction.



        Switch on Transfer user answers, if you want to use the function /

        If needed, add the callback parameter and name it "callback":





        Choose the page which callback should lead to:
