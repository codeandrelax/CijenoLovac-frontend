# CijenoLovac-frontend
Frontend part of the CijenoLovac application

# Plan

1. Set Up the Project Structure: Organize packages and add necessary dependencies.
2. Authentication: Implement login and signup with backend integration.
3. QR Code Scanning: Integrate the QR code scanning feature and send the result to the backend.
4. Receipts List: Create a fragment with RecyclerView to display receipts.
5. Statistics and Search: Add a home fragment to display statistics and a search feature for comparing prices.
6. User Profile and Settings: Add a profile section with the ability to log out.
7. Polish the UI: Use Material Design, add dark mode, and test the app on multiple devices.
8. Deployment: Generate a signed APK and prepare for release on Google Play.

# Detailed plan
1. Create different packages in your project to keep it organized:

- com.yourappname.ui → For all your activity and fragment classes.
- com.yourappname.api → For handling server communication (API classes).
- com.yourappname.model → For data classes/models (like Receipt, User, etc.).
- com.yourappname.utils → For helper classes and utility functions.


2. Add necessary libraries to build.gradle for various functionalities:

Retrofit: For API communication.
Gson: For JSON parsing.
Google ML Kit: For QR code scanning.
Jetpack Navigation: For fragment management and navigation.
Material Components: For Material Design elements.
Room (optional): If you want to store receipts offline or cache data locally.
groovy


implementation 'com.google.android.material:material:1.5.0'
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.google.code.gson:gson:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
implementation 'androidx.navigation:navigation-fragment:2.4.0'
implementation 'androidx.navigation:navigation-ui:2.4.0'

3. Design two XML layout files for Login and Signup screens.

Use EditText fields for email and password input.
Use Button for login/signup actions.


## Phase: QR Code Scanning
Goal: Integrate the QR code scanning feature into the app.

1. Create a Fragment for QR Scanning
Implement a Fragment that uses the Google ML Kit to scan QR codes.
You’ve already done this, so ensure that after scanning the code, the result is sent to the server using a POST request
2. Send the QR Code Data to the Server
Implement the logic that sends the QR code URL or data to your backend.
Receive and display the details of the purchased item (name, cost, purchase date, etc.) on a new Receipt Details Fragment.
