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
