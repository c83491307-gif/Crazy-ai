#!/data/data/com.termux/files/usr/bin/bash
set -e
pkg update -y
pkg install nodejs git -y
mkdir -p "$HOME/creazzy-space/public" "$HOME/creazzy-space/data"
echo "انسخ ملفات المشروع إلى $HOME/creazzy-space ثم نفّذ:"
echo "cd ~/creazzy-space && npm start"
echo "ثم افتح http://127.0.0.1:8787"
