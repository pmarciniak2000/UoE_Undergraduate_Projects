(define (domain travelling)
    (:requirements :adl )

    (:types
        agent car - objects
        location
        road air - method
    )

    (:constants
        ;; You should not need to add any additional constants
        Agent - agent
        Car - car ;;car is a constant since for this part we assume there is only one car, so to simplify things I made it a constant
    )

    (:predicates
	    (IsConnectedWith ?x - location ?y - location ?m - method)
        (At ?x - objects ?l - location)
	    (Visited ?x - agent ?l - location)

    )
    
    (:action Drive 
      :parameters (?x - location ?y - location ?m - road)
      :precondition (and (At Car ?x) (At Agent ?x) (IsConnectedWith ?x ?y ?m))
      :effect (and (At Agent ?y) (At Car ?y) (not(At Agent ?x)) (not(At Car ?x)))
    )

    (:action Fly 
      :parameters (?x - location ?y - location ?m - air)
      :precondition (and (At Agent ?x) (IsConnectedWith ?x ?y ?m))
      :effect (and (At Agent ?y) (not(At Agent ?x)))
    )

    (:action Visit 
      :parameters (?l - location)
      :precondition (and (At Agent ?l) (not(Visited Agent ?l)))
      :effect (Visited Agent ?l)
    )
)
