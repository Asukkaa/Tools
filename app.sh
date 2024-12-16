#!/bin/sh
JLINK_VM_OPTIONS=-Xmx12g
DIR=`dirname $0`
cd $DIR
$DIR/java $JLINK_VM_OPTIONS -m priv.koishi.tools/priv.koishi.tools.MainApplication "$@"
