
import numpy as np
import pandas as pd
eps = 0.000000001
from numpy import log2 as log

global data
global columns
global max_depth
global min_impurity

def filter(array, col, value):
    newarray = np.empty((5,5), dtype=str)
    count = 0
    for i in range(0,len(array)):
        if array[i][col] == value:
            newarray[i][0] = array[i][0]
            newarray[i][1] = array[i][1]
            newarray[i][2] = array[i][2]
            newarray[i][3] = array[i][3]
            newarray[i][4] = array[i][4]
        count+=1

    print(newarray)







def getTrainingMatrix(fileName, columnName):

    read = open(fileName)
    global data
    global columns
    columns = read.readline().split(",")
    changeCol = True
    if columns[len(columns)-1][0:len(columnName)]==columnName:
        changeCol = False
    coi = 0
    for i in range(0, len(columns)):
        if columns[i] == columnName:
            coi = i
    hold = read.readlines()
    data = [[""]*(len(columns)) for _ in range(len(hold))]
    for i in range(0, len(data)):

        split = hold[i].split(",")
        for j in range(0, len(split)):
            if changeCol:
                if j == coi:
                    data[i][len(split)-1] = split[coi]
                elif j == len(split)-1:
                    data[i][coi] = split[j]
                else:
                    data[i][j] = split[j]
            else:
                data[i][j] = split[j]


def setTreeMaxDepth(depth):

    global max_depth
    max_depth = len(columns)-1


def setMinimumImpurity(min):

    global min_impurity
    min_impurity = min


class Node:
    def __init__(self, feature_name, feature_value, feature_IG, leaf):
        self.feature_name = feature_name
        self.feature_value = feature_value
        self.feature_IG = feature_IG
        self.children = dict()
        self.leaf = leaf



getTrainingMatrix('TrainingData.txt', 'Play Golf')
setTreeMaxDepth(4)
setMinimumImpurity(0.20)
filter(data, 4, "FALSE")
node1 = Node("Windy", "True", 0.80, False)
