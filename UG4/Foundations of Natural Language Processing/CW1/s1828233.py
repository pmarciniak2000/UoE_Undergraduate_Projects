"""
Foundations of Natural Language Processing

Assignment 1

Please complete functions, based on their doc_string description
and instructions of the assignment. 

To test your code run:

```
[hostname]s1234567 python3 s1234567.py
```

Before submission executed your code with ``--answers`` flag
```
[hostname]s1234567 python3 s1234567.py --answers
```
include generated answers.py file.

Best of Luck!
"""
from collections import defaultdict, Counter

import numpy as np  # for np.mean() and np.std()
import nltk, sys, inspect
import nltk.corpus.util
from nltk import MaxentClassifier
from nltk.corpus import brown, ppattach  # import corpora

#Imports for Q9
from nltk.stem import WordNetLemmatizer

# Import the Twitter corpus and LgramModel
from nltk_model import *  # See the README inside the nltk_model folder for more information

# Import the Twitter corpus and LgramModel
from twitter.twitter import *

twitter_file_ids = "20100128.txt"
assert twitter_file_ids in xtwc.fileids()


# Some helper functions

def ppEandT(eAndTs):
    '''
    Pretty print a list of entropy-tweet pairs

    :type eAndTs: list(tuple(float,list(str)))
    :param eAndTs: entropies and tweets
    :return: None
    '''

    for entropy, tweet in eAndTs:
        print("{:.3f} [{}]".format(entropy, ", ".join(tweet)))


def compute_accuracy(classifier, data):
    """
    Computes accuracy (range 0 - 1) of a classifier.
    :type classifier: NltkClassifierWrapper or NaiveBayes
    :param classifier: the classifier whose accuracy we compute.
    :type data: list(tuple(list(any), str))
    :param data: A list with tuples of the form (list with features, label)
    :rtype float
    :return accuracy (range 0 - 1).
    """
    correct = 0
    for d, gold in data:
        predicted = classifier.classify(d)
        correct += predicted == gold
    return correct/len(data)


def apply_extractor(extractor_f, data):
    """
    Helper function:
    Apply a feature extraction method to a labeled dataset.
    :type extractor_f: (str, str, str, str) -> list(any)
    :param extractor_f: the feature extractor, that takes as input V, N1, P, N2 (all strings) and returns a list of features
    :type data: list(tuple(str))
    :param data: a list with tuples of the form (id, V, N1, P, N2, label)

    :rtype list(tuple(list(any), str))
    :return a list with tuples of the form (list with features, label)
    """
    r = []
    for d in data:
        r.append((extractor_f(*d[1:-1]), d[-1]))
    return r


class NltkClassifierWrapper:
    """
    This is a little wrapper around the nltk classifiers so that we can interact with them
    in the same way as the Naive Bayes classifier.
    """
    def __init__(self, classifier_class, train_features, **kwargs):
        """

        :type classifier_class: a class object of nltk.classify.api.ClassifierI
        :param classifier_class: the kind of classifier we want to create an instance of.
        :type train_features: list(tuple(list(any), str))
        :param train_features: A list with tuples of the form (list with features, label)
        :param kwargs: additional keyword arguments for the classifier, e.g. number of training iterations.
        :return None
        """
        self.classifier_obj = classifier_class.train(
            [(NltkClassifierWrapper.list_to_freq_dict(d), c) for d, c in train_features], **kwargs)

    @staticmethod
    def list_to_freq_dict(d):
        """
        :param d: list(any)
        :param d: list of features
        :rtype dict(any, int)
        :return: dictionary with feature counts.
        """
        return Counter(d)

    def classify(self, d):
        """
        :param d: list(any)
        :param d: list of features
        :rtype str
        :return: most likely class
        """
        return self.classifier_obj.classify(NltkClassifierWrapper.list_to_freq_dict(d))

    def show_most_informative_features(self, n = 10):
        self.classifier_obj.show_most_informative_features(n)

# End helper functions

# ==============================================
# Section I: Language Identification [60 marks]
# ==============================================

# Question 1 [7 marks]
def train_LM(corpus):
    '''
    Build a bigram letter language model using LgramModel
    based on the all-alpha subset the entire corpus

    :type corpus: nltk.corpus.CorpusReader
    :param corpus: An NLTK corpus
    :rtype: LgramModel
    :return: A padded letter bigram model based on nltk.model.NgramModel
    '''

    # subset the corpus to only include all-alpha tokens,
    # converted to lower-case (_after_ the all-alpha check)
    corpus_tokens = [word for word in corpus.words() if word.isalpha()] #remove non-alpha tokens
    corpus_tokens = [x.lower() for x in corpus_tokens] #convert remaining tokens to lower case

    # Return a smoothed (using the default estimator) padded bigram
    # letter language model
    
    # build bigram model
    model = LgramModel(2,corpus_tokens,pad_left=True, pad_right=True)

    return model


# Question 2 [7 marks]
def tweet_ent(file_name, bigram_model):
    '''
    Using a character bigram model, compute sentence entropies
    for a subset of the tweet corpus, removing all non-alpha tokens and
    tweets with less than 5 all-alpha tokens, then converted to lowercase

    :type file_name: str
    :param file_name: twitter file to process
    :rtype: list(tuple(float,list(str)))
    :return: ordered list of average entropies and tweets'''

    # Clean up the tweet corpus to remove all non-alpha
    # tokens and tweets with less than 5 (remaining) tokens, converted
    # to lowercase
    list_of_tweets = xtwc.sents(file_name)
    cleaned_list_of_tweets = []
    for i in range(len(list_of_tweets)):
        lst = [word for word in list_of_tweets.__getitem__(i) if word.isalpha()] #remove non alpha tokens
        low = []
        if len(lst) > 4: #remove tweets with less than 5 tokens
            for i in range(len(lst)):
                low.append(lst[i].lower())#convert tokens to lowercase
            cleaned_list_of_tweets.append(low)


    #  Construct a list of tuples of the form: (entropy,tweet)
    #  for each tweet in the cleaned corpus, where entropy is the
    #  average word for the tweet, and return the list of
    #  (entropy,tweet) tuples sorted by entropy
    
    entropies = []
    #loop through each tweet in the cleaned list
    for tweet in cleaned_list_of_tweets:
        sum = 0
        for word in tweet:
            #calc entropy here at word-level
            entropy = bigram_model.entropy(word,pad_left=True, pad_right=True,perItem=True)
            sum += entropy
            avg = sum/len(tweet) #find avg entropy per tweet
        entropies.append(((avg),tweet))

    entropies.sort() #sort the entropies in ascending order by entropy value

    return entropies


# Question 3 [8 marks]
def open_question_3():
    '''
    Question: What differentiates the beginning and end of the list
    of tweets and their entropies?

    :rtype: str
    :return: your answer [500 chars max]
    '''
    return inspect.cleandoc("""The tweets at the beginning mostly contain tokens which represent single words or letters in the English language that are short, correctly spelled and often represent common words in English, like stop-words.  They have an entropy of around ~2.5.

    The tweets at the end mostly seem to contain tokens containing Japanese characters which represent multiple words as opposed to single words at the start of the list. They have a higher entropy of ~17.5 compared to the entropies at the beginning.
    """)[0:500]


# Question 4 [8 marks]
def open_question_4() -> str:
    '''
    Problem: noise in Twitter data

    :rtype: str
    :return: your answer [500 chars max]
    '''
    return inspect.cleandoc("""The data still contains non-english tokens( 笑,não) and the presence of stop-words(a, in, t). To remove non-English tokens we could compare them against a corpus of English words to filter them out. Using a similar method with a bank of stop-words we could also filter out the stop-words which don’t add much value to the meaning of a tweet.

    Another problem is not all words are spelt correctly (compatble,loooooooooooooook). To fix words not spelt correctly we could use a spell checker library.
    """)[0:500]


# Question 5 [15 marks]
def tweet_filter(list_of_tweets_and_entropies):
    '''
    Compute entropy mean, standard deviation and using them,
    likely non-English tweets in the all-ascii subset of list 
    of tweets and their letter bigram entropies

    :type list_of_tweets_and_entropies: list(tuple(float,list(str)))
    :param list_of_tweets_and_entropies: tweets and their
                                    english (brown) average letter bigram entropy
    :rtype: tuple(float, float, list(tuple(float,list(str)), list(tuple(float,list(str)))
    :return: mean, standard deviation, ascii tweets and entropies,
             non-English tweets and entropies
    '''

    #find how many tweets are in the lowest 90% entropy
    low_ent_len = round(len(list_of_tweets_and_entropies)*0.9-0.5)#-0.5 to ensure rounding down

    # Find the "ascii" tweets - those in the lowest-entropy 90%
    #  of list_of_tweets_and_entropies
    list_of_ascii_tweets_and_entropies = list_of_tweets_and_entropies[:low_ent_len]

    # Extract a list of just the entropy values
    list_of_entropies = list(map(lambda x: x[0], list_of_ascii_tweets_and_entropies))

    # Compute the mean of entropy values for "ascii" tweets
    mean = np.mean(list_of_entropies)

    # Compute their standard deviation
    standard_deviation = np.std(list_of_entropies)

    # Get a list of "probably not English" tweets, that is
    #  "ascii" tweets with an entropy greater than (mean + std_dev))
    threshold = mean + standard_deviation
    list_of_not_English_tweets_and_entropies = list(filter(lambda x: x[0] > threshold, list_of_ascii_tweets_and_entropies))

    # Return mean, standard_deviation,
    #  list_of_ascii_tweets_and_entropies,
    #  list_of_not_English_tweets_and_entropies
    return mean, standard_deviation, list_of_ascii_tweets_and_entropies, list_of_not_English_tweets_and_entropies


# Question 6 [15 marks]
def open_question_6():
    """
    Suppose you are asked to find out what the average per word entropy of English is.
    - Name 3 problems with this question, and make a simplifying assumption for each of them.
    - What kind of experiment would you perform to estimate the entropy after you have these simplifying assumptions?
       Justify the main design decisions you make in your experiment.
    :rtype: str
    :return: your answer [1000 chars max]
    """
    return inspect.cleandoc("""Not every word in the English language is equally representative so we would first have to find a representative sample of English text. Assume we have an ideal corpus as required.

    Many words in English are actually borrowed foreign words, e.g.'renaissance' from French, which often have a word structure different to English words and will affect the results. Assume we will not include such words.
    
    The corpus may still be noisy so assume everything is lower-case and non-alpha characters are removed.

    To find the entropy we could use the individual word probabilities:
    Count frequency of each word in the corpus
    Find the word probability of each word by dividing the frequency of each word by the no. of words in the corpus.
    Using the formula: entropy=-sum(p(x)log2p(x))
    For every word, multiply the probability found in Step 2 by the base2 log of that probability.
    Sum the result for each word and make the value -ve which will give an estimate for the average word entropy for English.
    """)[:1000]


#############################################
# SECTION II - RESOLVING PP ATTACHMENT AMBIGUITY
#############################################

# Question 7 [15 marks]
class NaiveBayes:
    """
    Naive Bayes model with Lidstone smoothing (parameter alpha).
    """

    def __init__(self, data, alpha):
        """
        :type data: list(tuple(list(any), str))
        :param data: A list with tuples of the form (list with features, label)
        :type alpha: float
        :param alpha: \alpha value for Lidstone smoothing
        """
        self.vocab = self.get_vocab(data)
        self.alpha = alpha
        self.prior, self.likelihood = self.train(data, alpha, self.vocab)

    @staticmethod
    def get_vocab(data):
        """
        Compute the set of all possible features from the (training) data.
        :type data: list(tuple(list(any), str))
        :param data: A list with tuples of the form (list with features, label)
        :rtype: set(any)
        :return: The set of all features used in the training data for all classes.
        """
        ftrs = [x[0] for x in data] #extract all lists of features from data                             
        uniq_ftrs = {item for sublist in ftrs for item in sublist} # flatten list and use set to remove duplicates

        return uniq_ftrs

    @staticmethod
    def train(data, alpha, vocab):
        """
        Estimates the prior and likelihood from the data with Lidstone smoothing.

        :type data: list(tuple(list(any), str))
        :param data: A list of tuples ([f1, f2, ... ], c) with the first element
                     being a list of features and the second element being its class.

        :type alpha: float
        :param alpha: \alpha value for Lidstone smoothing

        :type vocab: set(any)
        :param vocab: The set of all features used in the training data for all classes.


        :rtype: tuple(dict(str, float), dict(str, dict(any, float)))
        :return: Two dictionaries: the prior and the likelihood (in that order).
        We expect the returned values to relate as follows to the probabilities:
            prior[c] = P(c)
            likelihood[c][f] = P(f|c)
        """

        #use set comprehension to find all unique classes
        classes = {x[1] for x in data}

        #extract class labels from all of data
        all_classes = [x[1] for x in data]

        vocab_length = len(vocab)
        data_length = len(data)
        dict_likelihood = {}
        dict_prior = {}

        #find p(f|c) and p(c)
        #for p(f|c) go through each class and then each item to populate dictionary with likelihoods
        for c in classes:
            ftrs_per_class = all_classes.count(c)
            dict_prior.update({c:ftrs_per_class/data_length}) #find p(c) by counting occurences of each class and divinding by length of data
            subdict = {}
            for item in vocab:
                count = 0
                #count = len([x for x in data if item in x[0] and x[1] == c])
                for ftr in data:
                    if item in ftr[0] and ftr[1] == c: #increment occurrence each time that item in that class appears in data
                        count+=1
                #likelihood w/ smoothing=(frequency+alpha)/((no.of ftrs)+(no.unique items * alpha))        
                likelihood = (count + alpha)/((ftrs_per_class*len(ftr[0])) + (vocab_length * alpha))
                subdict.update({item:likelihood})
            dict_likelihood.update({c:subdict})
        
        assert alpha >= 0.0
        return (dict_prior, dict_likelihood)


    def prob_classify(self, d):
        """
        Compute the probability P(c|d) for all classes.
        :type d: list(any)
        :param d: A list of features.
        :rtype: dict(str, float)
        :return: The probability p(c|d) for all classes as a dictionary.
        """

        #find pd
        #for each ftr in the vocab find sum(p(d) = p(d|c)*p(c)) for all c's
        #store values in dictionary so can be used later
        dict_pd = {}
        for ftr in d:
            pd=0
            if ftr in self.vocab: #only consider features in vocab
                for c in list(self.prior.keys()):
                    pd += self.likelihood.get(c, {}).get(ftr) * self.prior.get(c)
                dict_pd.update({ftr:pd})

        #want p(c|d) = (p(d|c)*p(c))/p(d)
        #use bayes rule to find posterior prob
        #for each class find the posterior prob using the likelihood,priors and p(d) dict
        dict = {}
        for c in list(self.prior.keys()):#go through all classes
            pd = 1
            pdc = 1
            for ftr in d:
                if ftr in self.vocab: #only consider features in vocab
                    pd *= dict_pd.get(ftr)
                    pdc *= self.likelihood.get(c, {}).get(ftr)
            prob = self.prior.get(c) * pdc / pd
            dict.update({c:prob})

        return dict

    def classify(self, d):
        """
        Compute the most likely class of the given "document" with ties broken arbitrarily.
        :type d: list(any)
        :param d: A list of features.
        :rtype: str
        :return: The most likely class.
        """
        #call prob_classify to get dictionary with probabilities of document belonging to each class
        class_prob = self.prob_classify(d)
        v = list(class_prob.values()) #get a list of all values
        k = list(class_prob.keys()) #get a list of all keys

        return k[v.index(max(v))] #return the key at the index of the max value, i.e. the most probable class



# Question 8 [10 marks]
def open_question_8() -> str:
    """
    How do you interpret the differences in accuracy between the different ways to extract features?
    :rtype: str
    :return: Your answer of 500 characters maximum.
    """
    return inspect.cleandoc("""Single features are not very informative. On its own P gives a higher acc than N2 as it has a larger impact on where the PP attaches. The last option is the best as it gives the model more features meaning more discriminative power.

        The LR model has a higher acc compared to the NB model. This is due to the assumption of conditional independence for every feature in the NB model. In our problem the features are not always independent,e.g. the combination of v,p may affect whom the PP attaches to.""")[:500]


# Feature extractors used in the table:
# see your_feature_extractor for documentation on arguments and types.
def feature_extractor_1(v, n1, p, n2):
    return [v]


def feature_extractor_2(v, n1, p, n2):
    return [n1]


def feature_extractor_3(v, n1, p, n2):
    return [p]


def feature_extractor_4(v, n1, p, n2):
    return [n2]


def feature_extractor_5(v, n1, p, n2):
    return [("v", v), ("n1", n1), ("p", p), ("n2", n2)]


# Question 9.1 [5 marks]
def your_feature_extractor(v, n1, p, n2):
    """
    Takes the head words and produces a list of features. The features may
    be of any type as long as they are hashable.
    :type v: str
    :param v: The verb.
    :type n1: str
    :param n1: Head of the object NP.
    :type p: str
    :param p: The preposition.
    :type n2: str
    :param n2: Head of the NP embedded in the PP.
    :rtype: list(any)
    :return: A list of features produced by you.
    """

    paras = [v,n1,p,n2]
    ftrs = []

    #convert head words to lower case
    v = v.lower()
    n1 = n1.lower()
    n2 = n2.lower()
    p = p.lower()

    #add the lemmas of the nouns and verb
    lemmatizer = WordNetLemmatizer()
    ftrs.append(("n1lemma",lemmatizer.lemmatize(n1)))
    ftrs.append(("n2lemma",lemmatizer.lemmatize(n2)))
    ftrs.append(("vlemma",lemmatizer.lemmatize(v)))
    ftrs.append(("plemma",lemmatizer.lemmatize(p)))

    # add POS tag for nouns and verbs
    ftrs.append(("n1pos",nltk.tag.pos_tag([n1])[0][1]))
    ftrs.append(("n2pos",nltk.tag.pos_tag([n2])[0][1]))
    ftrs.append(("vpos",nltk.tag.pos_tag([v])[0][1]))

    #add word tuples
    ftrs.append(("combovp",(p,v))) 
    ftrs.append(("combopn1",(p,n1))) 
    ftrs.append(("combon1n2",(n2,n1))) 
    ftrs.append(("combopn1",(p,n2))) 
    ftrs.append(("combovn2",(v,n2)))
        
    return ftrs


# Question 9.2 [10 marks]
def open_question_9():
    """
    Briefly describe your feature templates and your reasoning for them.
    Pick 3 examples of informative features and discuss why they make sense or why they do not make sense
    and why you think the model relies on them.
    :rtype: str
    :return: Your answer of 1000 characters maximum.
    """
    return inspect.cleandoc("""The feature templates include:
    Lemmatised forms of v,n1,n2,p to reduce variety in words making the head words more representative
    POS tags for v,n1,n2 as they help identify the part-of-speech those words belong to
    Tuple combinations of head-words to help show which combinations of words occur frequently together

    Note: all head words were converted to lower case to maintain consistency between features
    
    -(plemma,of): This makes sense as the most informative feature because ‘of’ is a very common preposition, and prepositions have a lot of discriminative power as they can be used by the model to look for a pattern of them attaching to either a verb or noun.
    - (n1n2,(1,7)): This makes sense as an informative feature, as it shows that if both n1,n2 are numeric values, they are likely to be attached to each other in meaning.
    - (n1pos,PRP): This makes sense as PRP means possessive pronoun which provides a lot of information on what an object belongs to, making it important to the model.
    """)[:1000]


"""
Format the output of your submission for both development and automarking. 
!!!!! DO NOT MODIFY THIS PART !!!!!
"""

def answers():
    # Global variables for answers that will be used by automarker
    global ents, lm
    global best10_ents, worst10_ents, mean, std, best10_ascci_ents, worst10_ascci_ents
    global best10_non_eng_ents, worst10_non_eng_ents
    global answer_open_question_4, answer_open_question_3, answer_open_question_6,\
        answer_open_question_8, answer_open_question_9
    global ascci_ents, non_eng_ents

    global naive_bayes
    global acc_extractor_1, naive_bayes_acc, lr_acc, logistic_regression_model, dev_features

    print("*** Part I***\n")

    print("*** Question 1 ***")
    print('Building brown bigram letter model ... ')
    lm = train_LM(brown)
    print('Letter model built')

    print("*** Question 2 ***")
    ents = tweet_ent(twitter_file_ids, lm)
    print("Best 10 english entropies:")
    best10_ents = ents[:10]
    ppEandT(best10_ents)
    print("Worst 10 english entropies:")
    worst10_ents = ents[-10:]
    ppEandT(worst10_ents)

    print("*** Question 3 ***")
    answer_open_question_3 = open_question_3()
    print(answer_open_question_3)

    print("*** Question 4 ***")
    answer_open_question_4 = open_question_4()
    print(answer_open_question_4)

    print("*** Question 5 ***")
    mean, std, ascci_ents, non_eng_ents = tweet_filter(ents)
    print('Mean: {}'.format(mean))
    print('Standard Deviation: {}'.format(std))
    print('ASCII tweets ')
    print("Best 10 English entropies:")
    best10_ascci_ents = ascci_ents[:10]
    ppEandT(best10_ascci_ents)
    print("Worst 10 English entropies:")
    worst10_ascci_ents = ascci_ents[-10:]
    ppEandT(worst10_ascci_ents)
    print('--------')
    print('Tweets considered non-English')
    print("Best 10 English entropies:")
    best10_non_eng_ents = non_eng_ents[:10]
    ppEandT(best10_non_eng_ents)
    print("Worst 10 English entropies:")
    worst10_non_eng_ents = non_eng_ents[-10:]
    ppEandT(worst10_non_eng_ents)

    print("*** Question 6 ***")
    answer_open_question_6 = open_question_6()
    print(answer_open_question_6)


    print("*** Part II***\n")

    print("*** Question 7 ***")
    naive_bayes = NaiveBayes(apply_extractor(feature_extractor_5, ppattach.tuples("training")), 0.1)
    naive_bayes_acc = compute_accuracy(naive_bayes, apply_extractor(feature_extractor_5, ppattach.tuples("devset")))
    print(f"Accuracy on the devset: {naive_bayes_acc * 100}%")

    print("*** Question 8 ***")
    answer_open_question_8 = open_question_8()
    print(answer_open_question_8)

    # This is the code that generated the results in the table of the CW:

    # A single iteration of suffices for logistic regression for the simple feature extractors.
    #
    # extractors_and_iterations = [feature_extractor_1, feature_extractor_2, feature_extractor_3, eature_extractor_4, feature_extractor_5]
    #
    # print("Extractor    |  Accuracy")
    # print("------------------------")
    #
    # for i, ex_f in enumerate(extractors, start=1):
    #     training_features = apply_extractor(ex_f, ppattach.tuples("training"))
    #     dev_features = apply_extractor(ex_f, ppattach.tuples("devset"))
    #
    #     a_logistic_regression_model = NltkClassifierWrapper(MaxentClassifier, training_features, max_iter=6, trace=0)
    #     lr_acc = compute_accuracy(a_logistic_regression_model, dev_features)
    #     print(f"Extractor {i}  |  {lr_acc*100}")


    print("*** Question 9 ***")
    training_features = apply_extractor(your_feature_extractor, ppattach.tuples("training"))
    dev_features = apply_extractor(your_feature_extractor, ppattach.tuples("devset"))
    logistic_regression_model = NltkClassifierWrapper(MaxentClassifier, training_features, max_iter=10)
    lr_acc = compute_accuracy(logistic_regression_model, dev_features)

    print("30 features with highest absolute weights")
    logistic_regression_model.show_most_informative_features(30)

    print(f"Accuracy on the devset: {lr_acc*100}")

    answer_open_question_9 = open_question_9()
    print("Answer to open question:")
    print(answer_open_question_9)




if __name__ == "__main__":
    if len(sys.argv) > 1 and sys.argv[1] == '--answers':
        from autodrive_embed import run, carefulBind
        import adrive1

        with open("userErrs.txt", "w") as errlog:
            run(globals(), answers, adrive1.extract_answers, errlog)
    else:
        answers()
