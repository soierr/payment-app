tc qdisc add dev eth0 root handle 1: prio

tc filter add dev eth0 protocol ip parent 1: prio 1 u32 \
match ip sport 8082 0xffff \
flowid 1:1 action drop
