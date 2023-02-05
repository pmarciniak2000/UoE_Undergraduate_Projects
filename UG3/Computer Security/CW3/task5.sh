#!/usr/bin/env bash
python -c 'import json;print(json.dumps({"command":"GET", "name":"ExamSolutions.pdf", "length":100, "offset":-100}))' | nc localhost 4040
