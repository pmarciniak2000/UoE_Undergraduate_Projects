import nltk, inspect, sys, hashlib
import itertools

from nltk.corpus import brown

# module for computing a Conditional Frequency Distribution
from nltk.probability import ConditionalFreqDist

# module for computing a Conditional Probability Distribution
from nltk.probability import ConditionalProbDist, LidstoneProbDist

from nltk.tag import map_tag

from adrive2 import trim_and_warn

assert map_tag('brown', 'universal', 'NR-TL') == 'NOUN', '''
Brown-to-Universal POS tag map is out of date.'''


class HMM:
    def __init__(self, train_data):
        """
        Initialise a new instance of the HMM.

        :param train_data: The training dataset, a list of sentences with tags
        :type train_data: list(list(tuple(str,str)))
        """
        self.train_data = train_data

        # Emission and transition probability distributions
        self.emission_PD = None
        self.transition_PD = None
        self.states = []
        self.viterbi = []
        self.backpointer = []

    # Q1

    # Compute emission model using ConditionalProbDist with a LidstoneProbDist estimator.
    #   To achieve the latter, pass a function
    #    as the probdist_factory argument to ConditionalProbDist.
    #   This function should take 3 arguments
    #    and return a LidstoneProbDist initialised with +0.001 as gamma and an extra bin.
    #   See the documentation/help for ConditionalProbDist to see what arguments the
    #    probdist_factory function is called with.
    def emission_model(self, train_data):
        """Compute an emission model based on labelled training data.
        Don't forget to lowercase the observation otherwise it mismatches the test data.

        :param train_data: The training dataset, a list of sentences with tags
        :type train_data: list(list(tuple(str,str)))
        :return: The emission probability distribution and a list of the states
        :rtype: Tuple[ConditionalProbDist, list(str)]
        """
        # prepare data

        # Don't forget to lowercase the observation otherwise it mismatches the test data
        # Do NOT add <s> or </s> to the input sentences
        data = [[(w[0].lower(), w[1]) for w in line] for line in train_data]
        data = [(tag,word) for sublist in data for (word,tag) in sublist] #flatten list

        # compute the emission model
        emission_FD = ConditionalFreqDist(data)
        lidstone_estimator = lambda fd: LidstoneProbDist(fd, gamma=+0.001, bins=fd.B() + 1)
        self.emission_PD = ConditionalProbDist(emission_FD, lidstone_estimator)
        # find unique states in order of appearance
        for x in data:
            if(x[0] not in self.states):
                self.states.append(x[0])

        return self.emission_PD, self.states

    # Q1

    # Access function for testing the emission model
    # For example model.elprob('VERB','is') might be -1.4
    def elprob(self, state, word):
        """
        The log of the estimated probability of emitting a word from a state

        :param state: the state name
        :type state: str
        :param word: the word
        :type word: str
        :return: log base 2 of the estimated emission probability
        :rtype: float
        """
        prob = self.emission_PD[state].logprob(word)
        return prob


    # Q2
    # Compute transition model using ConditionalProbDist with the same
    #  estimator as above (but without the extra bin)
    # See comments for emission_model above for details on the estimator.
    def transition_model(self, train_data):
        """
        Compute a transition model using a ConditionalProbDist based on
          labelled data.

        :param train_data: The training dataset, a list of sentences with tags
        :type train_data: list(list(tuple(str,str)))
        :return: The transition probability distribution
        :rtype: ConditionalProbDist
        """
        # prepare the data
    
        # The data object should be an array of tuples of conditions and observations,
        # in our case the tuples will be of the form (tag_(i),tag_(i+1)).
        # DON'T FORGET TO ADD THE START SYMBOL </s> and the END SYMBOL </s>
        start = ('<s>','<s>')
        end = ('</s>','</s>')
        updated = []
        for s in train_data:
            s.insert(0,start)
            s.append(end)
            updated.append(s)

        tags=(((s[i][1],s[i+1][1]) for i in range(len(s)-1)) for s in updated)
        data = itertools.chain.from_iterable(tags)

        # compute the transition model
        transition_FD = ConditionalFreqDist(data)
        lidstone_estimator = lambda fd: LidstoneProbDist(fd, gamma=+0.001, bins=fd.B())
        self.transition_PD = ConditionalProbDist(transition_FD, lidstone_estimator)

        return self.transition_PD

    # Q2
    # Access function for testing the transition model
    # For example model.tlprob('VERB','VERB') might be -2.4
    def tlprob(self, state1, state2):
        """
        The log of the estimated probability of a transition from one state to another

        :param state1: the first state name
        :type state1: str
        :param state2: the second state name
        :type state2: str
        :return: log base 2 of the estimated transition probability
        :rtype: float
        """
        prob = self.transition_PD[state1].logprob(state2)
        return prob

    # Train the HMM
    def train(self):
        """
        Trains the HMM from the training data
        """
        self.emission_model(self.train_data)
        self.transition_model(self.train_data)

    # Part B: Implementing the Viterbi algorithm.

    # Q3
    # Initialise data structures for tagging a new sentence.
    # Describe the data structures with comments.
    # Use the models stored in the variables: self.emission_PD and self.transition_PD
    # Input: first word in the sentence to tag and the total number of observations.
    def initialise(self, observation, number_of_observations):
        """
        Initialise data structures self.viterbi and self.backpointer for tagging a new sentence.

        :param observation: the first word in the sentence to tag
        :type observation: str
        :param number_of_observations: the number of observations
        :type number_of_observations: int
        """
        # self.viterbi represents an N by M matrix where N are the possible states(tags)
        # and M is the number of observations
        # each value in self.viterbi will be a +ve float representing the cost of reaching a certain point in a tag sequence

        # like self.viterbi, self.backpointer represents an N by M matrix
        # it holds the previous states allowing us to recover the combination 
        # that yielded the lowest cost state sequence

        # Initialise step 0 of viterbi, including
        #  transition from <s> to observation
        # use costs (- log-base-2 probabilities)

        self.viterbi = [[0 for i in range(number_of_observations)] for j in range(len(self.states))]
        self.backpointer = [[0 for i in range(number_of_observations)] for j in range(len(self.states))]

        for idx,tag in enumerate(self.states):
            cost = -self.transition_PD['<s>'].logprob(tag) + (-self.emission_PD[tag].logprob(observation))
            for i in range(number_of_observations):
                self.viterbi[idx][i] = cost
                # Initialise step 0 of backpointer
                self.backpointer[idx][i] = self.states[0]


    # Q3
    # Access function for testing the viterbi data structure
    # For example model.get_viterbi_value('VERB',2) might be 6.42
    def get_viterbi_value(self, state, step):
        """
        Return the current value from self.viterbi for
        the state (tag) at a given step

        :param state: A tag name
        :type state: str
        :param step: The (0-origin) number of a step:  if negative,
          counting backwards from the end, i.e. -1 means the last step
        :type step: int
        :return: The value (a cost) for state as of step
        :rtype: float
        """
        return self.viterbi[self.states.index(state)][step]

    # Q3
    # Access function for testing the backpointer data structure
    # For example model.get_backpointer_value('VERB',2) might be 'NOUN'
    def get_backpointer_value(self, state, step):
        """
        Return the current backpointer from self.backpointer for
        the state (tag) at a given step

        :param state: A tag name
        :type state: str
        :param step: The (0-origin) number of a step:  if negative,
          counting backwards from the end, i.e. -1 means the last step
        :type step: int
        :return: The state name to go back to at step-1
        :rtype: str
        """
        return self.backpointer[self.states.index(state)][step]

    # Q4a
    # Tag a new sentence using the trained model and already initialised data structures.
    # Use the models stored in the variables: self.emission_PD and self.transition_PD.
    # Update the self.viterbi and self.backpointer data structures.
    # Describe your implementation with comments.
    def tag(self, observations):
        """
        Tag a new sentence using the trained model and already initialised data structures.

        :param observations: List of words (a sentence) to be tagged
        :type observations: list(str)
        :return: List of tags corresponding to each word of the input
        """
        tags = []

        for t in range(1,len(observations)): # fixme to iterate over steps
            for idx,tag in enumerate(self.states): # fixme to iterate over states
                min_cost = float('inf')
                min_tag = None
                prev_word = self.viterbi[idx][t-1]

                # for every state find which state gives the minimum costs
                for other_idx,other_tag in enumerate(self.states): 
                    current_cost = self.viterbi[other_idx][t-1]
                    transition_cost = -self.transition_PD[other_tag].logprob(tag) + current_cost
                    if transition_cost < min_cost:
                        min_cost = transition_cost
                        min_tag = other_tag

                # the state with the lowest cost gets added to the backpointer
                # the cost for that state is added to the viterbi data structure
                cost = min_cost + (-self.emission_PD[tag].logprob(observations[t]))
                self.viterbi[idx][t] = cost
                self.backpointer[idx][t] = min_tag

        # Add a termination step with cost based solely on cost of transition to </s> , end of sentence.
        
        # Find which state to start the backpropagation from
        min_cost = float('inf')
        min_tag = None
        for idx,tag in enumerate(self.states):
            eof_cost = -self.transition_PD[tag].logprob('</s>') #only consider transition cost no emission for termination step
            seq_cost = eof_cost + self.viterbi[idx][len(observations)-1]
            if seq_cost < min_cost:
                min_cost = seq_cost
                min_tag = tag

        best_path_pointer = min_tag

        # Reconstruct the tag sequence using the backpointers.
        # Return the tag sequence corresponding to the best path as a list.
        # The order should match that of the words in the sentence.

        #Use backpointer to rebuild tag sequence starting from the state found in the termination step
        tags = [None] * len(observations)
        tags[-1] = best_path_pointer
        for i in reversed(range(1,len(observations))):
            tags[i-1] = self.backpointer[self.states.index(tags[i])][i]

        return tags

    def tag_sentence(self, sentence):
        """
        Initialise the HMM, lower case and tag a sentence. Returns a list of tags.
        :param sentence: the sentence
        :type sentence: list(str)
        :rtype: list(str)
        """
        sentence = [word.lower() for word in sentence] #lowercase the tokens
        self.initialise(sentence[0],len(sentence)) #initalise model with first word in sentence and the sentence length
        tags = self.tag(sentence)

        return tags



def answer_question4b():
    """
    Report a hand-chosen tagged sequence that is incorrect, correct it
    and discuss
    :rtype: list(tuple(str,str)), list(tuple(str,str)), str
    :return: incorrectly tagged sequence, correctly tagged sequence and your answer [max 280 chars]
    """

    # One sentence, i.e. a list of word/tag pairs, in two versions
    #  1) As tagged by your HMM
    #  2) With wrong tags corrected by hand
    tagged_sequence = [("I'm", 'X'), ('ruddy', 'X'), ('lazy', 'X'), (',', '.'), ('and', 'CONJ'), ("I'm", 'PRT'), ('getting', 'VERB'), ('on', 'ADP'), ('in', 'ADP'), ('years', 'NOUN'), ('.', '.')]
    correct_sequence = [("I'm", 'PRT'), ('ruddy', 'ADV'), ('lazy', 'ADJ'), (',', '.'), ('and', 'CONJ'), ("I'm", 'PRT'), ('getting', 'VERB'), ('on', 'PRT'), ('in', 'ADP'), ('years', 'NOUN'), ('.', '.')]
    # Why do you think the tagger tagged this example incorrectly?
    answer = inspect.cleandoc("""\
    The word ruddy confuses the model and causes it to tag all its surrounding words as X 
    because it has not seen the word before it cannot understand its meaning. This is further proved as in a later part of the sentence 'I'm' 
    is tagged correctly but next to ruddy it wasn't.""")

    return tagged_sequence, correct_sequence, trim_and_warn("Q4a", 280, answer)


# Q5a
def hard_em(labeled_data, unlabeled_data, k):
    """
    Run k iterations of hard EM on the labeled and unlabeled data.
    Follow the pseudo-code in the coursework instructions.

    :param labeled_data:
    :param unlabeled_data:
    :param k: number of iterations
    :type k: int
    :return: HMM model trained with hard EM.
    :rtype: HMM
    """

    #filter data to remove <s> and </s> so the model doesn't consider these as states
    filtered_labeled_data = []
    for sentence in labeled_data:
        sentence = list(filter(lambda x: x!=('<s>','<s>') and x!=('</s>','</s>'),sentence))
        filtered_labeled_data.append(sentence)

    #train T_0
    T_k = HMM(filtered_labeled_data)
    T_k.train()

    # Train the HMM.
    for i in range(0,k):
        P = []
        filtered_labeled_data = []
        
        #filter out <s> and </s> as they are added each iteration by the transition_model
        for sentence in labeled_data:
            sentence = list(filter(lambda x: x!=('<s>','<s>') and x!=('</s>','</s>'),sentence))
            filtered_labeled_data.append(sentence)

        # add the filtered labeled data to P
        for item in filtered_labeled_data:
            P.append(item)
        
        #tag unlabeled data with T_k
        for sentence in unlabeled_data:
            tags = T_k.tag_sentence(sentence)
            #add this newely tagged data to P
            P.append(list(zip(sentence, tags)))
        #Train model using updated P
        T_k = HMM(P)
        T_k.train()
    return T_k


def answer_question5b():
    """
    Sentence:  In    fact  he    seemed   delighted  to  get   rid  of  them   .
    Gold POS:  ADP   NOUN  PRON  VERB     VERB      PRT  VERB  ADJ  ADP  PRON  .
    T_0     :  PRON  VERB  NUM    ADP     ADJ       PRT  VERB  NUM  ADP  PRON  .
    T_k     :  PRON  VERB  PRON  VERB     ADJ       PRT  VERB  NUM  ADP  NOUN  .

    1) T_0 erroneously tagged "he" as "NUM" and T_k correctly identifies it as "PRON".
        Speculate why additional unlabeled data might have helped in that case.
        Refer to the training data (inspect the 20 sentences!).
    2) Where does T_k mislabel a word but T_0 is correct? Why do you think did hard EM hurt in that case?

    :rtype: str
    :return: your answer [max 500 chars]
    """
    return trim_and_warn("Q5b", 500, inspect.cleandoc("""1.The training data does not contain a single sentence with the word 'he'
    , so additional unlabeled data may have helped if it contained that word.
    2. T_k mislabels the word 'them' while T_0 doesn't, this is likely because each consecutive iteration
    of the EM algorithm trains the model on the original labelled data and the previous iterations tags, which 
    can may affect the results if one iteration tagged 'them' differently."""))



def answer_question6():
    """
    Suppose you have a hand-crafted grammar that has 100% coverage on
        constructions but less than 100% lexical coverage.
        How could you use a POS tagger to ensure that the grammar
        produces a parse for any well-formed sentence,
        even when it doesn't recognise the words within that sentence?

    :rtype: str
    :return: your answer [max 500 chars]
    """
    return trim_and_warn("Q6", 500, inspect.cleandoc("""
    We could predict the POS tag of an unknown word(s) using the context of the sentence or the surrounding words. 
    This could be done by looking at the surrounding words POS tags to try to work out their meaning and predict the most likely 
    tag for the unknown word based on those. We can also look at the prefix/sufix/substring of the unknown word to see if it 
    matches that of a known word and use that word as a guide on how to tag the word."""))


def answer_question7():
    """
    Why else, besides the speedup already mentioned above, do you think we
    converted the original Brown Corpus tagset to the Universal tagset?
    What do you predict would happen if we hadn't done that?  Why?

    :rtype: str
    :return: your answer [max 500 chars]
    """

    return trim_and_warn("Q7", 500, inspect.cleandoc("""\
    The Universal tagset allows our model to be more accurate, as with the orignal tagset the model 
    would have more tags to choose from and would have to make finer distinctions between words, leading the
    model to make more mistakes and hurting accuracy. The benefit of this would be that the model would have more
    descriptive power as it has more tags at its disposal but for the purposes of this coursework the Universal tagset,
    had enough tags to cover all the main parts of speech."""))


def compute_acc(hmm, test_data, print_mistakes):
    """
    Computes accuracy (0.0 - 1.0) of model on some data.
    :param hmm: the HMM
    :type hmm: HMM
    :param test_data: the data to compute accuracy on.
    :type test_data: list(list(tuple(str, str)))
    :param print_mistakes: whether to print the first 10 model mistakes
    :type print_mistakes: bool
    :return: float
    """
    # modify this to print the first 10 sentences with at least one mistake if print_mistakes = True
    correct = 0
    incorrect = 0

    incorrect_sentences_test = [] #sentences tagged in correctly by model with test data tags
    incorrect_sentences_hmm = [] #incorrect sentences tagges by hmm model
    for sentence in test_data:
        s = [word for (word, tag) in sentence]
        tags = hmm.tag_sentence(s)

        for ((word, gold), tag) in zip(sentence, tags):
            if tag == gold:
                correct += 1
            else:
                incorrect += 1
        
        #find the first 10 sentences with incorrect tags to 
        if len(incorrect_sentences_hmm) < 10 and print_mistakes and incorrect > 0:
            incorrect_sentences_test.append(sentence)
            incorrect_sentences_hmm.append(list(tuple(zip(s,tags))))     

    #print the first 10 sentences with at least one mistake if print_mistakes = True
    if print_mistakes:
        for i in range(len(incorrect_sentences_hmm)):
            print(incorrect_sentences_hmm[i])
            print(incorrect_sentences_test[i])

    return float(correct) / (correct + incorrect)


# Useful for testing
def isclose(a, b, rel_tol=1e-09, abs_tol=0.0):
    # http://stackoverflow.com/a/33024979
    return abs(a - b) <= max(rel_tol * max(abs(a), abs(b)), abs_tol)


def answers():
    global tagged_sentences_universal, test_data_universal, \
        train_data_universal, model, test_size, train_size, ttags, \
        correct, incorrect, accuracy, \
        good_tags, bad_tags, answer4b, answer5, answer6, answer7, answer5b, \
        t0_acc, tk_acc

    # Load the Brown corpus with the Universal tag set.
    tagged_sentences_universal = brown.tagged_sents(categories='news', tagset='universal')

    # Divide corpus into train and test data.
    test_size = 500
    train_size = len(tagged_sentences_universal) - test_size

    # tail test set
    test_data_universal = tagged_sentences_universal[-test_size:]  # [:test_size]
    train_data_universal = tagged_sentences_universal[:train_size]  # [test_size:]
    if hashlib.md5(''.join(map(lambda x: x[0],
                               train_data_universal[0] + train_data_universal[-1] + test_data_universal[0] +
                               test_data_universal[-1])).encode(
            'utf-8')).hexdigest() != '164179b8e679e96b2d7ff7d360b75735':
        print('!!!test/train split (%s/%s) incorrect -- this should not happen, please contact a TA !!!' % (
        len(train_data_universal), len(test_data_universal)), file=sys.stderr)

    # Create instance of HMM class and initialise the training set.
    model = HMM(train_data_universal)

    # Train the HMM.
    model.train()

    # Some preliminary sanity checks
    # Use these as a model for other checks
    e_sample = model.elprob('VERB', 'is')
    if not (type(e_sample) == float and e_sample <= 0.0):
        print('elprob value (%s) must be a log probability' % e_sample, file=sys.stderr)

    t_sample = model.tlprob('VERB', 'VERB')
    if not (type(t_sample) == float and t_sample <= 0.0):
        print('tlprob value (%s) must be a log probability' % t_sample, file=sys.stderr)

    if not (type(model.states) == list and \
            len(model.states) > 0 and \
            type(model.states[0]) == str):
        print('model.states value (%s) must be a non-empty list of strings' % model.states, file=sys.stderr)

    print('states: %s\n' % model.states)

    ######
    # Try the model, and test its accuracy [won't do anything useful
    #  until you've filled in the tag method
    ######
    s = 'the cat in the hat came back'.split()
    ttags = model.tag_sentence(s)
    print("Tagged a trial sentence:\n  %s" % list(zip(s, ttags)))

    v_sample = model.get_viterbi_value('VERB', 5)
    if not (type(v_sample) == float and 0.0 <= v_sample):
        print('viterbi value (%s) must be a cost' % v_sample, file=sys.stderr)

    b_sample = model.get_backpointer_value('VERB', 5)
    if not (type(b_sample) == str and b_sample in model.states):
        print('backpointer value (%s) must be a state name' % b_sample, file=sys.stderr)

    # check the model's accuracy (% correct) using the test set
    accuracy = compute_acc(model, test_data_universal, print_mistakes=True)
    print('\nTagging accuracy for test set of %s sentences: %.4f' % (test_size, accuracy))

    #Tag the sentence again to put the results in memory for automarker.
    model.tag_sentence(s)

    # Question 5a
    # Set aside the first 20 sentences of the training set
    num_sentences = 20
    semi_supervised_labeled = train_data_universal[:num_sentences]  # type list(list(tuple(str, str)))
    semi_supervised_unlabeled = [[word for (word, tag) in sent] for sent in train_data_universal[num_sentences:]]  # type list(list(str))
    print("Running hard EM for Q5a. This may take a while...")
    t0 = hard_em(semi_supervised_labeled, semi_supervised_unlabeled, 0) # 0 iterations
    tk = hard_em(semi_supervised_labeled, semi_supervised_unlabeled, 3)
    print("done.")

    t0_acc = compute_acc(t0, test_data_universal, print_mistakes=False)
    tk_acc = compute_acc(tk, test_data_universal, print_mistakes=False)
    print('\nTagging accuracy of T_0: %.4f' % (t0_acc))
    print('\nTagging accuracy of T_k: %.4f' % (tk_acc))
    ########

    # Print answers for 4b, 5b, 6 and 7.
    bad_tags, good_tags, answer4b = answer_question4b()
    print('\nA tagged-by-your-model version of a sentence:')
    print(bad_tags)
    print('The tagged version of this sentence from the corpus:')
    print(good_tags)
    print('\nDiscussion of the difference:')
    print(answer4b)
    answer5b = answer_question5b()
    print("\nFor Q5b:")
    print(answer5b)
    answer6 = answer_question6()
    print('\nFor Q6:')
    print(answer6)
    answer7 = answer_question7()
    print('\nFor Q7:')
    print(answer7)


if __name__ == '__main__':
    if len(sys.argv) > 1 and sys.argv[1] == '--answers':
        import adrive2
        from autodrive_embed import run, carefulBind

        with open("userErrs.txt", "w") as errlog:
            run(globals(), answers, adrive2.a2answers, errlog)
    else:
        answers()
