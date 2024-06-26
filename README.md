# Lab 06

## Task 1: VERIFIER

To achieve the target is developed an implementation of Petri Net Readers and Writers.
More in particular, this petri Net is composed of 7 places named: Idle, ChooseAction, ReadyToRead, ReadyToWrite, Reading, Writing and HasPermission.

For guarantee safety property has been written three methods: `isMutuallyExclusive`, `isReachable` and `isBounded`. 
Each of them is developed using `pnRW.paths(initialState, depth)`, this method is useful for get a Seq of all possible path with a fixed length.

### isMutuallyExclusive 
This method checks a Seq of paths that there aren't both Reading status and Writing status at the same time.
More in particular, with for-yield, I obtain each path possible with a fixed length, for a each path I select each states.

The `diff` function is useful for get the difference between the actual state and the multiset of the wrong conditions and then check the size.

```
 def isMutuallyExclusive(initialState: MSet[Place], depth: Int): Boolean =
    (for
      p <- pnRW.paths(initialState, depth)
      s <- p
    yield s.diff(MSet(Reading, Writing)).size != s.size - 2 && s.diff(MSet(Writing, Writing)).size != s.size - 2).reduce(_ && _)

```
### isReachable
This method check all possible states are reachable from a fixed initial state.

A for-comprehension loop iterates over all paths, over all states in each path, and over all places in each state.
For each iteration, the current place is returned.
Finally, all returned places are collected into a set and compared with the set of all possible places. If the two sets are equal, it means that all possible states are reachable, so the function returns true. Otherwise, it returns false. 
```
def isReachable(initialState: MSet[Place], depth: Int): Boolean =
    (for
      path <- pnRW.paths(initialState, depth)
      state <- path
      place <- state.asList
    yield place).toSet == Place.values.toSet
```

### isBounded

This method checks if the Petri system is bounded by comparing the number of tokens in each state with the maximum number of tokens that can be in the Petri net.

A for-comprehension loop iterates over all paths and over all states in each path. 
For each iteration, it checks if the size of the current state (i.e., the number of tokens in it) is less than or equal to the maximum number of tokens that can be in the Petri net. This is determined by the `maxTokenInPN` function.
Finally, using `.reduce(_ && _)` all the boolean results are reduced using the logical AND operator. If all results are true, it means that the Petri net is bounded, so the function returns true. Otherwise, it returns false.

```
def isBounded(initialState: MSet[Place], depth: Int): Boolean =
    (for
      path: Path[Marking[Place]] <- pnRW.paths(initialState, depth)
      state <- path
    yield state.size <= maxTokenInPN(initialState)).reduce(_ && _)
```

Note that this solution is correct only if we apply it to a Readers and Writers Petri Net,
because we know that in this type of nets no more tokens are generated after the initial configuration, 
so we can know exactly the max possible amount of tokens (k + HasPermission). 
For others Petri Nets we need to have a boundary to take into account for check if the number of tokens is increasing to infinite or not.
## Task 3: ARTIST

### Priorities
The main idea is to add priority values for each transaction. Transactions with more high priority values have more priority for to be executed.
So is added an extra parameter inside Trn case class with 1 how to default value. It is useful in case someone decides to not use priority function.

The code is available in `src/main/scala/u06/modelling/ExtendedPetriNet.scala` file.


`case class Trn[P](cond: MSet[*[P]], eff: MSet[*[P]], inh: MSet[*[P]], priority: Int = 1)`

```
def toSystem: System[Marking[P]] = m =>
    val allTransitions =
        for
            Trn(cond, eff, inh, priority) <- pn   // get any transition
            if m disjoined inh          // check inhibition
            out <- m extract cond       // remove precondition
        yield (priority, out union eff)

    val maxPriority = allTransitions.map(_._1).max
    allTransitions.filter((p, _) => p == maxPriority).map(_._2)
```
* `toSystem` method is changed to transform the Petri net into a system by generating all possible transitions from a given marking, filtering out those with the maximum priority, and returning the resulting markings.

* `val maxPriority = allTransitions.map(_._1).max`: This line calculates the maximum priority among all transitions.  
* `allTransitions.filter((p, _) => p == maxPriority).map(_._2)`: This line filters the transitions to keep only those with the maximum priority, and then maps the result to return only the markings, not the priorities.

Then is added another operator for add different priorities. A possible example of its use is the follow: `MSet(*(ChooseAction)) ~~> MSet(*(ReadyToRead)) priority 5,` it add priority 5 for the transaction from ChooseAction to ReadyToRead.


### Colors

The goal is add another abstraction like different colors for each transaction/token.
A transaction has to recevice and release a token with specific color.
* a new enumeration is added for the colors 
```
enum Color:
  case Black, Red
```

* Is added a new case class, it is defined like a couple of values (place-color)  
```
@targetName("Token")
  case class *[P](place: P, color: Color = Color.Black)
```
The default color is black in case someone decides to not use the color abstraction.

A possible example of this use is the follow:
`MSet(*(Idle, Red)) ~~> MSet(*(ChooseAction, Black)),`
in this case, the transaction that goes from Idle to ChooseAction, gives a red token and releases a black token.

# Lab 07

## Task 1: SIMULATOR

The task's aim is to obtain some runs statistics about CTMC like average time of done status during execution of nruns and find the percentage about the total time spent during fail status.

The solution is in `scala/u07/modelling/CTMCSimulation.scala` where there are three API for obtain statistics about a generic CTMC.
Inside the file `scala/u07/examples/StochasticChannelSimulation.scala` there are two examples about uses of these APIs.

### averageTimeToReachState
```
def averageTimeToReachState(nRun: Int, initialState: S, stateToCheck: S) : Double =
    (0 to nRun).foldLeft(0.0)((z, t) =>
        z + self.newSimulationTrace(initialState, new Random)
        .take(10)
        .toList
        .find(e => e._2 == stateToCheck).map(e => e.time).getOrElse(0.0)) / nRun
```
* The function has 3 parameters: run number, start state and state to check reachability.
* The function uses a range from 0 to nRun and applies the foldLeft operation on it. It is useful to get the sum of the total time spent in DONE status.
* For each element t in the range, it simulates a stochastic channel trace with IDLE as the initial state.
* It then finds the first occurrence where the state is in stateToCheck and maps it to its time.
* Finally, the total time is divided by nRun to calculate the average time.

### percentageTimeInState
```
def percentageTimeInState(nRun: Int, initialState: S, stateToCheck: S): Double =
    val totalTimes = (0 to nRun).foldLeft((0.0, 0.0))((acc, _) => {
        val (conditionTime, totTime) = self.newSimulationTrace(initialState, new Random)
            .take(10)
            .toList
            .sliding(2)
            .foldLeft((0.0, 0.0))((z, s) => if (s(0).state == stateToCheck) (z._1 + (s(1).time - s(0).time), s(1).time) else (z._1, s(1).time))

        (acc._1 + conditionTime, acc._2 + totTime)
      })

      totalTimes._1 / totalTimes._2
```
* The function has 3 parameters:  run number, start state and state to check the percentage.
* The function initializes totalTimes as a tuple representing the total fail time and the total time of all simulations.
* For each element in the range, it simulates a stochastic channel trace with IDLE as the initial state and it takes the first 10 states of this trace and converts it to a list.
* It then uses the sliding method to create a sliding window of size 2 over the list. This allows it to compare each state with the next one.
* For each pair of states, if the first state is FAIL, it adds the time difference between the two states to the fail time and sets the total time to the time of the second state. If the first state is not FAIL, it leaves the fail time unchanged and sets the total time to the time of the second state.
* It adds the fail time and total time of the current run to the corresponding values in totalTimes.
* Finally, it divides the total fail time by the total time to get the relative fail time.


A possible use of these two methods is:
```
@main def mainStochasticChannelSimulation =
  println(stocChannel.averageTimeToReachState(10, IDLE, DONE))
  println(stocChannel.percentageTimeInState(10, IDLE, FAIL))
```
## Task 2: GURU

First of all, I realized a stochastic Petri Net of Readers and Writer using rates level provided by lecture lesson.
The possible places inside this net are the follow: Idle, ChooseAction, ReadyToRead, ReadyToWrite, Reading, Writing and HasPermission.

The stochastic Petri Net realized is the follow:

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

To perform this task, I realized a function for get the percentage presence of a particular state.
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

To perform this task, I wrote the model of Writers and Readers.
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

1. For the first question I asked a generic sentence about model checker knowledge.

> Do you know what is this [Readers and Writers model checker]

>This appears to be a model defined in the Continuous-Time Markov Chain (CTMC) formalism using the probabilistic model checker tool PRISM. CTMCs are a mathematical model used to describe the stochastic behavior of systems that evolve over continuous time according to probabilistic transitions between states. In this model, there are seven states labeled as p1 through p7, each representing the number of tokens in different places. Transitions between these states are probabilistic and are labeled with actions (t1 through t7) and associated probabilities or rates. For example, [t1] represents an action that can occur when there are tokens in p1 and space in p2, with a probability of 1. Upon this action, one token moves from p1 to p2.The rest of the transitions similarly represent the movement of tokens between different places according to certain conditions and probabilities. This model seems to describe a system involving the movement of tokens between different locations, with varying probabilities or rates associated with each transition.

Yes LLM know what is and know its utility.

2. Now I'm trying to taste if LLM the main concept about our exercise: stochastic property. It is essential for model cheking.

>Can you understand the meaning of a stochastic property?

>Certainly! In the context of a stochastic model like the one you provided, a stochastic property typically refers to a quantitative measure or condition that characterizes the behavior of the system in probabilistic terms. Stochastic properties are often expressed as logical assertions or quantitative metrics that describe certain aspects of the system's behavior over time.For example, some common stochastic properties include:1. **Reachability**: Whether a certain state or set of states can be reached from a given initial state with a non-zero probability.2. **Transient Analysis**: The probability distribution or expected time until reaching a particular state or set of states from a given initial state.3. **Steady-State Analysis**: The long-term behavior of the system, often characterized by the probability distribution of being in different states after the system has run for a sufficiently long time.4. **Safety Properties**: Properties that specify that certain unwanted states or behaviors should never occur with a high probability.5. **Liveness Properties**: Properties that specify that certain desired states or behaviors should eventually occur with a high probability. To analyze stochastic properties, probabilistic model checking tools like PRISM can be used. These tools allow users to express properties in temporal logic or other formalisms and then automatically check whether these properties hold for the given stochastic model.

3. I want to try if LLM can understand what kind of property I want to write and check if it is correct or not.

>Can you write the property for check if is possible that exist an scenario where p6 and p7 have both a token?

>Sure, to express the property that both p6 and p7 have at least one token simultaneously, you can use the temporal logic formula in PRISM. One way to express this property is using the Until operator. The property can be written as: `Pmax=? [ F (p6>0 & p7>0) ]`. This property translates to: "What is the maximum probability that eventually (F) both p6 and p7 have at least one token?" Here's a breakdown of the components: - `Pmax=?`: This part of the formula asks for the maximum probability. PRISM will compute the maximum probability of the specified event occurring. - `[ F (p6>0 & p7>0) ]`: This is the actual property being checked. It states that eventually (`F` for "finally") both `p6` and `p7` have at least one token (`p6>0 & p7>0`). You can input this property into PRISM along with your model to check if there exists a scenario where both `p6` and `p7` have at least one token with a non-zero probability.

Yes LLM can write a correct proprety from a text request.

4. Now from a specific proprety I find out if LLM understand what it means. I'm tring with reading status. 
> Do you know what `P=? [(true) U<=k (p6>0)]` means?

> Yes, I can explain that to you. The formula `P=? [(true) U<=k (p6>0)]` is a probabilistic reachability property in PRISM, expressed in the probabilistic computation tree logic (PCTL). Let's break it down: - `P=?`: This part indicates that we are asking for the probability of the specified property. The `?` symbol means "what is the probability that...". - `[(true) U<=k (p6>0)]`: This is the actual property being checked. It's a Until (U) temporal operator with a probability bound.  - `(true)`: This part represents a trivial condition that is always true, essentially indicating that the property holds from any state. - `U<=k`: This is the Until operator bounded by a probability threshold `k`. It means "until" with a probability threshold. It checks whether a condition holds until another condition is reached with at most probability `k`. - `(p6>0)`: This is the condition we are interested in. It specifies that `p6` has at least one token. So, putting it all together, the property `P=? [(true) U<=k (p6>0)]` asks: "What is the probability that from any state, `p6` will have at least one token before reaching a state with probability at most `k`?" In other words, it's checking the probability of reaching a state where `p6` has at least one token before reaching a state with a probability less than or equal to `k`.

In this case too it is able to know what it means correctly.

5. I'm tring to check if LLM can intepret directly a model and verify some proprety.
> From this model [Readers and Writers model checker] give me the answer of this proprety `[(true) U<=k (p6>0)&(p7>0)]`

> I'm sorry but I can't interpret this model directly. You have to use for example PRISM and then check the property.

It can't elaborate directly a model.

6. But I can try to ask if it can interpret a model without a directly elaborate and try to verify a possible prorpety.

> ok, can you interprets this property `[(true) U<=k (p6>0)&(p7>0)]` and give me a plausible solution without execute that model?

> Let's analyze the possibility and conditions under which both p6 and p7 can simultaneously have at least one token, given the transitions defined in your model. This analysis will help us interpret the property P=? [(true) U<=k (p6>0)&(p7>0)] without actually executing it in PRISM. System Analysis: Initial Conditions: p1 starts full at N tokens. p2, p3, p4, p6, and p7 start empty. p5 starts with 1 token. Transition Analysis: Transition t1: Moves a token from p1 to p2. Transition t2: Moves a token from p2 to p3. Transition t3: Moves a token from p2 to p4. Transition t4: Requires p3 > 0 and p5 > 0, moves a token from p3 to p6. Transition t5: Requires p4 > 0, p5 > 0, and p6 = 0. It moves a token from p4 to p7 and decreases p5 by 1. Transition t6: Recycles tokens from p6 back to p1. Transition t7: Recycles tokens from p7 back to both p1 and p5. Analysis for Simultaneous Token Presence in p6 and p7: For p6: Tokens reach p6 via t4, which depends on p3 (populated via t2). For p7: Tokens reach p7 via t5, which depends on p4 (populated via t3) and requires p6 = 0. Given these dependencies and conditions, it seems structurally challenging for both p6 and p7 to have tokens simultaneously under the model's initial conditions and transition rules. This difficulty stems from t5 requiring p6 = 0 to allow a token to move to p7. This setup creates a mutual exclusion between tokens in p6 and p7. Plausible Path Analysis: A token in p6 would need to be moved or recycled via t6 or other mechanisms before a token could be moved to p7 via t5. Since t5 also consumes a token in p5 and requires p6 = 0, the opportunity for overlap is essentially null under the current configuration unless additional dynamics or states change (e.g., modifying transition conditions or adding new paths that allow simultaneous token presence without the p6 = 0 constraint in t5). Conclusion: Based on the given model setup and transition rules, it appears improbable (structurally impossible under current rules) that p6 and p7 can simultaneously contain tokens. Therefore, the property P=? [(true) U<=k (p6>0)&(p7>0)] would likely evaluate to a very low probability or even zero, unless the model's structure or initial conditions are changed to allow such an event.

Yes It can give us a possible solution, in this case the result is exactly 0, so it is correct.

# Lab 09

## Task 1: BASIC-Q-LEARNING

For improve this task I used `TryQLearningMatrix.scala` example and write some experiment changing the follow parameters: gamma, alpha and epsilon


### gamma

gamma (Discount Factor) is a number between 0 and 1 and determines the importance of future rewards. Increasing gamma will make the agent more focused on long-term rewards. This could lead to the agent taking actions that may not have an immediate reward but will lead to a higher reward in the future. Decreasing gamma will make the agent more short-sighted, focusing more on immediate rewards.

For instance we use the follow configuration:
* Hight gamma value like 0.9
* Two jumps: (1,0) => (1,4) and (3,0) => (3,2) with this reward: 5 and 10

we obtain the following policy:
```
      >	      >	      >	      v	      <
      >	      >	      >	      ^	      <
      ^	      >	      >	      ^	      <
      ^	      >	      >	      ^	      <
      ^	      >	      >	      ^	      <

```
Let's see that this configuration tents to ignore the first jump because an hight gamma value make more focus on long-term reward, in this case policy tents to get the second jump with more reward.

If we change the gamma value using a lower input like: 0.2, we obtain this policy:

```
      >	      v	      >	      v	      <
      >	      ^	      >	      ^	      <
      >	      ^	      ^	      ^	      ^
      >	      ^	      ^	      ^	      ^
      >	      ^	      ^	      ^	      ^
```
even if the second jump's reward  is two times bigger than the first one, we obtain a few path to the first jump. This happens because the short-term reward grow of importance.


### alpha

alpha (Learning Rate) is also a number between 0 and 1 and represents the extent to which the newly acquired information will override the old information. Increasing alpha will make the agent learn faster from new experiences, meaning it will quickly adapt to changes in the environment. However, a high alpha can also cause the agent to forget previous learnings quickly. Decreasing alpha will make the agent learn slower, meaning it will take longer to adapt to changes in the environment but will retain previous learnings for longer.

Using the that example if I change the alpha value with the a lower value and after a higher value (0.1 and 0.9), policy and v-table don't change significantly.
### epsilon

epsilon (Exploration Rate) is the probability that our agent will explore the environment rather than exploit it. Increasing epsilon will make the agent explore the environment more, meaning it will take more random actions to discover new states. This can be beneficial in environments where the agent has not explored much yet. Decreasing epsilon will make the agent exploit its current knowledge more, meaning it will take the actions that it believes will lead to the highest reward based on its current knowledge.

* I chose to elevate the matrix size like (10, 10) for improve the changing visual effect.

* Adding an extra reward opposite to the initial state which its reward is elevated (like 100) I create a position that represents the best end-point.
```case ((8,8),DOWN) => 100;```

The best policy must catch that poit and create the best path to arrive in this case (8,8).
If we maintain a lower epsilon value, Q-learning might not create a better path because it is not programmed to try to explore the matrix if it found a possible good end-point.
For istance with a epsilon value of 0.3 we obtain this policy:
```
      >	      v	      <	      <	      <	      v	      v	      >	      >	      >
      >	      ^	      <	      <	      <	      <	      <	      ^	      >	      <
      >	      ^	      <	      <	      <	      <	      ^	      >	      v	      >
      >	      ^	      <	      <	      ^	      <	      v	      ^	      v	      >
      >	      ^	      <	      <	      <	      ^	      <	      >	      v	      <
      >	      ^	      <	      <	      <	      ^	      ^	      >	      >	      <
      ^	      ^	      ^	      <	      <	      <	      >	      >	      v	      >
      ^	      ^	      ^	      <	      ^	      >	      ^	      >	      v	      >
      >	      >	      ^	      ^	      <	      ^	      >	      >	      v	      <
      ^	      ^	      <	      ^	      ^	      >	      >	      >	      ^	      <
```
Let's see that with (0, 0) initial position, the probability to find the point with most reward is not in our favor because this policy hasn't the tendency to deep exploration.

Otherwise, if we grow the epsilon value to 0.8 for instance we can observe that policy tends to reach the max reward:

```
      >	      v	      <	      v	      >	      >	      >	      >	      v	      <
      >	      ^	      >	      >	      >	      >	      >	      >	      v	      <
      >	      >	      >	      >	      >	      >	      >	      >	      v	      <
      >	      >	      >	      >	      >	      >	      >	      >	      v	      <
      >	      >	      >	      >	      >	      >	      >	      >	      v	      <
      >	      >	      >	      >	      >	      >	      >	      >	      v	      <
      >	      >	      >	      >	      >	      >	      >	      >	      v	      <
      >	      >	      >	      >	      >	      >	      >	      >	      v	      <
      >	      >	      >	      >	      >	      >	      >	      >	      v	      <
      >	      >	      >	      >	      >	      >	      >	      >	      ^	      <
```
### Episodes number

Until now as always been used 10000 how number of episodes for learners.
```
      >	      v	      <	      v	      <
      >	      ^	      <	      ^	      <
      >	      ^	      <	      ^	      <
      >	      ^	      <	      ^	      <
      >	      ^	      <	      ^	      <
```
If we decrease significantly this number like 100, we can observe that the policy accuracy decreases significantly
```
      >	      v	      <	      v	      <
      >	      ^	      <	      ^	      <
      >	      ^	      <	      ^	      <
      ^	      ^	      ^	      ^	      ^
      ^	      ^	      ^	      ^	      <
```
## Task 2: DESIGN-BY-Q-LEARNING

For improve this task I used `TryQLearningMatrix.scala` example to implement my own Q-learning algorithm.
I created a ExtendedQMatrix class for modify the QMatrix already implemented. 

### Corridor

For simulate map composed by a corridor I created a Matrix 10x5.

I want to simulate the corridor's wall, so I set a negative reward when Q-learning try to go outside the map:

```
case ((_,0),RIGHT) => -10;
case ((_,4),RIGHT) => -10;
```
The initial state is  `initial = (0,1)`.

In case we want to add some obstacles is possible to add it like a negative reward in case Q-learning try to go in that direction.
I saved the position of the obstacles in a list `def obstacles: Set[(Int, Int)] = Set((2,1), (5,2))` and i check every time if the next state is an obstacle and in case i set a negative reward.

```
28.24	31.38	34.05	47.83	53.14	59.05	65.61	72.90	81.00	90.00
31.38	34.87	37.83	53.14	59.05	65.61	72.90	81.00	90.00	100.00
34.87	38.74	43.05	47.83	53.14	49.05	65.61	72.90	81.00	90.00
31.38	34.87	38.74	43.05	47.83	53.14	59.05	65.61	72.90	81.00
28.24	31.38	34.87	38.74	43.05	47.83	53.14	59.05	65.61	72.90
```
```
      v	      v	      v	      v	      v	      v	      v	      v	      v	      v
      >	      v	      *	      >	      >	      >	      >	      >	      >	      >
      >	      >	      >	      >	      ^	      *	      >	      >	      >	      ^
      >	      >	      >	      >	      >	      >	      >	      >	      >	      ^
      ^	      ^	      ^	      ^	      ^	      ^	      ^	      ^	      ^	      ^
```

### Items

For improve this part i added some items in a map. These items have a positive reward.
Every time an items is collected, that is removed from the map. I use a list for save the position of the items inside the world `var totalItems = Set((1, 1), (3, 3), (7, 2))`
and a list for maintain in memory the concept of remaining items in a play `var remainingItems = Set((1, 1), (3, 3), (7, 2))`.
An item is identify by  `$` symbol.

For avoid that the best policy maintain a static policy near the item position i add a negative reward in case robot cross to a position where items was present.
`case (s, a) if totalItems.contains(s) && !remainingItems.contains(s) => (totalItems.size-remainingItems.size + 1) * -4` it adds a negative growing reward.

For to keep the robot moving and collect all items, each items has a growing rewarding value. 
```
case(s, a) if remainingItems.contains(s) =>
    remainingItems = remainingItems - s
    (totalItems.size-remainingItems.size + 1) * 20
```

At this point is necessary reset each episode and setup items in the map.
For this reason I add a
```
trait ResetFunction:
    def execute(): Unit
``` 
and a  `def resetFunction: ResetFunction` inside System trait inside QRL.scala

Then I modified the QSystem case class included in `QRLImpl.scala`: I added a new parameter in the constructor: `resetMap: ResetFunction` and add the reset execution inside the learn method `system.resetFunction.execute()`.

After all I added a reset method inside `ExtendedQMatrix` class. More in particular I added `resetMap: ResetFunction,` in Facade case class and add a new parameter in `def qSystem = QSystem(environment = qEnvironment(), initial, terminal, resetMap).`

``` 
60.17	74.52	70.00	74.70	82.67	91.85	102.06	113.40	102.06
63.11	85.83	78.64	83.67	91.85	102.06	113.40	126.00	113.40
72.08	81.04	87.96	91.85	102.06	113.40	126.00	140.00	126.00
71.76	81.10	96.50	86.38	91.85	102.06	113.40	126.00	113.40
66.41	72.37	82.12	90.35	83.29	91.85	102.06	113.40	102.06
60.27	68.74	75.04	78.78	74.98	82.67	91.85	102.06	91.85
``` 
``` 
      >	      v	      v	      v	      v	      >	      >	      v	      <
      >	      $	      v	      v	      v	      >	      >	      v	      <
      >	      ^	      >	      >	      >	      >	      >	      $	      <
      >	      >	      >	      $	      >	      >	      >	      ^	      <
      ^	      ^	      ^	      ^	      ^	      ^	      >	      ^	      <
      >	      >	      ^	      ^	      ^	      ^	      >	      ^	      <
``` 