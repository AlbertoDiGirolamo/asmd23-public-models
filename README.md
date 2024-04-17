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

The task's aim is to obtain some statistics about runs about CTMC like average time of done status during execution of nruns and find the percentage about the total time spent during fail status.

The solution is in `scala/u07/examples/StochasticChannelSimulation.scala`

### averageTimeStochasticChannel

* The function uses a range from 0 to nRun and applies the foldLeft operation on it. It is useful for obtain the sum about the total time spent in DONE status.
* For each element t in the range, it simulates a stochastic channel trace with IDLE as the initial state.
* It then finds the first occurrence where the state is DONE and maps it to its time.
* Finally, the total time is divided by nRun to calculate the average time.

### relativeFailTime

* The function initializes totalTimes as a tuple representing the total fail time and the total time of all simulations.
* For each element in the range, it simulates a stochastic channel trace with IDLE as the initial state and it takes the first 10 states of this trace and converts it to a list.
* It then uses the sliding method to create a sliding window of size 2 over the list. This allows it to compare each state with the next one.
* For each pair of states, if the first state is FAIL, it adds the time difference between the two states to the fail time and sets the total time to the time of the second state. If the first state is not FAIL, it leaves the fail time unchanged and sets the total time to the time of the second state.
* It adds the fail time and total time of the current run to the corresponding values in totalTimes.
* Finally, it divides the total fail time by the total time to get the relative fail time.


## Task 3: CHEMIST

Task's purpose is realyse a dynamic simulation of chemical reactions. More specifically, let's simulate Brussellator's chemical reaction.

The stochastic Petri Net of Brussellator is composed by six place named: A, B, D, E, X, Y. Therefore the Petri Net is composed by the following code: 
```
val spnBrussellator = SPN[Place](
    Trn(MSet(A), m => 1.0 , MSet(X), MSet()),
    Trn(MSet(X, X, Y), m => 1.0 , MSet(X, X, X), MSet()),
    Trn(MSet(B, X), m => 1.0 , MSet(Y, D), MSet()),
    Trn(MSet(X), m => 1.0 ,MSet(E), MSet())
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
Event(0.0,{B|B|B|X|A|Y})
Event(0.3075338571039183,{D|B|B|A|Y|Y})
Event(1.8217323168205766,{Y|Y|D|B|B|X})
Event(2.215163872960756,{Y|Y|Y|D|D|B})
Event(2.215163872960756,{Y|Y|Y|D|D|B})
Event(2.215163872960756,{Y|Y|Y|D|D|B})
Event(2.215163872960756,{Y|Y|Y|D|D|B})
Event(2.215163872960756,{Y|Y|Y|D|D|B})
Event(2.215163872960756,{Y|Y|Y|D|D|B})
Event(2.215163872960756,{Y|Y|Y|D|D|B})

```
The xchart library is used to create a chart that shows the number of X and Y tokens over time.








