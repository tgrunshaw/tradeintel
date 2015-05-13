#!/bin/sh
#
# Daily script.
#
# Script to run innobackupex script (for all databases on server), check for success, and apply logs to backups.
# Original Source: https://gist.github.com/1131224
# Original Author: Owen Carter
#
# @Author: Tim Grunshaw
#
# This script does a FULL backup everytime, the compresses them. Chosen over incremental for ease of use. 
#
# Change the values below when changing server or backup location.

INNOBACKUPEX=innobackupex-1.5.1
INNOBACKUPEXFULL=/usr/bin/$INNOBACKUPEX
USEROPTIONS="--user=root --password=toor"
BACKUPDIR=/home/tim/mysqlBackup/backups/daily
BACKUPUSER=root
TMPFILE="/tmp/innobackupex-runner.$$.tmp"

# Age of oldest retained backups in days.
AGE=7

# Some info output

echo "----------------------------"
echo
echo "backup_daily.sh: MySQL backup script"
echo "Started: `date`"
echo

# Check options before proceeding

if [ ! -x $INNOBACKUPEXFULL ]; then
 error
 echo "$INNOBACKUPEXFULL does not exist."; echo
 exit 1
fi

if [ ! -d $BACKUPDIR ]; then
 error
 echo "Backup destination folder: $BACKUPDIR does not exist."; echo
 exit 1
fi

# Check that MySQL credentials are correct & that the MySQL server is up.
if ! `echo 'exit' | mysql -s $USEROPTIONS` ; then
 echo
 echo "HALTED: Supplied mysql username or password appears to be incorrect (not copied here for security, see script)"; echo
 echo "Or MySQL is not running?"; echo
 exit 1
fi

# Now run the command to produce the backup; capture it's output.

echo "Check completed OK; running $INNOBACKUPEX command."

$INNOBACKUPEXFULL $USEROPTIONS --defaults-file=/etc/mysql/my.cnf $BACKUPDIR > $TMPFILE 2>&1

if [ -z "`tail -1 $TMPFILE | grep 'completed OK!'`" ] ; then
 echo "$INNOBACKUPEX failed:"; echo
 echo "---------- ERROR OUTPUT from $INNOBACKUPEX ----------"
 cat $TMPFILE
 rm -f $TMPFILE
 exit 1
fi

THISBACKUP=`awk -- "/Backup created in directory/ { split( \\\$0, p, \"'\" ) ; print p[2] }" $TMPFILE`
rm -f $TMPFILE

echo "Databases backed up successfully to: $THISBACKUP"
echo
echo "Now applying logs to the backed-up databases"

# Run the command to apply the logfiles to the backup directory.
$INNOBACKUPEXFULL --apply-log --defaults-file=/etc/mysql/my.cnf $THISBACKUP > $TMPFILE 2>&1

if [ -z "`tail -1 $TMPFILE | grep 'completed OK!'`" ] ; then
 echo "$INNOBACKUPEX --apply-log failed:"; echo
 echo "---------- ERROR OUTPUT from $INNOBACKUPEX --apply-log ----------"
 cat $TMPFILE
 rm -f $TMPFILE
 exit 1
fi

echo "Logs applied to backed-up databases"
echo

#Compress backup
echo "Compressing backup files"
tar -zcf $THISBACKUP.tar.gz -C / $(echo $THISBACKUP | cut -c 2-)
chown $BACKUPUSER:$BACKUPUSER $THISBACKUP.tar.gz
rm -r $THISBACKUP

# Cleanup

echo "Cleaning up old backups (older than $AGE days) and temporary files"
rm -f $TMPFILE
cd /tmp ; find $BACKUPDIR -maxdepth 1 -ctime +$AGE -exec echo "removing: "{} \; -exec rm -rf {} \;

echo
echo "completed: `date`"
exit 0
