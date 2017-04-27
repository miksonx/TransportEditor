#!/bin/bash

function run-remotely {
TEvaraint="$1"
config="$2"
serv="$3"

echo "source .shrc && cd git/$TEvariant/tools/benchmarks && ./benchmark.sh configs/$config.json 2>&1; exit" | ssh -tt "u-pl$serv"
echo "`date -u '+[%H:%M:%S]'` Finished: $config"
}

configs='echo seq-sat-${ipc}-rrapn seq-sat-${ipc}-msfa3 seq-sat-${ipc}-msfa5 tempo-sat-${ipc}-tfd2014 tempo-sat-${ipc}-rrapnsched tempo-sat-${ipc}-msfa3sched tempo-sat-${ipc}-msfa5sched'
TEvariant="TransportEditor-final"
exp_configs=""

for ipc in ipc08 ipc11 ipc14; do
    for config in `eval $configs`; do
        if [ -f "configs/$config.json" ]; then
            echo "Found config: $config"
            exp_configs="$exp_configs $config"
        fi
    done
done

i=3
for config in $exp_configs; do
    run-remotely "$TEvariant" "$config" "$i" &
    i=$((i+1))
done

echo "Waiting for jobs to finish..."
wait $(jobs -p)
echo "Ended, waiting for AFS sync"
sleep 30s
cd "$HOME/git/$TEvariant/tools/benchmarks"
. merge-last-results.sh
echo "Merge dir: `pwd`/results/merged"
