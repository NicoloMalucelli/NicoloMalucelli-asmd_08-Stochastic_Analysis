# Stochastic Analysis

## Task 1: PRISM

I implemented the Readers and Writers Petri Net in PRISM using the following Continuous Time Markow Chain:

```
ctmc
const int K=10;
const int p;
module readers_and_writers

    p1 : [0..K] init K;
    p2 : [0..K] init 0;
    p3 : [0..K] init 0;
    p4 : [0..K] init 0;
    p5 : [0..K] init 1;
    p6 : [0..K] init 0;
    p7 : [0..K] init 0;

    [t1] p1>0 & p2<K -> 1 : (p1'=p1-1) & (p2'=p2+1);
    [t2] p2>0 & p3<K -> 200000 : (p2'=p2-1) & (p3'=p3+1);
    [t3] p2>0 & p4<K -> 100000 : (p2'=p2-1) & (p4'=p4+1);
    [t4] p3>0 & p5>0 & p6<K & p5<K -> 100000 : (p3'=p3-1) & (p6'=p6+1);
    [t5] p4>0 & p5>0 & p7<K & p6=0 -> 100000 : (p4'=p4-1) & (p5'=p5-1) & (p7'=p7+1);
    [t6] p6>0 & p1<K -> 0.1*p6 : (p6'=p6-1) & (p1'=p1+1);
    [t7] p7>0 & p1<K & p5<K -> 0.2 : (p7'=p7-1) & (p1'=p1+1) & (p5'=p5+1);

endmodule
```

Where the place `p1` identifies the idle place, the place `p6` the readers, and the place `p7` the writers.

After that I checked some properties, exploring the model's behaviour over time

![at_least_one_reader](https://github.com/NicoloMalucelli/asmd_08-Stochastic_Analysis/assets/73821474/8fc6eafe-bd49-4291-88f7-b38b0f9e0c9e) \
*probability that at least one process is reading `P=? [(true) U<=p (p6>0)] `*

![at_least_one_writer](https://github.com/NicoloMalucelli/asmd_08-Stochastic_Analysis/assets/73821474/9581e49d-ed44-4e38-b7d8-14430e34aaed) \
*probability that at least one process is writing `P=? [(true) U<=p (p7>0)] `*

As we can notice, is more likely that a process will read rather than write due to the higher transition rate of the first.


![at_least_one_reader_and_one_writer](https://github.com/NicoloMalucelli/asmd_08-Stochastic_Analysis/assets/73821474/ec9971aa-1330-462b-9d20-f28cc416cde1) \
*probability that at least one process is reading and at least one is writing*

This last graph has been generated from this formula `P=? [(true) U<=p (p7>0 & p6>0)] ` and show us that is not possible to be in a situation in which a process is writing while another is reading 

## Task 3: Large Scale Design

The system behaviour is described by the following set of rules:

```
val rules = DAP[Place](
    Rule(MSet(S, M1), m => 1, MSet(S, R1), MSet(R1, T), MSet(M1)), // start the communication
    Rule(MSet(M1), m => 1, MSet(R1), MSet(R1, T, S), MSet(M1)), // forward message M1
    Rule(MSet(T, M1), m => 1, MSet(T, R1), MSet(R1), MSet(M2)), // M1 message arrived at destination, forward M2 message
    Rule(MSet(M2), m => 1, MSet(R2), MSet(R2, T, S), MSet(M2)), // forward message M2
    Rule(MSet(M2, S), m => 1, MSet(S, R2), MSet(R2), MSet()), // forward message M2
    
    Rule(MSet(M1, M1), m => 100000, MSet(M1), MSet(), MSet()), // destroy messages in surplus
    Rule(MSet(M2, M2), m => 100000, MSet(M2), MSet(), MSet()), // destroy messages in surplus
)
```

- The places S and T represents respectively the source and the target of the communication; 
- M1 represents the message that S wants to send to T, while M2 is the T's reply;
- R1 and R2 places are used to indicate that the respective message (M1 or M2) has already been elaborated by the 
given node.

The places R1 and R2 are essential for a correct working. Without them, every node would forever exchange the same message
with its neighbours:
- A consumes the message M1 sending it to all its neighbours. By consuming the token in the place M1, the transition of
the node A is not firable anymore, however...
- ... when B (neighbour of A) consumes the token M1, send the message to all its neighbours, including also A, therefore the 
transition of A that was not firable before, becomes firable again
- this process continues forever until finally the source node receive the message M2

By introducing the places R1 and R2, A can send the message to its neighbours only if it hasn't done it yet. This avoid
the infinite loop and made the communication from S to T very much faster. Without the R1 and R2 places expedient, 
the simulation of the system is much slower because the number of firable transition at each step is way higher.

Given a network of k*k devices connected in a square-like network, the average simulation time is depicted 
by the following graph:  

![img.png](doc/LSDgraph.png)

The x-axis represents the width of the square (k), while the y-axis represents the simulation time in unit of times.
