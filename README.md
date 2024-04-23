# Lab 06

## Task 1: VERIFIER

To achieve the targer is developed an implementation of Petri Net Readers and Writers.
More in particulars this petri Net is composed by 7 places named: Idle, ChooseAction, ReadyToRead, ReadyToWrite, Reading, Writing and HasPermission.

For guarantee safety property is written three methods: `isMutuallyExclusive`, `isReachable` and `isBounded`. 
Each of them is developed using `pnRW.paths(initialState, depth)`, this method is usefull for obtain a Seq of all possible path with a fixed length.

### isMutuallyExclusive 
This method check a Seq of paths that there aren't both Reading status and Writing status at the same time.
More in particular with for-yield I obtain each path possible with a fixed length, for a each path I select each states.

The `diff` function is usefull for obtain the difference between the actual state and the multiset of the wrong conditions and then check the size.

### isReachable
This method check all possible states are reachable from a fixed initial state.

A for-comprehension loop iterates over all paths, over all states in each path, and over all places in each state.
For each iteration, the current place is returned.
Finally, all returned places are collected into a set and compared with the set of all possible places. If the two sets are equal, it means that all possible states are reachable, so the function returns true. Otherwise, it returns false. 

### isBounded

This method checks if the Petri system is bounded by comparing the number of tokens in each state with the maximum number of tokens that can be in the Petri net.

A for-comprehension loop iterates over all paths and over all states in each path. 
For each iteration, it checks if the size of the current state (i.e., the number of tokens in it) is less than or equal to the maximum number of tokens that can be in the Petri net. This is determined by the `maxTokenInPN` function.
Finally, using `.reduce(_ && _)` all the boolean results are reduced using the logical AND operator. If all results are true, it means that the Petri net is bounded, so the function returns true. Otherwise, it returns false.

## Task 3: ARTIST

### Priorities
The main idea is to add priority values for each transaction. Transactions with more high priority values have more priority for to be executed.
So is added an extra parameter inside Trn clase class with 1 how to default value. It is usefull in case someone decide to not use priority function.

* toSystem method is changed for transforms the Petri net into a system by generating all possible transitions from a given marking, filtering out those with the maximum priority, and returning the resulting markings.

* `val maxPriority = allTransitions.map(_._1).max`: This line calculates the maximum priority among all transitions.  
* `allTransitions.filter((p, _) => p == maxPriority).map(_._2)`: This line filters the transitions to keep only those with the maximum priority, and then maps the result to return only the markings, not the priorities.

Then is added another operator for add different priorities. A possible example of its use is the follow: `MSet(*(ChooseAction)) ~~> MSet(*(ReadyToRead)) priority 5,` it add priority 5 for the transaction from ChooseAction to ReadyToRead.


### Colors

The goal is add another abstraction like different colors for each transaction/token.
A transaction has to recevice and release a token with specific color.

Is added a new calse class where is defined a couple of values (place-color)  
```
@targetName("Token")
  case class *[P](place: P, color: Color = Color.Black)
```
The default color is black in case someone decide to not utilise the color abstraction.

A possible example of this use is the follow:
`MSet(*(Idle, Red)) ~~> MSet(*(ChooseAction, Black)),`
in this case the transaction give a red token and release a black token.

# Lab 07

## Task 1: SIMULATOR

The task's aim is to obtain some runs statistics about CTMC like average time of done status during execution of nruns and find the percentage about the total time spent during fail status.

The solution is in `scala/u07/modelling/CTMCSimulation.scala` where there are three API for obtain statistics about a generic CTMC.

### averageTimeToReachState

* The function has 3 parameters: run number, start state and state to check reachability.
* The function uses a range from 0 to nRun and applies the foldLeft operation on it. It is useful for obtain the sum about the total time spent in DONE status.
* For each element t in the range, it simulates a stochastic channel trace with IDLE as the initial state.
* It then finds the first occurrence where the state is in stateToCheck and maps it to its time.
* Finally, the total time is divided by nRun to calculate the average time.

### percentageTimeInState

* The function has 3 parameters:  run number, start state and state to check the percentage.
* The function initializes totalTimes as a tuple representing the total fail time and the total time of all simulations.
* For each element in the range, it simulates a stochastic channel trace with IDLE as the initial state and it takes the first 10 states of this trace and converts it to a list.
* It then uses the sliding method to create a sliding window of size 2 over the list. This allows it to compare each state with the next one.
* For each pair of states, if the first state is FAIL, it adds the time difference between the two states to the fail time and sets the total time to the time of the second state. If the first state is not FAIL, it leaves the fail time unchanged and sets the total time to the time of the second state.
* It adds the fail time and total time of the current run to the corresponding values in totalTimes.
* Finally, it divides the total fail time by the total time to get the relative fail time.

## Task 2: GURU

First of all i realised a stochastic Petri Net of Readers and Writer using rates level provided by lecture lesson.
The possible place inside this net are the follow: Idle, ChooseAction, ReadyToRead, ReadyToWrite, Reading, Writing and HasPermission.

The stochastic Petri Net realised is the follow:

```
val spnReadersWriters = SPN[Place](
    Trn(MSet(Idle), m => 1 , MSet(ChooseAction), MSet()),
    Trn(MSet(ChooseAction), m => 200000 , MSet(ReadyToRead), MSet()),
    Trn(MSet(ChooseAction), m => 100000 , MSet(ReadyToWrite), MSet()),
    Trn(MSet(ReadyToRead, HasPermission), m => 100000 , MSet(Reading, HasPermission), MSet()),
    Trn(MSet(Reading), m => 0.1 * m(Reading) , MSet(Idle), MSet()),
    Trn(MSet(ReadyToWrite, HasPermission), m => 100000 , MSet(Writing), MSet(Reading)),
    Trn(MSet(Writing), m => 0.2 , MSet(Idle, HasPermission), MSet())
  )

```

To perform this task I realised a function for obtain the percentage presence of a particular state.
```
 def percentageTimeInReadersWritersState(nRun: Int, initSet: MSet[Place], stateToCheck: Place) : Double =
    val totalTimes = (0 to nRun).foldLeft((0.0, 0.0))((acc, _) => {
      val (rwTime, totTime) = toCTMC(spnReadersWriters).newSimulationTrace(initSet, new Random)
        .take(10)
        .toList
        .sliding(2)
        .foldLeft((0.0, 0.0)) ( (z, s) =>  if (s(0).state(stateToCheck) > 0) (z._1 + (s(1).time - s(0).time), s(1).time) else (z._1, s(1).time))
      (acc._1 + rwTime, acc._2 + totTime)
    })


    totalTimes._1 / totalTimes._2
```

with `(s(0).state(stateToCheck) > 0` it check if during a simulation a particular state is present.

Following is write a table with some statistics about different rate value of Reading and Writing transaction:

| Reading | Writing | % of time in Reading | % of time in Writing |
|--------------|--------------|----------------------|----------------------|
| 400000       | 100000       | 90.1%                | 7.4%                 |
| 300000       | 100000       | 79.4%                | 14.1%                |
| 200000       | 100000       | 65.3%                | 20.8%                |
| 100000       | 100000       | 53.6%                | 41.7%                |
| 100000       | 200000       | 36.4%                | 52.8%                |
| 100000       | 300000       | 30.2%                | 55.0%                |
| 100000       | 400000       | 21.8%                | 74.3%                |

The rate value and the percentage follow a behaviour direct proportional like



## Task 3: CHEMIST

Task's purpose is realyse a dynamic simulation of chemical reactions. More specifically, let's simulate Brussellator's chemical reaction.

The stochastic Petri Net of Brussellator is composed by six place named: A, B, D, E, X, Y. Therefore the Petri Net is composed by the following code: 
```
val spnBrussellator = SPN[Place](
    Trn(MSet(), m => 1, MSet(A), MSet()),
    Trn(MSet(), m => 1, MSet(B), MSet()),
    Trn(MSet(A), m => 1, MSet(X), MSet()),
    Trn(MSet(X, X, Y), m =>  m(Y), MSet(X, X, X), MSet()),
    Trn(MSet(B, X), m => m(X) * 0.5, MSet(Y, D), MSet()),
    Trn(MSet(X), m => m(X) * 0.5, MSet(E), MSet())
  )
```

The simulation is started with 6 initial place: 
```
val execution = toCTMC(spnBrussellator).newSimulationTrace(MSet(X,Y,A,B,B,B), new Random)
      .take(10)
      .toList
```
after that let's obtain a simulation execution where we consider only first 10 states. Each state is represented as a pair of a timestamp and a marking.
An example of the contents of the execution variable is follow:
```
Event(0.0,{B|B|B|Y|X|A})
Event(0.149952306725683,{B|B|B|Y|X|A|A})
Event(0.18013469803906576,{B|B|B|Y|X|A|A|A})
Event(0.23547245929893335,{B|B|B|Y|X|A|A|A|A})
Event(0.2736230767856125,{B|B|B|Y|X|A|A|A|A|A})
Event(1.211966196786594,{B|B|B|Y|X|X|A|A|A|A})
Event(1.3218807547538567,{B|B|B|Y|X|X|X|A|A|A})
...

```
The xchart library is used to create a chart that shows the number of X and Y tokens over time.

The main problem here is to find a transactions's good rate level. 

What follows is a logical explanation.
Consider a system where the transition rate of reactions that consume molecule Y and produce molecule X is proportional to the quantity of Y. This means that the greater the amount of Y in the system, the higher the transition rate, and vice versa.

Similarly, the transition rates for reactions that consume molecule X depend on the amount of X present. Hence, a higher concentration of X results in increased transition rates, and a lower concentration results in decreased rates.

In the Brussellator model, there are two transitions that consume X. To account for these dual consumption pathways, the rate of each transition involving the consumption of X is halved, or multiplied by 0.5. Conversely, there is only one transition that consumes Y.

![Brussellator graph](https://github.com/AlbertoDiGirolamo/asmd23-public-models/blob/master/brussellator.png)

# Lab 08

## Task 1: PRISM

For perform this task i'ss needed to write the model of Writers and Readers.
It is in the lecture slide like follow:

```
ctmc
const int N = 20;

module RW

p1 : [0..N] init N;
p2 : [0..N] init 0;
p3 : [0..N] init 0;
p4 : [0..N] init 0;
p5 : [0..N] init 1;
p6 : [0..N] init 0;
p7 : [0..N] init 0;

[t1] p1>0 & p2<N  -> 1 : (p1'=p1-1)&(p2'=p2+1);
[t2] p2>0 & p3<N ->  200000 : (p2'=p2-1) & (p3'=p3+1);
[t3] p2>0 & p4<N -> 100000 : (p2'=p2-1) & (p4'=p4+1);
[t4] p3>0 & p5>0 & p6<N -> 100000 : (p3'=p3-1) & (p6'=p6+1);
[t5] p4>0 & p5>0 & p6=0 & p7<N -> 100000 : (p4'=p4-1) & (p5'=p5-1) & (p7'=p7+1);
[t6] p6>0 & p1<N -> p6*1 : (p6'=p6-1) & (p1'=p1+1);
[t7] p7>0 & p5<N & p1<N -> 0.5 : (p7'=p7-1) & (p1'=p1+1) & (p5'=p5+1);

endmodule
```
After that, I used probabilistic model checker PRISM to check some probabilities about some characteristics that a Readers and Writers system must guarantee.

### Condition: p6 > 0
This property aims to verify the operational capability of the Readers and Writers System. It means that anyone who wishes to read within the system should be able to do so sooner or later.

<img src="https://github.com/AlbertoDiGirolamo/asmd23-public-models/blob/master/lab08IMG/cond%20p6.png" width="500px">

It shows the probability increasing; after around 10 iterations, the probability is close to 1.

<img src="https://github.com/AlbertoDiGirolamo/asmd23-public-models/blob/master/lab08IMG/p6.png" width="500px">

###  Condition: p7 > 0
Like the previous property, It means that anyone who wishes to write within the system should be able to do so sooner or later.

<img src="https://github.com/AlbertoDiGirolamo/asmd23-public-models/blob/master/lab08IMG/cond%20p7.png" width="500px">

It shows the probability increasing; after around 10 iterations, the probability is close to 1.

<img src="https://github.com/AlbertoDiGirolamo/asmd23-public-models/blob/master/lab08IMG/p7.png" width="500px">

###  Condition: p6 > 0 & p7 > 0

This experiment whant to demonstrate the impossibility to do in the same time read and write operations.

<img src="https://github.com/AlbertoDiGirolamo/asmd23-public-models/blob/master/lab08IMG/p6%20and%20p7.png" width="500px">

This graph show 0 probability for that condition.

<img src="https://github.com/AlbertoDiGirolamo/asmd23-public-models/blob/master/lab08IMG/cond%20p6%20and%20p7.png" width="500px">

## Task 4: LLM-STOCHASTIC-ANALYSIS

To perform this task I used LLM ChatGPT.

> Do you know what is this [Readers and Writers model checker]

>This appears to be a model defined in the Continuous-Time Markov Chain (CTMC) formalism using the probabilistic model checker tool PRISM. CTMCs are a mathematical model used to describe the stochastic behavior of systems that evolve over continuous time according to probabilistic transitions between states. In this model, there are seven states labeled as p1 through p7, each representing the number of tokens in different places. Transitions between these states are probabilistic and are labeled with actions (t1 through t7) and associated probabilities or rates. For example, [t1] represents an action that can occur when there are tokens in p1 and space in p2, with a probability of 1. Upon this action, one token moves from p1 to p2.The rest of the transitions similarly represent the movement of tokens between different places according to certain conditions and probabilities. This model seems to describe a system involving the movement of tokens between different locations, with varying probabilities or rates associated with each transition.

>Can you understand the meaning of a stochastic property?
>Certainly! In the context of a stochastic model like the one you provided, a stochastic property typically refers to a quantitative measure or condition that characterizes the behavior of the system in probabilistic terms. Stochastic properties are often expressed as logical assertions or quantitative metrics that describe certain aspects of the system's behavior over time.For example, some common stochastic properties include:1. **Reachability**: Whether a certain state or set of states can be reached from a given initial state with a non-zero probability.2. **Transient Analysis**: The probability distribution or expected time until reaching a particular state or set of states from a given initial state.3. **Steady-State Analysis**: The long-term behavior of the system, often characterized by the probability distribution of being in different states after the system has run for a sufficiently long time.4. **Safety Properties**: Properties that specify that certain unwanted states or behaviors should never occur with a high probability.5. **Liveness Properties**: Properties that specify that certain desired states or behaviors should eventually occur with a high probability. To analyze stochastic properties, probabilistic model checking tools like PRISM can be used. These tools allow users to express properties in temporal logic or other formalisms and then automatically check whether these properties hold for the given stochastic model.

>Can you write the property for check if is possible that exist an scenario where p6 and p7 have both a token?

>Sure, to express the property that both p6 and p7 have at least one token simultaneously, you can use the temporal logic formula in PRISM. One way to express this property is using the Until operator. The property can be written as:

```
Pmax=? [ F (p6>0 & p7>0) ]
```

> This property translates to: "What is the maximum probability that eventually (F) both p6 and p7 have at least one token?" Here's a breakdown of the components: - `Pmax=?`: This part of the formula asks for the maximum probability. PRISM will compute the maximum probability of the specified event occurring. - `[ F (p6>0 & p7>0) ]`: This is the actual property being checked. It states that eventually (`F` for "finally") both `p6` and `p7` have at least one token (`p6>0 & p7>0`). You can input this property into PRISM along with your model to check if there exists a scenario where both `p6` and `p7` have at least one token with a non-zero probability.

> Do you know what `P=? [(true) U<=k (p6>0)]` means?

> Yes, I can explain that to you. The formula `P=? [(true) U<=k (p6>0)]` is a probabilistic reachability property in PRISM, expressed in the probabilistic computation tree logic (PCTL). Let's break it down: - `P=?`: This part indicates that we are asking for the probability of the specified property. The `?` symbol means "what is the probability that...". - `[(true) U<=k (p6>0)]`: This is the actual property being checked. It's a Until (U) temporal operator with a probability bound.  - `(true)`: This part represents a trivial condition that is always true, essentially indicating that the property holds from any state. - `U<=k`: This is the Until operator bounded by a probability threshold `k`. It means "until" with a probability threshold. It checks whether a condition holds until another condition is reached with at most probability `k`. - `(p6>0)`: This is the condition we are interested in. It specifies that `p6` has at least one token. So, putting it all together, the property `P=? [(true) U<=k (p6>0)]` asks: "What is the probability that from any state, `p6` will have at least one token before reaching a state with probability at most `k`?" In other words, it's checking the probability of reaching a state where `p6` has at least one token before reaching a state with a probability less than or equal to `k`.



