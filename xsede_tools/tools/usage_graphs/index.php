<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>VCGR XCG Accounting Data</title>
<link rel="stylesheet" type="text/css" href="view.css" media="all">
<script type="text/javascript" src="view.js"></script>
<script type="text/javascript" src="calendar.js"></script>
</head>
<form action="formprocessing.php" method="post">




<body id="main_body" >
	<img id="top" src="top.png" alt="">
	<div id="form_container">
	
		<h1><a>VCGR XCG Accounting Data</a></h1>
		<form id="form_295623" class="appnitro"  method="post" action="">
					<div class="form_description">
			<h2>VCGR XCG Accounting Data</h2>
			<p>Info from completed jobs</p>
		</div>						
			<ul >
			
					<li id="li_3" >
		<label class="description" for="element_3">Report Type</label>
		<div>
		<select class="element select medium" id="element_3" name="report"> 
<option value="Daily" >Daily</option>
<option value="Weekly" >Weekly</option>
<option value="Monthly" selected="selected">Monthly</option>
<option value="Yearly" >Yearly</option>
<option value="BES" >BES</option>
<option value="OS" >OS</option>
<option value="User" >User</option>

		</select>
		</div> <p class="guidelines" id="guide_2"><small>required</small></p>
		</li>		<li id="li_4" >
		<label class="description" for="element_4">Usage Stats</label>
		<div>
		<select class="element select medium" id="element_4" name="usagestats"> 
<option value="jobwallclockhrs" selected="selected">Wall Clock Hours</option>
<option value="jobuserhrs" >User Hours</option>
<option value="numjobs" >Number of Jobs</option>

		</select>
		</div> <p class="guidelines" id="guide_2"><small>required</small></p>
		</li>		<li id="li_5" >
		<label class="description" for="element_5">OS</label>
		<div>
		<select class="element select medium" id="element_5" name="OS"> 
<option value="All" selected="selected">All</option>
<option value="Windows" >Windows</option>
<option value="Linux" >Linux</option>
<option value="Mac" >Mac</option>

		</select>
		</div> <p class="guidelines" id="guide_2"><small>required</small></p>
		</li>		<li id="li_6" >
		<label class="description" for="element_6">BES/Machine</label>
		<div>

		<select class="element select medium" id="element_6" name="besname"> 
			<option value="" selected="selected">All</option>
<?php
//		ob_start ();
		$fileSuffix=date("y-m-d.H:i:s");
		$debugfile = "index-debug-$fileSuffix.txt";
		$graphs_root_path = "/home/vcgr/public_html/XCG/2.0/stats/usage_graphs";
		$result_path = "$graphs_root_path/output";
		$debugfile_path = "$result_path/$debugfile";
		$graphFileName = "graph-$fileSuffix.png";
		$graph_path = "$result_path/$graphFileName";
		$debugFile = fopen($debugfile_path,'w') OR die("Could not open debug file $debugfile_path");
		$conn = mysql_connect("mysql.cs.virginia.edu", "xcg_ro", "password") OR die(mysql_error());
		$db = mysql_select_db("vcgr", $conn) or die(mysql_error());
                // Create SQL query to lookup bes names
                $besquery="SELECT besmachinename FROM tmpXCGJobInfo GROUP BY besmachinename ORDER BY besmachinename";

		//write query to output file for debugging
		fwrite($debugFile, "Query to retrieve bes machine names is $besquery\n");

		$besresult = mysql_query($besquery);
		
		$bescount=0;
		while($besrow = mysql_fetch_array($besresult, MYSQL_NUM))
		{
                   fwrite($debugFile, "  Processing row $bescount\n");
                   $bescount++;
		   if($besrow[0]!==NULL)
                      $string = "<option value=\"".$besrow[0]."\">".$besrow[0]."</option>";
		      fwrite($debugFile, "Option string is $string\n");

		      echo $string;
		}
//		ob_end_flush();
?>
		</select>
		</div> <p class="guidelines" id="guide_2"><small>optional</small></p>
		</li>		<li id="li_7" >
		<label class="description" for="element_7">User </label>
		<div>
		<select class="element select medium" id="element_7" name="user"> 
			<option value="" selected="selected">All</option>
<?php
//		ob_start ();
                // Create SQL query to lookup bes names
                $userquery="SELECT username FROM tmpXCGJobInfo GROUP BY username ORDER BY username";

		//write query to output file for debugging
		fwrite($debugFile, "Query to retrieve usernames is $userquery\n");

		$userresult = mysql_query($userquery);
		
		$usercount=0;
		while($userrow = mysql_fetch_array($userresult, MYSQL_NUM))
		{
                   fwrite($debugFile, "  Processing row $usercount\n");
                   $usercount++;
		   if($userrow[0]!==NULL)
                      $string = "<option value=\"".$userrow[0]."\">".$userrow[0]."</option>";
		      fwrite($debugFile, "Option string is $string\n");

		      echo $string;
		}
//		ob_end_flush();
                fclose($debugFile);
?>



		</div> <p class="guidelines" id="guide_2"><small>optional</small></p>
		</li>		<li id="li_1" >
		<label class="description" for="element_1">Start </label>
		<span>
			<input id="element_1_1" name="startm" class="element text" size="2" maxlength="2" value="" type="text"> /
			<label for="element_1_1">MM</label>
		</span>
		<span>
			<input id="element_1_2" name="startd" class="element text" size="2" maxlength="2" value="" type="text"> /
			<label for="element_1_2">DD</label>
		</span>
		<span>
	 		<input id="element_1_3" name="starty" class="element text" size="4" maxlength="4" value="" type="text">
			<label for="element_1_3">YYYY</label>
		</span>
	
		<span id="calendar_1">
			<img id="cal_img_1" class="datepicker" src="calendar.gif" alt="Pick a date.">	
		</span>
		<script type="text/javascript">
			Calendar.setup({
			inputField	 : "element_1_3",
			baseField    : "element_1",
			displayArea  : "calendar_1",
			button		 : "cal_img_1",
			ifFormat	 : "%B %e, %Y",
			onSelect	 : selectDate
			});
		</script> <p class="guidelines" id="guide_2"><small>optional (first date is 5-10-10)</small></p>
		 
		</li>		<li id="li_2" >
		<label class="description" for="element_2">End </label>
		<span>
			<input id="element_2_1" name="endm" class="element text" size="2" maxlength="2" value="" type="text"> /
			<label for="element_2_1">MM</label>
		</span>
		<span>
			<input id="element_2_2" name="endd" class="element text" size="2" maxlength="2" value="" type="text"> /
			<label for="element_2_2">DD</label>
		</span>
		<span>
	 		<input id="element_2_3" name="endy" class="element text" size="4" maxlength="4" value="" type="text">
			<label for="element_2_3">YYYY</label>
		</span>
	
		<span id="calendar_2">
			<img id="cal_img_2" class="datepicker" src="calendar.gif" alt="Pick a date.">	
		</span>
		<script type="text/javascript">
			Calendar.setup({
			inputField	 : "element_2_3",
			baseField    : "element_2",
			displayArea  : "calendar_2",
			button		 : "cal_img_2",
			ifFormat	 : "%B %e, %Y",
			onSelect	 : selectDate
			});
		</script>
		 
		</li>
			
					<li class="buttons">
			    <input type="hidden" name="form_id" value="295623" />
			    
				<input id="saveForm" class="button_text" type="submit" name="submit" value="Submit" />
		</li>
			</ul>
		</form>	
		<div id="footer">
			Generated by <a href="http://www.phpform.org">pForm</a>
		</div>
	</div>
	<img id="bottom" src="bottom.png" alt="">
	</body>
</html>
