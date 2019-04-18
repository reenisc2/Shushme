# ShushMe
Google Places API demo app. The core of this project is the Shushme project from Udacity UD855, Advanced Android Apps. Unfortunately, the app as developed in the course depends on the deprecated Places for Android SDK, including the PlacePicker. This version of Shushme is a first attempt at getting some locations to test the geofencing aspects of the app.

## Developer Instructions

To use this app, you will need to sign up for a Google Account, get an API key, and enable the appropriate SDKs.

Clone or download the project.

In the res/values/strings.xml, replace the "Insert API key" with your key in the string "api_key".

Build the project and run it on your phone!

## User Instructions

There are three settings that must be enabled:
- Enable Geofences: this turns the geofencing on/off
- Location permissions: the first time you run the app, you must allow Shushme to access the device's location. Select "Allow", which will the set the checkbox and gray out the permission; you will not be asked again unless the app is uninstalled and then installed again.
- Ringer permissions: the first time you run the app, you will be asked to allow the app to change the "Do not disturb" settings. As with location permissions, selecting "Allow" willl set the checkox and gray out the permission, and you will not need to grant permission again unless the app is uninstalled.

After permissions have been granted, you can add locations to be fenced. Press "Add New Locaton", which will bring up a map centered on your current location. Select a place, then the back arrow to return to the main screen.

## Known limitations, necessary improvements

If the device is inside a geofence and the Enable Geofences switch is moved to the off position, the ringer will not be restored to its normal mode; i.e., it will remained silenced until the user manually restores the ringer.

There is durrently no way to remove locations for geofences.

Refactoring is necessary to adhere to best practices. This version was simply an attempt to get something working without the convenience of PlacePicker.

### Version 1.0.5 (4/5/2019)

Added a "Delete Location" button. This is proof of concept, and currently deletes the locations in the reverse order from their insertion.

### Version 1.0.6 (4/12/2019)

It is now possible to add more than one location on the map when the user selects "Add A Location"

### Version 1.0.7 (4/16/2019)

WIP - Context menu or Alert Dialog to set radius, or to choose to delete a location. Does not work yet, and values are not saved.
Moved API key to a separate file that is not checked in (no accidental checkins).

### Version 1.0.8 (4/17/2019)

Short click instead of long click when selecting a location from the list.

### Version 1.0.9 (4/18/2019)

If no radius is set, the default of 100.0 meters is used. To change the default, use the preferences setting from the action bar.
When a location is added, the radius is displayed below the address on the list on the main screen.
Click on an item to change the radius or to delete the item. If the radius is changed, that change will be reflected in the list.
On the alert dialog box to change the radius or delete the item, the buttons are now "Cancel" or "OK" instead of "Cancel" and "Save".

