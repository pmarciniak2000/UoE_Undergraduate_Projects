(define (domain travelling)
    (:requirements :adl )

    (:types
        agent car - objects
        ;;v t r - car, not needed since define V T R as car anyway? needed if want less constants
        location
        method
    )

    (:constants
        ;; You should not need to add any additional constants
        Agent - agent
        V - car ;;Volve(V),Toyota(T),Renault(R) are all constants since we assume the agent only needs three cars one from each dealer
        T - car
        R - car
    )

    (:predicates
	    (IsConnectedWith ?x - location ?y - location ?m - method)
        (At ?x - objects ?l - location)
	    (Visited ?x - agent ?l - location)
        (IsHired ?c - car)
    )
    
    (:functions
        (budget)
        (totalCost)
    )
    
    (:action HireCar
      :parameters (?c - car ?l - location)
      :precondition (and(At Agent ?l) (At ?c ?l) (not(IsHired ?c)) (<= (+ (totalCost) 2) (budget)))
      :effect (and (IsHired ?c) (increase (totalCost) 2))
    )
    
    (:action ReturnVolvo ;;no need for parameters in return actions since only one car of each type
      :parameters ()
      :precondition (and (At Agent E) (At V E) (IsHired V))
      :effect (not (IsHired V))
    )
    
    (:action ReturnToyota
      :parameters ()
      :precondition (and (At Agent C) (At T C) (IsHired T))
      :effect (not (IsHired T))
    )

    (:action ReturnRenault
      :parameters ()
      :precondition (and (At Agent A) (At R A) (IsHired R))
      :effect (not (IsHired R))
    )
    
    (:action Drive 
      :parameters (?x - location ?y - location ?c - car) ;;added c as a parameter for this task as before assumed there was only one care so could just use constant, but now there are 3 diff cars so need type
      :precondition (and (At ?c ?x) (At Agent ?x) (isHired ?c) (IsConnectedWith ?x ?y Road) (<= (+ (totalCost) 1) (budget))) ;; agent's car totaled, so can only drive hire cars so must add isHired as predicate
      :effect (and (At Agent ?y) (At ?c ?y) (not(At Agent ?x)) (not(At ?c ?x)) (increase (totalCost) 1))
    )

    (:action TakeBus 
      :parameters (?x - location ?y - location ?m - road)
      :precondition (and (IsConnectedWith ?x ?y ?m) (At Agent ?x) (<= (+ (totalCost) 5) (budget)))
      :effect (and (At Agent ?y) (not(At Agent ?x)) (increase (totalCost) 5))
    )

    (:action Fly 
      :parameters (?x - location ?y - location)
      :precondition (and (At Agent ?x) (IsConnectedWith ?x ?y Air) (<= (+ (totalCost) 10) (budget)))
      :effect (and (At Agent ?y) (not(At Agent ?x)) (increase (totalCost) 10))
    )

    (:action Visit 
      :parameters (?l - location)
      :precondition (and (At Agent ?l) (not(Visited Agent ?l)))
      :effect (Visited Agent ?l)
    )
)
