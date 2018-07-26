from os import listdir
from pip._internal import main;
from pip._internal import get_installed_distributions

dist_list = get_installed_distributions()
dist_names = [dist.project_name for dist in dist_list]
#print(dist_names);
main(["install", "pygubu"]);

import os.path;
import sys, glob, codecs, pygubu, xml;
import xml.etree.ElementTree as ET
from difflib import SequenceMatcher
#Load correct tk
try:
    import tkinter as tk  # for python 3
except:
    import Tkinter as tk  # for python 2
#switch to utf8
sys.stdout = codecs.getwriter("utf-8")(sys.stdout.detach());
#Finds all xml files starting with GTIP
def findGTIPXML(scanDir):
    matchingFiles = [];
    for root, dirs, files in os.walk(scanDir):
        for file in files:
            if file.endswith(".xml") and file.startswith('GTIP'):
                fPath = os.path.join(root, file);
                matchingFiles.append(fPath);
    print(str(len(matchingFiles)));
    return matchingFiles;
#Creates and returns (CODE, DESCRIPTION, DIRECTORY) tuple for all xml GTIP entries under given scanDir.
def generateTuples(scanDir):
    AllFileDirs = (findGTIPXML(scanDir));
    Tuples = [];
    for d in AllFileDirs:
        tCode = "";
        tDescription = "";
        root = ET.parse(d).getroot();
        for GTIP in root.findall('GTIP'):
            for CODE in GTIP.iter('CODE'):
                tCode = CODE;
            for DESCR in GTIP.iter('DESCR'):
                tDescription = DESCR;
            Tuples.append((tCode.text, tDescription.text, d.replace("\\\\","\\")));#CODE, DESCRIPTION, DIRECTORY
    #print(Tuples);
    return Tuples;

#Finds the given given code in the tupleslist
def findEntryByCode(Code, TuplesList):
    return [item for item in TuplesList if item[0] == Code];

#Finds entries by description
def findEntryByDescription(partialDescription, TuplesList, tolerance):
    matchingList = [item for item in TuplesList if SequenceMatcher(None, partialDescription, item[1]).ratio() > tolerance];
    return matchingList;

class Application:
    def __init__(self, master):
        self.tuplesList = [];
        print(os.path.join(os.getcwd(), "gtip.ui"));
        self.builder = builder = pygubu.Builder()
        #builder.add_from_file("""C:\\Users\\goktug.kayacan\\Desktop\py\\test.ui""");
        builder.add_from_file(os.path.join(os.getcwd(), "gtip.ui"));
        def WipeInfoTexts():
            self.builder.get_object('tInfo').delete(1.0, tk.END);
            self.builder.get_object('tInfoDir').delete(1.0, tk.END);
            self.builder.get_object('tInfoCode').delete(1.0, tk.END);

        def ScanXML():
            self.tuplesList = generateTuples(self.builder.get_object('tboxDir').get());
            print("XML Scan Completed " + str(len(self.tuplesList)) + " Entries found.");
            self.builder.get_object('lCode').configure(text = ("XML Scan Completed " + str(len(self.tuplesList)) + " Entries found."));
        #Gets description by code
        def FindDesc():
            WipeInfoTexts();
            codeEntryText = self.builder.get_object('tboxCodeEntry').get();
            entry = findEntryByCode(codeEntryText, self.tuplesList);
            if not entry:
                self.builder.get_object('lCode').configure(text = ("No matching entries"));
            else:
                self.builder.get_object('tInfo').insert(tk.INSERT, entry[0][1]);
                self.builder.get_object('tInfoCode').insert(tk.INSERT, entry[0][0]);
                self.builder.get_object('tInfoDir').insert(tk.INSERT, entry[0][2]);

        def FindCode():
            WipeInfoTexts();
            descEntryText = self.builder.get_object('tboxDescEntry').get();
            entry = findEntryByDescription(descEntryText, self.tuplesList, 0.6);
            print(entry);
            if not entry:
                self.builder.get_object('lCode').configure(text = ("No matching entries"));
            else:
                self.builder.get_object('tInfo').insert(tk.INSERT, entry[0][1]);
                self.builder.get_object('tInfoCode').insert(tk.INSERT, entry[0][0]);
                self.builder.get_object('tInfoDir').insert(tk.INSERT, entry[0][2]);
        #Create the widget using a master as parent
        self.mainwindow = builder.get_object('mainwindow', master)
        #Set callbacks
        callbacks = {'ScanXML': ScanXML, 'FindDesc': FindDesc, 'FindCode': FindCode}
        builder.connect_callbacks(callbacks);

if __name__ == '__main__':
    root = tk.Tk()
    app = Application(root)
    root.mainloop()
