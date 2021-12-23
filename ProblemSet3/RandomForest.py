from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
import pandas as pd
import numpy as np
import warnings
import sys


def convertCategorical(column):
    warnings.filterwarnings("ignore")
    map_convert = get_mapping(column, False)
    keys.append(map_convert)
    for i in range(len(data[column])):
        data[column][i] = convert(data[column][i], map_convert)


def convertContinuous(column):
    warnings.filterwarnings("ignore")
    map_convert = get_mapping(column, True, 2)
    keys.append(map_convert)
    for i in range(len(data[column])):
        data[column][i] = convert(data[column][i], map_convert)


def convert(value, map):
    return map[value]


def get_min_max(column):
    _min = data[column][0]
    _max = data[column][0]

    for value in data[column]:
        if value < _min:
            _min = value

        if value > _max:
            _max = value

    return _min, _max


def get_mapping(column, continuous, num_Categories=4):
    map_convert = {}
    unique_values = data[column].tolist()
    unique_values = set(unique_values)
    unique_values = list(unique_values)
    count = 0

    if not continuous:
        for value in unique_values:
            map_convert[value] = count
            count += 1

        return map_convert
    else:
        _min, _max = get_min_max(column)
        rangesInner = {"none": "none"}
        del rangesInner["none"]

        place = _min
        iterator = (_max - _min) / num_Categories
        for i in range(1, num_Categories + 1):
            rangesInner[i] = [place, (_min + (iterator * i))]
            place = (_min + (iterator * i))

        for value in unique_values:
            for i in rangesInner:
                rng = rangesInner[i]
                if (value >= rng[0] and value <= rng[1]):
                    map_convert[value] = i

        ranges[column] = rangesInner
        return map_convert


def get_classify_mapping(colData, columnIndex, isY=False):
    rangesInner = None
    if isY:
        rangesInner = ranges[YName]
    else:
        rangesInner = ranges[data.columns[columnIndex]]

    for i in rangesInner:
        rng = rangesInner[i]
        if (colData >= rng[0] and colData <= rng[1]):
            if isY:
                print("FOUND")
            return i


def isContinuous(column, limit):
    unique_values = column.tolist()
    unique_values = set(unique_values)
    unique_values = list(unique_values)

    if len(unique_values) > limit:
        return True
    else:
        return False


def input(list):
    for i in range(len(list[0])):
        list[0][i] = keys[i][list[0][i]]
    list[0].pop(Y)
    return list


def output(response):
    for key in keys[Y]:
        if (keys[Y][key] == response):
            return key


global keys
keys = list()
global data
global rootData
global ranges
global Y
global YName

ranges = {"none": "none"}
del ranges["none"]

data = pd.read_csv(sys.argv[1])
rootData = pd.read_csv(sys.argv[1])
index = 0
for i in data.columns:
    if i == sys.argv[2]:
        Y = index
        YName = i
    index += 1

for column in data.columns:
    columnData = data[column]
    if (columnData.dtype is np.dtype(np.float64) or isContinuous(columnData, 20)):
        convertContinuous(column)
    else:
        convertCategorical(column)

X = data.drop(sys.argv[2], axis=1)
y = data.pop(sys.argv[2])

rootX = rootData.drop(sys.argv[2], axis=1)
rootY = rootData.pop(sys.argv[2])

##this for aggregate accuracy of 100 runs
if sys.argv[3] == "average":

    sum = 0
    for k in range(100):
        X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2)
        X_train = np.array(X_train).astype(float)
        y_train = np.array(y_train).astype(float)
        X_test = np.array(X_test).astype(float)
        y_test = np.array(y_test).astype(float)
        ##defaults:       True           100             None          "gini"                0                  "auto"
        clf = RandomForestClassifier(bootstrap=True, n_estimators=100, max_depth=4, criterion="gini",
                                     min_impurity_decrease=.005, max_features=4)

        clf.fit(X_train, y_train)

        predict = clf.predict(X_test)
        correct = 0
        for i in range(len(predict)):
            if predict[i] == y_test[i]:
                correct += 1

        ##print(correct/len(X_test))
        sum += correct / len(X_test)
    print("average of 100 runs given hyper-parameters: ", sum / 100)
elif sys.argv[3] == "classify":

    ##this is just for classifying individual inputs

    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2)
    X_train = np.array(X_train).astype(float)
    y_train = np.array(y_train).astype(float)
    X_test = np.array(X_test).astype(float)
    y_test = np.array(y_test).astype(float)
    ##defaults:       True           100             None          "gini"                0                  "auto"
    clf = RandomForestClassifier(bootstrap=True, n_estimators=100, max_depth=4, criterion="gini",
                                 min_impurity_decrease=.005, max_features=4)

    clf.fit(X_train, y_train)

    ##input converts the values into their encoded keys on which the model was trained, clf.predict is the model prediction method that utilizes the now
    ##numerical encoded values we pass to it from input() and output() once again converts that numerical back a string for ease of use to see classification
    ##String[] -> input() -> Integer[] -> clf.predict() -> Integer -> output() -> String..... unless of course the input was already an int like the titanic
    ## dataset, regardless just put the input in like this [ [ input ] ] into the input() method

    parsed = []
    arg = sys.argv[4].split(',')
    count = 0
    featureData = []
    for feature in arg:
        if isContinuous(rootData[rootData.columns[count]], 20):
            try:
                int(feature)
                featureData.append(int(feature))
            except ValueError:
                featureData.append(get_classify_mapping(float(feature), count))
        else:
            keyCount = count
            if (count == Y):
                keyCount += 1

            try:
                int(feature)
                featureData.append(keys[keyCount][int(feature)])
            except ValueError:
                featureData.append(keys[keyCount][feature])

        count += 1
    parsed.append(featureData)

    if (y.dtype is np.dtype(np.float64) or isContinuous(rootY, 20)):
        prediction = int(clf.predict(parsed))
        rng = ranges[YName][prediction]
        print("RESULT: " + str(prediction))
        print("RANGE: " + str(rng[0]) + " => " + str(rng[1]))
    else:
        print("RESULT: " + str(output(clf.predict(parsed))))

else:
    print("sorry your command argument is incorrect... try 'classify' or 'average'")