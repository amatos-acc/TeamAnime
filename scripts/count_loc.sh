#!/usr/bin/env bash
# Counts nonblank, non-comment Java lines (the assignment's 500-line metric).
# Strips // line comments and /* ... */ (including Javadoc) blocks.
set -euo pipefail
cd "$(dirname "$0")/.."
total=0
for f in $(find src -name '*.java' | sort); do
  n=$(awk '
    { line=$0 }
    inblock { if (index(line,"*/")) { line=substr(line,index(line,"*/")+2); inblock=0 } else next }
    { while (index(line,"/*")) {
        pre=substr(line,1,index(line,"/*")-1); rest=substr(line,index(line,"/*")+2)
        if (index(rest,"*/")) line=pre substr(rest,index(rest,"*/")+2); else { line=pre; inblock=1 }
      } }
    { sub(/\/\/.*/,"",line); gsub(/[ \t\r]/,"",line); if (length(line)>0) c++ }
    END { print c+0 }' "$f")
  printf "%5d  %s\n" "$n" "$f"
  total=$((total+n))
done
echo "-----"
printf "%5d  TOTAL (nonblank, non-comment)\n" "$total"
