# Task 1: VERIFIER

To achieve the targer is developed an implementation of Petri Net Readers and Writers.
More in particulars this petri Net is composed by 7 places named: Idle, ChooseAction, ReadyToRead, ReadyToWrite, Reading, Writing and HasPermission.

For guarantee safety property is written three methods: `isMutuallyExclusive`, `isReachable` and `isBounded`. 
Each of them is developed using `pnRW.paths(initialState, depth)`, this method is usefull for obtain a Seq of all possible path with a fixed length.

## isMutuallyExclusive 
This method check a Seq of paths that there aren't both Reading status and Writing status at the same time.
More in particular with for-yield I obtain each path possible with a fixed length, for a each path I select each states.

The `diff` function is usefull for obtain the difference between the actual state and the multiset of the wrong conditions and then check the size.

## isReachable
This method check all possible states are reachable from a fixed initial state.

A for-comprehension loop iterates over all paths, over all states in each path, and over all places in each state.
For each iteration, the current place is returned.
Finally, all returned places are collected into a set and compared with the set of all possible places. If the two sets are equal, it means that all possible states are reachable, so the function returns true. Otherwise, it returns false. 

## isBounded

This method checks if the Petri system is bounded by comparing the number of tokens in each state with the maximum number of tokens that can be in the Petri net.

A for-comprehension loop iterates over all paths and over all states in each path. 
For each iteration, it checks if the size of the current state (i.e., the number of tokens in it) is less than or equal to the maximum number of tokens that can be in the Petri net. This is determined by the `maxTokenInPN` function.
Finally, using `.reduce(_ && _)` all the boolean results are reduced using the logical AND operator. If all results are true, it means that the Petri net is bounded, so the function returns true. Otherwise, it returns false.

# Task 3: ARTIST

## Priorities
The main idea is to add priority values for each transaction. Transactions with more high priority values have more priority for to be executed.
So is added an extra parameter inside Trn clase class with 1 how to default value. It is usefull in case someone decide to not use priority function.

* toSystem method is changed for transforms the Petri net into a system by generating all possible transitions from a given marking, filtering out those with the maximum priority, and returning the resulting markings.

* `val maxPriority = allTransitions.map(_._1).max`: This line calculates the maximum priority among all transitions.  
* `allTransitions.filter((p, _) => p == maxPriority).map(_._2)`: This line filters the transitions to keep only those with the maximum priority, and then maps the result to return only the markings, not the priorities.

Then is added another operator for add different priorities. A possible example of its use is the follow: `MSet(*(ChooseAction)) ~~> MSet(*(ReadyToRead)) priority 5,` it add priority 5 for the transaction from ChooseAction to ReadyToRead.


## Colors

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

