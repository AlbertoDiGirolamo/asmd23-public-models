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
