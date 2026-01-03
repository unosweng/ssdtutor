# *FireBugs* Request & Response Forms
This repository branch (*dev_backend_form_v1*) contains an implementation of forms within Eclipse Plug-in that supports data communication with backend service. This README will list the implemented features and parameters, To-do list, and an important Note. Note that this implementation is not fully modulated at this version. For the viability purpose, the implementation is developed on the top of the view module (*CryptoMisuseDetection.java*). 
**Note**: The [*php*](./php) folder in this repository contains contents for the backend service, such as PHP pages. The actual functioning pages exist in the server but are stored in this repo for an archiving purpose. In other words, it is not necessary to hold this *php* folder for running FireBugs Request and Response Forms.

## Overview of APIs / Parameters
The developed features to communicate with DB are listed below:
- Form-relevant Features: 
	- REQUEST FORM ([code lines](https://git.unl.edu/firebug/firebug/-/blob/2a17d1aa9c43509be5e80454edb01547598a48fe/bugdetectionexample/eclipseworkspace/security-bug-detector/src/view/CryptoMisuseDetection.java#L81-278))
		- *btnSearch*: **Search** a particular question by keyword (e.g., student ID, code, etc.) from DB
		- *btnSend*: **Send** a student's question to DB
		- *btnShowAll*: **Retrieve** all questions from DB
	- VOTE ([code lines](https://git.unl.edu/firebug/firebug/-/blob/2a17d1aa9c43509be5e80454edb01547598a48fe/bugdetectionexample/eclipseworkspace/security-bug-detector/src/view/CryptoMisuseDetection.java#L283-416))
		- *btnVoteUp*: **Update** upvote value of a particular response in DB
		- *btnVoteDn*: **Udpate** downvote value of a particular response in DB
	- RESPONSE FORM ([code lines](https://git.unl.edu/firebug/firebug/-/blob/2a17d1aa9c43509be5e80454edb01547598a48fe/bugdetectionexample/eclipseworkspace/security-bug-detector/src/view/CryptoMisuseDetection.java#L422-549))
		- *btnShowUnansweredQuestions*: **Retrieve** students' questions unanswered by any instructor from DB  
		- *btnResponse*: **Send** a instructor's response to DB
 
 - Other Feature:
	 - Periodical Pooling ([code lines](https://git.unl.edu/firebug/firebug/-/blob/2a17d1aa9c43509be5e80454edb01547598a48fe/bugdetectionexample/eclipseworkspace/security-bug-detector/src/view/CryptoMisuseDetection.java#L555-695))
		 - *param_period*: Determine how **frequent** Eclipse will pool a notification from DB

## To-Do
- Need to make the "check-in" link in the notification pop-up box enable to update the Notification table.
- Now, the *periodical pooling* pools the information from DB for the HARDCODED student (i.e., 99999999) by [this code line](https://git.unl.edu/firebug/firebug/-/blob/2a17d1aa9c43509be5e80454edb01547598a48fe/bugdetectionexample/eclipseworkspace/security-bug-detector/src/view/CryptoMisuseDetection.java#L590). In future, one should make this to be automatic by replacing the corresponding line based on one's design. For example, simply one might think of designing a user to enter a student's ID into the textbox, then the below line should get a string from the corresponding textbox.
- Make notification frame size to be adaptive to the size of pooled textual-contents. Currently, the frame size is fixed (300x200). In order to make it flexible, please update [this code line](https://git.unl.edu/firebug/firebug/-/blob/2a17d1aa9c43509be5e80454edb01547598a48fe/bugdetectionexample/eclipseworkspace/security-bug-detector/src/view/CryptoMisuseDetection.java#L626), accordingly.

## Note
For the security concern, the DB connection information in [data.properties](./src/database.properties) file is not provided in this repository. Please contact author to get the information.
## Author
-   **[Chulwoo (Mike) Pack](https://github.com/chulwoopack)**  -  _research associate and developer_ at University of Nebraska-Lincoln
