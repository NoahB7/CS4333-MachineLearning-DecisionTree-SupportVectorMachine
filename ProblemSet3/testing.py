from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
import pandas as pd
import numpy as np

def convertCategorical(column):
    map_convert = get_mapping(column)
    keys.append(map_convert)
    for i in range(len(data[column])):
        data[column][i] = convert(data[column][i], map_convert)

def convert(value,map):
    return map[value]

def get_mapping(column):
    map_convert = {}
    unique_values = data[column].tolist()
    unique_values = set(unique_values)
    unique_values = list(unique_values)
    count = 0
    for value in unique_values:
        map_convert[value] = count
        count += 1
    return map_convert

global keys
keys = list()
global data
data = pd.read_csv('C:\\Users\\noah_\\PycharmProjects\\MLPS3\\data.txt')

for column in data.columns:
    if(type(column)!= int or type(column) != float):
        convertCategorical(column)

X = data.drop('SURVIVED', axis=1)
y = data.pop('SURVIVED')

X_train, X_test, y_train, y_test = train_test_split(X,y,test_size=0.2, random_state = 50)
X_train = np.array(X_train).astype(int)
y_train = np.array(y_train).astype(int)
X_test = np.array(X_test).astype(int)
y_test = np.array(y_test).astype(int)

clf = RandomForestClassifier(max_depth=4, random_state=50)
clf.fit(X_train, y_train)

predict = clf.predict(X_test)
correct = 0
for i in range(len(predict)):
    print(predict[i] == y_test, " ", y_test[i])
    if(predict[i] == y_test[i]):
        correct += 1

print(correct/len(X_test))