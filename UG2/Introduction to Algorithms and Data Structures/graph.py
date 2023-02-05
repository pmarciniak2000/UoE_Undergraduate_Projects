import math


def euclid(p,q):
    x = p[0]-q[0]
    y = p[1]-q[1]
    return math.sqrt(x*x+y*y)
                
class Graph:

    dists = []
    perm = []

    # Complete as described in the specification, taking care of two cases:
    # the -1 case, where we read points in the Euclidean plane, and
    # the n>0 case, where we read a general graph in a different format.
    # self.perm, self.dists, self.n are the key variables to be set up.
    
    def __init__(self,n,filename):
        f = open(filename, "r") #reads file
        f1 = f.readlines() # reads individual lines
        table = [] # table of values from file
        for line in f1: 
            values = line.strip().split() #splits the two values for each line into an array, while strip removes all spaces in front and behind values
            result = list(map(int,values)) #converts the string values in the array to ints
            table.append(result)
        
        perm = []

        if n == -1: #euclid tsp   
            self.n = len((table))
            dists = [[0 for _ in range(len(table))] for _ in range(len(table))] 
            for i in range(len(table)):
                for j in range(len(table)):
                    dists[i][j] = euclid(table[i],table[j])
            self.dists = dists

            perm = list(range(self.n)) #initialising perm to perm[i] = i 
            self.perm = perm
            
        elif n > 0: #general tsp
            self.n = n
            dists = [self.n * [0] for _ in range(self.n)]
            for x, y, d in table:
                dists[x][y] = d
                dists[y][x] = d
            self.dists = dists

            perm = list(range(self.n)) #initialising perm to perm[i] = i 
            self.perm = perm     


    # Complete as described in the spec, to calculate the cost of the
    # current tour (as represented by self.perm).
    def tourValue(self):
        cost = 0
        for i in range(len(self.perm)-1):
            cost = cost + self.dists[self.perm[i]][self.perm[i + 1]]
        cost = cost + self.dists[self.perm[len(self.perm)-1]][self.perm[0]] #add cost of wraparound node
        return cost



    # Attempt the swap of cities i and i+1 in self.perm and commit
    # commit to the swap if it improves the cost of the tour.
    # Return True/False depending on success.
    def trySwap(self,i):
        oldCost = self.tourValue()
        oldPerm = self.perm[:]
        self.perm[i], self.perm[(i + 1) % self.n] = self.perm[(i + 1) % self.n], self.perm[i]
        newCost = self.tourValue()
        if newCost < oldCost:
            return True
        else:
            self.perm = oldPerm #if cost not lower revert back
            return False


    # Consider the effect of reversing the segment between
    # self.perm[i] and self.perm[j], and commit to the reversal
    # if it improves the tour value.
    # Return True/False depending on success.              
    def tryReverse(self,i,j):
        originalCost = self.tourValue()
        originalPerm = self.perm[:]
        self.perm[i:j + 1] = reversed(self.perm[i:j + 1])
        updatedCost = self.tourValue()
        
        if updatedCost < originalCost:
            return True
        else:
            self.perm = originalPerm #if cost not lower revert back
            return False 


    def swapHeuristic(self):
        better = True
        while better:
            better = False
            for i in range(self.n):
                if self.trySwap(i):
                    better = True

    def TwoOptHeuristic(self):
        better = True
        while better:
            better = False
            for j in range(self.n-1):
                for i in range(j):
                    if self.tryReverse(i,j):
                        better = True
                

    # Implement the Greedy heuristic which builds a tour starting
    # from node 0, taking the closest (unused) node as 'next'
    # each time.
    def Greedy(self):
        
        bestNodeIndex = 0
        nodesLeft = list(range(len(self.perm))) #list of nodes still not in tour
        nodesLeft.remove(0) # remove 0 since we start constructing tour at 0
        tour = [0] #permutation of greedy tour starting at 0
        
        while len(nodesLeft) != 0: # while not empty keep constructing tour
            lastNode = tour[-1] # gets last added node
            
            bestCost = 10000000000000000#set this to arbitrarily high value or max value from dists
            
            for i in nodesLeft:
                nextCost = self.dists[lastNode][i]
                if nextCost < bestCost: # < meaning if another node has same distance it wont be set as best cost, so if two nodes are equal we just use the first one found
                    bestCost = nextCost
            
                bestNodeIndex = self.dists[lastNode].index(bestCost) # finds index of closest node based on what bestCost was 
                while bestNodeIndex not in nodesLeft:
                    bestNodeIndex = self.dists[lastNode].index(bestCost, bestNodeIndex + 1) #makes sure that if duplicate values in list, it picks different index
            
            tour.append(bestNodeIndex)
            nodesLeft.remove(bestNodeIndex)
        
        self.perm = tour


    #Builds a tour, starting with one node, which is a subset of all nodes available
    #then iteratively inserts each following node such that the cost of inserting the selected point between the nodes is minimal
    #for simplicity starts building tour at 0
    def NearestInsertion(self):
        nodesLeft = list(range(len(self.perm))) #list of nodes still not in tour
        nodesLeft.remove(0) # remove 0 since we start constructing tour at 0
        tour = [0] #permutation of greedy tour starting at 0

        #first find the city closest to the first point, using part of the method from greedy
        bestCost = 10000000000000000 #set this to arbitrarily high value or max value from dists
        for point in nodesLeft:
                nextCost = self.dists[tour[0]][point]
                if nextCost < bestCost: # < meaning if another node has same distance it wont be set as best cost, so if two nodes are equal we just use the first one found
                    bestCost = nextCost
        
        bestNodeIndex = self.dists[tour[0]].index(bestCost) # finds index of closest node based on what bestCost was 
        while bestNodeIndex not in nodesLeft:
            bestNodeIndex = self.dists[tour[0]].index(bestCost, bestNodeIndex + 1) #makes sure that if duplicate values in list, it picks different index
        
        
        tour.append(bestNodeIndex)
        nodesLeft.remove(bestNodeIndex)

        #now given a partial tour, arbitrarily select a node k that is not in the tour
        #find the edge [i,j] in the partial tour which minimises cik + ckj - cij. Where cij is distance(cost) between node i and j
        #Insert k between i and j
        #repeat the above until all nodes are in the tour
        
        while len(nodesLeft) != 0: # while not empty keep constructing tour
            #find node k which is closest to any point in the subtour
            dist = 10000000000000000 #set this to arbitrarily high value or max value from dists
            for choices in nodesLeft:
                for node in tour:
                    nextDist = self.dists[choices][node]
                    if nextDist < dist:
                        dist = nextDist
                        k = choices
            
            #now find edge i,j in the subtour such that cos of inserting the node between the edge will be minimal

            minCost = 10000000000000000 #set this to arbitrarily high value or max value from dists
            
            idx = 0
            jdx = 0
            for i in tour:
                for j in tour:
                    nextCost = self.dists[i][k] + self.dists[k][j] - self.dists[i][j] # finds value which minimises cik + ckj - cij
                    if j - i == 1 or j - i == -1 or j - i == len(tour) -1 or i - j == len(tour) -1 and nextCost < minCost: #check to only look at values next to each other inc.wraparound
                        minCost = nextCost
                        idx = i #saves index of i,j that gave minimal cost, so that k can be inserted between these
                        jdx = j
            nodesLeft.remove(k)
            tour.insert(idx + 1, k) #insert after idx or before jdx
        
        self.perm = tour
