#!/bin/bash

# Functions for managing jobs in a grid queue.
#
# Author: Chris Koeritz
# Author: Vanamala Venkataswamy

############################

# this first section of functions is related to jobs that have been submitted to a queue.

# echoes an integer for the count of jobs remaining in an unfinished state.
compute_remaining_jobs()
{
  local queue_path=$1
  local grid_app="$(pick_grid_app)"
  outfile="$(mktemp "$TEST_TEMP/job_stats.XXXXXX")"
  raw_grid "$grid_app" qstat $queue_path | tail -n +2 | sed -e '/^$/d' >$outfile
  retval=${PIPESTATUS[0]}
  if [ $retval -ne 0 ]; then
    # the qstat call failed, and we want to send back an error signal.
    remaining="-1"
  else
    # compute how many jobs are in a finished state, and subtract from total.
    total_lines=$(wc -l $outfile | awk '{print $1}')
    done_lines=$(grep 'ERROR\|FINISHED' $outfile | wc -l)
    remaining=$(($total_lines - $done_lines))
  fi
#wrong old way.  remaining=$(raw_grid \"$grid_app\" qstat $queue_path | grep 'QUEUED\|RUNNING\|On' | wc | gawk '{print $1 }')
  echo "$remaining"
}

compute_error_jobs()
{
  local queue_path="$1"; shift
  local grid_app="$(pick_grid_app)"
  echo $(raw_grid \"$grid_app\" qstat $queue_path | grep 'ERROR' | wc | gawk '{print $1 }')
}

show_error_jobs()
{
  local queue_path=$1
  if [ "$(compute_error_jobs $queue_path)" != "0" ]; then
    echo "FAILURE: These jobs had errors:"
    local grid_app="$(pick_grid_app)"
    raw_grid \"$grid_app\" qstat $queue_path | grep 'ERROR'
    return 1
  else
    echo "SUCCESS: No jobs in an error state were seen."
    return 0
  fi
}

# cancels all the jobs on the queue.  this is useful after an error is detected,
# since it will clean the queue up for further tests.
function cancel_all_in_queue()
{
  local queue_path=$1; shift

  # start by clearing out all those that are finished already.
  grid qcomplete $queue_path --all

  # get a listing of all the stuff in the queue.
  holding="$GRID_OUTPUT_FILE"
  GRID_OUTPUT_FILE="$(mktemp $TEST_TEMP/job_processing/cancellation_list.XXXXXX)"
  grid qstat $queue_path
  tickets=($(gawk '{ print $1 }' <$GRID_OUTPUT_FILE))
  # show what we're going to whack.
  echo "Cancelling $(expr ${#tickets[*]} - 1) queue jobs:"
  # we kill in batches because otherwise qkill seems to choke (from too long a command line?).
  local BATCH_SIZE=50
  # start i at 1 instead of 0 since we don't need a ticket called "Ticket".
  for ((i=1; i < ${#tickets[*]}; i+=$BATCH_SIZE)); do
    local zaps=()
    for ((j=i; j < i + $BATCH_SIZE && j < ${#tickets[*]}; j++)); do
      zaps+=(${tickets[j]})
    done
    echo "batch $(expr $i / $BATCH_SIZE): ${zaps[*]}"
    grid qkill $queue_path ${zaps[*]}
  done

  grid qcomplete $queue_path --all
  grid qstat $queue_path
  
  # show any refugees if something managed to escape our queue crushing.
  if [ $(compute_remaining_jobs $queue_path) -ne 0 ]; then
    echo "After cancelling all jobs, queue is left with:"
    tail -n +2 $GRID_OUTPUT_FILE
  fi
  \rm "$GRID_OUTPUT_FILE"

  # restore the normal output file.
  holding="$GRID_OUTPUT_FILE"
  GRID_OUTPUT_FILE="$holding"
}

# this function can be used to wait for all pending jobs until they're
# complete or have ended up in an error state.
function wait_for_all_pending_jobs()
{
  local queue_path=$1; shift
  local whack_jobs=$1; shift

  if [ ! -z "$whack_jobs" ]; then
    echo Waiting for all jobs and removing them when finished.
  else
    echo Waiting for all jobs but leaving them in queue.
  fi

  if [ -z "$QUEUE_SLEEP_DURATION" ]; then
    QUEUE_SLEEP_DURATION=120
  fi

  if [ -z "$QUEUE_TRIES_ALLOWED" ]; then
    # if they haven't set a value for this, we assume they're willing to wait an hour.
    local minsHour=$((60 * 60 / $QUEUE_SLEEP_DURATION))
    QUEUE_TRIES_ALLOWED=$minsHour
  fi

  local tries_remaining=$QUEUE_TRIES_ALLOWED

  local error_count=0

  # start by clearing out any that are finished.
  grid qcomplete $queue_path --all

  local left=$(compute_remaining_jobs $queue_path)
  local last_left=$left
  while [ "$left" != "0" ]; do
    echo -n `date`
    echo ": There are $left jobs still queued or running."
    if [ "$left" == "-1" ]; then
      echo "warning: saw a qstat failure while computing remaining jobs on '$queue_path'"
    fi
    sleep $QUEUE_SLEEP_DURATION
    left=$(compute_remaining_jobs $queue_path)
    # we do not count the negative one values against the caller; that's almost always
    # because the queue is so busy that we time out when doing qstat.
    if [ "$left" == "$last_left" -a "$left" != "-1" ]; then
      ((tries_remaining--))
      if [ $tries_remaining -le 0 ]; then
        echo -n `date`
        echo ": FAILURE: there are still $left jobs queued or running after maximum tries."
        ((error_count++))
        cancel_all_in_queue $queue_path
        break;
      fi
    else
      tries_remaining=$QUEUE_TRIES_ALLOWED
    fi
    # update the tracking of the last count of items left.
    last_left=$left
    # try dropping any that are already complete, so we don't have to keep looking at them.
    if [ ! -z "$whack_jobs" ]; then
      grid qcomplete $queue_path --all
    fi
  done
  
  # don't try to calculate the error jobs when we already know we failed.
  if [ $error_count -eq 0 ]; then
    # list the error jobs.
    show_error_jobs $queue_path
    # make sure we track the presence of an error job _as_ an error.
    if [ $? -ne 0 ]; then ((error_count++)); fi
  fi
  grid qcomplete --all $queue_path
  if [ $? -ne 0 ]; then ((error_count++)); fi
  return $error_count
}

############################

# this second section of functions concerns asynchronous BES job submissions.

# submits a job directly to a BES by running a jsdl file and providing a target folder
# for the job state to arrive at.  it needs the unique job name, which must be a valid
# rns path, the jsdl file to use for running the job, and the BES resource name.
function submit_asynchronous_job_on_BES()
{
  jobname="$1"; shift
  jsdl_file="$1"; shift
  resource_name="$1"; shift
  ASYNCHRONOUS_BES_PENDING_JOBS+=($jobname)
  grid run --async-name=$jobname --jsdl=local:$GENERATED_JSDL_FOLDER/$jsdl_file "$resource_name"
  assertEquals "Submitting job: $jobname" 0 $?
}

# a list of active async status folders to watch.
ASYNCHRONOUS_BES_PENDING_JOBS=()

# this returns as successful if the job in the state file is already done.
# if non-successful, then a non-zero exit is returned.
function check_job_status_file()
{
  local status_file="$1"; shift
  grep "State:[         ]*Finished" "$status_file" &>/dev/null
  local retval=$?
#extra debugging noise...
#echo ====================
#echo retval is $retval and status file we check: $status_file
#echo it has contents:
#cat $status_file
#echo ====================
  grep -i exception $status_file &>/dev/null
  if [ $? -eq 0 ]; then
    # this is a big deal.  it means the job is not going to finish, because we are faulting
    # somehow.
    echo "Detected exception in status file '$status_file'; bailing out as emergency measure."
    exit 1
  fi
  return $retval
}

# given a list of asynchronously submitted jobs (i.e. a list of RNS paths to the
# asynchronous output folder), this will scan through them and await their
# completion.  it will not return until all jobs are complete, or a failure
# was detected.  note that this relies on a variable "available_resources"
# that should be set to the list of resources on the queue path to check the
# jobs on (which are assumed to have been submitted to each resource?).
function poll_job_dirs_until_finished()
{
  local pending=($*)
  local jobname
  local resource

  JOB_OUTPUT_FILE="$(mktemp $TEST_TEMP/job_processing/status.XXXXXX)"
  echo "Status of job completions can be found in $JOB_OUTPUT_FILE"
  local my_output="$(mktemp "$TEST_TEMP/job_processing/out_poll_job_dirs.XXXXXX")"
  while [ ${#pending[*]} -ne 0 ]; do
    for resource in $available_resources; do
      for jobname in ${pending[*]}; do
        echo -e "\n------------------------------\n" 2>&1 >>$JOB_OUTPUT_FILE
        echo "$jobname..." 2>&1 >>$JOB_OUTPUT_FILE
        grid cat $jobname/status
        \mv -f "$GRID_OUTPUT_FILE" "$my_output"
        cat "$my_output" 2>&1 >>$JOB_OUTPUT_FILE
        sleep 3  # no point in crushing the machine.
        check_job_status_file "$my_output"
        if [ $? -eq 0 ]; then
          # remove that job from the array of pending jobs.
          pending=(${pending[*]#${jobname}})
          echo "successfully finished: $jobname" >>$JOB_OUTPUT_FILE
#echo here is thing before remove: $jobname
#grid ls $jobname
#cat $GRID_OUTPUT_FILE
          grid rm -rf $jobname
          assertEquals "clear folder for job: $jobname" 0 $?
        else
          echo "job not done yet: $jobname" >>$JOB_OUTPUT_FILE
        fi
      done
      sleep 5  # snooze a little since there's no point in a hot spot here.
    done
  done
  \rm -f "$my_output"
}

############################

# the third section provides some useful functions for jobs that are submitted
# via a queue submission point.

SUBMISSION_POINT_JOB_LIST=()
  # this will be filled by the get_job_list_from_queue function.

# retrieves all the jobs that the queue is holding for us.
function get_job_list_from_queue()
{
  # this path will show all our submitted tickets.  we want to get that list as
  # a set of job tickets to use.
  grid ls $QUEUE_PATH/jobs/mine/all
  assertEquals "Getting list of my tickets in queue." 0 $?
  # scarf up the job ids we found.
  SUBMISSION_POINT_JOB_LIST=($(cat $GRID_OUTPUT_FILE | tail -n +2))
}

# attempts to wait for all pending jobs that are listed under the queue as mine.
function drain_my_jobs_out_of_queue()
{
  local i
  local count
  # we will allow the outer loop to run this many times before bailing.
  local iterations_left=20

  did_ls_test=0  # global variable to stop us from repeating the ticket set too many times.

  # keep iterating until we've cleaned all the jobs up or been interrupted.
  while [ ${#SUBMISSION_POINT_JOB_LIST[*]} -gt 0 ]; do
    # retrieve all the current running jobs into JOBS_LIST.
    get_job_list_from_queue

    # we do one round of testing with ls on the job tickets just to see that
    # that feature works.  we stop so we don't noisily keep repeating the info.
    if [ $did_ls_test -ne 1 ]; then
      # we also show the tickets this one time.
      echo -e "\njob tickets found..."
      for ((i=0; i < ${#SUBMISSION_POINT_JOB_LIST[*]}; i++)); do
        echo "  ${SUBMISSION_POINT_JOB_LIST[$i]}"
      done
      # run the test of showing a job's submitted file.
      single_job=${SUBMISSION_POINT_JOB_LIST[0]}
      grid ls $QUEUE_PATH/jobs/mine/all/$single_job
      local retval=$?
      echo "Contents of first job's file..."
      cat $GRID_OUTPUT_FILE
      assertEquals "Testing ls listing a submitted job" 0 $retval
      did_ls_test=1
    fi

    for i in ${SUBMISSION_POINT_JOB_LIST[*]}; do
      # we opt for a more subtle completion call per job, just as an alternative to
      # our more standard --all flag.
#echo "examining job $i"
      count=120
      retval=1
      local grid_app="$(pick_grid_app)"
      until [ $count -le 0 ]; do
#echo "seconds left: $count"
        sleep 10
        jobStatus="$(raw_grid $grid_app qstat $QUEUE_PATH $i)"
#echo qstat returned: $jobStatus
        # stop waiting for a job if it's marked as finished.
        if [[ "$jobStatus" =~ .*FINISHED.* ]]; then
#echo "  saw job in finished state"
            count=-1
            grid qcomplete $QUEUE_PATH $i
            retval=$?
            # bail out if we see any error when asking it to complete.
            if [ $retval -ne 0 ]; then break; fi
        fi
        # also stop waiting on jobs in error state; we whack these.
        if [[ "$jobStatus" =~ .*ERROR.* ]]; then
#echo "  saw job in error state"
            count=-1
            grid qkill $QUEUE_PATH $i
            retval=$?
            # bail out if we see any error when asking it to complete.
            if [ $retval -ne 0 ]; then break; fi
        fi
        count=$(( $count - 10 ))
#echo count is now $count
      done
      assertEquals "requesting job $i complete." 0 $retval
    done

    if [ ${#SUBMISSION_POINT_JOB_LIST[*]} -gt 0 ]; then
      # snooze for a bit to ensure that we don't spin too fast or spew too much.
      sleep 14
    fi

    # bail out if we have used up our iterations.
    iterations_left=$(( $iterations_left - 1 ))
#echo "outer loop has $iterations_left times left"
    if [ $iterations_left -le 0 ]; then
      # this counts as a failure.
      echo "Leaving drain_my_jobs_out_of_queue due to using up all allotted iterations."
      return 1
    fi
  done
}

##############

# finds the resources that exist under the queue specified in the config file
# and echoes a space separated list of their names.  note that this will pay
# attention to the BES_RESOURCE_OVERRIDE variable; if that is non-blank, then
# the variable's value will be the only resource name returned.  (the
# variable can include more than one space-separated resource if desired.)
get_BES_resources()
{
  if [ ! -z "$BES_RESOURCE_OVERRIDE" ]; then
    for besnam in "$BES_RESOURCE_OVERRIDE"; do
      local shorty="$(basename $besnam)"
      # if the BES has no path attached, we'll assume it lives under the queue.
      if [ "$besnam" == "$shorty" ]; then
        echo "$QUEUE_PATH/resources/$besnam"
      else
        echo "$besnam"
      fi
    done
    return 0
  fi
  local RESRC_FILE="$(mktemp "$TEST_TEMP/job_processing/queue_resources.XXXXXX")"
  grid ls $QUEUE_PATH/resources 
  # check for a failure of the resource check.
  if [ $? -ne 0 ]; then echo ""; return 1; fi
  \cp -f $GRID_OUTPUT_FILE "$RESRC_FILE"
  # looks like we got a file successfully, so process it by stripping the
  # resources header off of it.
  sed -i "/resources:/d" "$RESRC_FILE"
  for besnam in $(cat "$RESRC_FILE"); do
    local shorty="$(basename $besnam)"
    # if the BES has no path attached, we'll assume it lives under the queue.
    if [ "$besnam" == "$shorty" ]; then
      echo "$QUEUE_PATH/resources/$besnam"
    else
      echo "$besnam"
    fi
  done
  return 0
}

##############


