<?php

# Connect DB
$con = new mysqli("", "", "", "");
if (!$con) {
    echo "Failed to connect to MySQL: " . $mysqli -> connect_error;
    exit();
}

$columns = array('requestId', 'studentId', 'studentName', 'errType', 'requestedAt');
$column = isset($_GET['column']) && in_array($_GET['column'], $columns) ? $_GET['column'] : $columns[0];

$date_from = isset($_GET['date_from']) ? $_GET['date_from'] : date("1980-01-01");
$date_to   = isset($_GET['date_to']) ? $_GET['date_to'] : date("3021-12-12");
// Get the sort order for the column, ascending or descending, default is ascending.
$sort_order = isset($_GET['order']) && strtolower($_GET['order']) == 'desc' ? 'DESC' : 'ASC';

// Get the result...
if ($result = $con->query('SELECT Q.requestId,studentId,studentName,errType,`desc`,requestedAt,code,responseId,reviewerName,solution,reviewedAt,voteUp,voteDn FROM Requests Q LEFT JOIN Responses A ON Q.requestId=A.requestId WHERE ((\'' . $date_from . '\' < requestedAt AND requestedAt < \'' . $date_to . '\') OR (requestedAt IS NULL)) ORDER BY ' . $column . ' ' . $sort_order . ' LIMIT 100;')) {

	$up_or_down = str_replace(array('ASC','DESC'), array('up','down'), $sort_order);
	$asc_or_desc = $sort_order == 'ASC' ? 'desc' : 'asc';
	$add_class = ' class="highlight"';

	$tr_class = ' class="unanswered"';
?>
	<!DOCTYPE html>
	<html>
		<head>
			<title> Table Sorting Test </title>
			<style>
			table {
				width: 100%;
			}
			tr {
				background-color: #ECF9F4;
			}
			.header {
				background-color: #FFFFFF;
			}
			.unanswered {
				background-color: #FEEFF1;
			}
			tr:hover{
				/* Add a grey background color to the table header and on hover */
				background-color: #eeeeee;
			}
			
			/*
 			 * Styling Popup Div
			 *
			 */ 
			.div-popup {
			  display: none;
			  background-color: white;
			  position: fixed;
			  width: 500px;
			  height: 300px;
			  bottom: 0;
			  right: 15px;
			  border: 3px solid #f1f1f1;
			  z-index: 9;
			}
			.td-popup {
			  width: 30%;
			}
			</style>
		</head>
		<body>
			<!--
			<label>Date from: </label>
			<input type="text" id="txtDateFrom">
			<label>Date to: </label>
			<input type="text" id="txtDateTo">
			-->
			<table>
				<tr class="header">
					<th><a href="ctutor_table_dev.php?column=requestId&order=<?php echo $asc_or_desc; ?>">Requested ID<i class="fas fa-sort<?php echo $column == 'requestId' ? '-' . $up_or_down : ''; ?>"></i></a></th>
					<th><a href="ctutor_table_dev.php?column=studentId&order=<?php echo $asc_or_desc; ?>">Student ID<i class="fas fa-sort<?php echo $column == 'studentId' ? '-' . $up_or_down : ''; ?>"></i></a></th>
					<th><a href="ctutor_table_dev.php?column=studentName&order=<?php echo $asc_or_desc; ?>">Student Name<i class="fas fa-sort<?php echo $column == 'studentName' ? '-' . $up_or_down : ''; ?>"></i></a></th>
					<th><a href="ctutor_table_dev.php?column=errType&order=<?php echo $asc_or_desc; ?>">Error Type<i class="fas fa-sort<?php echo $column == 'errorType' ? '-' . $up_or_down : ''; ?>"></i></a></th>
					<th>Code</th>
					<th>Desc</th>
					<th><a href="ctutor_table_dev.php?column=requestedAt&order=<?php echo $asc_or_desc; ?>">Requested At<i class="fas fa-sort<?php echo $column == 'requestedAt' ? '-' . $up_or_down : ''; ?>"></i></a></th>
					<th>Response Detail</th>
				</tr>
				<?php while ($row = $result->fetch_assoc()): ?>
				<?php if (is_null($row['responseId'])==false){ $tr_class=' class="answered"';} else { $tr_class=' class="unanswered"';}?>
				<tr<?php echo $tr_class;?>>
					<td><?php echo $row['requestId']; ?></td>
					<td><?php echo $row['studentId']; ?></td>
					<td<?php echo $column == 'studentName' ? $add_class : ''; ?>><?php echo $row['studentName']; ?></td>
					<td<?php echo $column == 'errType' ? $add_class : ''; ?>><?php echo $row['errType']; ?></td>
					<td><?php echo $row['code']; ?></td>
					<td><?php echo $row['desc']; ?></td>
					<td<?php echo $column == 'requestedAt' ? $add_class : ''; ?>><?php echo $row['requestedAt']; ?></td>
					<!--<td><a href="responseId=<?php echo $row['responseId']; ?>">See detail</a></td>-->
					<td><a href="javascript:openDiv('<?php echo $row['reviewerName']; ?>','<?php echo $row['solution']; ?>','<?php echo $row['reviewedAt']; ?>','<?php echo $row['voteUp']; ?>','<?php echo $row['voteDn']; ?>')">See detail</a></td>

				</tr>
				<?php endwhile; ?>
			</table>

			<div class="div-popup" id="divResponseDetail">
			    <h1> Response Detail </h1>
			    <table>
				<tr>
			    	    <td class="td-popup"><b>Reviewer Name:</b></td>
				    <td id="tdReviewerName"></td>
				</tr>
				<tr>
				    <td class="td-popup"><b>Reviewed At:</b></td>
				    <td id="tdReviewedAt"></td>
				</tr>
				</tr>
				    <td class="td-popup"><b>Solution:</b></td>
				    <td id="tdSolution"></td>
				</tr>
                                </tr>
                                    <td class="td-popup"><b>Upvotes:</b></td>
                                    <td id="tdVoteUp"></td>
                                </tr>
                                </tr>
                                    <td class="td-popup"><b>Downvotes:</b></td>
                                    <td id="tdVoteDn"></td>
                                </tr>

			    </table>
			    <button type=button" onclick="closeDiv()">Close</button>
                	</div>
		</body>
		<script>
	function openDiv(reviewerName, solution, reviewedAt, voteUp, voteDn) {
		    document.getElementById("tdReviewerName").innerHTML = reviewerName;
		    document.getElementById("tdSolution").innerHTML = solution;
		    document.getElementById("tdReviewedAt").innerHTML = reviewedAt;
		    document.getElementById("tdVoteUp").innerHTML = voteUp;
		    document.getElementById("tdVoteDn").innerHTML = voteDn;
		    document.getElementById("divResponseDetail").style.display = "block";
		}
	function closeDiv(){
		document.getElementById("divResponseDetail").style.display = "none";
		}
		</script>
	</html>
	<?php
	$result->free();
}
?>
