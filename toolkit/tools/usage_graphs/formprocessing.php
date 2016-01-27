<html>
   <body>
	<?php
		$fileSuffix=date("y-m-d.H:i:s");
		$graphs_root_path = "/home/vcgr/public_html/XCG/2.0/stats/usage_graphs";
		$result_path = "$graphs_root_path/output";
		$ploticus_path = "/home/vcgr/Ploticus";
		$outputfile = "outputdata-$fileSuffix.csv";
		$debugfile = "formprocessing-debug-$fileSuffix.txt";
		$outfile_path = "$result_path/$outputfile";
		$debugfile_path = "$result_path/$debugfile";
		$graphFileName = "graph-$fileSuffix.png";
		$graph_path = "$result_path/$graphFileName";
		$debugFile = fopen($debugfile_path,'w') OR die("Could not open debug file $debugfile_path");
		fwrite($debugFile, "Starting\n");

                // cleanup old stuff in output directory
	        $timeout = strtotime("-2 days");
		fwrite($debugFile, "Cleaning up old output files from directory $result_path with date older than $timeout\n");
	        if ($result_dir_handle = opendir($result_path)) {
	           while ( ($filename = readdir($result_dir_handle)) !== false) {
	               $fullname = $result_path.'/'.$filename;
	               fwrite($debugFile, "Checking file $fullname\n");
	               if ($filename != '.' && 
	                   $filename != '..' &&
	                   filemtime($fullname) < strtotime("-2 days") ) {
			   unlink ($fullname);
 	                   fwrite($debugFile, "Deleting file $fullname\n");
		       }
		    }
	            closedir($result_dir_handle);
	        }


		$conn = mysql_connect("mysql.cs.virginia.edu", "xcg_ro", "password") OR die(mysql_error());
		$db = mysql_select_db("vcgr", $conn) or die(mysql_error());

		$report=$_POST['report'];
	   	if ($report === NULL || $report === "") {
	      		$report="Weekly";
	   	}
		$usagestats=$_POST['usagestats'];
	   	if ($usagestats === NULL || $usagestats === "") {
	      		$usagestats="Wall Clock Hours";
	   	}	
		$OS=$_POST['OS'];
	   	if ($OS === NULL || $OS === "") {
	      		$OS="All";
	   	}
		$user=$_POST['user'];
		$besname=$_POST['besname'];
		$numWhereConditions=0;
		$whereClause="";
		$groupByClause="";
		$orderByClause="";

		fwrite($debugFile, "Form input: report: $report; stat: $usagestats; os: $OS; user: $user; BES: $besname\n");

		//X variable will be first column of output
		if($report === "Daily") {
			$columns='DATE(recordtimestamp) AS jobdate'; 
			$xlbl='Daily'; 
			$groupByClause="jobdate";
			$orderByClause="jobdate";
		} else if($report === "Weekly") {
			$columns='jobmondaydate'; 
			$xlbl='Weekly'; 
			$groupByClause="jobmondaydate";
			$orderByClause="jobmondaydate";
		} else if($report === "Monthly") {
			$columns='CONCAT(jobmonthname, " ", jobyear) AS jobmonthyear'; 
			$xlbl='Monthly'; 
			$groupByClause="jobyear, jobmonth, jobmonthname";
			$orderByClause="jobyear, jobmonth";
		} else if($report === "Yearly") {
    		$columns='jobyear'; 
			$xlbl='Yearly'; 
			$groupByClause="jobyear";
			$orderByClause="jobyear";
      		} else if($report === "BES") {
			$columns='besmachinename'; 
			$xlbl='BES'; 
			$groupByClause="besmachinename";
			$orderByClause="besmachinename";
		} else if($report === "OS") {
			$columns='os'; 
			$xlbl='OS';
			$groupByClause="os";
			$orderByClause="os";
		} else if($report === "User") {
			$columns="IF(username IS NULL, 'unknown', username) AS u1"; 
			$xlbl='XCG User'; 
			$groupByClause="u1";
			$orderByClause="u1";
		}	

      		if($OS==="All") {
      			$name1='Windows XP'; $name2='Linux'; $name3='Mac'; $color1='red'; $color2='brightblue'; $color3='green'; 
   		} else if($OS === "Windows") {
			$whereClause="os = 'Windows_XP'";
			$numWhereConditions++;
			$name1='Windows XP'; $color1='red'; 
		}
		if($OS === "Linux") {
			$whereClause="os = 'LINUX'";
			$numWhereConditions++;
			$name1='Linux'; $color1='brightblue'; 
		}
		if($OS === "Mac") {
			$whereClause="os = 'MACOS'";
			$numWhereConditions++;
			$name1='Mac'; $color1='green'; 
		}

      		if($usagestats === "jobwallclockhrs") {
     			$ylbl='Wall Clock Hours'; 
         		if($OS==="All") {
         			$columns=$columns.', SUM(windowswallclockhrs), SUM(linuxwallclockhrs), SUM(macoswallclockhrs)'; 
      			} else if($OS === "Windows") {
				$columns=$columns.', SUM(windowswallclockhrs)'; 
			}
			if($OS === "Linux") {
				$columns=$columns.', SUM(linuxwallclockhrs)'; 
			}
			if($OS === "Mac") {
				$columns=$columns.', SUM(macoswallclockhrs)'; 
			}
		} else if($usagestats === "jobuserhrs") {
			$ylbl='User Hours'; 
         		if($OS==="All") {
         			$columns=$columns.', SUM(windowsuserhrs), SUM(linuxuserhrs), SUM(macosuserhrs)'; 
      			} else if($OS === "Windows") {
				$columns=$columns.', SUM(windowsuserhrs)'; 
			}
			if($OS === "Linux") {
				$columns=$columns.', SUM(linuxuserhrs)'; 
			}
			if($OS === "Mac") {
				$columns=$columns.', SUM(macosuserhrs)'; 
			}
		} else if($usagestats === "numjobs") {
			$ylbl='Number of Jobs'; 
         		if($OS==="All") {
         			$columns=$columns.", SUM(IF(os='Windows_XP', 1, 0)), SUM(IF(os='LINUX', 1, 0)), SUM(IF(os='MACOS', 1, 0))"; 
      			} else if($OS === "Windows") {
				$columns=$columns.", SUM(IF(os='Windows_XP', 1, 0))"; 
			}
			if($OS === "Linux") {
				$columns=$columns.", SUM(IF(os='LINUX', 1, 0))"; 
			}
			if($OS === "Mac") {
				$columns=$columns.", SUM(IF(os='MACOS', 1, 0))"; 
			}
		}		
			
		if($user != NULL) {
			if ($numWhereConditions > 0)
				$whereClause=$whereClause." AND ";
			$whereClause=$whereClause."username = '$user'";
			$numWhereConditions++;
		}
	
		if($besname != NULL) {
			if ($numWhereConditions > 0)
				$whereClause=$whereClause." AND ";
			$whereClause=$whereClause."besmachinename = '$besname'";
			$numWhereConditions++;
		}
	
		$y1=$_POST['starty'];
		$m1=$_POST['startm'];
		$d1=$_POST['startd'];
		$y2=$_POST['endy'];
		$m2=$_POST['endm'];
		$d2=$_POST['endd'];
	
		if ($y1 != NULL) {
			if ($numWhereConditions > 0)
				$whereClause=$whereClause." AND ";
			$whereClause=$whereClause."DATE(recordtimestamp) >= '$y1-$m1-$d1'";
			$numWhereConditions++;
	                $startDate="$m1/$d1/$y1";
		} else {
	                $startDate="Beginning of time";
	        }

		if ($y2 != NULL) {
			if ($numWhereConditions > 0)
				$whereClause=$whereClause." AND ";
			$whereClause=$whereClause."DATE(recordtimestamp) < ADDDATE(DATE('$y2-$m2-$d2'), 1)";
			$numWhereConditions++;
	                $endDate="$m2/$d2/$y2";
		} else {
	                $endDate="End of time";
	        }

      		// Create SQL query and ploticus command based on form input
      		$query="SELECT $columns FROM tmpXCGJobInfo";
		if ($numWhereConditions > 0)
			$query=$query." WHERE $whereClause";
		if ($groupByClause != "")
			$query=$query." GROUP BY $groupByClause";
		if ($orderByClause != "")
			$query=$query." ORDER BY $orderByClause";

      		if($OS==="All") {
	      		$command="$graphs_root_path/createAllOSGraph.sh '$name1' '$name2' '$name3' $color1 $color2 $color3 '$outfile_path' '$xlbl' '$ylbl' '$graph_path' > '$result_path/pl-stdout-$fileSuffix.txt' 2> '$result_path/pl-stderr-$fileSuffix.txt'";
		} else {
	  		$command="$graphs_root_path/createOneOSGraph.sh '$name1' $color1 '$outfile_path' '$xlbl' '$ylbl' '$graph_path' > '$result_path/pl-stdout-$fileSuffix.txt' 2> '$result_path/pl-stderr-$fileSuffix.txt'";
		}

		//begin to download data
		//write query to output file for debugging
		fwrite($debugFile, "Query is $query\n");

		$result = mysql_query($query);
		
		$openfile = fopen($outfile_path, 'w') OR die("Can't open the file $outfile_path.");

		//export data to CSV
		$count=0;
		while($row = mysql_fetch_array($result, MYSQL_NUM))
		{
			fwrite($debugFile, "  Processing row $count\n");
			$count++;
			$string = "";
			for($x = 0; $x < mysql_num_fields($result); $x++) {
			        if ($x > 0) {
	                                $string = $string.", ";
                                } 
				if($row[$x]===NULL)
					$string = $string."0";
				else
					$string = $string.$row[$x]; 
					 
			}
			$string = $string."\n"; 
			fwrite($openfile, $string);
		}
		fclose($openfile);

		//create and display the graph
		fwrite($debugFile, "  Executing command $command\n");
		$exec_out = exec($command);
		fwrite($debugFile, "  Ploticus exec output: $exec_out\n");
		ob_start ();
		echo "<title>XCG Usage Graph</title>";
		echo "<h1>VCGR Usage Graph</h1>";
		echo "<h2>Graph Parameters:</h2>";


		echo "<table padding=\"1\">";
		echo " <tr>";
		echo "  <td>Report Type:</td>";
		echo "  <td><b>$report</b></td>";
		echo " </tr>";
		echo " <tr>";
		echo "  <td>Usage Stat:</td>";
		echo "  <td><b>$ylbl</b></td>";
		echo " </tr>";
		echo " <tr>";
		echo "  <td>OS:</td>";
		echo "  <td><b>$OS</b></td>";
		echo " </tr>";
		echo " <tr>";
		echo "  <td>BES Name:</td>";
		if ($besname === NULL || $besname === "") {
		    echo "  <td><b>All</b></td>";
		} else {
		    echo "  <td><b>$besname</b></td>";
		}
		echo " </tr>";
		echo " <tr>";
		echo "  <td>Date Range:</td>";
		echo "  <td><b>$startDate - $endDate</b></td>";
		echo " </tr>";
		echo "</table>";

		echo "<img src=\"output/$graphFileName\"/>";
		ob_end_flush();
		fwrite($debugFile, "Done\n");
		fclose($debugFile);

	?>
	</body>
</html>
