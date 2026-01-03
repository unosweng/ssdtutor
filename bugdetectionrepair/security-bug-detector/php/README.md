
# PHP Pages & SQL Statements for Backend Service
The contents in this folder are for the backend services such as SQL statements that allow you to reproduce PHP pages and tables currently implemented on the server. The actual functioning pages exist in the server but are stored in this repo only for an archiving purpose. Note that the DB connection credentials are removed from the files for the security purpose.

## SQL Statements used in PHP Pages
In the server (*/var/www/html*), there are two different PHP pages, *ctutor_stat_v3.php* and *ctutor_table_v3.php*, as shown below:
```
ctutor_stat_v3.php    # The PHP page showing various statistics
ctutor_table_v3.php   # The PHP page showing Q&A Dashboard
```
### SQL Statements used in the ctutor_stat_v3.php page:
*  This [sql](https://git.unl.edu/firebug/firebug/-/blob/ae7a661855a80d2f045a1cc23fca40b5b3005de2/bugdetectionexample/eclipseworkspace/security-bug-detector/php/ctutor_stat_v3.php#L126) statement retrieves the total number of questions.
*  This [sql](https://git.unl.edu/firebug/firebug/-/blob/ae7a661855a80d2f045a1cc23fca40b5b3005de2/bugdetectionexample/eclipseworkspace/security-bug-detector/php/ctutor_stat_v3.php#L128) statement retrieves the total number of responsed questions.
* This [sql](https://git.unl.edu/firebug/firebug/-/blob/ae7a661855a80d2f045a1cc23fca40b5b3005de2/bugdetectionexample/eclipseworkspace/security-bug-detector/php/ctutor_stat_v3.php#L133) statement retrieves the total number of compile-related questions.
* This [sql](https://git.unl.edu/firebug/firebug/-/blob/ae7a661855a80d2f045a1cc23fca40b5b3005de2/bugdetectionexample/eclipseworkspace/security-bug-detector/php/ctutor_stat_v3.php#L135) statement retrieves the total number of runtime-related questions.
* This [sql](https://git.unl.edu/firebug/firebug/-/blob/ae7a661855a80d2f045a1cc23fca40b5b3005de2/bugdetectionexample/eclipseworkspace/security-bug-detector/php/ctutor_stat_v3.php#L137) statement constructs a table to build the *Keyword Cloud* by *code* fragment.
	* In order to adjust the number of items to be joined, please change the number after `LIMIT`, accordingly.
	* In order to adjust the adaptive size of items, please change [this code](https://git.unl.edu/firebug/firebug/-/blob/b132994ca882810ec5191ea3f2f8c8696ea9c1c0/bugdetectionexample/eclipseworkspace/security-bug-detector/php/ctutor_stat_v3.php#L55-77), accordingly.
* This [sql](https://git.unl.edu/firebug/firebug/-/blob/ae7a661855a80d2f045a1cc23fca40b5b3005de2/bugdetectionexample/eclipseworkspace/security-bug-detector/php/ctutor_stat_v3.php#L139) statement constructs a table to build the *Keyword Cloud* by *description*.
	* In order to adjust the number of items to be joined, please change the number after `LIMIT`, accordingly.
	* In order to adjust the adaptive size of items, please change [this code](https://git.unl.edu/firebug/firebug/-/blob/b132994ca882810ec5191ea3f2f8c8696ea9c1c0/bugdetectionexample/eclipseworkspace/security-bug-detector/php/ctutor_stat_v3.php#L55-77), accordingly.
* This [sql](https://git.unl.edu/firebug/firebug/-/blob/ae7a661855a80d2f045a1cc23fca40b5b3005de2/bugdetectionexample/eclipseworkspace/security-bug-detector/php/ctutor_stat_v3.php#L144) statement retrieves the top 3 reviewers received the most upvotes.
### SQL Statements used in the ctutor_table_v3.php page:
* In order to make more field to be sortable, please add the corresponding field [here](https://git.unl.edu/firebug/firebug/-/blob/b132994ca882810ec5191ea3f2f8c8696ea9c1c0/bugdetectionexample/eclipseworkspace/security-bug-detector/php/ctutor_table_v3.php#L10-11), accordingly.
* This [sql](https://git.unl.edu/firebug/firebug/-/blob/b132994ca882810ec5191ea3f2f8c8696ea9c1c0/bugdetectionexample/eclipseworkspace/security-bug-detector/php/ctutor_table_v3.php#L19) statement joins two tables, Requests and Responses, to construct the Q&A dashboard.
	* In order to adjust the number of items to be displayed, please change the number after `LIMIT`, accordingly.
## SQL Statements to Reproduce Tables and Trigger
### Create *Requests* Table
```sql
CREATE TABLE `Requests` (
`requestId` int unsigned NOT NULL AUTO_INCREMENT,
`studentId` int unsigned DEFAULT NULL,
`studentName` varchar(20) DEFAULT NULL,
`vcs` varchar(20) DEFAULT NULL,
`requestedAt` datetime DEFAULT NULL,
`code` varchar(200) DEFAULT NULL,
`errType` varchar(20) DEFAULT NULL,
`desc` text,
PRIMARY KEY (`requestId`)
);
```
### Create *Responses* Table
```sql
CREATE TABLE `Responses` (
`responseId` int unsigned NOT NULL AUTO_INCREMENT,
`requestId` int unsigned NOT NULL,
`reviewerName` varchar(20) DEFAULT NULL,
`reviewedAt` datetime DEFAULT NULL,
`voteUp` int unsigned DEFAULT '0',
`voteDn` int unsigned DEFAULT '0',
`solution` varchar(200) DEFAULT NULL,
PRIMARY KEY (`responseId`),
KEY `requestId` (`requestId`),
CONSTRAINT `Responses_ibfk_1` FOREIGN KEY (`requestId`) REFERENCES `Requests` (`requestId`)
);
```
### Create *Notifications* Table
```sql
CREATE TABLE `Notifications` (
`notiId` int unsigned NOT NULL AUTO_INCREMENT,
`requestId` int unsigned NOT NULL,
`responseId` int unsigned NOT NULL,
PRIMARY KEY (`notiId`),
KEY `requestId` (`requestId`),
KEY `responseId` (`responseId`),
CONSTRAINT `Notifications_ibfk_1` FOREIGN KEY (`requestId`) REFERENCES `Requests` (`requestId`),
CONSTRAINT `Notifications_ibfk_2` FOREIGN KEY (`responseId`) REFERENCES `Responses` (`responseId`)
);
```
### Create *Keywords* Table
```sql
CREATE TABLE `Keywords` (
`keywordId` int unsigned NOT NULL AUTO_INCREMENT,
`keyword` varchar(255) DEFAULT NULL,
PRIMARY KEY (`keywordId`);
```
### Create *notiTrigger* Trigger
```sql
CREATE TRIGGER `notiTrigger` AFTER INSERT ON `Responses` FOR EACH ROW BEGIN
INSERT INTO `Notifications` (`requestId`, `responseId`)
VALUES (NEW.requestId, NEW.responseId);
END
```
## Author
-   **[Chulwoo (Mike) Pack](https://github.com/chulwoopack)**  -  _research associate and developer_ at University of Nebraska-Lincoln