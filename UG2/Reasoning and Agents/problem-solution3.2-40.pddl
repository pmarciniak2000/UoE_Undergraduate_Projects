(define (problem travelling-31)
    (:domain travelling)
    (:objects
        Agent - agent
        Car - car
        Road - road
        Air - air
        A - location
        B - location
        C - location
        D - location
        E - location
        F - location
        G - location
        H - location
        I - location
    )

    (:init
	(IsConnectedWith E A Road)
    (IsConnectedWith A E Road)
    (IsConnectedWith A I Road)
    (IsConnectedWith I A Road)
    (IsConnectedWith I H Road)
    (IsConnectedWith H I Road)
    (IsConnectedWith H B Road)
    (IsConnectedWith B H Road)
    (IsConnectedWith H G Road)
    (IsConnectedWith G H Road)
    (IsConnectedWith G F Road)
    (IsConnectedWith F G Road)
    (IsConnectedWith F D Road)
    (IsConnectedWith D F Road)
    (IsConnectedWith D C Road)
    (IsConnectedWith C D Road)
    (IsConnectedWith F C Road)
    (IsConnectedWith C F Road)
    (IsConnectedWith A B Air)
    (IsConnectedWith B A Air)
    (IsConnectedWith A C Air)
    (IsConnectedWith C B Air)
    (At Agent E)
    (At Car E)
    (= (totalCost) 0)
    (= (budget) 40)
    )

    (:goal (and (Visited Agent D) (Visited Agent B))
  )
)
