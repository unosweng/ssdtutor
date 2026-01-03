<!DOCTYPE html>
<html>
<head>

<style>
/*
* All tables in the main stat page are affected by the below style.
*/
table, td, th {
  border: 1px solid black;
  padding: 5px;
}
table {
  border-collapse: collapse;
}
th {text-align: left;}
#tbBasicStats, #tbAdvStats, #tbRnH {
  width: 50%;
  border: 2px;
  font-size: 18px;
}
tr:hover{
  /* Add a grey background color to the table header and on hover */
  background-color: #f1f1f1;
}

/*
* Keyword Cloud is affected by the below style.
* Note that Keyword Cloud construct an unnumbered list first and then below lines will actually styling the cloud. 
* This code is developed based on the following reference: https://dev.to/alvaromontoro/create-a-tag-cloud-with-html-and-css-1e90
*/
ul.cloud {
  width: 50%;
  list-style: none;
  padding-left: 0;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  line-height: 2.5rem;
}

/* 
* Keyword Cloud: Set keywords default color and size;
*/
ul.cloud a {
  color: #a33;
  display: block;
  font-size: 1.5rem;
  padding: 0.5rem 0.5rem;
  text-decoration: none;
  position: relative;
}

/*
* Keyword Cloud: Set of CSS rules using attribute-based selectors to assign adaptive size.
* The attribute here, data-weight, would have been assigned from the PHP side.
*/
ul.cloud a[data-weight="0"] { --size: 0; }
ul.cloud a[data-weight="1"] { --size: 1; }
ul.cloud a[data-weight="2"] { --size: 2; }
ul.cloud a[data-weight="3"] { --size: 3; }
ul.cloud a[data-weight="4"] { --size: 4; }
ul.cloud a[data-weight="5"] { --size: 5; }
ul.cloud a[data-weight="6"] { --size: 6; }
ul.cloud a[data-weight="7"] { --size: 7; }
ul.cloud a[data-weight="8"] { --size: 8; }
ul.cloud a[data-weight="9"] { --size: 9; }
ul.cloud a[data-weight="10"] { --size: 10; }
/*
* Keyword Cloud: Keywords' size and color tone will be adaptively assigned by below lines.
*/
ul.cloud a {
  --size: 4;
  font-size: calc(var(--size) * 0.2rem + 1.0rem);
  opacity: calc((15 - (9 - var(--size))) / 15); 
}

/*
* Keyword Cloud: Control the hovering effects for the Keywords.
*/
ul.cloud a::before {
  content: "";
  position: absolute;
  top: 0;
  left: 50%;
  width: 0;
  height: 100%;
  background-color: #a23;
  transform: translate(-50%, 0);
  opacity: 0.1;
  transition: width 0.25s;
}
ul.cloud a:focus::before,
ul.cloud a:hover::before {
  width: 100%;
}
</style>
</head>
<body>

<h1>Cyber Tutor Stat Page (Ver.3: Updated on May 26, 2021)</h1>
<hr>

<?php
$q_studentId = intval($_GET['studentId']);

# Connect DB
$con = new mysqli("", "", "", "");
if (!$con) {
        printf("died");
        die('Could not connect: ' . mysqli_error($con));
}

# SQL statements
# NOTE: We pull various statistics from DB at once, instead of pulling each statistics one by one.
#       To do so,
#       1. We first prepare multiple SQL statments.
#       2. The multiple SQL statements are bound into a single statement ($sql).
#       3. We query the single statement to DB, and receive the result ($result) that contains the result for each SQL statement.
#       4. We iterate the result ($result) to process each result. The iteration here is controlled by an index ($cnt).
##############
# Basic Stat #
##############
# sql index 1, $cnt=1, Total number of questions
$sql = "SELECT COUNT(*) FROM Requests;";
# sql index 2; $cnt=2, Total number of responsed questions
$sql .= "SELECT COUNT(DISTINCT Q.requestId) FROM Requests Q INNER JOIN Responses A ON Q.requestId = A.requestId;";
#################
# Advanced Stat #
#################
# sql index 3; $cnt=3, Total number of compile-related questions
$sql .= "SELECT COUNT(*) FROM Requests WHERE errType LIKE '%compile%';";
# sql index 4; $cnt=4, Total number of runtime-related questions
$sql .= "SELECT COUNT(*) FROM Requests WHERE errType LIKE '%runtime%';";
# sql index 5; $cnt=5, Keyword Cloud by Code
$sql .= "SELECT keywordId,keyword,COUNT(requestId) AS questionCount FROM Requests as Q JOIN Keywords as K ON Q.code LIKE CONCAT('%',K.keyword,'%') GROUP BY keywordId ORDER BY questionCount DESC LIMIT 10;";
# sql index 6; $cnt=6, Keyword Cloud by Description
$sql .= "SELECT keywordId,keyword,COUNT(requestId) AS questionCount FROM (SELECT * FROM Requests LIMIT 500) as Q JOIN Keywords as K ON Q.desc LIKE CONCAT('%', K.keyword, '%') GROUP BY keywordId ORDER BY questionCount DESC LIMIT 10;";
#######################
# Rankings and Honors #
#######################
# sql index 7; $cnt=7, Top 3 reviewers received the most upvotes
$sql .= "SELECT reviewerName, SUM(voteUp) AS vote_tot FROM Responses GROUP BY reviewerName ORDER BY vote_tot DESC LIMIT 3;";

	
# Init containers for each statistics
$cnt = 0;
$basic_stat_1 = "";
$basic_stat_2 = "";
$cloud_table_code = "<ul class=\"cloud\" role=\"navigation\" aria-label=\"Webdev keyword cloud\">";
$cloud_table_desc = "<ul class=\"cloud\" role=\"navigation\" aria-label=\"Webdev keyword cloud\">";
$adv_stat_1 = "";
$adv_stat_2 = "";
$rnh_stat_1 = "<table id='tbRnH'>
		<tr>
		<th>Reviewer</th>     
                <th>Total Upvotes</th>
                </tr>";

# Execute multi-SQL queries
if($con -> multi_query($sql)){
	do {
		// Count sql index
		$cnt = $cnt + 1;

		// Store result set
		if ($result = $con -> store_result()){
			if($cnt == 1){
				while ($row = $result -> fetch_row()){
					$basic_stat_1 = $row[0];
				}
				$result -> free_result();
			}
			if($cnt == 2){
				while ($row = $result -> fetch_row()){
					$basic_stat_2 = $row[0];
				}
				$result -> free_result();
			}
			if($cnt == 3){
				while ($row = $result -> fetch_row()){
					$adv_stat_1 = $row[0];
				}
				$result -> free_result();
			}
			if($cnt == 4){
				while ($row = $result -> fetch_row()){
					$adv_stat_2 = $row[0];
				}
				$result -> free_result();
			}
			if($cnt == 5){
				$keyword_cnt_max = 0;
				$keyword_cnt_min = 0;
				$keyword_norm_factor = 10;
				foreach($result as $key=>$row){
					if($key==0){
						$keyword_cnt_max = $row['questionCount'];
					}
					if($key==9){
						$keyword_cnt_min = $row['questionCount'];
					}
				}
				foreach($result as $row){
					$keywordId = $row['keyId'];
					$keyword   = $row['keyword'];
					$counter   = $row['questionCount'];
					$norm_weight = ceil(($counter-$keyword_cnt_min) / ($keyword_cnt_max-$keyword_cnt_min) * $keyword_norm_factor);
					$cloud_table_code .= "<li><a data-weight=\"" . $norm_weight . "\">" . strtoupper($keyword) . "</a></li>";
				}				
				$cloud_table_code .= "</ul>";
				$result -> free_result();
			}
			if($cnt == 6){
				$keyword_cnt_max = 0;
                                $keyword_cnt_min = 0;
                                $keyword_norm_factor = 10;
				foreach($result as $key=>$row){
                                        if($key==0){
                                                $keyword_cnt_max = $row['questionCount'];
                                        }
                                        if($key==9){
                                                $keyword_cnt_min = $row['questionCount'];
                                        }
                                }
				foreach($result as $row){
                                        $keywordId = $row['keyId'];
                                        $keyword   = $row['keyword'];
                                        $counter   = $row['questionCount'];
					$norm_weight = ceil(($counter-$keyword_cnt_min) / ($keyword_cnt_max-$keyword_cnt_min) * $keyword_norm_factor);
					$cloud_table_desc .= "<li><a data-weight=\"" . $norm_weight . "\">" . strtoupper($keyword) . "</a></li>";
                                }
                                $cloud_table_desc .= "</ul>";
                                $result -> free_result();
			}
			if($cnt == 7){
				while ($row = mysqli_fetch_array($result)){
					$rnh_stat_1 .= "<tr>";
					$rnh_stat_1 .= "<td>" . $row['reviewerName'] . "</td>";
					$rnh_stat_1 .= "<td>" . $row['vote_tot'] . "</td>";
					$rnh_stat_1 .= "</tr>";
				}
				$rnh_stat_1 .= "</table>";
				$result -> free_result();
			}
		}
	} while ($con -> next_result());
}
//mysqli_close($con);
//
//
$con->close();


///////////////////////
// Page Construction //
///////////////////////
// Basic Stats 
$qq = (int)$basic_stat_1;
$aq = (int)$basic_stat_2;
$uq = $qq-$aq;
$aq_percent = (float)$aq/(float)$qq * 100;
$aq_percent = number_format($aq_percent, 2, '.', '');
$uq_percent = (float)$uq/(float)$qq * 100;
$uq_percent = number_format($uq_percent, 2, '.', '');
echo "<h2>Basic Stats:</h2>";
echo "<h3>Response coverage</h3>";
echo "<table id='tbBasicStats'>
  <tr>
    <th>Stats</th>
    <th>Values</th>
  </tr>
  <tr>
    <td># of Queried Questions</td>
    <td>" . $qq . "</td>
  </tr>
  <tr>
    <td># of Answered Questions</td>
    <td style='color:blue'>" . $aq . " (" . $aq_percent . "%)" . "</td>
  </tr>
  <tr>
    <td># of Unanswered Questions</td>
    <td style='color:red'>" . $uq . " (" . $uq_percent . "%)" . "</td>
  </tr>
</table>";
echo "<br>";

// Advanced Stats
echo "<hr>";
$err_com = (int)$adv_stat_1;
$err_run = (int)$adv_stat_2;
$err_oth = $qq - ($err_com + $err_run);
$err_com_percent = (float)$err_com/(float)$qq * 100;
$err_com_percent = number_format($err_com_percent, 2, '.', '');
$err_run_percent = (float)$err_run/(float)$qq * 100;
$err_run_percent = number_format($err_run_percent, 2, '.', '');
$err_oth_percent = (float)$err_oth/(float)$qq * 100;
$err_oth_percent = number_format($err_oth_percent, 2, '.', '');
echo "<h2>Advanced Stats:</h2>";
echo "<h3>Distribution of error types in <i>Questions</i></h3>";
echo "<table id='tbAdvStats'>
  <tr>
    <th>Stats</th>
    <th>Values</th>
  </tr>
  <tr>
    <td># of Compile Errors</td>
    <td>" . $err_com . " (" . $err_com_percent . "%)" . "</td>
  </tr>
  <tr>
    <td># of Runtime Errors</td>
    <td>" . $err_run . " (" . $err_run_percent . "%)" . "</td>
  </tr>
  <tr>
    <td># of Other Errors</td>
    <td>" . $err_oth . " (" . $err_oth_percent . "%)" . "</td>
  </tr>
</table>";
echo "<br>";

// Keyword Cloud by Code
echo "<h3>Keyword Cloud (of <i>code fragment</i>)</h3>";		
echo $cloud_table_code;

// Keyword Cloud by Description
echo "<h3>Keyword Cloud (of <i>description</i>)</h3>";
echo $cloud_table_desc;

// Rankings and Honors
echo "<hr>";
echo "<h2>Reviewer Rankings and Honors:</h2>";
echo "<h3>Top 3 the most upvoted reviewers</h3>";
echo $rnh_stat_1;
echo "<br>";

// Q&A Dashboard
echo "<hr>";
echo "<h2><a href=\"ctutor_table_v3.php\">Q&A Dashboard</a></h2>";
?>

</body>
</html>
