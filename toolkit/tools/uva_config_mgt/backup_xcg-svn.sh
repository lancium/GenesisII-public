
#needs testing on real svn db.

pushd $HOME

mv scripts/svn_startup.sh scripts/svn_startup.sh.hold

killall svnserve
sleep 10
sep="-"

backup_file="$HOME/xcg-svn-backup-$(date +"%Y$sep%m$sep%d$sep%H%M$sep%S" | tr -d '/\n/').tar.gz"

tar -czf "$backup_file" $HOME/.secrets $HOME/repos $HOME/scripts

mv scripts/svn_startup.sh.hold scripts/svn_startup.sh 

popd

bash $HOME/scripts/svn_startup.sh

