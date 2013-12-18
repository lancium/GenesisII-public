#!/bin/bash

# This script runs through all the known tests as a regression test sweep.
# It should claim that all tests succeeded for a new build to be considered successful.
#
# If an optional first parameter is given, it can be used to control which tests are run.
# If the parameter is GFFS, then only GFFS tests are run.  likewise if it is EMS, only EMS
# tests are run.
#
# Author: Chris Koeritz

# Extra notes:
#
#   Currently the parallel jobs test is disabled unless TEST_PARALLEL_JOBS is a variable
# with a non-empty value.  The current test requires MPICH, but can be adapted to OpenMPI
# by changing the type in the JSDL files for the queue-parallel-jobs-test.sh.  The JSDL
# files need to be modified in the <ns4:SPMDVariation> section.

export REGRESSION_DIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd $REGRESSION_DIR

TIME_START="$(date +"%s")"

source prepare_tests.sh prepare_tests.sh

# if that didn't work, complain.
if [ -z "$XSEDE_TEST_ROOT" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
source $XSEDE_TEST_ROOT/library/establish_environment.sh

which="$1"; shift
verbosity="$1"; shift

GFFS_ENABLED=1
EMS_ENABLED=1
VERBOSE=1

if [ "$which" == "--help" -o "$which" == "-help" -o "$which" == "-h" ]; then
  echo "$(basename $0): Runs the GFFS or EMS regression tests."
  echo
  echo "   $(basename $0) {EMS|GFFS} {summary|full}"
  echo
  echo "To run the EMS tests, pass the 'EMS' flag as the first parameter."
  echo "To run the GFFS tests, pass the 'GFFS' flag as the first parameter."
  echo "To run both tests, do not pass a first parameter on the command line."
  echo "By default, the report will be a 'full' listing that includes all test"
  echo "run logging.  If 'summary' is passed as the second parameter, then only"
  echo "the test results will be presented."
  exit 0
fi

if [ "$verbosity" == "summary" ]; then
  VERBOSE=0
fi

if [ ! -e EMS_Tests ]; then
  EMS_ENABLED=0
fi

if [ ! -e GFFS_Tests ]; then
  GFFS_ENABLED=0
fi

if [ $EMS_ENABLED -eq 0 ] && [ $GFFS_ENABLED -eq 0 ]; then
  echo "No tests to run: the test folders don't exist"
  exit 1
fi

if [ "$which" == "GFFS" ]; then
  EMS_ENABLED=0
fi
if [ "$which" == "EMS" ]; then
  GFFS_ENABLED=0
fi

if [ $EMS_ENABLED -eq 0 ] && [ $GFFS_ENABLED -eq 0 ]; then
  echo "No tests to run: a test folder exists but the tests for that folder were disabled"
  exit 1
fi

##############

# begin defining the sets of tests we'd like to run.  currently there are
# two major types: gffs and ems.

# the standard set of tests for the global federated file system.
GFFS_TESTS=( \
  GFFS_Tests/Functional_Tests/gffsFileOpsTest.sh \
  GFFS_Tests/Functional_Tests/gffsFuseCommands.sh \
  GFFS_Tests/Functional_Tests/gffsGridCommands.sh \
  GFFS_Tests/Functional_Tests/gffsReplCommands.sh \
  GFFS_Tests/Functional_Tests/rnsBearTrap.sh \
  GFFS_Tests/Performance_Tests/directoryTree.sh \
  GFFS_Tests/Performance_Tests/gnuMake.sh \
  GFFS_Tests/Performance_Tests/largeRNSDirectory.sh \
  GFFS_Tests/Performance_Tests/localAndRemoteData.sh \
)

if [ ! -z "$AUTOBUILD_RUNNING" ]; then
  # only add the multiple user tests for automated, testing, bootstrap builds.
  GFFS_TESTS+=( \
    GFFS_Tests/Scalability_Tests/multiuser-gffs-submit.sh \
    GFFS_Tests/Functional_Tests/test_sts_replication.sh \
  )
fi

# the standard tests for the execution management services.
EMS_TESTS=( \
  EMS_Tests/besStatus/bes-attributes-and-activities.sh \
  EMS_Tests/fileStagingTests/protocols-test.sh \
  EMS_Tests/besFunctionality/bes-submission-test-sync.sh \
  EMS_Tests/queueFunctionalityTests/queue-submission-test.sh \
  EMS_Tests/performanceTests/queue-performance-test.sh \
  EMS_Tests/performanceTests/file-staging-performance-test.sh \
  EMS_Tests/faultJobsTests/bes-submission-sync-fault.sh \
)

if [ ! -z "$AUTOBUILD_RUNNING" ]; then
  # only add the multiple user tests for automated, testing, bootstrap builds.
  EMS_TESTS+=( \
    EMS_Tests/multiUserTests/multiuser-3users-manyjobs.sh \
    EMS_Tests/multiUserTests/multiuser-10users-fewjobs.sh \
  )
fi

# selectively enable certain tests that only make sense for particular
# types of EMS.

if [ "$BES_TYPE" = "Genesis" ]; then
  echo "Adding in Genesis-specific tests because BES_TYPE is Genesis."

  EMS_TESTS+=(EMS_Tests/rnsEnabledBESTests/bes-test-async.sh)
  # the submission point test should come after the async test, because the
  # async test is currently enabled for multi-instance testing, and does not
  # clean up after itself.  the submission point test will clean up.
  EMS_TESTS+=(EMS_Tests/rnsEnabledBESTests/bes-test-submission-point.sh)
else
  # Unicore specific tests...
  if [ ! -z "TEST_PARALLEL_JOBS" ]; then
    # only add this one if the flag above is set.
    EMS_TESTS+=(EMS_Tests/queueParallelJobs/queue-parallel-jobs-test.sh)
  fi
fi

#holding onto these until fully vetted:
#  EMS_Tests/multiUserTests/bes-multiuser-10-few.sh \
#  EMS_Tests/multiUserTests/bes-multiuser-3-many.sh \

##############

# now that all tests have been defined, we build up our total list of tests.

FULL_TEST_SET=()

if [ $EMS_ENABLED -eq 1 ]; then
  FULL_TEST_SET+=(${EMS_TESTS[*]})
fi
if [ $GFFS_ENABLED -eq 1 ]; then
  FULL_TEST_SET+=(${GFFS_TESTS[*]})
fi

echo Full set of tests:
for ((test_iter=0; $test_iter < ${#FULL_TEST_SET[*]}; test_iter++)); do
  echo "$(expr $test_iter + 1): ${FULL_TEST_SET[$test_iter]}"
done

##############

#no # patch the grid client to increase its memory limit.
### we have seen that 512M is not enough for the test suite.
##pushd $GENII_INSTALL_DIR &>/dev/null
### cygwin erroneously says "grid" exists if "grid.exe" exists.
##if [ ! -f "grid.exe" ]; then
##  if [ -f grid ]; then sed -i -e "s/-Xmx512[Mm]/-Xmx1G/" grid; chmod 755 grid; fi
##  if [ -f grid.bat ]; then sed -i -e "s/-Xmx512[Mm]/-Xmx1G/" grid.bat; chmod 755 grid.bat; fi
##fi
##popd &>/dev/null

##############

create_work_area

##############

FAIL_COUNT=0

REG_TEMP="$TEST_TEMP/regression_$(date +"%Y_%m_%d")"
if [ ! -d "$REG_TEMP" ]; then
  mkdir "$REG_TEMP"
fi

# go to the top of the hierarchy.
cd $XSEDE_TEST_ROOT

for ((test_iter=0; $test_iter < ${#FULL_TEST_SET[*]}; test_iter++)); do
  echo -e "\n======================================================================"
  echo -n `date`": " 
  echo "Now running test $(expr $test_iter + 1): ${FULL_TEST_SET[$test_iter]}"
  output_file="$(mktemp $REG_TEMP/regression.XXXXXX)"
  echo "  Test output file: $output_file"

  echo "==============" >"$output_file"
  echo "Log state prior to test:" >>"$output_file"
  check_logs_for_errors >>"$output_file"
  echo "==============" >>"$output_file"

  if [ $VERBOSE -ne 1 ]; then
    bash $XSEDE_TEST_ROOT/${FULL_TEST_SET[$test_iter]} >>"$output_file" 2>&1
    retval=$?
  else
    bash $XSEDE_TEST_ROOT/${FULL_TEST_SET[$test_iter]} 2>&1 | tee -a "$output_file"
    retval=${PIPESTATUS[0]}
  fi

  if [ $retval -ne 0 ]; then
    ((FAIL_COUNT++))
    echo "FAILURE: exit code $retval for test ${FULL_TEST_SET[$test_iter]}"
    TEST_RESULTS[$test_iter]="FAIL"
  else
    echo "OK: successful test run for test ${FULL_TEST_SET[$test_iter]}"
    TEST_RESULTS[$test_iter]="OKAY"
  fi

  echo "==============" >>"$output_file"
  echo "Log state after test:" >>"$output_file"
  check_logs_for_errors >>"$output_file"
  echo "==============" >>"$output_file"
done

# final analysis--how did the test run do?

echo -e "\n\nResults table for this test run:\n"
for ((test_iter=0; $test_iter < ${#FULL_TEST_SET[*]}; test_iter++)); do
  num=$(expr $test_iter + 1)
  if [ $num -lt 10 ]; then num="0$num"; fi
  echo "$num: ${TEST_RESULTS[$test_iter]} -- ${FULL_TEST_SET[$test_iter]}"
done
echo

# figure out how long things took.
TIME_END="$(date +"%s")"
duration="$(($TIME_END - $TIME_START))"
# prepare to print duration in hours and minutes.
minutes="$(($duration / 60))"
hours="$(($duration / 60))"
# grab out the hours we calculated from the minutes sum.
minutes="$(($minutes - $hours * 60))"
if (($minutes < 10)); then minutes="0$minutes"; fi
if (($hours < 10)); then hours="0$hours"; fi
echo "Total testing duration: $hours:$minutes hh:mm ($duration seconds total)"

if [ $FAIL_COUNT -ne 0 ]; then
  echo "FAILURE: $FAIL_COUNT Tests Failed out of ${#FULL_TEST_SET[*]} Tests."
  exit 1
else
  echo "OK: All ${#FULL_TEST_SET[*]} Tests Ran Successfully."
  exit 0
fi


