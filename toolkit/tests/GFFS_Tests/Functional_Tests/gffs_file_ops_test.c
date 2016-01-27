
// GFFS file operations test suite.
//
// Author: Chris Koeritz, 2011 10 06.

// NOTE: this test is being constructed very quickly and there are probably
// numerous memory leaks i didn't catch.  but it's a test app, so that should
// not be a system stability concern.

#include <dirent.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/stat.h>

#ifndef __APPLE__
  #include <malloc.h>
#endif

////////////////////////////////////////////////////////////////////////////

// global constants...

const int PATH_MAXIMUM = 5000;  // the largest path we'll deal with here.
const int MAX_FILE = 10000;  // maximum file size we'll try to use.
const int OKAY = 1;  // true, goodness, success, etc.
const int BAD = 0;  // failure, false, etc.
const int ATTRIBUTE_CHECKS = 14;  // number of different file attributes to test.
const int HIGHEST_CHMOD_VALUE = 512;  // the largest value we'll apply as a mode.
// filenames used for testing.
const char *FILENAME_1 = "grommet";
const char *FILENAME_2 = "claghorn";
const char *FILENAME_3 = "mycroft-holmes";
const char *FILENAME_4 = "sherlock-holmes";
const char *FILENAME_5 = "mrs-hudson";
const char *FILENAME_6 = "wallace";
const char *FILENAME_7 = "homer.fugue";
const char *FILENAME_8 = "margorie";
const char *FILENAME_9 = "archie-goodwin";
const char *FILENAME_10 = "saul-panzer";
// directories we'll use in the tests.
const char *DIRECTORY_1 = "doughnuts";
const char *DIRECTORY_2 = "cronkite";

////////////////////////////////////////////////////////////////////////////

// global variables with defaults...

// filled in from command line parameters.  the default is
// only used if no parameters were given.
char *DIRECTORY = (char *)"./GFFS_Mount_Point";

////////////////////////////////////////////////////////////////////////////

// simple global variables...

// the size of the last file that was created.
int LAST_FILE_SIZE = 0;
// the last buffer we created to use as file contents.
char *LAST_BUFFER = NULL;

////////////////////////////////////////////////////////////////////////////

// bank of helper functions...

// makes a character buffer with random contents.
char *make_buffer(int size)
{
  char *new_buff = (char *)malloc(size);
  int i;
  for (i = 0; i < size; i++)
    new_buff[i] = (char)(rand() % 256);
  return new_buff;
}

// combines a "file" name and our directory to get a full path.
char *make_full_filename(const char *file)
{
  char *to_return = (char *)malloc(PATH_MAXIMUM);
  to_return[0] = '\0';
  strcpy(to_return, DIRECTORY);
  strcat(to_return, "/");
  strcat(to_return, file);
  return to_return;
}

// returns a new string with just the filename and no directory.
// pretty long for what it's doing; is there a system call for this?
char *strip_dir(const char *file)
{
  int len = strlen(file);
  char *to_return = (char *)malloc(len + 1);
  to_return[0] = '\0';
  int i;
  int slash_posn = -1;
  for (i = len - 1; i >= 0; i--) {
    if (file[i] == '/') {
      slash_posn = i;
      break;
    }
  }
  if (slash_posn < 0) {
    // no slashes.
    strcpy(to_return, file);
  } else {
//printf("file=%s slashpos=%d len=%d\n", file, slash_posn, len);
    strncpy(to_return, file + slash_posn + 1, len - slash_posn);
  }
  return to_return;
}

// finds a file within an arbitrary directory.
int find_file2(const char *directory, const char *file)
{
  int to_return = BAD;  // assume it's not there.
  char *just_file = strip_dir(file);
//printf("just file turned out as %s\n", just_file);
  DIR *dir = opendir(directory);
  if (!dir) return to_return;
  struct dirent *entry = readdir(dir);
  while (entry) {
    char *curr_file = entry->d_name;
//printf("file in dir is %s\n", entry->d_name);
    if (strcmp(curr_file, just_file) == 0) {
      to_return = OKAY;  // found it.
      break;
    }
    entry = readdir(dir);
  }
  closedir(dir);
  free(just_file);
  return to_return;
}

// locates the "file" in our directory, if possible.  OKAY is returned
// if the file was found, BAD otherwise.
int find_file(const char *file) { return find_file2(DIRECTORY, file); }

// print out a message about the failed test and increment the error count variable.
#define REPORT_FAILURE(funcname, error_msg, error_count) { \
  error_count++; \
  printf("FAILURE seen in %s: %s\n", funcname, error_msg); \
}

// checks that the file's contents are identical to the buffer.
// on success, OKAY is returned.
int compare_file(const char *func, const char *filename, int file_size, char *buffer)
{
  FILE *ptr = fopen(filename, "rb");
  if (!ptr) {
    REPORT_FAILURE(func, "file could not be opened", file_size);
    return BAD;
  }
  // move to end of file so we can get size.
  fseek(ptr, 0, SEEK_END);
  // grab the size.
  int size_now = ftell(ptr);
  // put the file back at the start.
  rewind(ptr);
  if (size_now != file_size) {
//printf("wanted %d, got %d\n", file_size, size_now);
    REPORT_FAILURE(func, "size of file is erroneous", size_now);
    return BAD;
  }
  char *temp_buff = (char *)malloc(size_now);
  int read = fread(temp_buff, 1, size_now, ptr);
  if (read != size_now) {
    REPORT_FAILURE(func, "bytes read does not match size", size_now);
    return BAD;
  }
  fclose(ptr);
  int worked = memcmp(temp_buff, buffer, size_now);
  if (worked != 0) {
    REPORT_FAILURE(func, "file contents differed", size_now);
    return BAD;
  }
  return OKAY;
}

// creates the file specified and fills it with random contents.
// zero indicates success.
int create_random_file(const char *func, const char *filename)
{
  int to_return = 0;
  // create a file.
  FILE *filehandle = fopen(filename, "wb");
  if (!filehandle) {
    REPORT_FAILURE(func, "failed to open file", to_return);
    return to_return;  // can't continue.
  }
  int buff_size = rand() % MAX_FILE + 1;
  LAST_FILE_SIZE = buff_size;
  char *buff = make_buffer(buff_size);
  int written = fwrite(buff, 1, buff_size, filehandle);
  if (written != buff_size)
    REPORT_FAILURE(func, "writing to new file failed", to_return);
  fclose(filehandle);
  // the buffer must be freed elsewhere, if it's going to be.
  LAST_BUFFER = buff;
  return to_return;
}

// returns zero if the file was successfully appened to with the new addition.
int append_to_file(const char *func, const char *filename, int buff_size, const char *new_addition)
{
  int to_return = 0;
  // open it for appending...
  FILE *filehandle = fopen(filename, "ab");
  if (!filehandle) {
    REPORT_FAILURE(func, "failed to open file for appends", to_return);
    return to_return;  // can't continue.
  }
  int written = fwrite(new_addition, 1, buff_size, filehandle);
  if (written != buff_size)
    REPORT_FAILURE(func, "appending to new file failed", to_return);
  fclose(filehandle);
  // this function does not track last buffer.
  return to_return;
}

// removes the "filename" specified; zero is success.
int delete_file(const char *func, const char *filename)
{
  int ret = unlink(filename);
  if (ret != 0)
    REPORT_FAILURE(func, "failed to open file", ret);
  return ret;
}

////////////////////////////////////////////////////////////////////////////

// the main body of tests...

// all test methods return zero on success, as if they had been shelled out.

int test_file_open_close()
{
  #define func "test_file_open_close"
  int to_return = 0;  // success so far.
  // create a file in the directory.
  char *fname = make_full_filename(FILENAME_1);
  if (create_random_file(func, fname) != 0) {
    REPORT_FAILURE(func, "failed to create the file", to_return);
  }
  free(LAST_BUFFER);
  // test that it showed up.
  if (!find_file(fname)) {
    REPORT_FAILURE(func, "finding new file failed", to_return);
  }
  // now clean up.
  if (delete_file(func, fname) != 0) {
    REPORT_FAILURE(func, "cleaning up file failed", to_return);
  }
  free(fname);
  return to_return;
  #undef func
}

int test_file_attributes()
{
  #define func "test_file_attributes"

// this kind of test will not work.
// the only thing people can affect is the 'other' permissions, which affect unauthenticated
// users.  so most perm changes are disallowed, and thus cannot be tested as if gffs was an
// NFS file system or whatever.  --CAK
return 0;

  int to_return = 0;  // success so far.
  // create a file in the directory.
  char *fname = make_full_filename(FILENAME_2);
  if (create_random_file(func, fname) != 0) {
    REPORT_FAILURE(func, "failed to create the file", to_return);
  }
  free(LAST_BUFFER);
  // test that it showed up.
  if (!find_file(fname))
    REPORT_FAILURE(func, "finding new file failed", to_return);
  int checknum;
  // set some different modes on the file.  none of these should hinder our
  // ability to check the file state or reset the mode.
  for (checknum = 0; checknum < ATTRIBUTE_CHECKS; checknum++) {
    // pick a random mode, hopefully kept in bounds.
    mode_t new_mode = (mode_t)(rand() % HIGHEST_CHMOD_VALUE);
    int worked = chmod(fname, new_mode);
    if (worked != 0) {
      REPORT_FAILURE(func, "setting mode on file failed", to_return);
      continue;
    }
    struct stat statbuff;
    worked = stat(fname, &statbuff);
    if (worked != 0) {
      REPORT_FAILURE(func, "acquiring stat on file failed", to_return);
      continue;
    }
    if (statbuff.st_mode % HIGHEST_CHMOD_VALUE != new_mode) {
      printf("[got mode %d, wanted mode %d]\n", statbuff.st_mode % HIGHEST_CHMOD_VALUE, new_mode);
      REPORT_FAILURE(func, "checking for new mode on file failed", to_return);
    }
  }
  mode_t reset_mode = 511;  // a+rwx.
  int worked = chmod(fname, reset_mode);
  if (worked != 0) {
    REPORT_FAILURE(func, "resetting file mode failed", to_return);
  } else {
    // no error on resetting mode, so clean up.
    if (delete_file(func, fname) != 0) {
      REPORT_FAILURE(func, "cleaning up file failed", to_return);
    }
  }
  return to_return;
  #undef func
}

int test_file_creation()
{
  #define func "test_file_creation"
  int to_return = 0;  // success so far.
  // create a file in the directory.
  char *fname = make_full_filename(FILENAME_3);
  if (create_random_file(func, fname) != 0) {
    REPORT_FAILURE(func, "failed to create the file", to_return);
  }
  free(LAST_BUFFER);
  // test that it showed up.
  if (!find_file(fname)) {
    REPORT_FAILURE(func, "finding new file failed", to_return);
  }
  // now clean up.
  if (delete_file(func, fname) != 0) {
    REPORT_FAILURE(func, "cleaning up file failed", to_return);
  }
  free(fname);
  return to_return;
  #undef func
}

int test_file_deletion()
{
  #define func "test_file_deletion"
  int to_return = 0;  // success so far.
  // create a file in the directory.
  char *fname = make_full_filename(FILENAME_4);
  if (create_random_file(func, fname) != 0) {
    REPORT_FAILURE(func, "failed to create the file", to_return);
  }
  free(LAST_BUFFER);
  // test that it showed up.
  if (!find_file(fname)) {
    REPORT_FAILURE(func, "finding new file failed", to_return);
  }
  // now clean up.
  if (delete_file(func, fname) != 0) {
    REPORT_FAILURE(func, "cleaning up file failed", to_return);
  }
  free(fname);
  return to_return;
  #undef func
}

int test_file_renaming()
{
  #define func "test_file_renaming"
  int to_return = 0;  // success so far.
  // create a file in the directory.
  char *fname = make_full_filename(FILENAME_5);
  if (create_random_file(func, fname) != 0) {
    REPORT_FAILURE(func, "failed to create the file", to_return);
  }
  free(LAST_BUFFER);
  // test that it showed up.
  if (!find_file(fname)) {
    REPORT_FAILURE(func, "finding new file failed", to_return);
  }
  // rename the file.
  char *new_fname = make_full_filename(FILENAME_1);
//printf("new name is %s\n", new_fname);
  int worked = rename(fname, new_fname);
  if (worked != 0) {
    REPORT_FAILURE(func, "normal renaming of file failed", to_return);
    return to_return;
  }
  // test that the renaming worked.
  if (!find_file(new_fname)) {
    REPORT_FAILURE(func, "finding newly renamed file failed", to_return);
  }
  // now create a subdirectory to move/rename the file into.
  char *new_dir = make_full_filename(DIRECTORY_1);
  worked = rmdir(new_dir);  // try to clean it first just in case.
//printf("removing dir got %d\n", worked);
  worked = mkdir(new_dir, 0777);
  if (worked != 0) {
    REPORT_FAILURE(func, "making new subdir failed", to_return);
  }
  // rename the file such that it's moved to the subdir.
  char *newest_fname = (char *)malloc(PATH_MAXIMUM);
  newest_fname[0] = '\0';
  strcpy(newest_fname, new_dir);
  strcat(newest_fname, "/");
  strcat(newest_fname, FILENAME_7);
//printf("newest name is %s\n", newest_fname);
  worked = rename(new_fname, newest_fname);
  if (worked != 0) {
    REPORT_FAILURE(func, "renaming and moving file failed", to_return);
  }
  // test that the renaming/moving worked.
  if (!find_file2(new_dir, FILENAME_7)) {
    REPORT_FAILURE(func, "finding file after moving to subdir failed", to_return);
  }
  // now clean up.
  if (delete_file(func, newest_fname) != 0) {
    REPORT_FAILURE(func, "cleaning up file failed", to_return);
  }
  worked = rmdir(new_dir);
  if (worked != 0) {
    REPORT_FAILURE(func, "cleaning up new directory failed", to_return);
  }
  return to_return;
  #undef func
}

int test_directory_creation_and_deletion()
{
  #define func "test_directory_creation_and_deletion"
  int to_return = 0;  // success so far.
  // create a subdirectory to operate in.
  char *new_dir = make_full_filename(DIRECTORY_2);
  int worked = rmdir(new_dir);  // try to clean it first just in case.
//printf("removing dir got %d\n", worked);
  worked = mkdir(new_dir, 0777);
  if (worked != 0) {
    REPORT_FAILURE(func, "making new subdir failed", to_return);
  }
  // pick a name in the folder.
  char *fname = (char *)malloc(PATH_MAXIMUM);
  fname[0] = '\0';
  strcpy(fname, new_dir);
  strcat(fname, "/");
  strcat(fname, FILENAME_6);
  // create the file in there
  if (create_random_file(func, fname) != 0) {
    REPORT_FAILURE(func, "failed to create file in new dir", to_return);
  }
  free(LAST_BUFFER);
  // test that it showed up.
  if (!find_file2(new_dir, FILENAME_6)) {
    REPORT_FAILURE(func, "finding file after creating in subdir failed", to_return);
  }
  // clean up.
  if (delete_file(func, fname) != 0) {
    REPORT_FAILURE(func, "cleaning up file failed", to_return);
  }
  worked = rmdir(new_dir);
  if (worked != 0) {
    REPORT_FAILURE(func, "cleaning up new directory failed", to_return);
  }
  return to_return;
  #undef func
}

int test_file_read()
{
  #define func "test_file_read"
  int to_return = 0;  // success so far.
  // create a file in the directory.
  char *fname = make_full_filename(FILENAME_8);
  if (create_random_file(func, fname) != 0) {
    REPORT_FAILURE(func, "failed to create the file", to_return);
  }
  // added to deal with cache synchronization issues.
  sync();
  // test that it showed up.
  if (!find_file(fname)) {
    REPORT_FAILURE(func, "finding new file failed", to_return);
  }
  // check the file contents (i.e. do the read test here).
  if (!compare_file(func, fname, LAST_FILE_SIZE, LAST_BUFFER)) {
    REPORT_FAILURE(func, "finding new file failed", to_return);
  }
  // now clean up.
  free(LAST_BUFFER);
  if (delete_file(func, fname) != 0) {
    REPORT_FAILURE(func, "cleaning up file failed", to_return);
  }
  free(fname);
  return to_return;
  #undef func
}

int test_file_write()
{
  #define func "test_file_write"
  int to_return = 0;  // success so far.
  // create a file in the directory (this tests the write).
  char *fname = make_full_filename(FILENAME_9);
  if (create_random_file(func, fname) != 0) {
    REPORT_FAILURE(func, "failed to create the file", to_return);
  }
  // added to deal with cache synchronization issues.
  sync();
  // test that it showed up.
  if (!find_file(fname)) {
    REPORT_FAILURE(func, "finding new file failed", to_return);
  }
  // check the file contents (validate the write).
  if (!compare_file(func, fname, LAST_FILE_SIZE, LAST_BUFFER)) {
    REPORT_FAILURE(func, "finding new file failed", to_return);
  }
  // now clean up.
  free(LAST_BUFFER);
  if (delete_file(func, fname) != 0) {
    REPORT_FAILURE(func, "cleaning up file failed", to_return);
  }
  free(fname);
  return to_return;
  #undef func
}

int test_file_append()
{
  #define func "test_file_append"
  int to_return = 0;  // success so far.
  // create a file in the directory.
  char *fname = make_full_filename(FILENAME_10);
  if (create_random_file(func, fname) != 0) {
    REPORT_FAILURE(func, "failed to create the file", to_return);
  }
  // added to deal with cache synchronization issues.
  sync();
  // test that it showed up.
  if (!find_file(fname)) {
    REPORT_FAILURE(func, "finding new file failed", to_return);
  }
  // check the file contents.
  if (!compare_file(func, fname, LAST_FILE_SIZE, LAST_BUFFER)) {
    REPORT_FAILURE(func, "comparing new file failed", to_return);
  }
  // copy the buffer and our new chunk so we can compare the entirety.
  int new_addition = rand() % MAX_FILE;
  int new_total = LAST_FILE_SIZE + new_addition;
  char *big_buff = (char *)malloc(new_total);
  char *just_new = (char *)malloc(new_addition);  // we need the new part separate so we can write it.
  memcpy(big_buff, LAST_BUFFER, LAST_FILE_SIZE);
  // build images for the new part and for the total file contents.
  int i;
  for (i = LAST_FILE_SIZE; i < new_total; i++) {
    char ranchar = (char)(rand() % 256);
    big_buff[i] = ranchar;
    just_new[i - LAST_FILE_SIZE] = ranchar;
  }
  // clean old buffer, since we don't care about it anymore.
  free(LAST_BUFFER);
  // now write the new stuff on the file's end.
  int worked = append_to_file(func, fname, new_addition, just_new);
  if (worked != 0) {
    REPORT_FAILURE(func, "failed to append to file", to_return);
    return BAD;
  }
  // added to deal with cache synchronization issues.
  sync();
  // and make sure that the total file contents are right.
  if (!compare_file(func, fname, new_total, big_buff)) {
    REPORT_FAILURE(func, "file contents after append are wrong", to_return);
  }
  // now clean up.
  free(big_buff);
  if (delete_file(func, fname) != 0) {
    REPORT_FAILURE(func, "cleaning up file failed", to_return);
  }
  free(fname);
  return to_return;
  #undef func
}

int test_file_truncation_then_append()
{
  #define func "test_file_truncation_then_append"
  int to_return = 0;  // success so far.
  // create a file in the directory.
  char *fname = make_full_filename(FILENAME_10);
  if (create_random_file(func, fname) != 0) {
    REPORT_FAILURE(func, "failed to create the file", to_return);
  }
  // added to deal with cache synchronization issues.
  sync();
  // test that it showed up.
  if (!find_file(fname)) {
    REPORT_FAILURE(func, "finding new file failed", to_return);
  }
  // check the file contents.
  if (!compare_file(func, fname, LAST_FILE_SIZE, LAST_BUFFER)) {
    REPORT_FAILURE(func, "comparing new file failed", to_return);
  }
  free(LAST_BUFFER);
  // now stomp those contents and write a new version (this tests the truncation).
  if (create_random_file(func, fname) != 0) {
    REPORT_FAILURE(func, "failed to re-create the file", to_return);
  }
  // added to deal with cache synchronization issues.
  sync();
  // compare the newest version.
  if (!compare_file(func, fname, LAST_FILE_SIZE, LAST_BUFFER)) {
    REPORT_FAILURE(func, "comparing newer file contents failed", to_return);
  }
  // copy the buffer and our new chunk so we can compare the entirety.
  int new_addition = rand() % MAX_FILE;
  int new_total = LAST_FILE_SIZE + new_addition;
  char *big_buff = (char *)malloc(new_total);
  char *just_new = (char *)malloc(new_addition);  // we need the new part separate so we can write it.
  memcpy(big_buff, LAST_BUFFER, LAST_FILE_SIZE);
  // build images for the new part and for the total file contents.
  int i;
  for (i = LAST_FILE_SIZE; i < new_total; i++) {
    char ranchar = (char)(rand() % 256);
    big_buff[i] = ranchar;
    just_new[i - LAST_FILE_SIZE] = ranchar;
  }
  // clean old buffer, since we don't care about it anymore.
  free(LAST_BUFFER);
  // now write the new stuff on the file's end (this tests an append after trunc).
  int worked = append_to_file(func, fname, new_addition, just_new);
  if (worked != 0) {
    REPORT_FAILURE(func, "failed to append to file", to_return);
    return BAD;
  }
  // added to deal with cache synchronization issues.
  sync();
  // and make sure that the total file contents are right.
  if (!compare_file(func, fname, new_total, big_buff)) {
    REPORT_FAILURE(func, "file contents after append are wrong", to_return);
  }
  // now clean up.
  free(big_buff);
  if (delete_file(func, fname) != 0) {
    REPORT_FAILURE(func, "cleaning up file failed", to_return);
  }
  free(fname);
  return to_return;
  #undef func
}

////////////////////////////////////////////////////////////////////////////

// main program...

// macro used to check the individual tests and update error count.
#define CHECK_RUN(to_call) \
  printf("Running " #to_call "...\n"); \
  if ((to_call) != 0) { errors++; } \
  tests++

// runs the overall test suite for GFFS in C.
int main(int argc, char *argv[])
{
  printf("\n\n");  // space out from text above the run.

  // process arguments on command line...
  if (argc > 1) {
    DIRECTORY = argv[1];  // get directory name from cmd line.
  }

  printf("test will run against directory: %s\n", DIRECTORY);

  // check if directory exists or fail out.
  DIR *dir = opendir(DIRECTORY);
  if (!dir) {
    printf("directory %s does not exist; cannot start test.\n", DIRECTORY);
    return 1;
  }
  closedir(dir);

  // start the tests running...
  int errors = 0;  // how many errors were encountered.
  int tests = 0;  // how many tests were run.

  char *SUITE = (char *)"GFFS file operations test suite";
  printf("beginning %s...\n", SUITE);

  // meta-data operations.
  CHECK_RUN(test_file_open_close());
  CHECK_RUN(test_file_attributes());
  CHECK_RUN(test_file_creation());
  CHECK_RUN(test_file_deletion());
  CHECK_RUN(test_file_renaming());
  CHECK_RUN(test_directory_creation_and_deletion());

  // I/O operations.
  CHECK_RUN(test_file_read());
  CHECK_RUN(test_file_write());
  CHECK_RUN(test_file_append());
  CHECK_RUN(test_file_truncation_then_append());
  
  if (errors == 0) {
    printf("All %d tests passed.\n", tests);
  } else {
    printf("There were %d failures out of %d tests.\n", errors, tests);
  }
  printf("done with %s.\n", SUITE);
  return !(errors == 0);  // zero means we had total success.
}

