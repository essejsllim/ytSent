import re
import csv

def clean(string):
     string = re.sub(r"[^\x00-\xFFFF]", "", string) #remove non-utf-8 characters
     string = re.sub(r"\+\w*", "USERNAME", string) #cover usernames. Doesn't catch multi-word names
     string = re.sub(r"\d\d?:\d\d", "TIMESTAMP", string) #replace timestamps
     string = re.sub(r'http\S+', 'URL', string) #replace urls
     string = re.sub(r'www\.\S+', 'URL', string) #replace urls
     return(string)

def cleanCSV():
     inFile = open(readFileName, 'r')
     inFileReader = csv.reader(inFile, delimiter=',', quotechar='"', quoting = csv.QUOTE_ALL, skipinitialspace = True)
     outFile = open(writeFileName, 'w')
     outFileWriter = csv.writer(outFile, delimiter='\t', quotechar='"', lineterminator='\n')
     i = 0
     for line in inFileReader:
          isReply = False
          if line[0] == '':
               isReply = True
               comment = clean(line[12])
          else:
               comment = clean(line[4])
          comment = comment.replace("\n","")
          #outFileWriter.writerow([0, comment, isReply])
          if i > 0:
               outFileWriter.writerow([0, comment])
          i+=1
     inFile.close()
     outFile.close()

#allow program to run from command line
if __name__ =="__main__":
    cleanCSV()