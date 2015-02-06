#!/usr/bin/env python

import sys

if len(sys.argv) != 5:
    raise ValueError("Usage: %s <input> <output> <skip-N-first-apps> <offset>" % sys.argv[0])

file_input = sys.argv[1]
file_output = sys.argv[2]
skip_apps = int(sys.argv[3])
offset = int(sys.argv[4])

# Read input file
with open(file_input) as f:
    file_content = f.read().splitlines()

for key, value in enumerate(file_content):
    trace = value.split(":")

    # Skip non-traces
    if value.startswith('#') or ("null" in value) or len(trace) < 8:
        continue

    # Skip primary node traces (already in sync)
    if int(trace[2]) <= skip_apps:
        continue

    # State trace
    if int(trace[0]) == 1:
        trace[5] = str(int(trace[5]) + offset)
        trace[6] = str(int(trace[6]) + offset)

    # Event trace
    if int(trace[0]) == 2:
        trace[5] = str(int(trace[5]) + offset)

    # Save modification
    file_content[key] = ":".join(trace)


# Write output file
with open(file_output, 'w') as f:
    f.write("\n".join(file_content))
